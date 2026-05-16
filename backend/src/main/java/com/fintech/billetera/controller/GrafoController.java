package com.fintech.billetera.controller;

import com.fintech.billetera.domain.Arista;
import com.fintech.billetera.service.GrafoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/grafo")
public class GrafoController {

    private final GrafoService servicio;

    public GrafoController(GrafoService servicio) {
        this.servicio = servicio;
    }

    /** Todas las aristas del grafo (resumen origen->destino con peso). */
    @GetMapping("/aristas")
    public List<Arista> aristas() {
        return servicio.aristas();
    }

    /** Aristas salientes de un usuario. */
    @GetMapping("/usuario/{id}/salientes")
    public List<Arista> salientes(@PathVariable String id) {
        return servicio.salientesDe(id);
    }

    /** Vecinos directos (IDs). */
    @GetMapping("/usuario/{id}/vecinos")
    public Set<String> vecinos(@PathVariable String id) {
        return servicio.vecinosDirectos(id);
    }

    /** Amigos de amigos (nivel 2 del BFS). */
    @GetMapping("/usuario/{id}/amigos-de-amigos")
    public Set<String> amigosDeAmigos(@PathVariable String id) {
        return servicio.amigosDeAmigos(id);
    }

    /**
     * BFS desde un usuario hasta una profundidad dada. Devuelve un mapa
     * nivel -> IDs alcanzados.
     */
    @GetMapping("/usuario/{id}/alcance")
    public Map<Integer, Set<String>> alcance(@PathVariable String id,
                                             @RequestParam(defaultValue = "3") int profundidad) {
        return servicio.alcance(id, profundidad);
    }

    /** Camino mas corto entre dos usuarios. */
    @GetMapping("/camino")
    public List<String> camino(@RequestParam String origen,
                               @RequestParam String destino) {
        return servicio.caminoEntre(origen, destino);
    }

    /** Top N rutas mas frecuentes (por peso total descendente). */
    @GetMapping("/rutas-frecuentes")
    public List<Arista> rutasFrecuentes(@RequestParam(defaultValue = "5") int top) {
        return servicio.rutasFrecuentes(top);
    }

    /** Ciclos dirigidos en el grafo. */
    @GetMapping("/ciclos")
    public List<List<String>> ciclos() {
        return servicio.ciclos();
    }
}
