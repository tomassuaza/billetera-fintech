package com.fintech.billetera.service;

import com.fintech.billetera.domain.Billetera;
import com.fintech.billetera.domain.Transaccion;
import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.domain.enums.EstadoTransaccion;
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
 */
@Service
public class TransaccionService {

    private final TransaccionRepository repo;
    private final BilleteraRepository billeteraRepo;
    private final UsuarioRepository usuarioRepo;
    private final ReversionRepository reversionRepo;
    private final FidelizacionService fidelizacionService;

    public TransaccionService(TransaccionRepository repo,
                              BilleteraRepository billeteraRepo,
                              UsuarioRepository usuarioRepo,
                              ReversionRepository reversionRepo,
                              FidelizacionService fidelizacionService) {
        this.repo = repo;
        this.billeteraRepo = billeteraRepo;
        this.usuarioRepo = usuarioRepo;
        this.reversionRepo = reversionRepo;
        this.fidelizacionService = fidelizacionService;
    }

    // -------------------- RECARGA --------------------

    /**
     * Recarga saldo a una billetera. El usuario duenho de la billetera
     * recibe los puntos correspondientes.
     */
    public Transaccion recargar(String idBilletera, BigDecimal monto) {
        Billetera b = obtenerBilleteraActiva(idBilletera);
        validarMontoPositivo(monto);

        b.acreditar(monto);
        billeteraRepo.guardar(b);

        Transaccion t = new Transaccion(generarId("TRX"),
                TipoTransaccion.RECARGA, monto, null, idBilletera, b.getIdUsuario());
        return finalizarYRegistrar(t, b.getIdUsuario());
    }

    // -------------------- RETIRO --------------------

    /**
     * Retira saldo de una billetera. Falla si el saldo es insuficiente.
     */
    public Transaccion retirar(String idBilletera, BigDecimal monto) {
        Billetera b = obtenerBilleteraActiva(idBilletera);
        validarMontoPositivo(monto);

        if (!b.debitar(monto)) {
            throw new RuntimeException("Saldo insuficiente en billetera " + idBilletera);
        }
        billeteraRepo.guardar(b);

        Transaccion t = new Transaccion(generarId("TRX"),
                TipoTransaccion.RETIRO, monto, idBilletera, null, b.getIdUsuario());
        return finalizarYRegistrar(t, b.getIdUsuario());
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
        validarMontoPositivo(monto);

        if (!origen.debitar(monto)) {
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
        return finalizarYRegistrar(t, origen.getIdUsuario());
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
            throw new RuntimeException("La billetera " + id + " esta inactiva");
        }
        return b;
    }

    private void validarMontoPositivo(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto debe ser mayor a cero");
        }
    }

    /**
     * Cierra el ciclo de una transaccion: marca como EXITOSA, calcula
     * puntos, actualiza ranking, apila para reversion y guarda.
     */
    private Transaccion finalizarYRegistrar(Transaccion t, String idUsuario) {
        int puntos = PoliticaPuntos.calcular(t.getTipo(), t.getMonto());
        t.setPuntosGenerados(puntos);
        t.setEstado(EstadoTransaccion.EXITOSA);

        Usuario u = usuarioRepo.buscarPorId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));
        int puntosAntes = u.getPuntos();
        u.sumarPuntos(puntos);
        usuarioRepo.guardar(u);
        fidelizacionService.actualizarRanking(u.getId(), puntosAntes, u.getPuntos());

        repo.guardar(t);
        reversionRepo.apilar(idUsuario, t);
        return t;
    }

    private String generarId(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
