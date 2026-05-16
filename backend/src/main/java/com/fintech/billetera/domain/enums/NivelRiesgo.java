package com.fintech.billetera.domain.enums;

/**
 * Calificacion de riesgo asignada a una transaccion por el modulo de
 * deteccion de patrones inusuales.
 *
 *  - NINGUNO: la transaccion no encendio ninguna regla.
 *  - BAJO: encendio una regla suave (ej. monto algo elevado).
 *  - MEDIO: encendio una regla relevante (ej. frecuencia alta).
 *  - ALTO: encendio una regla critica (ej. fragmentacion entre
 *    billeteras del mismo usuario hacia un mismo destino).
 *
 * Si una transaccion enciende varias reglas, se queda con el nivel mas
 * alto resultante.
 */
public enum NivelRiesgo {
    NINGUNO,
    BAJO,
    MEDIO,
    ALTO
}
