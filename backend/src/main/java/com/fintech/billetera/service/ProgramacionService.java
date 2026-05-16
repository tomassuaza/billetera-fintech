package com.fintech.billetera.service;

import com.fintech.billetera.domain.OperacionProgramada;
import com.fintech.billetera.domain.Transaccion;
import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.domain.enums.EstadoProgramada;
import com.fintech.billetera.domain.enums.TipoTransaccion;
import com.fintech.billetera.repository.ProgramacionRepository;
import com.fintech.billetera.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maneja las operaciones programadas (transferencias futuras, recargas
 * automaticas, pagos recurrentes, etc.) usando la PriorityQueue.
 */
@Service
public class ProgramacionService {

    private final ProgramacionRepository repo;
    private final TransaccionService transaccionService;
    private final UsuarioRepository usuarioRepo;
    private final NotificacionService notificacionService;

    public ProgramacionService(ProgramacionRepository repo,
                               TransaccionService transaccionService,
                               UsuarioRepository usuarioRepo,
                               NotificacionService notificacionService) {
        this.repo = repo;
        this.transaccionService = transaccionService;
        this.usuarioRepo = usuarioRepo;
        this.notificacionService = notificacionService;
    }

    /**
     * Programa una operacion para que se ejecute en la fecha indicada.
     */
    public OperacionProgramada programar(TipoTransaccion tipo,
                                         BigDecimal monto,
                                         String idBilleteraOrigen,
                                         String idBilleteraDestino,
                                         String idUsuarioGenerador,
                                         LocalDateTime fechaEjecucion) {
        if (fechaEjecucion == null) {
            throw new RuntimeException("La fecha de ejecucion es obligatoria");
        }
        if (!usuarioRepo.existe(idUsuarioGenerador)) {
            throw new RuntimeException("Usuario no encontrado: " + idUsuarioGenerador);
        }
        String id = "PRG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        OperacionProgramada op = new OperacionProgramada(id, tipo, monto,
                idBilleteraOrigen, idBilleteraDestino, idUsuarioGenerador, fechaEjecucion);
        return repo.programar(op);
    }

    /**
     * Ejecuta TODAS las operaciones cuya fecha ya paso.
     * Devuelve la lista de operaciones procesadas (exitosas o fallidas).
     */
    public List<OperacionProgramada> ejecutarPendientes() {
        List<OperacionProgramada> vencidas = repo.extraerVencidas();
        for (OperacionProgramada op : vencidas) {
            ejecutarUna(op);
        }
        return vencidas;
    }

    /**
     * Ejecuta una sola operacion programada (la que sea, sin importar fecha).
     * Util para forzar ejecucion desde el frontend.
     */
    public OperacionProgramada ejecutarPorId(String id) {
        OperacionProgramada op = repo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Operacion programada no encontrada: " + id));
        repo.cancelar(id);  // sacarla de la cola
        ejecutarUna(op);
        return op;
    }

    private void ejecutarUna(OperacionProgramada op) {
        try {
            Transaccion t = switch (op.getTipo()) {
                case RECARGA -> transaccionService.recargar(
                        op.getIdBilleteraDestino(), op.getMonto());
                case RETIRO -> transaccionService.retirar(
                        op.getIdBilleteraOrigen(), op.getMonto());
                case TRANSFERENCIA_INTERNA, TRANSFERENCIA_EXTERNA ->
                        transaccionService.transferir(
                                op.getIdBilleteraOrigen(),
                                op.getIdBilleteraDestino(),
                                op.getMonto());
                default -> throw new RuntimeException(
                        "Tipo no programable: " + op.getTipo());
            };
            op.setIdTransaccionGenerada(t.getId());
            op.setEstado(EstadoProgramada.EJECUTADA);

            // Bono por ejecucion exitosa de programada
            Usuario u = usuarioRepo.buscarPorId(op.getIdUsuarioGenerador()).orElseThrow();
            u.sumarPuntos(PoliticaPuntos.BONO_PROGRAMADA);
            usuarioRepo.guardar(u);

            notificacionService.emitirProgramadaEjecutada(
                    op.getIdUsuarioGenerador(), op.getId(), t.getId());
        } catch (Exception e) {
            op.setEstado(EstadoProgramada.FALLIDA);
            notificacionService.emitirProgramadaFallida(
                    op.getIdUsuarioGenerador(), op.getId(), e.getMessage());
        }
    }

    public List<OperacionProgramada> listarPendientes() {
        return repo.listarOrdenadasPorFecha();
    }

    /** Lista todas las operaciones de un usuario, incluyendo ya ejecutadas. */
    public List<OperacionProgramada> listarPorUsuario(String idUsuario) {
        List<OperacionProgramada> resultado = new ArrayList<>();
        for (OperacionProgramada op : repo.listarOrdenadasPorFecha()) {
            if (op.getIdUsuarioGenerador().equals(idUsuario)) {
                resultado.add(op);
            }
        }
        return resultado;
    }

    public boolean cancelar(String id) {
        return repo.cancelar(id);
    }
}
