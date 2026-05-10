package com.fintech.billetera.controller;

import com.fintech.billetera.domain.Billetera;
import com.fintech.billetera.dto.BilleteraRequest;
import com.fintech.billetera.service.BilleteraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/billeteras")
public class BilleteraController {

    private final BilleteraService servicio;

    public BilleteraController(BilleteraService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<Billetera> crear(@RequestBody BilleteraRequest req) {
        return ResponseEntity.ok(servicio.crear(
                req.getIdUsuario(), req.getNombre(), req.getTipo()));
    }

    @GetMapping
    public Collection<Billetera> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Billetera obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Billetera> listarPorUsuario(@PathVariable String idUsuario) {
        return servicio.listarPorUsuario(idUsuario);
    }

    @PostMapping("/{id}/desactivar")
    public Billetera desactivar(@PathVariable String id) {
        return servicio.desactivar(id);
    }

    @PostMapping("/{id}/activar")
    public Billetera activar(@PathVariable String id) {
        return servicio.activar(id);
    }
}
