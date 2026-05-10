package com.fintech.billetera.repository;

import com.fintech.billetera.domain.Transaccion;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * ============================================================================
 * ESTRUCTURAS DE DATOS:
 *   1. HashMap<String, Transaccion>           - busqueda por id
 *   2. HashMap<String, LinkedList<String>>    - historial por billetera
 *   3. HashMap<String, LinkedList<String>>    - historial por usuario
 * ============================================================================
 *
 * JUSTIFICACION DE LinkedList:
 * Las transacciones se agregan SIEMPRE al inicio (las mas recientes primero)
 * para que al consultar el historial el orden sea cronologico inverso.
 *
 * - LinkedList.addFirst() es O(1) - mantiene un puntero al head.
 * - ArrayList.add(0, x) seria O(n) porque corre todos los elementos.
 *
 * Por eso elegimos LinkedList: el historial se construye en O(1) por
 * insercion y se consulta en O(n) cuando hay que mostrarlo.
 *
 * NOTA: guardamos solo el id de la transaccion en las listas de historial,
 * no el objeto entero. La transaccion vive en el HashMap principal. Asi
 * evitamos duplicacion en memoria y mantenemos consistencia.
 *
 * COMPLEJIDAD:
 * - guardar(t):                   O(1) - actualiza 3 estructuras
 * - buscarPorId(id):              O(1)
 * - historialBilletera(idBill):   O(k) donde k = transacciones de esa billetera
 * - historialUsuario(idUser):     O(k) donde k = transacciones del usuario
 * ============================================================================
 */
@Repository
public class TransaccionRepository {

    private final Map<String, Transaccion> transacciones = new HashMap<>();
    private final Map<String, LinkedList<String>> historialPorBilletera = new HashMap<>();
    private final Map<String, LinkedList<String>> historialPorUsuario = new HashMap<>();

    /**
     * Guarda la transaccion y la indexa en los historiales correspondientes.
     * Si la transaccion ya existe (mismo id), actualiza su estado en el HashMap
     * pero NO la duplica en los historiales.
     */
    public Transaccion guardar(Transaccion t) {
        boolean esNueva = !transacciones.containsKey(t.getId());
        transacciones.put(t.getId(), t);

        if (esNueva) {
            // Indexar en historial de billetera origen
            if (t.getIdBilleteraOrigen() != null) {
                historialPorBilletera
                        .computeIfAbsent(t.getIdBilleteraOrigen(), k -> new LinkedList<>())
                        .addFirst(t.getId());
            }
            // Indexar en historial de billetera destino (si es diferente)
            if (t.getIdBilleteraDestino() != null
                    && !t.getIdBilleteraDestino().equals(t.getIdBilleteraOrigen())) {
                historialPorBilletera
                        .computeIfAbsent(t.getIdBilleteraDestino(), k -> new LinkedList<>())
                        .addFirst(t.getId());
            }
            // Indexar en historial de usuario generador
            if (t.getIdUsuarioGenerador() != null) {
                historialPorUsuario
                        .computeIfAbsent(t.getIdUsuarioGenerador(), k -> new LinkedList<>())
                        .addFirst(t.getId());
            }
        }
        return t;
    }

    public Optional<Transaccion> buscarPorId(String id) {
        return Optional.ofNullable(transacciones.get(id));
    }

    public Collection<Transaccion> listar() {
        return transacciones.values();
    }

    /**
     * Devuelve las transacciones de una billetera, mas recientes primero.
     * Recorre la LinkedList de IDs y resuelve cada una contra el HashMap.
     */
    public List<Transaccion> historialBilletera(String idBilletera) {
        LinkedList<String> ids = historialPorBilletera.get(idBilletera);
        if (ids == null) return Collections.emptyList();
        List<Transaccion> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            Transaccion t = transacciones.get(id);
            if (t != null) result.add(t);
        }
        return result;
    }

    /**
     * Devuelve las transacciones generadas por un usuario, mas recientes primero.
     */
    public List<Transaccion> historialUsuario(String idUsuario) {
        LinkedList<String> ids = historialPorUsuario.get(idUsuario);
        if (ids == null) return Collections.emptyList();
        List<Transaccion> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            Transaccion t = transacciones.get(id);
            if (t != null) result.add(t);
        }
        return result;
    }
}
