package com.fintech.billetera.service;

import com.fintech.billetera.domain.EventoAuditoria;
import com.fintech.billetera.domain.Transaccion;
import com.fintech.billetera.domain.enums.EstadoTransaccion;
import com.fintech.billetera.domain.enums.NivelRiesgo;
import com.fintech.billetera.domain.enums.TipoTransaccion;
import com.fintech.billetera.repository.AuditoriaRepository;
import com.fintech.billetera.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Modulo de deteccion de patrones inusuales. Se ejecuta una vez por
 * transaccion recien creada y aplica un conjunto de reglas sobre el
 * historial reciente del usuario para detectar comportamientos atipicos.
 *
 * Cuando una regla se activa:
 *  - Sube el nivel de riesgo de la transaccion (NINGUNO -> BAJO/MEDIO/ALTO).
 *  - Registra un EventoAuditoria con la regla y el detalle.
 *  - Encola una notificacion FRAUDE_DETECTADO al usuario.
 *
 * Las reglas no rechazan la transaccion (la dejan pasar para que el
 * usuario o un revisor humano decida). Solo la etiquetan y notifican.
 */
@Service
public class FraudeService {

    /** Cuantas transferencias en una ventana corta levantan alerta. */
    public static final int LIMITE_TRANSFERENCIAS_RAFAGA = 4;
    public static final int VENTANA_RAFAGA_MINUTOS = 5;

    /** Cuantos envios al mismo destino en intervalo corto son sospechosos. */
    public static final int LIMITE_MISMO_DESTINO = 3;
    public static final int VENTANA_MISMO_DESTINO_MINUTOS = 10;

    /** Factor sobre el promedio del usuario que se considera atipico. */
    public static final BigDecimal FACTOR_MONTO_ATIPICO = new BigDecimal("5");

    /** Cuantas billeteras distintas usadas hacia un mismo destino en
     *  poco tiempo levantan sospecha de fragmentacion. */
    public static final int LIMITE_FRAGMENTACION = 2;
    public static final int VENTANA_FRAGMENTACION_MINUTOS = 10;

    private final TransaccionRepository transRepo;
    private final AuditoriaRepository auditoriaRepo;
    private final NotificacionService notificacionService;

    public FraudeService(TransaccionRepository transRepo,
                         AuditoriaRepository auditoriaRepo,
                         NotificacionService notificacionService) {
        this.transRepo = transRepo;
        this.auditoriaRepo = auditoriaRepo;
        this.notificacionService = notificacionService;
    }

    /**
     * Aplica todas las reglas sobre la transaccion recien creada. Si
     * alguna se activa, mutar la transaccion (nivel + motivo) y emitir
     * los efectos colaterales.
     */
    public void analizar(Transaccion t) {
        if (t == null || t.getIdUsuarioGenerador() == null) return;

        List<Transaccion> historial = transRepo.historialUsuario(t.getIdUsuarioGenerador());
        List<String> motivos = new ArrayList<>();
        NivelRiesgo nivel = NivelRiesgo.NINGUNO;

        // Regla 1: rafaga de transferencias en ventana corta
        long enRafaga = transferenciasRecientes(historial, t, VENTANA_RAFAGA_MINUTOS);
        if (enRafaga >= LIMITE_TRANSFERENCIAS_RAFAGA) {
            nivel = subir(nivel, NivelRiesgo.MEDIO);
            motivos.add(enRafaga + " transferencias en " + VENTANA_RAFAGA_MINUTOS + " min");
        }

        // Regla 2: monto atipico vs promedio del usuario
        BigDecimal promedio = promedioHistorico(historial, t);
        if (promedio.signum() > 0
                && t.getMonto().compareTo(promedio.multiply(FACTOR_MONTO_ATIPICO)) > 0) {
            nivel = subir(nivel, NivelRiesgo.BAJO);
            motivos.add("monto " + FACTOR_MONTO_ATIPICO + "x mayor al promedio del usuario");
        }

        // Regla 3: muchos envios al mismo destino en intervalo corto
        if (esTransferencia(t.getTipo()) && t.getIdBilleteraDestino() != null) {
            long mismoDestino = enviosAlMismoDestino(historial, t,
                    VENTANA_MISMO_DESTINO_MINUTOS);
            if (mismoDestino >= LIMITE_MISMO_DESTINO) {
                nivel = subir(nivel, NivelRiesgo.MEDIO);
                motivos.add(mismoDestino + " envios al mismo destino en "
                        + VENTANA_MISMO_DESTINO_MINUTOS + " min");
            }
        }

        // Regla 4: fragmentacion (varias billeteras propias hacia el mismo destino)
        if (esTransferencia(t.getTipo()) && t.getIdBilleteraDestino() != null) {
            int billeterasUsadas = billeterasOrigenDistintas(historial, t,
                    VENTANA_FRAGMENTACION_MINUTOS);
            if (billeterasUsadas >= LIMITE_FRAGMENTACION) {
                nivel = subir(nivel, NivelRiesgo.ALTO);
                motivos.add("fragmentacion: " + billeterasUsadas
                        + " billeteras propias hacia el mismo destino");
            }
        }

        if (nivel == NivelRiesgo.NINGUNO) return;

        String motivo = String.join("; ", motivos);
        t.setNivelRiesgo(nivel);
        t.setMotivoRiesgo(motivo);
        transRepo.guardar(t);

        EventoAuditoria evento = new EventoAuditoria(
                "AUD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                t.getId(),
                t.getIdUsuarioGenerador(),
                "deteccion-automatica",
                nivel,
                motivo
        );
        auditoriaRepo.registrar(evento);
        notificacionService.emitirFraudeDetectado(
                t.getIdUsuarioGenerador(), t.getId(), motivo);
    }

