package com.fintech.billetera.controller;

import com.fintech.billetera.domain.Notificacion;
import com.fintech.billetera.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService servicio;

    public NotificacionController(NotificacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Notificacion> listarPorUsuario(@PathVariable String idUsuario) {
        return servicio.listarPorUsuario(idUsuario);
    }

    @GetMapping("/usuario/{idUsuario}/pendientes")
    public Map<String, Integer> pendientes(@PathVariable String idUsuario) {
        return Map.of("pendientes", servicio.pendientes(idUsuario));
    }

    @PostMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable String id) {
        return servicio.marcarLeida(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/usuario/{idUsuario}/leer-todas")
    public Map<String, Integer> marcarTodasLeidas(@PathVariable String idUsuario) {
        return Map.of("marcadas", servicio.marcarTodasLeidas(idUsuario));
    }

    @PostMapping("/usuario/{idUsuario}/despachar")
    public List<Notificacion> despachar(@PathVariable String idUsuario) {
        return servicio.despachar(idUsuario);
    }

    @PostMapping("/usuario/{idUsuario}/sacar-siguiente")
    public ResponseEntity<Notificacion> sacarSiguiente(@PathVariable String idUsuario) {
        return servicio.sacarSiguiente(idUsuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
