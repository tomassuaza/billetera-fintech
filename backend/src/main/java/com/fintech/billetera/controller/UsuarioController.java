package com.fintech.billetera.controller;

import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.dto.UsuarioRequest;
import com.fintech.billetera.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<Usuario> registrar(@RequestBody UsuarioRequest req) {
        return ResponseEntity.ok(servicio.registrar(req.getNombre(), req.getCorreo()));
    }

    @GetMapping
    public Collection<Usuario> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
