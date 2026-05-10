package com.fintech.billetera.repository;

import com.fintech.billetera.domain.OperacionProgramada;
import com.fintech.billetera.domain.enums.EstadoProgramada;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ============================================================================
 * ESTRUCTURA DE DATOS: PriorityQueue<OperacionProgramada>  (Cola de prioridad)
 *                      + HashMap<String, OperacionProgramada> (indice por id)
 * ============================================================================
 *
 * JUSTIFICACION:
 * Las operaciones programadas DEBEN ejecutarse en orden de fecha, no en orden
 * de creacion. Si programo una operacion para mañana y luego otra para ayer,
 * la segunda debe ejecutarse primero al barrer pendientes.
 *
 * PriorityQueue es un heap binario que mantiene siempre el elemento con
 * mayor prioridad listo en la cabeza. Como OperacionProgramada implementa
 * Comparable comparando fechaEjecucion, el peek/poll devuelve la operacion
 * con la fecha mas cercana.
 *
 * COMPLEJIDAD:
 * - programar(op):                 O(log n)  - heapify-up
 * - peekProxima():                 O(1)      - cabeza del heap
 * - extraerProxima():              O(log n)  - heapify-down
 * - cancelar(id):                  O(n)      - remove busca linealmente
 * - listarOrdenadasPorFecha():     O(n log n) - copia y ordena
 *
 * NOTA: la PriorityQueue NO recorre en orden ordenado al iterar; solo el
 * tope esta garantizado. Por eso para listar en orden hay que extraer todo
 * o usar un TreeSet alternativo. Aqui hacemos copia ordenada cuando se pide.
 * ============================================================================
 */
@Repository
public class ProgramacionRepository {

    private final PriorityQueue<OperacionProgramada> cola = new PriorityQueue<>();
    private final Map<String, OperacionProgramada> indice = new HashMap<>();

    /** Programa una nueva operacion en la cola de prioridad. */
    public OperacionProgramada programar(OperacionProgramada op) {
        cola.offer(op);
        indice.put(op.getId(), op);
        return op;
    }

    /** Devuelve la proxima operacion a ejecutarse (sin sacarla). */
    public Optional<OperacionProgramada> peekProxima() {
        return Optional.ofNullable(cola.peek());
    }

    /** Saca y devuelve la proxima operacion a ejecutarse. */
    public Optional<OperacionProgramada> extraerProxima() {
        OperacionProgramada op = cola.poll();
        if (op != null) indice.remove(op.getId());
        return Optional.ofNullable(op);
    }

    /**
     * Saca todas las operaciones cuya fecha de ejecucion ya paso.
     * Las devuelve en orden de fecha ascendente (las mas antiguas primero).
     */
    public List<OperacionProgramada> extraerVencidas() {
        List<OperacionProgramada> vencidas = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();
        while (!cola.isEmpty() && !cola.peek().getFechaEjecucion().isAfter(ahora)) {
            OperacionProgramada op = cola.poll();
            indice.remove(op.getId());
            vencidas.add(op);
        }
        return vencidas;
    }

    public Optional<OperacionProgramada> buscarPorId(String id) {
        return Optional.ofNullable(indice.get(id));
    }

    /** Cancela una operacion programada (si todavia esta pendiente). */
    public boolean cancelar(String id) {
        OperacionProgramada op = indice.get(id);
        if (op == null) return false;
        op.setEstado(EstadoProgramada.CANCELADA);
        cola.remove(op);
        indice.remove(id);
        return true;
    }

    /**
     * Devuelve TODAS las operaciones programadas ordenadas por fecha ascendente.
     * Util para el listado en el frontend.
     */
    public List<OperacionProgramada> listarOrdenadasPorFecha() {
        List<OperacionProgramada> lista = new ArrayList<>(cola);
        Collections.sort(lista);
        return lista;
    }

    public int tamano() {
        return cola.size();
    }
}
