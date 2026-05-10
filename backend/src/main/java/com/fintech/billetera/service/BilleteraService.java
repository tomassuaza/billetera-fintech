package com.fintech.billetera.service;

import com.fintech.billetera.domain.Billetera;
import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.domain.enums.TipoBilletera;
import com.fintech.billetera.repository.BilleteraRepository;
import com.fintech.billetera.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Maneja el ciclo de vida de las billeteras digitales.
 * Garantiza que cada billetera quede asociada a su usuario propietario.
 */
@Service
public class BilleteraService {

    private final BilleteraRepository repo;
    private final UsuarioRepository usuarioRepo;

    public BilleteraService(BilleteraRepository repo, UsuarioRepository usuarioRepo) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
    }

    public Billetera crear(String idUsuario, String nombre, TipoBilletera tipo) {
        Usuario u = usuarioRepo.buscarPorId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));
        if (nombre == null || nombre.isBlank()) {
            throw new RuntimeException("El nombre de la billetera es obligatorio");
        }
        if (tipo == null) {
            throw new RuntimeException("El tipo de billetera es obligatorio");
        }

        String id = "BIL-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Billetera b = new Billetera(id, idUsuario, nombre, tipo);
        repo.guardar(b);

        // Asociar al usuario para mantener la integridad del agregado
        u.getIdsBilleteras().add(id);
        usuarioRepo.guardar(u);

        return b;
    }

    public Billetera obtener(String id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Billetera no encontrada: " + id));
    }

    public Collection<Billetera> listar() {
        return repo.listar();
    }

    public List<Billetera> listarPorUsuario(String idUsuario) {
        if (!usuarioRepo.existe(idUsuario)) {
            throw new RuntimeException("Usuario no encontrado: " + idUsuario);
        }
        return repo.listarPorUsuario(idUsuario);
    }

    public Billetera desactivar(String id) {
        Billetera b = obtener(id);
        b.setActiva(false);
        return repo.guardar(b);
    }

    public Billetera activar(String id) {
        Billetera b = obtener(id);
        b.setActiva(true);
        return repo.guardar(b);
    }
}
