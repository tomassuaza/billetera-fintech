package com.fintech.billetera.controller;

import com.fintech.billetera.domain.Transaccion;
import com.fintech.billetera.domain.enums.TipoBilletera;
import com.fintech.billetera.domain.enums.TipoTransaccion;
import com.fintech.billetera.service.AnaliticaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analitica")
public class AnaliticaController {

    private final AnaliticaService servicio;

    public AnaliticaController(AnaliticaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/usuarios-activos")
    public List<AnaliticaService.EntradaConteo> usuariosActivos(
            @RequestParam(defaultValue = "5") int top) {
        return servicio.topUsuariosActivos(top);
    }

    @GetMapping("/billeteras-activas")
    public List<AnaliticaService.EntradaConteo> billeterasActivas(
            @RequestParam(defaultValue = "5") int top) {
        return servicio.topBilleterasActivas(top);
    }

    @GetMapping("/frecuencia-por-tipo")
    public Map<TipoTransaccion, Integer> frecuenciaPorTipo() {
        return servicio.frecuenciaPorTipo();
    }

    @GetMapping("/categorias-billetera")
    public Map<TipoBilletera, Integer> conteoPorCategoria() {
        return servicio.conteoPorCategoriaBilletera();
    }

    @GetMapping("/monto-rango")
    public Map<String, Object> montoEnRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return servicio.montoMovilizadoEnRango(desde, hasta);
    }

    @GetMapping("/top-valor")
    public List<Transaccion> topPorValor(@RequestParam(defaultValue = "5") int top) {
        return servicio.topTransaccionesPorValor(top);
    }
}
