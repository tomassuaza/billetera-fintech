package com.fintech.billetera.service;

import com.fintech.billetera.domain.Billetera;
import com.fintech.billetera.domain.Transaccion;
import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.domain.enums.EstadoTransaccion;
import com.fintech.billetera.domain.enums.TipoBilletera;
import com.fintech.billetera.domain.enums.TipoTransaccion;
import com.fintech.billetera.repository.BilleteraRepository;
import com.fintech.billetera.repository.TransaccionRepository;
import com.fintech.billetera.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reportes y consultas analiticas sobre el conjunto de transacciones.
 *
 * Es un servicio de SOLO LECTURA: recorre las estructuras existentes
 * (HashMap principal de transacciones, historiales por usuario y por
 * billetera) y agrega resultados sin modificar nada.
 *
 * Para "transacciones de mayor valor" usamos un TreeSet con un
 * Comparator que ordena por monto descendente. TreeSet mantiene los
 * elementos ordenados conforme se insertan (rojo-negro), por lo que
 * extraer el top N es recorrer en orden el inicio del arbol.
 */
@Service
public class AnaliticaService {

    private final TransaccionRepository transRepo;
    private final UsuarioRepository usuarioRepo;
    private final BilleteraRepository billeteraRepo;

    public AnaliticaService(TransaccionRepository transRepo,
                            UsuarioRepository usuarioRepo,
                            BilleteraRepository billeteraRepo) {
        this.transRepo = transRepo;
        this.usuarioRepo = usuarioRepo;
        this.billeteraRepo = billeteraRepo;
    }

    // -------------------- TOP USUARIOS / BILLETERAS --------------------

    /**
     * Usuarios con mas transacciones generadas (en orden descendente).
     * El conteo cuenta operaciones EXITOSAS para no premiar intentos
     * fallidos.
     */
    public List<EntradaConteo> topUsuariosActivos(int n) {
        Map<String, Integer> conteo = new HashMap<>();
        for (Transaccion t : transRepo.listar()) {
            if (t.getEstado() != EstadoTransaccion.EXITOSA) continue;
            if (t.getIdUsuarioGenerador() == null) continue;
            conteo.merge(t.getIdUsuarioGenerador(), 1, Integer::sum);
        }
        return conteo.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(n)
                .map(e -> {
                    String nombre = usuarioRepo.buscarPorId(e.getKey())
                            .map(Usuario::getNombre).orElse("(desconocido)");
                    return new EntradaConteo(e.getKey(), nombre, e.getValue());
                })
                .collect(Collectors.toList());
    }

    /**
     * Billeteras con mayor numero de transacciones donde aparecen como
     * origen o como destino.
     */
    public List<EntradaConteo> topBilleterasActivas(int n) {
        Map<String, Integer> conteo = new HashMap<>();
        for (Transaccion t : transRepo.listar()) {
            if (t.getEstado() != EstadoTransaccion.EXITOSA) continue;
            if (t.getIdBilleteraOrigen() != null) {
                conteo.merge(t.getIdBilleteraOrigen(), 1, Integer::sum);
            }
            if (t.getIdBilleteraDestino() != null
                    && !Objects.equals(t.getIdBilleteraDestino(), t.getIdBilleteraOrigen())) {
                conteo.merge(t.getIdBilleteraDestino(), 1, Integer::sum);
            }
        }
        return conteo.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(n)
                .map(e -> {
                    String nombre = billeteraRepo.buscarPorId(e.getKey())
                            .map(Billetera::getNombre).orElse("(eliminada)");
                    return new EntradaConteo(e.getKey(), nombre, e.getValue());
                })
                .collect(Collectors.toList());
    }

    // -------------------- DISTRIBUCIONES --------------------

    /**
     * Cuantas transacciones EXITOSAS hay de cada tipo (RECARGA, RETIRO,
     * TRANSFERENCIA, REVERSION).
     */
    public Map<TipoTransaccion, Integer> frecuenciaPorTipo() {
        Map<TipoTransaccion, Integer> conteo = new EnumMap<>(TipoTransaccion.class);
        for (TipoTransaccion tipo : TipoTransaccion.values()) conteo.put(tipo, 0);
        for (Transaccion t : transRepo.listar()) {
            if (t.getEstado() != EstadoTransaccion.EXITOSA) continue;
            conteo.merge(t.getTipo(), 1, Integer::sum);
        }
        return conteo;
    }

    /**
     * Cuantas billeteras existen de cada categoria. Util para entender
     * las preferencias del producto.
     */
    public Map<TipoBilletera, Integer> conteoPorCategoriaBilletera() {
        Map<TipoBilletera, Integer> conteo = new EnumMap<>(TipoBilletera.class);
        for (TipoBilletera cat : TipoBilletera.values()) conteo.put(cat, 0);
        for (Billetera b : billeteraRepo.listar()) {
            conteo.merge(b.getTipo(), 1, Integer::sum);
        }
        return conteo;
    }

    // -------------------- MONTO Y TOP VALOR --------------------

    /**
     * Monto total movilizado por tipo de transaccion dentro del rango
     * temporal indicado (incluyente en ambos extremos).
     */
    public Map<String, Object> montoMovilizadoEnRango(LocalDateTime desde,
                                                      LocalDateTime hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new RuntimeException("Rango invalido: 'desde' debe ser <= 'hasta'");
        }
        BigDecimal total = BigDecimal.ZERO;
        Map<TipoTransaccion, BigDecimal> porTipo = new EnumMap<>(TipoTransaccion.class);
        int contadas = 0;
        for (Transaccion t : transRepo.listar()) {
            if (t.getEstado() != EstadoTransaccion.EXITOSA) continue;
            LocalDateTime f = t.getFecha();
            if (f.isBefore(desde) || f.isAfter(hasta)) continue;
            total = total.add(t.getMonto());
            porTipo.merge(t.getTipo(), t.getMonto(), BigDecimal::add);
            contadas++;
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("desde", desde);
        resultado.put("hasta", hasta);
        resultado.put("totalTransacciones", contadas);
        resultado.put("montoTotal", total);
        resultado.put("porTipo", porTipo);
        return resultado;
    }

    /**
     * Top N transacciones por monto. Usa un TreeSet con Comparator
     * descendente por monto (desempata por id para tolerar repetidos)
     * de modo que la insercion mantiene el orden ordenado en O(log n)
     * por elemento.
     */
    public List<Transaccion> topTransaccionesPorValor(int n) {
        Comparator<Transaccion> porValorDesc = Comparator
                .comparing(Transaccion::getMonto).reversed()
                .thenComparing(Transaccion::getId);
        TreeSet<Transaccion> ordenadas = new TreeSet<>(porValorDesc);
        for (Transaccion t : transRepo.listar()) {
            if (t.getEstado() != EstadoTransaccion.EXITOSA) continue;
            ordenadas.add(t);
        }
        return ordenadas.stream().limit(n).collect(Collectors.toList());
    }

    // -------------------- DTO INTERNO --------------------

    public static class EntradaConteo {
        private final String id;
        private final String nombre;
        private final int cantidad;

        public EntradaConteo(String id, String nombre, int cantidad) {
            this.id = id;
            this.nombre = nombre;
            this.cantidad = cantidad;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public int getCantidad() { return cantidad; }
    }
}
