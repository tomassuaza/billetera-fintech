package com.fintech.billetera.service;

import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.repository.FidelizacionRepository;
import com.fintech.billetera.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

/**
 * Maneja registro, busqueda y eliminacion de usuarios.
 * Tambien inicializa la posicion del usuario en el ranking de fidelizacion.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final FidelizacionRepository fidelizacionRepo;

    public UsuarioService(UsuarioRepository repo, FidelizacionRepository fidelizacionRepo) {
        this.repo = repo;
        this.fidelizacionRepo = fidelizacionRepo;
    }

    public Usuario registrar(String nombre, String correo) {
        if (nombre == null || nombre.isBlank()) {
            throw new RuntimeException("El nombre es obligatorio");
        }
        String id = UUID.randomUUID().toString().substring(0, 8);
        Usuario u = new Usuario(id, nombre, correo);
        repo.guardar(u);
        // Registrar en el TreeMap de ranking con 0 puntos
        fidelizacionRepo.actualizarRanking(id, 0, 0);
        return u;
    }

    public Usuario obtener(String id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
    }

    public Collection<Usuario> listar() {
        return repo.listar();
    }

    public void eliminar(String id) {
        Usuario u = obtener(id);
        // Quitar del ranking
        fidelizacionRepo.actualizarRanking(id, u.getPuntos(), 0);
        // El bucket de 0 podria quedar con el id; lo limpiamos:
        fidelizacionRepo.actualizarRanking(id, 0, 0);  // refresca
        if (!repo.eliminar(id)) {
            throw new RuntimeException("Usuario no encontrado: " + id);
        }
    }
}
