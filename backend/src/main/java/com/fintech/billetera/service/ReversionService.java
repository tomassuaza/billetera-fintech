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

import java.util.UUID;

/**
 * Maneja la reversion de operaciones, usando la pila de cada usuario.
 *
 * Cuando se revierte una transaccion:
 *  1. Se invierte el efecto sobre los saldos de las billeteras
 *  2. Se restan al usuario los puntos que habia ganado por la transaccion
 *  3. Se actualiza el ranking del usuario (TreeMap)
 *  4. Se marca la transaccion original como REVERTIDA
 *  5. Se crea una NUEVA transaccion de tipo REVERSION (no reversible)
 *     que queda registrada en el historial para auditoria
 */
@Service
public class ReversionService {

    private final ReversionRepository pilaRepo;
    private final TransaccionRepository transRepo;
    private final BilleteraRepository billeteraRepo;
    private final UsuarioRepository usuarioRepo;
    private final FidelizacionService fidelizacionService;

    public ReversionService(ReversionRepository pilaRepo,
                            TransaccionRepository transRepo,
                            BilleteraRepository billeteraRepo,
                            UsuarioRepository usuarioRepo,
                            FidelizacionService fidelizacionService) {
        this.pilaRepo = pilaRepo;
        this.transRepo = transRepo;
        this.billeteraRepo = billeteraRepo;
        this.usuarioRepo = usuarioRepo;
        this.fidelizacionService = fidelizacionService;
    }

    /**
     * Deshace la ultima operacion del usuario (la que esta en el tope de su pila).
     * Si la transaccion en el tope ya fue revertida por un id especifico,
     * se descarta y se intenta con la siguiente.
     */
    public Transaccion deshacerUltima(String idUsuario) {
        // Saca del tope hasta encontrar una transaccion reversible no revertida
        while (true) {
            Transaccion t = pilaRepo.pop(idUsuario)
                    .orElseThrow(() -> new RuntimeException(
                            "No hay operaciones para revertir del usuario " + idUsuario));
            if (t.isReversible() && t.getEstado() != EstadoTransaccion.REVERTIDA) {
                return revertir(t.getId());
            }
            // Si no es reversible o ya esta revertida, sigue al siguiente
        }
    }

    /**
     * Revierte una transaccion especifica por su ID.
     */
    public Transaccion revertir(String idTransaccion) {
        Transaccion original = transRepo.buscarPorId(idTransaccion)
                .orElseThrow(() -> new RuntimeException("Transaccion no encontrada: " + idTransaccion));

        if (!original.isReversible()) {
            throw new RuntimeException("La transaccion no es reversible");
        }
        if (original.getEstado() == EstadoTransaccion.REVERTIDA) {
            throw new RuntimeException("La transaccion ya fue revertida");
        }

        // 1. Invertir el efecto sobre billeteras segun el tipo
        invertirSaldos(original);

        // 2. Restar los puntos al usuario y actualizar ranking
        Usuario u = usuarioRepo.buscarPorId(original.getIdUsuarioGenerador())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        int puntosAntes = u.getPuntos();
        u.restarPuntos(original.getPuntosGenerados());
        usuarioRepo.guardar(u);
        fidelizacionService.actualizarRanking(u.getId(), puntosAntes, u.getPuntos());

        // 3. Marcar la original como REVERTIDA
        original.setEstado(EstadoTransaccion.REVERTIDA);
        transRepo.guardar(original);

        // 4. Crear transaccion de tipo REVERSION (espejo, no reversible)
        Transaccion reversion = new Transaccion(
                "REV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                TipoTransaccion.REVERSION,
                original.getMonto(),
                original.getIdBilleteraDestino(),  // origen y destino invertidos
                original.getIdBilleteraOrigen(),
                original.getIdUsuarioGenerador()
        );
        reversion.setEstado(EstadoTransaccion.EXITOSA);
        reversion.setReversible(false);
        transRepo.guardar(reversion);

        return reversion;
    }

    /**
     * Aplica el efecto inverso de la transaccion sobre las billeteras.
     */
    private void invertirSaldos(Transaccion t) {
        switch (t.getTipo()) {
            case RECARGA -> {
                Billetera b = billeteraRepo.buscarPorId(t.getIdBilleteraDestino())
                        .orElseThrow(() -> new RuntimeException("Billetera destino no encontrada"));
                if (!b.debitar(t.getMonto())) {
                    throw new RuntimeException(
                            "No se puede revertir: saldo insuficiente en " + b.getId());
                }
                billeteraRepo.guardar(b);
            }
            case RETIRO -> {
                Billetera b = billeteraRepo.buscarPorId(t.getIdBilleteraOrigen())
                        .orElseThrow(() -> new RuntimeException("Billetera origen no encontrada"));
                b.acreditar(t.getMonto());
                billeteraRepo.guardar(b);
            }
            case TRANSFERENCIA_INTERNA, TRANSFERENCIA_EXTERNA -> {
                Billetera origen = billeteraRepo.buscarPorId(t.getIdBilleteraOrigen())
                        .orElseThrow(() -> new RuntimeException("Billetera origen no encontrada"));
                Billetera destino = billeteraRepo.buscarPorId(t.getIdBilleteraDestino())
                        .orElseThrow(() -> new RuntimeException("Billetera destino no encontrada"));
                if (!destino.debitar(t.getMonto())) {
                    throw new RuntimeException(
                            "No se puede revertir: saldo insuficiente en destino");
                }
                origen.acreditar(t.getMonto());
                billeteraRepo.guardar(origen);
                billeteraRepo.guardar(destino);
            }
            default -> throw new RuntimeException(
                    "Tipo de transaccion no reversible: " + t.getTipo());
        }
    }

    /** Cuantas operaciones tiene el usuario disponibles para deshacer. */
    public int operacionesReversibles(String idUsuario) {
        return pilaRepo.tamano(idUsuario);
    }
}
