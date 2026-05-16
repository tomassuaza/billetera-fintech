package com.fintech.billetera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion fintech de billeteras digitales.
 *
 * Este sistema demuestra el uso de las siguientes estructuras de datos:
 * - HashMap (UsuarioRepository, BilleteraRepository, TransaccionRepository,
 *   NotificacionRepository) para acceso por id en O(1).
 * - LinkedList con addFirst para el historial de transacciones por billetera
 *   y por usuario (orden cronologico inverso, mas reciente primero).
 * - ArrayDeque como Pila para las operaciones reversibles por usuario.
 * - LinkedList como Cola FIFO para el buzon de notificaciones por usuario
 *   (offer/poll en O(1)).
 * - PriorityQueue (heap binario) para las operaciones programadas, ordenadas
 *   por fecha de ejecucion.
 * - TreeMap (arbol rojo-negro) para el ranking de usuarios por puntos, con
 *   consultas por rango en O(log n + k).
 * - Grafo dirigido y ponderado (lista de adyacencia) para la red de
 *   transferencias entre usuarios. Soporta BFS, deteccion de ciclos y
 *   busqueda de rutas frecuentes.
 */
@SpringBootApplication
public class BilleteraApplication {
    public static void main(String[] args) {
        SpringApplication.run(BilleteraApplication.class, args);
    }
}
