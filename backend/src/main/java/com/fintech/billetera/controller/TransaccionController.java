package com.fintech.billetera.controller;

import com.fintech.billetera.domain.Transaccion;
import com.fintech.billetera.dto.RecargaRequest;
import com.fintech.billetera.dto.RetiroRequest;
import com.fintech.billetera.dto.TransferenciaRequest;
import com.fintech.billetera.service.ReversionService;
import com.fintech.billetera.service.TransaccionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints para operaciones financieras y consulta de historial.
 * Tambien expone los endpoints de reversion (deshacer).
 */
@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final ReversionService reversionService;

    public TransaccionController(TransaccionService transaccionService,
                                 ReversionService reversionService) {
        this.transaccionService = transaccionService;
        this.reversionService = reversionService;
    }

    @PostMapping("/recarga")
    public Transaccion recargar(@RequestBody RecargaRequest req) {
        return transaccionService.recargar(req.getIdBilletera(), req.getMonto());
    }

    @PostMapping("/retiro")
    public Transaccion retirar(@RequestBody RetiroRequest req) {
        return transaccionService.retirar(req.getIdBilletera(), req.getMonto());
    }

    @PostMapping("/transferencia")
    public Transaccion transferir(@RequestBody TransferenciaRequest req) {
        return transaccionService.transferir(
                req.getIdBilleteraOrigen(),
                req.getIdBilleteraDestino(),
                req.getMonto());
    }

    @GetMapping("/{id}")
    public Transaccion obtener(@PathVariable String id) {
        return transaccionService.obtener(id);
    }

    @GetMapping("/billetera/{idBilletera}")
    public List<Transaccion> historialBilletera(@PathVariable String idBilletera) {
        return transaccionService.historialBilletera(idBilletera);
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Transaccion> historialUsuario(@PathVariable String idUsuario) {
        return transaccionService.historialUsuario(idUsuario);
    }

    // -------- Reversion --------

    @PostMapping("/{id}/reversion")
    public Transaccion revertir(@PathVariable String id) {
        return reversionService.revertir(id);
    }

    @PostMapping("/usuario/{idUsuario}/deshacer")
    public Transaccion deshacerUltima(@PathVariable String idUsuario) {
        return reversionService.deshacerUltima(idUsuario);
    }

    @GetMapping("/usuario/{idUsuario}/reversibles")
    public Map<String, Integer> contarReversibles(@PathVariable String idUsuario) {
        return Map.of("count", reversionService.operacionesReversibles(idUsuario));
    }
}
