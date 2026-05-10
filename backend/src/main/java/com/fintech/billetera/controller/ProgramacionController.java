package com.fintech.billetera.controller;

import com.fintech.billetera.domain.OperacionProgramada;
import com.fintech.billetera.dto.ProgramacionRequest;
import com.fintech.billetera.service.ProgramacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programadas")
public class ProgramacionController {

    private final ProgramacionService servicio;

    public ProgramacionController(ProgramacionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public OperacionProgramada programar(@RequestBody ProgramacionRequest req) {
        return servicio.programar(
                req.getTipo(),
                req.getMonto(),
                req.getIdBilleteraOrigen(),
                req.getIdBilleteraDestino(),
                req.getIdUsuarioGenerador(),
                req.getFechaEjecucion());
    }

    @GetMapping
    public List<OperacionProgramada> listarPendientes() {
        return servicio.listarPendientes();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<OperacionProgramada> listarPorUsuario(@PathVariable String idUsuario) {
        return servicio.listarPorUsuario(idUsuario);
    }

    @PostMapping("/ejecutar-vencidas")
    public List<OperacionProgramada> ejecutarVencidas() {
        return servicio.ejecutarPendientes();
    }

    @PostMapping("/{id}/ejecutar")
    public OperacionProgramada ejecutar(@PathVariable String id) {
        return servicio.ejecutarPorId(id);
    }

    @PostMapping("/{id}/cancelar")
    public Boolean cancelar(@PathVariable String id) {
        return servicio.cancelar(id);
    }
}
