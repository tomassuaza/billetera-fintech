package com.fintech.billetera.domain.enums;

/**
 * Categoria de la notificacion. Cada tipo se origina en un evento
 * distinto del sistema:
 *
 *  - SALDO_BAJO: la billetera quedo por debajo de un umbral despues de
 *    una operacion (retiro o transferencia saliente).
 *  - ASCENSO_NIVEL: el usuario subio de NivelUsuario por puntos acumulados.
 *  - OPERACION_RECHAZADA: una operacion fallo (saldo insuficiente,
 *    billetera inactiva, etc).
 *  - PROGRAMADA_EJECUTADA: una OperacionProgramada se ejecuto exitosamente.
 *  - PROGRAMADA_FALLIDA: una OperacionProgramada fallo al ejecutarse.
 *  - FRAUDE_DETECTADO: el modulo de auditoria marco una transaccion
 *    como sospechosa.
 *  - BIENVENIDA: se emite al registrar un usuario nuevo.
 */
public enum TipoNotificacion {
    SALDO_BAJO,
    ASCENSO_NIVEL,
    OPERACION_RECHAZADA,
    PROGRAMADA_EJECUTADA,
    PROGRAMADA_FALLIDA,
    FRAUDE_DETECTADO,
    BIENVENIDA
}
