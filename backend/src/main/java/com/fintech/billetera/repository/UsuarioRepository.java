package com.fintech.billetera.repository;

import com.fintech.billetera.domain.Usuario;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================================
 * ESTRUCTURA DE DATOS: HashMap<String, Usuario>  (Tabla hash)
 * ============================================================================
 *
 * JUSTIFICACION:
 * El acceso a usuarios por su ID es la operacion mas frecuente del sistema.
 * Cada vez que se hace una transaccion, una consulta de saldo, una asignacion
 * de puntos, etc., se necesita encontrar al usuario por su id.
 *
 * Con HashMap obtenemos O(1) promedio en get/put/remove. Una alternativa
 * con ArrayList daria O(n), inviable cuando crezcan los datos.
 *
 * COMPLEJIDAD DE OPERACIONES:
 * - guardar(usuario):    O(1) promedio
 * - buscarPorId(id):     O(1) promedio
 * - eliminar(id):        O(1) promedio
 * - existe(id):          O(1) promedio
 * - listar():            O(n) - itera todos los valores
 * ============================================================================
 */
@Repository
public class UsuarioRepository {

    private final Map<String, Usuario> usuarios = new HashMap<>();

    public Usuario guardar(Usuario u) {
        usuarios.put(u.getId(), u);
        return u;
    }

    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public Collection<Usuario> listar() {
        return usuarios.values();
    }

    public boolean eliminar(String id) {
        return usuarios.remove(id) != null;
    }

    public boolean existe(String id) {
        return usuarios.containsKey(id);
    }
}