    // -------------------- HELPERS DE REGLAS --------------------

    private long transferenciasRecientes(List<Transaccion> historial,
                                         Transaccion actual, int minutos) {
        LocalDateTime desde = actual.getFecha().minusMinutes(minutos);
        long cuenta = 0;
        for (Transaccion h : historial) {
            if (h.getEstado() != EstadoTransaccion.EXITOSA) continue;
            if (!esTransferencia(h.getTipo())) continue;
            if (h.getFecha().isBefore(desde)) continue;
            cuenta++;
        }
        return cuenta;
    }

    private BigDecimal promedioHistorico(List<Transaccion> historial,
                                         Transaccion actual) {
        BigDecimal total = BigDecimal.ZERO;
        int cuenta = 0;
        for (Transaccion h : historial) {
            if (h.getId().equals(actual.getId())) continue;
            if (h.getEstado() != EstadoTransaccion.EXITOSA) continue;
            if (h.getTipo() == TipoTransaccion.REVERSION) continue;
            total = total.add(h.getMonto());
            cuenta++;
        }
        if (cuenta == 0) return BigDecimal.ZERO;
        return total.divide(BigDecimal.valueOf(cuenta), 2, java.math.RoundingMode.HALF_UP);
    }

    private long enviosAlMismoDestino(List<Transaccion> historial,
                                      Transaccion actual, int minutos) {
        LocalDateTime desde = actual.getFecha().minusMinutes(minutos);
        long cuenta = 0;
        for (Transaccion h : historial) {
            if (h.getEstado() != EstadoTransaccion.EXITOSA) continue;
            if (!esTransferencia(h.getTipo())) continue;
            if (h.getFecha().isBefore(desde)) continue;
            if (!Objects.equals(h.getIdBilleteraDestino(), actual.getIdBilleteraDestino())) continue;
            cuenta++;
        }
        return cuenta;
    }

    private int billeterasOrigenDistintas(List<Transaccion> historial,
                                          Transaccion actual, int minutos) {
        LocalDateTime desde = actual.getFecha().minusMinutes(minutos);
        Set<String> origenes = new HashSet<>();
        for (Transaccion h : historial) {
            if (h.getEstado() != EstadoTransaccion.EXITOSA) continue;
            if (!esTransferencia(h.getTipo())) continue;
            if (h.getFecha().isBefore(desde)) continue;
            if (!Objects.equals(h.getIdBilleteraDestino(), actual.getIdBilleteraDestino())) continue;
            if (h.getIdBilleteraOrigen() != null) origenes.add(h.getIdBilleteraOrigen());
        }
        return origenes.size();
    }

    private boolean esTransferencia(TipoTransaccion tipo) {
        return tipo == TipoTransaccion.TRANSFERENCIA_INTERNA
                || tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA;
    }

    private NivelRiesgo subir(NivelRiesgo actual, NivelRiesgo nuevo) {
        return actual.ordinal() >= nuevo.ordinal() ? actual : nuevo;
    }

    // -------------------- CONSULTAS --------------------

    public List<EventoAuditoria> historial() {
        return auditoriaRepo.listarTodo();
    }

    public List<EventoAuditoria> historialDe(String idUsuario) {
        return auditoriaRepo.listarPorUsuario(idUsuario);
    }

    public int totalEventos() {
        return auditoriaRepo.tamano();
    }
}
