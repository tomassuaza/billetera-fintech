package com.fintech.billetera.domain;

import com.fintech.billetera.domain.enums.TipoNotificacion;
import java.time.LocalDateTime;

/**
 * Mensaje generado por el sistema y dirigido a un usuario. Vive en la
 * cola FIFO de su buzon hasta ser consumido (marcado como leido).
 *
 * El campo idReferencia es opcional y apunta al recurso que origino la
 * notificacion (id de transaccion, id de billetera, id de programada),
 * para que el frontend pueda navegar directo si lo necesita.
 */
public class Notificacion {
    private String id;
    private String idUsuario;
    private TipoNotificacion tipo;
    private String mensaje;
    private String idReferencia;
    private LocalDateTime fecha;
    private boolean leida;

    public Notificacion() {}

    public Notificacion(String id, String idUsuario, TipoNotificacion tipo,
                        String mensaje, String idReferencia) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.idReferencia = idReferencia;
        this.fecha = LocalDateTime.now();
        this.leida = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getIdReferencia() { return idReferencia; }
    public void setIdReferencia(String idReferencia) { this.idReferencia = idReferencia; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}
