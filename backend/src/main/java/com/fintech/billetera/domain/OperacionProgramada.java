package com.fintech.billetera.domain;

import com.fintech.billetera.domain.enums.EstadoProgramada;
import com.fintech.billetera.domain.enums.TipoTransaccion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Operacion que se ejecutara automaticamente en una fecha futura.
 *
 * Implementa Comparable para que la PriorityQueue las ordene
 * naturalmente por fecha de ejecucion. La que tenga la fecha mas
 * cercana sale primero al hacer poll().
 */
public class OperacionProgramada implements Comparable<OperacionProgramada> {

    private String id;
    private TipoTransaccion tipo;
    private BigDecimal monto;
    private String idBilleteraOrigen;
    private String idBilleteraDestino;
    private String idUsuarioGenerador;
    private LocalDateTime fechaEjecucion;
    private EstadoProgramada estado;
    private String idTransaccionGenerada;  // ID de la transaccion creada al ejecutarse
    private LocalDateTime fechaCreacion;

    public OperacionProgramada() {}

    public OperacionProgramada(String id, TipoTransaccion tipo, BigDecimal monto,
                               String origen, String destino, String idUsuario,
                               LocalDateTime fechaEjecucion) {
        this.id = id;
        this.tipo = tipo;
        this.monto = monto;
        this.idBilleteraOrigen = origen;
        this.idBilleteraDestino = destino;
        this.idUsuarioGenerador = idUsuario;
        this.fechaEjecucion = fechaEjecucion;
        this.estado = EstadoProgramada.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Comparacion por fecha de ejecucion ascendente.
     * La operacion con fecha mas cercana es "menor" -> sale primero del heap.
     */
    @Override
    public int compareTo(OperacionProgramada otra) {
        return this.fechaEjecucion.compareTo(otra.fechaEjecucion);
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
    public LocalDateTime getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(LocalDateTime f) { this.fechaEjecucion = f; }
    public EstadoProgramada getEstado() { return estado; }
    public void setEstado(EstadoProgramada e) { this.estado = e; }
    public String getIdTransaccionGenerada() { return idTransaccionGenerada; }
    public void setIdTransaccionGenerada(String s) { this.idTransaccionGenerada = s; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }
}
