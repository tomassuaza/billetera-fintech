package com.fintech.billetera.repository;

import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * ============================================================================
 * ESTRUCTURA DE DATOS: TreeMap<Integer, Set<String>>  (Arbol balanceado)
 * ============================================================================
 *
 * JUSTIFICACION:
 * El sistema requiere consultar usuarios por rango de puntos
 * (ej. "dame todos los usuarios entre 1000 y 5000 puntos"). Un HashMap NO
 * sirve aqui porque las claves no estan ordenadas; tendriamos que iterar
 * todos los puntos y filtrar.
 *
 * TreeMap es un arbol rojo-negro que mantiene las claves ordenadas. Eso
 * permite usar subMap(min, max) para hacer consultas por rango en O(log n + k)
 * donde k es el numero de puntos distintos en el rango.
 *
 * EL DISENO:
 * - Clave: cantidad de puntos (Integer)
 * - Valor: Set<String> de IDs de usuarios con esa cantidad de puntos
 *
 * Usamos un Set como valor porque pueden haber multiples usuarios con la
 * misma cantidad de puntos (ej. dos usuarios con 250 puntos cada uno).
 *
 * COMPLEJIDAD:
 * - actualizarRanking(idUser, antes, despues):  O(log n)
 * - usuariosEnRango(min, max):                   O(log n + k)
 * - topN(n):                                     O(n) en valores
 * - tamano():                                    O(1)
 * ============================================================================
 */
@Repository
public class FidelizacionRepository {

    private final TreeMap<Integer, Set<String>> ranking = new TreeMap<>();

    /**
     * Actualiza la posicion de un usuario en el ranking cuando sus puntos
     * cambian. Saca el id del bucket viejo y lo mete en el nuevo.
     *
     * @param idUsuario    id del usuario
     * @param puntosAntes  puntos previos (puede ser 0 para primer registro)
     * @param puntosDespues puntos nuevos
     */
    public void actualizarRanking(String idUsuario, int puntosAntes, int puntosDespues) {
        // Quitar del bucket anterior si existe
        if (ranking.containsKey(puntosAntes)) {
            Set<String> bucketAnterior = ranking.get(puntosAntes);
            bucketAnterior.remove(idUsuario);
            if (bucketAnterior.isEmpty()) {
                ranking.remove(puntosAntes);
            }
        }
        // Agregar al bucket nuevo
        ranking.computeIfAbsent(puntosDespues, k -> new HashSet<>()).add(idUsuario);
    }

    /**
     * Devuelve los IDs de usuarios cuyos puntos estan en el rango [min, max].
     * Usa subMap para extraer solo el rango relevante del arbol -> O(log n + k).
     */
    public Set<String> usuariosEnRango(int min, int max) {
        Set<String> result = new HashSet<>();
        // subMap(min, true, max, true) -> incluye ambos extremos
        for (Set<String> bucket : ranking.subMap(min, true, max, true).values()) {
            result.addAll(bucket);
        }
        return result;
    }

    /**
     * Devuelve los N usuarios con mayor cantidad de puntos.
     * Recorre el arbol en orden descendente.
     */
    public List<String> topN(int n) {
        List<String> top = new ArrayList<>();
        // descendingMap() para ir del mayor al menor
        for (Set<String> bucket : ranking.descendingMap().values()) {
            for (String id : bucket) {
                top.add(id);
                if (top.size() >= n) return top;
            }
        }
        return top;
    }

    /** Numero total de usuarios registrados en el ranking. */
    public int tamano() {
        return ranking.values().stream().mapToInt(Set::size).sum();
    }
}
