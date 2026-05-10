package com.fintech.billetera.dto;

import java.math.BigDecimal;

public class TransferenciaRequest {
    private String idBilleteraOrigen;
    private String idBilleteraDestino;
    private BigDecimal monto;

    public String getIdBilleteraOrigen() { return idBilleteraOrigen; }
    public void setIdBilleteraOrigen(String s) { this.idBilleteraOrigen = s; }
    public String getIdBilleteraDestino() { return idBilleteraDestino; }
    public void setIdBilleteraDestino(String s) { this.idBilleteraDestino = s; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal m) { this.monto = m; }
}
