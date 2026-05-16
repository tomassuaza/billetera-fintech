package com.fintech.billetera.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Arista del grafo dirigido y ponderado de transferencias entre usuarios.
 *
 * Cada arista resume todas las transferencias externas que ha enviado el
 * usuario origen al usuario destino. El peso acumula el monto total y
 * el conteo cuantas transferencias hubo, de modo que el grafo refleja
 * la intensidad de la relacion financiera entre dos usuarios sin
 * almacenar una arista por transaccion individual.
 */
public class Arista {

    private String idOrigen;
    private String idDestino;
    private BigDecimal pesoTotal;
    private int conteo;
    private LocalDateTime primera;
    private LocalDateTime ultima;

    public Arista() {}

    public Arista(String idOrigen, String idDestino, BigDecimal montoInicial) {
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.pesoTotal = montoInicial;
        this.conteo = 1;
        this.primera = LocalDateTime.now();
        this.ultima = this.primera;
    }

    /** Suma una nueva transferencia a la arista existente. */
    public void acumular(BigDecimal monto) {
        this.pesoTotal = this.pesoTotal.add(monto);
        this.conteo++;
        this.ultima = LocalDateTime.now();
    }

    /**
     * Resta una transferencia (usado al revertir). Devuelve true si la
     * arista queda con conteo > 0; false si quedo en 0 y debe eliminarse.
     */
    public boolean restar(BigDecimal monto) {
        this.pesoTotal = this.pesoTotal.subtract(monto);
        this.conteo--;
        if (this.pesoTotal.signum() < 0) {
            this.pesoTotal = BigDecimal.ZERO;
        }
        return this.conteo > 0;
    }

    public String getIdOrigen() { return idOrigen; }
    public void setIdOrigen(String idOrigen) { this.idOrigen = idOrigen; }
    public String getIdDestino() { return idDestino; }
    public void setIdDestino(String idDestino) { this.idDestino = idDestino; }
    public BigDecimal getPesoTotal() { return pesoTotal; }
    public void setPesoTotal(BigDecimal pesoTotal) { this.pesoTotal = pesoTotal; }
    public int getConteo() { return conteo; }
    public void setConteo(int conteo) { this.conteo = conteo; }
    public LocalDateTime getPrimera() { return primera; }
    public void setPrimera(LocalDateTime primera) { this.primera = primera; }
    public LocalDateTime getUltima() { return ultima; }
    public void setUltima(LocalDateTime ultima) { this.ultima = ultima; }
}
