package com.fintech.billetera.dto;

import com.fintech.billetera.domain.enums.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProgramacionRequest {
    private TipoTransaccion tipo;
    private BigDecimal monto;
    private String idBilleteraOrigen;
    private String idBilleteraDestino;
    private String idUsuarioGenerador;
    private LocalDateTime fechaEjecucion;

    public TipoTransaccion getTipo() { return tipo; }
    public void setTipo(TipoTransaccion tipo) { this.tipo = tipo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getIdBilleteraOrigen() { return idBilleteraOrigen; }
    public void setIdBilleteraOrigen(String s) { this.idBilleteraOrigen = s; }
    public String getIdBilleteraDestino() { return idBilleteraDestino; }
    public void setIdBilleteraDestino(String s) { this.idBilleteraDestino = s; }
    public String getIdUsuarioGenerador() { return idUsuarioGenerador; }
    public void setIdUsuarioGenerador(String s) { this.idUsuarioGenerador = s; }
    public LocalDateTime getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(LocalDateTime f) { this.fechaEjecucion = f; }
}
