package com.fintech.billetera.repository;

import com.fintech.billetera.domain.Billetera;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * ESTRUCTURA DE DATOS: HashMap<String, Billetera>  (Tabla hash)
 * ============================================================================
 *
 * JUSTIFICACION:
 * Cada transaccion necesita resolver el codigo de billetera origen y/o
 * destino para validar saldo, debitar y acreditar. Al ser la operacion
 * mas frecuente, requiere ser O(1).
 *
 * Para listar las billeteras de un usuario se itera sobre values(). Es O(n)
 * pero acceptable para volumenes academicos. En un sistema real agregariamos
 * un indice secundario HashMap<idUsuario, Set<idBilletera>>.
 *
 * COMPLEJIDAD:
 * - guardar / buscarPorId / eliminar: O(1) promedio
 * - listarPorUsuario:                  O(n) (itera y filtra)
 * ============================================================================
 */
@Repository
public class BilleteraRepository {

    private final Map<String, Billetera> billeteras = new HashMap<>();

    public Billetera guardar(Billetera b) {
        billeteras.put(b.getId(), b);
        return b;
    }

    public Optional<Billetera> buscarPorId(String id) {
        return Optional.ofNullable(billeteras.get(id));
    }

    public Collection<Billetera> listar() {
        return billeteras.values();
    }

    public List<Billetera> listarPorUsuario(String idUsuario) {
        return billeteras.values().stream()
                .filter(b -> b.getIdUsuario().equals(idUsuario))
                .collect(Collectors.toList());
    }

    public boolean eliminar(String id) {
        return billeteras.remove(id) != null;
    }

    public boolean existe(String id) {
        return billeteras.containsKey(id);
    }
}
