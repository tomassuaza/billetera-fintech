package com.fintech.billetera.service;

import com.fintech.billetera.domain.Billetera;
import com.fintech.billetera.domain.Transaccion;
import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.domain.enums.EstadoTransaccion;
import com.fintech.billetera.domain.enums.NivelUsuario;
import com.fintech.billetera.domain.enums.TipoTransaccion;
import com.fintech.billetera.repository.BilleteraRepository;
import com.fintech.billetera.repository.ReversionRepository;
import com.fintech.billetera.repository.TransaccionRepository;
import com.fintech.billetera.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Servicio central de operaciones financieras del sistema.
 *
 * Cada operacion (recargar, retirar, transferir):
 *  1. Valida los datos
 *  2. Modifica los saldos de las billeteras involucradas
 *  3. Crea el registro de Transaccion (estado EXITOSA)
 *  4. Calcula y suma puntos al usuario generador
 *  5. Actualiza el ranking del usuario en el TreeMap
 *  6. Apila la transaccion en la pila de reversion del usuario
 *  7. Indexa la transaccion en los historiales (LinkedList)
 *  8. Emite notificaciones derivadas (saldo bajo, ascenso de nivel)
 */
@Service
public class TransaccionService {

    private final TransaccionRepository repo;
    private final BilleteraRepository billeteraRepo;
    private final UsuarioRepository usuarioRepo;
    private final ReversionRepository reversionRepo;
    private final FidelizacionService fidelizacionService;
    private final NotificacionService notificacionService;
    private final GrafoService grafoService;
    private final FraudeService fraudeService;

    public TransaccionService(TransaccionRepository repo,
                              BilleteraRepository billeteraRepo,
                              UsuarioRepository usuarioRepo,
                              ReversionRepository reversionRepo,
                              FidelizacionService fidelizacionService,
                              NotificacionService notificacionService,
                              GrafoService grafoService,
                              FraudeService fraudeService) {
        this.repo = repo;
        this.billeteraRepo = billeteraRepo;
        this.usuarioRepo = usuarioRepo;
        this.reversionRepo = reversionRepo;
        this.fidelizacionService = fidelizacionService;
        this.notificacionService = notificacionService;
        this.grafoService = grafoService;
        this.fraudeService = fraudeService;
    }

    // -------------------- RECARGA --------------------

    /**
     * Recarga saldo a una billetera. El usuario duenho de la billetera
     * recibe los puntos correspondientes.
     */
    public Transaccion recargar(String idBilletera, BigDecimal monto) {
        Billetera b = obtenerBilleteraActiva(idBilletera);
        validarMontoPositivo(b.getIdUsuario(), monto, idBilletera);

        b.acreditar(monto);
        billeteraRepo.guardar(b);

        Transaccion t = new Transaccion(generarId("TRX"),
                TipoTransaccion.RECARGA, monto, null, idBilletera, b.getIdUsuario());
        return finalizarYRegistrar(t, b.getIdUsuario(), b);
    }

    // -------------------- RETIRO --------------------

    /**
     * Retira saldo de una billetera. Falla si el saldo es insuficiente.
     */
    public Transaccion retirar(String idBilletera, BigDecimal monto) {
        Billetera b = obtenerBilleteraActiva(idBilletera);
        validarMontoPositivo(b.getIdUsuario(), monto, idBilletera);

        if (!b.debitar(monto)) {
            notificacionService.emitirOperacionRechazada(b.getIdUsuario(),
                    "Saldo insuficiente en billetera " + b.getNombre(), idBilletera);
            throw new RuntimeException("Saldo insuficiente en billetera " + idBilletera);
        }
        billeteraRepo.guardar(b);

        Transaccion t = new Transaccion(generarId("TRX"),
                TipoTransaccion.RETIRO, monto, idBilletera, null, b.getIdUsuario());
        return finalizarYRegistrar(t, b.getIdUsuario(), b);
    }

    // -------------------- TRANSFERENCIA --------------------

