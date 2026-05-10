package com.fintech.billetera.service;

import com.fintech.billetera.domain.enums.TipoTransaccion;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Politica de asignacion de puntos por tipo de transaccion.
 *
 * Reglas (segun el enunciado del proyecto):
 * - Recarga: 1 punto por cada 100 unidades
 * - Retiro: 2 puntos por cada 100 unidades
 * - Transferencia (interna o externa): 3 puntos por cada 100 unidades
 * - Bono por pago programado ejecutado exitosamente: +10 puntos extra
 *
 * Todos los calculos usan division entera hacia abajo: 250 unidades de
 * recarga generan 2 puntos (no 2.5).
 */
public final class PoliticaPuntos {

    private static final BigDecimal CIEN = new BigDecimal(100);
    public static final int BONO_PROGRAMADA = 10;

    private PoliticaPuntos() {}

    /**
     * Calcula los puntos generados por una transaccion segun su tipo y monto.
     */
    public static int calcular(TipoTransaccion tipo, BigDecimal monto) {
        int factor = switch (tipo) {
            case RECARGA -> 1;
            case RETIRO -> 2;
            case TRANSFERENCIA_INTERNA, TRANSFERENCIA_EXTERNA -> 3;
            default -> 0;
        };
        if (factor == 0 || monto == null) return 0;
        int unidadesDeCien = monto.divide(CIEN, 0, RoundingMode.DOWN).intValue();
        return unidadesDeCien * factor;
    }
}
