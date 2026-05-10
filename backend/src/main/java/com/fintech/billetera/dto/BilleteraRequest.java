package com.fintech.billetera.dto;

import com.fintech.billetera.domain.enums.TipoBilletera;

public class BilleteraRequest {
    private String idUsuario;
    private String nombre;
    private TipoBilletera tipo;

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoBilletera getTipo() { return tipo; }
    public void setTipo(TipoBilletera tipo) { this.tipo = tipo; }
}