    /**
     * Transfiere saldo entre billeteras. Determina automaticamente si es
     * interna (mismo usuario) o externa (entre usuarios distintos).
     */
    public Transaccion transferir(String idOrigen, String idDestino, BigDecimal monto) {
        if (idOrigen.equals(idDestino)) {
            throw new RuntimeException("La billetera origen y destino no pueden ser la misma");
        }
        Billetera origen = obtenerBilleteraActiva(idOrigen);
        Billetera destino = obtenerBilleteraActiva(idDestino);
        validarMontoPositivo(origen.getIdUsuario(), monto, idOrigen);

        if (!origen.debitar(monto)) {
            notificacionService.emitirOperacionRechazada(origen.getIdUsuario(),
                    "Saldo insuficiente para transferir desde " + origen.getNombre(),
                    idOrigen);
            throw new RuntimeException("Saldo insuficiente en billetera " + idOrigen);
        }
        destino.acreditar(monto);
        billeteraRepo.guardar(origen);
        billeteraRepo.guardar(destino);

        TipoTransaccion tipo = origen.getIdUsuario().equals(destino.getIdUsuario())
                ? TipoTransaccion.TRANSFERENCIA_INTERNA
                : TipoTransaccion.TRANSFERENCIA_EXTERNA;

        Transaccion t = new Transaccion(generarId("TRX"), tipo, monto,
                idOrigen, idDestino, origen.getIdUsuario());

        // Solo las transferencias entre usuarios distintos viven en el
        // grafo. Las internas mueven dinero dentro de la misma persona.
        if (tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA) {
            grafoService.registrarTransferencia(
                    origen.getIdUsuario(), destino.getIdUsuario(), monto);
        }
        return finalizarYRegistrar(t, origen.getIdUsuario(), origen);
    }

    // -------------------- CONSULTAS --------------------

    public Transaccion obtener(String id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Transaccion no encontrada: " + id));
    }

    public List<Transaccion> historialBilletera(String idBilletera) {
        return repo.historialBilletera(idBilletera);
    }

    public List<Transaccion> historialUsuario(String idUsuario) {
        return repo.historialUsuario(idUsuario);
    }

    // -------------------- HELPERS PRIVADOS --------------------

    private Billetera obtenerBilleteraActiva(String id) {
        Billetera b = billeteraRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Billetera no encontrada: " + id));
        if (!b.isActiva()) {
            notificacionService.emitirOperacionRechazada(b.getIdUsuario(),
                    "Billetera inactiva: " + b.getNombre(), id);
            throw new RuntimeException("La billetera " + id + " esta inactiva");
        }
        return b;
    }

    /**
     * Verifica que el monto sea positivo. Si no, emite notificacion de
     * rechazo dirigida al usuario duenho de la billetera involucrada.
     */
    private void validarMontoPositivo(String idUsuario, BigDecimal monto,
                                      String idReferencia) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            notificacionService.emitirOperacionRechazada(idUsuario,
                    "Monto invalido (debe ser mayor a cero)", idReferencia);
            throw new RuntimeException("El monto debe ser mayor a cero");
        }
    }

    /**
     * Cierra el ciclo de una transaccion: marca como EXITOSA, calcula
     * puntos, actualiza ranking, apila para reversion, guarda y emite
     * las notificaciones derivadas (saldo bajo del origen, ascenso de
     * nivel del usuario).
     */
    private Transaccion finalizarYRegistrar(Transaccion t, String idUsuario,
                                            Billetera billeteraImpactada) {
        int puntos = PoliticaPuntos.calcular(t.getTipo(), t.getMonto());
        t.setPuntosGenerados(puntos);
        t.setEstado(EstadoTransaccion.EXITOSA);

        Usuario u = usuarioRepo.buscarPorId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));
        int puntosAntes = u.getPuntos();
        NivelUsuario nivelAntes = u.getNivel();
        u.sumarPuntos(puntos);
        usuarioRepo.guardar(u);
        fidelizacionService.actualizarRanking(u.getId(), puntosAntes, u.getPuntos());

        repo.guardar(t);
        reversionRepo.apilar(idUsuario, t);

        // Notificaciones derivadas
        if (nivelAntes != u.getNivel()) {
            notificacionService.emitirAscensoNivel(idUsuario, nivelAntes, u.getNivel());
        }
        // El saldo bajo solo aplica cuando el saldo de la billetera
        // disminuye (retiros y transferencias salientes). En recargas no
        // tiene sentido alertar.
        if (t.getTipo() != TipoTransaccion.RECARGA
                && billeteraImpactada.getSaldo()
                        .compareTo(NotificacionService.UMBRAL_SALDO_BAJO) < 0) {
            notificacionService.emitirSaldoBajo(idUsuario,
                    billeteraImpactada.getId(),
                    billeteraImpactada.getNombre(),
                    billeteraImpactada.getSaldo());
        }

        // Analisis de patrones inusuales. Se hace al final, despues de
        // que la transaccion ya esta registrada y visible en el historial,
        // para que las reglas puedan compararse contra el conjunto
        // completo de operaciones recientes del usuario.
        fraudeService.analizar(t);
        return t;
    }

    private String generarId(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
