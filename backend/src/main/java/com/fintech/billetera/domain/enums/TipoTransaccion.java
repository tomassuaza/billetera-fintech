package com.fintech.billetera.domain.enums;

public enum TipoTransaccion {
    RECARGA,
    RETIRO,
    TRANSFERENCIA_INTERNA,   // entre billeteras del mismo usuario
    TRANSFERENCIA_EXTERNA,   // hacia billetera de otro usuario
    REVERSION                // operacion que deshace una previa
}
