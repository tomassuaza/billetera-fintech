package com.fintech.billetera.controller;

import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.domain.enums.NivelUsuario;
import com.fintech.billetera.service.FidelizacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fidelizacion")
public class FidelizacionController {

    private final FidelizacionService servicio;

    public FidelizacionController(FidelizacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/top/{n}")
    public List<Usuario> topN(@PathVariable int n) {
        return servicio.topN(n);
    }

    @GetMapping("/rango")
    public List<Usuario> usuariosEnRango(@RequestParam int min, @RequestParam int max) {
        return servicio.usuariosEnRango(min, max);
    }

    @GetMapping("/nivel/{nivel}")
    public List<Usuario> usuariosPorNivel(@PathVariable NivelUsuario nivel) {
        return servicio.usuariosPorNivel(nivel);
    }

    @GetMapping("/conteo")
    public Map<NivelUsuario, Integer> conteoPorNivel() {
        return servicio.conteoPorNivel();
    }
}
