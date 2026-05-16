package com.fintech.billetera.repository;

import com.fintech.billetera.domain.Notificacion;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * ============================================================================
 * ESTRUCTURA DE DATOS: LinkedList<Notificacion> usada como Cola FIFO
 *                      (Map<idUsuario, LinkedList<Notificacion>>)
 * ============================================================================
 *
 * JUSTIFICACION:
 * Las notificaciones del sistema modelan un buzon por usuario. El orden
 * natural es "primero en entrar, primero en salir": cuando el frontend
 * pide despachar notificaciones, lo razonable es procesar primero las
 * mas antiguas (las que llevan mas tiempo esperando).
 *
 * Esto es exactamente el comportamiento de una Cola (Queue) FIFO. Java
 * provee la interfaz Queue con los metodos offer (encolar al final) y
 * poll (sacar del frente), ambos en O(1) cuando se implementan sobre
 * LinkedList.
 *
 * Adicionalmente, mantenemos un HashMap<String, Notificacion> con
 * indice global por id, para localizar una notificacion en O(1) cuando
 * el usuario quiere marcarla como leida o consultarla por id directamente.
 *
 * POR QUE LinkedList Y NO ArrayDeque:
 * ArrayDeque tambien implementa Queue y es mas eficiente en memoria.
 * Sin embargo, LinkedList nos permite obtener un snapshot ordenado
 * (toList) en O(n) preservando el orden de llegada sin restricciones de
 * capacidad. Para el ejercicio academico, ademas, LinkedList expresa
 * mejor el caracter de "cola encadenada" tipico del concepto.
 *
 * COMPLEJIDAD DE OPERACIONES:
 * - encolar(idUsuario, notif):    O(1) - HashMap.get + LinkedList.offer
 * - desencolarSiguiente(idUsr):   O(1) - LinkedList.poll
 * - pico(idUsuario):              O(1) - LinkedList.peek
 * - pendientes(idUsuario):        O(k) - cuenta las no leidas (k = tam cola)
 * - listarPorUsuario(idUsuario):  O(k) - snapshot completo
 * - marcarLeida(idNotif):         O(1) - HashMap.get + set
 * - eliminar(idNotif):            O(k) - hay que quitarla de la LinkedList
 * ============================================================================
 */
@Repository
public class NotificacionRepository {

    /** Buzon FIFO por usuario. La cabeza es la mas antigua. */
    private final Map<String, LinkedList<Notificacion>> buzones = new HashMap<>();

    /** Indice global por id para acceso O(1) al marcar leidas. */
    private final Map<String, Notificacion> porId = new HashMap<>();

    /**
     * Encola una notificacion al final del buzon del usuario destinatario.
     * Equivale a Queue.offer().
     */
    public Notificacion encolar(Notificacion n) {
        buzones.computeIfAbsent(n.getIdUsuario(), k -> new LinkedList<>())
               .offer(n);
        porId.put(n.getId(), n);
        return n;
    }

    /**
     * Saca la siguiente notificacion del frente del buzon (la mas antigua).
     * Equivale a Queue.poll(). La elimina del indice global tambien.
     */
    public Optional<Notificacion> desencolarSiguiente(String idUsuario) {
        LinkedList<Notificacion> cola = buzones.get(idUsuario);
        if (cola == null || cola.isEmpty()) return Optional.empty();
        Notificacion n = cola.poll();
        porId.remove(n.getId());
        return Optional.of(n);
    }

    /** Mira el frente sin sacarlo. Equivale a Queue.peek(). */
    public Optional<Notificacion> pico(String idUsuario) {
        LinkedList<Notificacion> cola = buzones.get(idUsuario);
        if (cola == null || cola.isEmpty()) return Optional.empty();
        return Optional.ofNullable(cola.peek());
    }

    /**
     * Snapshot ordenado del buzon (de mas antigua a mas reciente).
     * No remueve nada.
     */
    public List<Notificacion> listarPorUsuario(String idUsuario) {
        LinkedList<Notificacion> cola = buzones.get(idUsuario);
        if (cola == null) return Collections.emptyList();
        return new ArrayList<>(cola);
    }

    /** Cuenta cuantas notificaciones tiene el usuario sin leer. */
    public int pendientes(String idUsuario) {
        LinkedList<Notificacion> cola = buzones.get(idUsuario);
        if (cola == null) return 0;
        int c = 0;
        for (Notificacion n : cola) {
            if (!n.isLeida()) c++;
        }
        return c;
    }

    /** Tamano total del buzon (incluye leidas no descartadas). */
    public int tamano(String idUsuario) {
        LinkedList<Notificacion> cola = buzones.get(idUsuario);
        return cola == null ? 0 : cola.size();
    }

    public Optional<Notificacion> buscarPorId(String idNotificacion) {
        return Optional.ofNullable(porId.get(idNotificacion));
    }

    /**
     * Marca como leida y la deja en la cola (no la desencola).
     * Util cuando se quiere conservar el historial visible aunque ya
     * este leida.
     */
    public Optional<Notificacion> marcarLeida(String idNotificacion) {
        Notificacion n = porId.get(idNotificacion);
        if (n == null) return Optional.empty();
        n.setLeida(true);
        return Optional.of(n);
    }

    /** Marca todas las del usuario como leidas en un solo barrido. */
    public int marcarTodasLeidas(String idUsuario) {
        LinkedList<Notificacion> cola = buzones.get(idUsuario);
        if (cola == null) return 0;
        int marcadas = 0;
        for (Notificacion n : cola) {
            if (!n.isLeida()) {
                n.setLeida(true);
                marcadas++;
            }
        }
        return marcadas;
    }

    /**
     * Drena todas las no leidas del usuario: las marca como leidas y
     * devuelve la lista. Combina poll repetido con marcado para uso del
     * boton "despachar todas" del frontend.
     */
    public List<Notificacion> drenarPendientes(String idUsuario) {
        LinkedList<Notificacion> cola = buzones.get(idUsuario);
        if (cola == null) return Collections.emptyList();
        List<Notificacion> drenadas = new ArrayList<>();
        for (Notificacion n : cola) {
            if (!n.isLeida()) {
                n.setLeida(true);
                drenadas.add(n);
            }
        }
        return drenadas;
    }

    public boolean eliminar(String idNotificacion) {
        Notificacion n = porId.remove(idNotificacion);
        if (n == null) return false;
        LinkedList<Notificacion> cola = buzones.get(n.getIdUsuario());
        if (cola != null) cola.remove(n);
        return true;
    }
}
