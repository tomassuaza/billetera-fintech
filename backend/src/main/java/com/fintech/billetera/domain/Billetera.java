package com.fintech.billetera.domain;

import com.fintech.billetera.domain.enums.TipoBilletera;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Billetera digital de un usuario. Maneja saldo en BigDecimal para
 * evitar errores de redondeo propios de double/float en operaciones
 * financieras.
 */
public class Billetera {
    private String id;
    private String idUsuario;
    private String nombre;
    private TipoBilletera tipo;
    private BigDecimal saldo;
    private boolean activa;
    private LocalDateTime fechaCreacion;

    public Billetera() {}

    public Billetera(String id, String idUsuario, String nombre, TipoBilletera tipo) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.tipo = tipo;
        this.saldo = BigDecimal.ZERO;
        this.activa = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    /** Suma al saldo. Usado en recargas y transferencias entrantes. */
    public void acreditar(BigDecimal monto) {
        this.saldo = this.saldo.add(monto);
    }

    /**
     * Resta del saldo si hay fondos suficientes.
     * @return true si pudo debitar, false si no hay saldo
     */
    public boolean debitar(BigDecimal monto) {
        if (saldo.compareTo(monto) < 0) return false;
        this.saldo = this.saldo.subtract(monto);
        return true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoBilletera getTipo() { return tipo; }
    public void setTipo(TipoBilletera tipo) { this.tipo = tipo; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }
}
