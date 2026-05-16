package com.fintech.billetera.controller;

import com.fintech.billetera.domain.EventoAuditoria;
import com.fintech.billetera.service.FraudeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final FraudeService servicio;

    public AuditoriaController(FraudeService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/eventos")
    public List<EventoAuditoria> todos() {
        return servicio.historial();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<EventoAuditoria> porUsuario(@PathVariable String idUsuario) {
        return servicio.historialDe(idUsuario);
    }

    @GetMapping("/total")
    public Map<String, Integer> total() {
        return Map.of("total", servicio.totalEventos());
    }
}
