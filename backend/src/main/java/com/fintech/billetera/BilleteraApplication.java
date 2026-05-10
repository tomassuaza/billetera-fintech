package com.fintech.billetera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion fintech de billeteras digitales.
 *
 * Este sistema demuestra el uso de las siguientes estructuras de datos:
 * - HashMap (UsuarioRepository, BilleteraRepository, TransaccionRepository)
 * - LinkedList (Historial de transacciones por billetera y por usuario)
 * - ArrayDeque como Pila (Operaciones reversibles)
 * - ArrayDeque como Cola (Notificaciones - dia 9)
 * - PriorityQueue (Operaciones programadas)
 * - TreeMap (Ranking de usuarios por puntos)
 * - Grafo dirigido ponderado (Red de transferencias - dia 10)
 */
@SpringBootApplication
public class BilleteraApplication {
    public static void main(String[] args) {
        SpringApplication.run(BilleteraApplication.class, args);
    }
}
