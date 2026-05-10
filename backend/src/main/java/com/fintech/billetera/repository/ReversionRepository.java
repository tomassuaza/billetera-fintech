package com.fintech.billetera.repository;

import com.fintech.billetera.domain.Transaccion;
import org.springframework.stereotype.Repository;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================================
 * ESTRUCTURA DE DATOS: HashMap<String, ArrayDeque<Transaccion>>
 *                      Pila (Stack) por usuario
 * ============================================================================
 *
 * JUSTIFICACION:
 * Cada usuario tiene su propia pila de operaciones reversibles. Al pedir
 * "deshacer la ultima operacion" se debe revertir la mas reciente, que es
 * la que esta en el tope -> comportamiento LIFO natural de una pila.
 *
 * Usamos ArrayDeque (no la clase Stack legacy) porque:
 * - Es la implementacion recomendada por la documentacion oficial de Java.
 * - Es mas rapida que Stack (Stack hereda de Vector y tiene overhead de
 *   sincronizacion innecesaria).
 *
 * Cuando una transaccion se revierte, NO se elimina de la pila; se marca
 * como REVERTIDA en su estado. El push sigue siendo el origen de verdad
 * sobre que hizo el usuario en orden cronologico.
 *
 * COMPLEJIDAD:
 * - apilar(idUsuario, t):  O(1)
 * - peek(idUsuario):       O(1) - mira sin sacar
 * - pop(idUsuario):        O(1)
 * - tamano(idUsuario):     O(1)
 * ============================================================================
 */
@Repository
public class ReversionRepository {

    private final Map<String, Deque<Transaccion>> pilasPorUsuario = new HashMap<>();

    /** Apila una transaccion en la pila del usuario. */
    public void apilar(String idUsuario, Transaccion t) {
        pilasPorUsuario
                .computeIfAbsent(idUsuario, k -> new ArrayDeque<>())
                .push(t);
    }

    /** Devuelve la transaccion en el tope sin sacarla. */
    public Optional<Transaccion> peek(String idUsuario) {
        Deque<Transaccion> pila = pilasPorUsuario.get(idUsuario);
        if (pila == null || pila.isEmpty()) return Optional.empty();
        return Optional.ofNullable(pila.peek());
    }

    /** Saca y devuelve la transaccion del tope. */
    public Optional<Transaccion> pop(String idUsuario) {
        Deque<Transaccion> pila = pilasPorUsuario.get(idUsuario);
        if (pila == null || pila.isEmpty()) return Optional.empty();
        return Optional.ofNullable(pila.pop());
    }

    public int tamano(String idUsuario) {
        Deque<Transaccion> pila = pilasPorUsuario.get(idUsuario);
        return pila == null ? 0 : pila.size();
    }
}
