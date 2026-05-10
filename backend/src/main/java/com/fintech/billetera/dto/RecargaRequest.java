package com.fintech.billetera.dto;

import java.math.BigDecimal;

public class RecargaRequest {
    private String idBilletera;
    private BigDecimal monto;

    public String getIdBilletera() { return idBilletera; }
    public void setIdBilletera(String s) { this.idBilletera = s; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal m) { this.monto = m; }
}
