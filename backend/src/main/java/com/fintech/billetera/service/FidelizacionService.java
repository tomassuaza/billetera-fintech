package com.fintech.billetera.service;

import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.domain.enums.NivelUsuario;
import com.fintech.billetera.repository.FidelizacionRepository;
import com.fintech.billetera.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maneja el ranking y los niveles de fidelizacion.
 *
 * Cada vez que un usuario gana o pierde puntos (al hacer transacciones,
 * revertirlas, etc.), TransaccionService o ReversionService llama a
 * actualizarRanking() para mantener el TreeMap sincronizado.
 */
@Service
public class FidelizacionService {

    private final FidelizacionRepository repo;
    private final UsuarioRepository usuarioRepo;

    public FidelizacionService(FidelizacionRepository repo, UsuarioRepository usuarioRepo) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
    }

    /** Reposiciona al usuario en el TreeMap cuando cambian sus puntos. */
    public void actualizarRanking(String idUsuario, int puntosAntes, int puntosDespues) {
        repo.actualizarRanking(idUsuario, puntosAntes, puntosDespues);
    }

    /**
     * Devuelve los N usuarios con mas puntos, ya hidratados con sus datos.
     */
    public List<Usuario> topN(int n) {
        return repo.topN(n).stream()
                .map(usuarioRepo::buscarPorId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /** Usuarios cuyos puntos estan dentro de [min, max]. */
    public List<Usuario> usuariosEnRango(int min, int max) {
        return repo.usuariosEnRango(min, max).stream()
                .map(usuarioRepo::buscarPorId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /** Usuarios de un nivel especifico (consulta por rango usando los limites del enum). */
    public List<Usuario> usuariosPorNivel(NivelUsuario nivel) {
        return usuariosEnRango(nivel.getMin(), nivel.getMax());
    }

    /** Conteo de usuarios por cada nivel. */
    public Map<NivelUsuario, Integer> conteoPorNivel() {
        Map<NivelUsuario, Integer> conteo = new EnumMap<>(NivelUsuario.class);
        for (NivelUsuario nivel : NivelUsuario.values()) {
            conteo.put(nivel, usuariosEnRango(nivel.getMin(), nivel.getMax()).size());
        }
        return conteo;
    }
}
