package com.fintech.billetera.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Convierte las excepciones de negocio en respuestas HTTP claras.
 * Sin esta clase, todas las excepciones devolverian 500 generico.
 *
 * Asi el frontend recibe codigo 400 con un JSON {"error": "mensaje"}.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
        Map<String, String> body = new HashMap<>();
        body.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(body);
    }
}
