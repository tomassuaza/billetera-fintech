package com.fintech.billetera.repository;

import com.fintech.billetera.domain.Arista;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

/**
 * ============================================================================
 * ESTRUCTURA DE DATOS: Grafo dirigido y ponderado
 *                      Map<String, Map<String, Arista>>  (lista de adyacencia)
 * ============================================================================
 *
 * JUSTIFICACION:
 * El sistema modela la red de transferencias entre usuarios como un grafo
 * dirigido (la transferencia va de un origen a un destino) y ponderado
 * (cada arista acumula el monto total transferido y el numero de envios).
 *
 * Elegimos la representacion por LISTA DE ADYACENCIA porque:
 *
 *  - El grafo es ESPARSO: la mayoria de usuarios solo se relaciona con
 *    unos pocos otros. Una matriz de adyacencia gastaria O(V^2) memoria
 *    casi vacia. La lista de adyacencia gasta O(V + E).
 *  - El recorrido BFS / DFS sobre lista de adyacencia es O(V + E), igual
 *    que sobre matriz, sin el costo de memoria.
 *  - El acceso "vecinos de X" es O(grado(X)) en lista de adyacencia.
 *
 * DETALLE DE LA ESTRUCTURA:
 * El nivel exterior es un HashMap (idOrigen -> sus aristas salientes).
 * El nivel interior tambien es un HashMap (idDestino -> Arista) en vez
 * de una List<Arista>, porque buscar "ya existe la arista origen->destino"
 * para acumular el peso seria O(grado) sobre una List; con HashMap es O(1).
 *
 * COMPLEJIDAD:
 *  - registrarTransferencia(o, d, m):    O(1) promedio
 *  - revertirTransferencia(o, d, m):     O(1) promedio
 *  - vecinos(idUsuario):                 O(grado(u))
 *  - bfs(origen, profundidad):           O(V + E) en el peor caso
 *  - amigosDeAmigos(usuario):            O(V + E)
 *  - caminoMasCorto(o, d):               O(V + E) - BFS sin pesos
 *  - rutasFrecuentes(topN):              O(E log E) - se ordenan todas
 *  - detectarCiclos():                   O(V + E) - DFS con coloreo
 * ============================================================================
 */
@Repository
public class GrafoTransferenciasRepository {

    /** idOrigen -> (idDestino -> arista). */
    private final Map<String, Map<String, Arista>> adyacencia = new HashMap<>();

    /**
     * Registra (o acumula) una transferencia entre dos usuarios. Si la
     * arista origen->destino ya existe, suma el monto y aumenta el conteo;
     * si no, la crea.
     */
    public Arista registrarTransferencia(String idOrigen, String idDestino,
                                         BigDecimal monto) {
        Map<String, Arista> salientes = adyacencia
                .computeIfAbsent(idOrigen, k -> new HashMap<>());
        Arista a = salientes.get(idDestino);
        if (a == null) {
            a = new Arista(idOrigen, idDestino, monto);
            salientes.put(idDestino, a);
        } else {
            a.acumular(monto);
        }
        return a;
    }

    /**
     * Resta una transferencia previa (al revertirla). Si la arista queda
     * con conteo 0, la elimina del grafo.
     */
    public void revertirTransferencia(String idOrigen, String idDestino,
                                      BigDecimal monto) {
        Map<String, Arista> salientes = adyacencia.get(idOrigen);
        if (salientes == null) return;
        Arista a = salientes.get(idDestino);
        if (a == null) return;
        boolean siguePresente = a.restar(monto);
        if (!siguePresente) {
            salientes.remove(idDestino);
            if (salientes.isEmpty()) adyacencia.remove(idOrigen);
        }
    }

    /** Aristas salientes de un usuario (a quien le ha transferido). */
    public List<Arista> vecinos(String idUsuario) {
        Map<String, Arista> salientes = adyacencia.get(idUsuario);
        if (salientes == null) return Collections.emptyList();
        return new ArrayList<>(salientes.values());
    }

    /** Conjunto de IDs vecinos directos (solo destinos). */
    public Set<String> vecinosIds(String idUsuario) {
        Map<String, Arista> salientes = adyacencia.get(idUsuario);
        if (salientes == null) return Collections.emptySet();
        return new HashSet<>(salientes.keySet());
    }

