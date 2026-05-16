package com.fintech.billetera.domain;

import com.fintech.billetera.domain.enums.EstadoTransaccion;
import com.fintech.billetera.domain.enums.NivelRiesgo;
import com.fintech.billetera.domain.enums.TipoTransaccion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa un movimiento financiero del sistema.
 *
 * Toda transaccion tiene origen y/o destino:
 * - RECARGA: solo destino (la billetera que recibe)
 * - RETIRO: solo origen (la billetera que pierde saldo)
 * - TRANSFERENCIA: ambos
 * - REVERSION: invierte una transaccion previa
 *
 * El campo idUsuarioGenerador identifica al usuario que ejecuto la
 * operacion. Se usa para asignarle puntos y para llevar su pila de
 * operaciones reversibles.
 */
public class Transaccion {
    private String id;
    private TipoTransaccion tipo;
    private BigDecimal monto;
    private String idBilleteraOrigen;
    private String idBilleteraDestino;
    private String idUsuarioGenerador;
    private LocalDateTime fecha;
    private EstadoTransaccion estado;
    private int puntosGenerados;
    private boolean reversible;
    private String motivoRiesgo;
    private NivelRiesgo nivelRiesgo = NivelRiesgo.NINGUNO;

    public Transaccion() {}

    public Transaccion(String id, TipoTransaccion tipo, BigDecimal monto,
                       String origen, String destino, String idUsuarioGenerador) {
        this.id = id;
        this.tipo = tipo;
        this.monto = monto;
        this.idBilleteraOrigen = origen;
        this.idBilleteraDestino = destino;
        this.idUsuarioGenerador = idUsuarioGenerador;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoTransaccion.PENDIENTE;
        this.reversible = true;
        this.nivelRiesgo = NivelRiesgo.NINGUNO;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime f) { this.fecha = f; }
    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion e) { this.estado = e; }
    public int getPuntosGenerados() { return puntosGenerados; }
    public void setPuntosGenerados(int p) { this.puntosGenerados = p; }
    public boolean isReversible() { return reversible; }
    public void setReversible(boolean r) { this.reversible = r; }
    public String getMotivoRiesgo() { return motivoRiesgo; }
    public void setMotivoRiesgo(String m) { this.motivoRiesgo = m; }
    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(NivelRiesgo n) { this.nivelRiesgo = n; }
}
