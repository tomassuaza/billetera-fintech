package com.fintech.billetera.service;

import com.fintech.billetera.domain.Notificacion;
import com.fintech.billetera.domain.enums.NivelUsuario;
import com.fintech.billetera.domain.enums.TipoNotificacion;
import com.fintech.billetera.repository.NotificacionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade del modulo de notificaciones. Los servicios de negocio
 * (TransaccionService, ProgramacionService, UsuarioService, etc.)
 * llaman a los metodos emitir* cuando ocurre un evento relevante; ellos
 * NO saben nada de la estructura interna (Cola FIFO).
 *
 * Asi el dia que cambiemos la implementacion de la cola (por ejemplo a
 * un broker real tipo Kafka) solo cambia este servicio.
 */
@Service
public class NotificacionService {

    /**
     * Umbral por debajo del cual una billetera se considera con saldo
     * bajo y dispara la notificacion correspondiente.
     */
    public static final BigDecimal UMBRAL_SALDO_BAJO = new BigDecimal("10000");

    private final NotificacionRepository repo;

    public NotificacionService(NotificacionRepository repo) {
        this.repo = repo;
    }

    // -------------------- EMISORES --------------------

    public Notificacion emitirBienvenida(String idUsuario, String nombre) {
        return emitir(idUsuario, TipoNotificacion.BIENVENIDA,
                "Bienvenido " + nombre + ", tu cuenta esta lista para usar.",
                null);
    }

    public Notificacion emitirSaldoBajo(String idUsuario, String idBilletera,
                                        String nombreBilletera, BigDecimal saldo) {
        return emitir(idUsuario, TipoNotificacion.SALDO_BAJO,
                "Saldo bajo en " + nombreBilletera + ": $" + saldo
                        + " (umbral: $" + UMBRAL_SALDO_BAJO + ")",
                idBilletera);
    }

    public Notificacion emitirAscensoNivel(String idUsuario, NivelUsuario anterior,
                                           NivelUsuario nuevo) {
        return emitir(idUsuario, TipoNotificacion.ASCENSO_NIVEL,
                "Subiste de nivel: " + anterior + " a " + nuevo + ".",
                null);
    }

    public Notificacion emitirOperacionRechazada(String idUsuario, String motivo,
                                                 String idReferencia) {
        return emitir(idUsuario, TipoNotificacion.OPERACION_RECHAZADA,
                "Operacion rechazada: " + motivo,
                idReferencia);
    }

    public Notificacion emitirProgramadaEjecutada(String idUsuario, String idProgramada,
                                                  String idTransaccion) {
        return emitir(idUsuario, TipoNotificacion.PROGRAMADA_EJECUTADA,
                "Tu operacion programada se ejecuto exitosamente.",
                idTransaccion != null ? idTransaccion : idProgramada);
    }

    public Notificacion emitirProgramadaFallida(String idUsuario, String idProgramada,
                                                String motivo) {
        return emitir(idUsuario, TipoNotificacion.PROGRAMADA_FALLIDA,
                "Tu operacion programada fallo: " + motivo,
                idProgramada);
    }

    public Notificacion emitirFraudeDetectado(String idUsuario, String idTransaccion,
                                              String motivo) {
        return emitir(idUsuario, TipoNotificacion.FRAUDE_DETECTADO,
                "Actividad inusual detectada: " + motivo,
                idTransaccion);
    }

    // -------------------- CONSULTAS Y COMANDOS --------------------

    public List<Notificacion> listarPorUsuario(String idUsuario) {
        return repo.listarPorUsuario(idUsuario);
    }

    public int pendientes(String idUsuario) {
        return repo.pendientes(idUsuario);
    }

    public Optional<Notificacion> marcarLeida(String idNotificacion) {
        return repo.marcarLeida(idNotificacion);
    }

    public int marcarTodasLeidas(String idUsuario) {
        return repo.marcarTodasLeidas(idUsuario);
    }

    /**
     * Despacha (drena) las pendientes del usuario: las marca como leidas
     * y las devuelve en orden FIFO. Equivale a un poll repetido pero
     * conservando las notificaciones en el historial.
     */
    public List<Notificacion> despachar(String idUsuario) {
        return repo.drenarPendientes(idUsuario);
    }

    /** Sacar la siguiente y removerla por completo (poll real). */
    public Optional<Notificacion> sacarSiguiente(String idUsuario) {
        return repo.desencolarSiguiente(idUsuario);
    }

    // -------------------- INTERNO --------------------

    private Notificacion emitir(String idUsuario, TipoNotificacion tipo,
                                String mensaje, String idReferencia) {
        String id = "NTF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Notificacion n = new Notificacion(id, idUsuario, tipo, mensaje, idReferencia);
        return repo.encolar(n);
    }
}