    /**
     * Recorrido BFS clasico desde un nodo origen hasta la profundidad
     * indicada. Devuelve los nodos alcanzados (sin incluir el origen)
     * agrupados por nivel.
     */
    public Map<Integer, Set<String>> bfsPorNivel(String idOrigen,
                                                 int profundidadMax) {
        Map<Integer, Set<String>> niveles = new LinkedHashMap<>();
        Set<String> visitados = new HashSet<>();
        Deque<String> colaActual = new ArrayDeque<>();
        colaActual.offer(idOrigen);
        visitados.add(idOrigen);

        int nivel = 0;
        while (!colaActual.isEmpty() && nivel < profundidadMax) {
            Deque<String> siguienteNivel = new ArrayDeque<>();
            Set<String> esteNivel = new HashSet<>();
            while (!colaActual.isEmpty()) {
                String actual = colaActual.poll();
                for (String vecino : vecinosIds(actual)) {
                    if (visitados.add(vecino)) {
                        esteNivel.add(vecino);
                        siguienteNivel.offer(vecino);
                    }
                }
            }
            nivel++;
            if (!esteNivel.isEmpty()) niveles.put(nivel, esteNivel);
            colaActual = siguienteNivel;
        }
        return niveles;
    }

    /**
     * Devuelve TODAS las aristas del grafo (util para listados globales y
     * para rankings de rutas frecuentes).
     */
    public List<Arista> todasLasAristas() {
        List<Arista> todas = new ArrayList<>();
        for (Map<String, Arista> salientes : adyacencia.values()) {
            todas.addAll(salientes.values());
        }
        return todas;
    }

    /** IDs de todos los usuarios con al menos una arista saliente. */
    public Set<String> nodosConSalida() {
        return adyacencia.keySet();
    }

    /**
     * Devuelve un camino dirigido (lista ordenada de IDs) desde origen
     * hasta destino, usando BFS. Si no hay camino, lista vacia.
     */
    public List<String> caminoMasCorto(String idOrigen, String idDestino) {
        if (idOrigen.equals(idDestino)) return List.of(idOrigen);
        Map<String, String> padre = new HashMap<>();
        Deque<String> cola = new ArrayDeque<>();
        cola.offer(idOrigen);
        padre.put(idOrigen, null);

        while (!cola.isEmpty()) {
            String actual = cola.poll();
            for (String vecino : vecinosIds(actual)) {
                if (padre.containsKey(vecino)) continue;
                padre.put(vecino, actual);
                if (vecino.equals(idDestino)) {
                    return reconstruir(padre, idDestino);
                }
                cola.offer(vecino);
            }
        }
        return Collections.emptyList();
    }

    private List<String> reconstruir(Map<String, String> padre, String hasta) {
        LinkedList<String> camino = new LinkedList<>();
        String cursor = hasta;
        while (cursor != null) {
            camino.addFirst(cursor);
            cursor = padre.get(cursor);
        }
        return camino;
    }

    /**
     * Detecta ciclos dirigidos usando DFS con tres colores:
     *  BLANCO = no visitado, GRIS = en pila de recursion, NEGRO = terminado.
     * Si durante el DFS encontramos una arista hacia un nodo GRIS, hay ciclo.
     *
     * Devuelve la lista de ciclos encontrados (cada ciclo como lista de IDs).
     */
    public List<List<String>> detectarCiclos() {
        Map<String, Color> color = new HashMap<>();
        Map<String, String> padre = new HashMap<>();
        List<List<String>> ciclos = new ArrayList<>();

        Set<String> nodos = new HashSet<>(adyacencia.keySet());
        for (Map<String, Arista> sal : adyacencia.values()) {
            nodos.addAll(sal.keySet());
        }
        for (String n : nodos) color.put(n, Color.BLANCO);

        for (String n : nodos) {
            if (color.get(n) == Color.BLANCO) {
                dfs(n, color, padre, ciclos);
            }
        }
        return ciclos;
    }

    private void dfs(String u, Map<String, Color> color,
                     Map<String, String> padre, List<List<String>> ciclos) {
        color.put(u, Color.GRIS);
        for (String v : vecinosIds(u)) {
            Color cv = color.getOrDefault(v, Color.BLANCO);
            if (cv == Color.BLANCO) {
                padre.put(v, u);
                dfs(v, color, padre, ciclos);
            } else if (cv == Color.GRIS) {
                ciclos.add(reconstruirCiclo(u, v, padre));
            }
        }
        color.put(u, Color.NEGRO);
    }

    private List<String> reconstruirCiclo(String desde, String hasta,
                                          Map<String, String> padre) {
        LinkedList<String> ciclo = new LinkedList<>();
        ciclo.addFirst(hasta);
        String cursor = desde;
        while (cursor != null && !cursor.equals(hasta)) {
            ciclo.addFirst(cursor);
            cursor = padre.get(cursor);
        }
        ciclo.addFirst(hasta);
        return ciclo;
    }

    private enum Color { BLANCO, GRIS, NEGRO }
}
