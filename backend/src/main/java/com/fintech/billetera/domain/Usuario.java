package com.fintech.billetera.domain;

import com.fintech.billetera.domain.enums.NivelUsuario;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Usuario de la plataforma. Cada usuario puede poseer multiples billeteras
 * y acumula puntos por cada operacion que realiza.
 *
 * Los puntos determinan automaticamente el nivel del usuario
 * (ver NivelUsuario para los rangos).
 */
public class Usuario {
    private String id;
    private String nombre;
    private String correo;
    private int puntos;
    private NivelUsuario nivel;
    private LocalDateTime fechaRegistro;
    private List<String> idsBilleteras = new ArrayList<>();

    public Usuario() {}

    public Usuario(String id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.puntos = 0;
        this.nivel = NivelUsuario.BRONCE;
        this.fechaRegistro = LocalDateTime.now();
    }

    /** Suma puntos y recalcula automaticamente el nivel. */
    public void sumarPuntos(int p) {
        this.puntos += p;
        this.nivel = NivelUsuario.calcular(this.puntos);
    }

    /** Resta puntos (no permite valores negativos). Recalcula nivel. */
    public void restarPuntos(int p) {
        this.puntos = Math.max(0, this.puntos - p);
        this.nivel = NivelUsuario.calcular(this.puntos);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    public NivelUsuario getNivel() { return nivel; }
    public void setNivel(NivelUsuario nivel) { this.nivel = nivel; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }
    public List<String> getIdsBilleteras() { return idsBilleteras; }
    public void setIdsBilleteras(List<String> ids) { this.idsBilleteras = ids; }
}
