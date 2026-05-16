package com.fintech.billetera.repository;

import com.fintech.billetera.domain.EventoAuditoria;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * ============================================================================
 * ESTRUCTURA DE DATOS: LinkedList<EventoAuditoria> (historial cronologico)
 *                      + Map<idUsuario, LinkedList<EventoAuditoria>>
 *                        (indice por usuario)
 * ============================================================================
 *
 * JUSTIFICACION:
 * El historial de auditoria comparte las mismas necesidades que el
 * historial de transacciones: insercion frecuente al frente y consulta
 * en orden cronologico inverso (lo mas reciente primero).
 *
 * LinkedList.addFirst() es O(1); con ArrayList la insercion al inicio
 * seria O(n) por el desplazamiento.
 *
 * Mantener un indice por usuario evita filtrar el historial global
 * cuando solo se piden los eventos de un usuario especifico.
 *
 * COMPLEJIDAD:
 *  - registrar(evento):                O(1)
 *  - listarTodo():                     O(n)
 *  - listarPorUsuario(idUsuario):      O(k) - k eventos del usuario
 *  - tamano():                         O(1)
 * ============================================================================
 */
@Repository
public class AuditoriaRepository {

    private final LinkedList<EventoAuditoria> historial = new LinkedList<>();
    private final Map<String, LinkedList<EventoAuditoria>> porUsuario = new HashMap<>();

    public EventoAuditoria registrar(EventoAuditoria evento) {
        historial.addFirst(evento);
        if (evento.getIdUsuario() != null) {
            porUsuario.computeIfAbsent(evento.getIdUsuario(), k -> new LinkedList<>())
                      .addFirst(evento);
        }
        return evento;
    }

    public List<EventoAuditoria> listarTodo() {
        return new ArrayList<>(historial);
    }

    public List<EventoAuditoria> listarPorUsuario(String idUsuario) {
        LinkedList<EventoAuditoria> lista = porUsuario.get(idUsuario);
        if (lista == null) return Collections.emptyList();
        return new ArrayList<>(lista);
    }

    public int tamano() {
        return historial.size();
    }
}
