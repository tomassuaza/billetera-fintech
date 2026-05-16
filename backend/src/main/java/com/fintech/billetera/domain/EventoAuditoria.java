package com.fintech.billetera.domain;

import com.fintech.billetera.domain.enums.NivelRiesgo;
import java.time.LocalDateTime;

/**
 * Registro inmutable de un evento sospechoso detectado por el modulo
 * de fraude. Vive en el historial de auditoria y referencia la
 * transaccion que disparo la alerta para consultas posteriores.
 */
public class EventoAuditoria {
    private String id;
    private String idTransaccion;
    private String idUsuario;
    private String regla;
    private NivelRiesgo nivel;
    private String detalle;
    private LocalDateTime fecha;

    public EventoAuditoria() {}

    public EventoAuditoria(String id, String idTransaccion, String idUsuario,
                           String regla, NivelRiesgo nivel, String detalle) {
        this.id = id;
        this.idTransaccion = idTransaccion;
        this.idUsuario = idUsuario;
        this.regla = regla;
        this.nivel = nivel;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getRegla() { return regla; }
    public void setRegla(String regla) { this.regla = regla; }
    public NivelRiesgo getNivel() { return nivel; }
    public void setNivel(NivelRiesgo nivel) { this.nivel = nivel; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
