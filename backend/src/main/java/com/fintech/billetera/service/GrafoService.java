package com.fintech.billetera.service;

import com.fintech.billetera.domain.Arista;
import com.fintech.billetera.repository.GrafoTransferenciasRepository;
import com.fintech.billetera.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Operaciones de analisis sobre el grafo de transferencias entre usuarios.
 *
 * Se mantiene como facade delgado encima del repositorio para que los
 * controllers no toquen la estructura directamente, y para que el modulo
 * de analitica pueda reutilizar las mismas consultas mas adelante.
 */
@Service
public class GrafoService {

    private final GrafoTransferenciasRepository repo;
    private final UsuarioRepository usuarioRepo;

    public GrafoService(GrafoTransferenciasRepository repo,
                        UsuarioRepository usuarioRepo) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
    }

    // -------------------- COMANDOS --------------------

    public void registrarTransferencia(String idOrigen, String idDestino,
                                       BigDecimal monto) {
        repo.registrarTransferencia(idOrigen, idDestino, monto);
    }

    public void revertirTransferencia(String idOrigen, String idDestino,
                                      BigDecimal monto) {
        repo.revertirTransferencia(idOrigen, idDestino, monto);
    }

    // -------------------- CONSULTAS BASICAS --------------------

    public List<Arista> aristas() {
        return repo.todasLasAristas();
    }

    public List<Arista> salientesDe(String idUsuario) {
        validarUsuario(idUsuario);
        return repo.vecinos(idUsuario);
    }

    /**
     * Lista de usuarios a quienes el usuario indicado ha transferido al
     * menos una vez (vecinos directos en el grafo dirigido).
     */
    public Set<String> vecinosDirectos(String idUsuario) {
        validarUsuario(idUsuario);
        return repo.vecinosIds(idUsuario);
    }

    // -------------------- BFS Y AMIGOS DE AMIGOS --------------------

    /**
     * "Amigos de amigos": destinatarios alcanzables en exactamente 2
     * saltos desde el usuario, excluyendo a el mismo y a sus vecinos
     * directos. Util para sugerencias y deteccion de redes financieras
     * extendidas.
     */
    public Set<String> amigosDeAmigos(String idUsuario) {
        validarUsuario(idUsuario);
        Map<Integer, Set<String>> niveles = repo.bfsPorNivel(idUsuario, 2);
        return niveles.getOrDefault(2, Collections.emptySet());
    }

    /**
     * BFS general desde un usuario hasta una profundidad dada,
     * devolviendo los IDs agrupados por nivel.
     */
    public Map<Integer, Set<String>> alcance(String idOrigen, int profundidad) {
        validarUsuario(idOrigen);
        if (profundidad <= 0) {
            throw new RuntimeException("La profundidad debe ser mayor a cero");
        }
        return repo.bfsPorNivel(idOrigen, profundidad);
    }

    /**
     * Camino mas corto entre dos usuarios en cantidad de saltos. Lista
     * ordenada de IDs incluyendo origen y destino. Vacia si no hay
     * camino.
     */
    public List<String> caminoEntre(String idOrigen, String idDestino) {
        validarUsuario(idOrigen);
        validarUsuario(idDestino);
        return repo.caminoMasCorto(idOrigen, idDestino);
    }

    // -------------------- ANALISIS GLOBAL --------------------

    /**
     * Top N rutas mas frecuentes del grafo, ordenadas por peso total
     * descendente (monto acumulado entre dos usuarios). Si dos rutas
     * empatan en peso, gana la de mayor numero de transferencias.
     */
    public List<Arista> rutasFrecuentes(int topN) {
        if (topN <= 0) throw new RuntimeException("topN debe ser mayor a cero");
        return repo.todasLasAristas().stream()
                .sorted(Comparator
                        .comparing(Arista::getPesoTotal).reversed()
                        .thenComparing(Comparator.comparingInt(Arista::getConteo).reversed()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Ciclos dirigidos detectados en el grafo. Cada ciclo es la
     * secuencia de IDs que lo conforman (el primero y el ultimo
     * coinciden). Util para detectar lavado entre cuentas que se
     * devuelven dinero entre si.
     */
    public List<List<String>> ciclos() {
        return repo.detectarCiclos();
    }

    // -------------------- INTERNO --------------------

    private void validarUsuario(String idUsuario) {
        if (!usuarioRepo.existe(idUsuario)) {
            throw new RuntimeException("Usuario no encontrado: " + idUsuario);
        }
    }
}
