package com.fintech.billetera.domain.enums;

/**
 * Niveles de fidelizacion segun la cantidad de puntos acumulados.
 * Cada nivel define un rango [min, max] de puntos.
 */
public enum NivelUsuario {
    BRONCE(0, 500),
    PLATA(501, 1000),
    ORO(1001, 5000),
    PLATINO(5001, Integer.MAX_VALUE);

    private final int min;
    private final int max;

    NivelUsuario(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() { return min; }
    public int getMax() { return max; }

    /**
     * Devuelve el nivel correspondiente a una cantidad de puntos.
     * Complejidad: O(1) - solo recorre los 4 niveles existentes.
     */
    public static NivelUsuario calcular(int puntos) {
        for (NivelUsuario nivel : values()) {
            if (puntos >= nivel.min && puntos <= nivel.max) return nivel;
        }
        return BRONCE;
    }
}
