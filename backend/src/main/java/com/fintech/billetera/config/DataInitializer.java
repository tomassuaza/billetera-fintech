package com.fintech.billetera.config;

import com.fintech.billetera.domain.Billetera;
import com.fintech.billetera.domain.Usuario;
import com.fintech.billetera.domain.enums.TipoBilletera;
import com.fintech.billetera.domain.enums.TipoTransaccion;
import com.fintech.billetera.repository.UsuarioRepository;
import com.fintech.billetera.service.BilleteraService;
import com.fintech.billetera.service.ProgramacionService;
import com.fintech.billetera.service.TransaccionService;
import com.fintech.billetera.service.UsuarioService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Carga un conjunto de datos de demostracion al arrancar el sistema.
 *
 * Como toda la persistencia es en memoria, cada arranque empieza vacio.
 * Este inicializador crea tres usuarios con billeteras, varias
 * transacciones (incluyendo un ciclo en el grafo) y dos operaciones
 * programadas (una vencida, una futura) para que el evaluador encuentre
 * la aplicacion lista con contenido visible en cada modulo.
 *
 * Es idempotente: si ya hay usuarios registrados (por ejemplo, si la
 * aplicacion arranca con un estado precargado), no hace nada.
 */
@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner cargarDatosDemo(UsuarioRepository usuarioRepo,
                                             UsuarioService usuarioService,
                                             BilleteraService billeteraService,
                                             TransaccionService transaccionService,
                                             ProgramacionService programacionService) {
        return args -> {
            if (!usuarioRepo.listar().isEmpty()) return;

            // ---------- Usuarios ----------
            Usuario andres = usuarioService.registrar(
                    "Andres Herradura", "andres.herradura@correo.com");
            Usuario gayron = usuarioService.registrar(
                    "Gayron Prieto", "gayron.prieto@correo.com");
            Usuario susanita = usuarioService.registrar(
                    "Susanita Perez", "susanita.perez@correo.com");

            // ---------- Billeteras ----------
            Billetera andresAhorro = billeteraService.crear(
                    andres.getId(), "Ahorro principal", TipoBilletera.AHORRO);
            Billetera andresTransporte = billeteraService.crear(
                    andres.getId(), "Transporte", TipoBilletera.TRANSPORTE);

            Billetera gayronCompras = billeteraService.crear(
                    gayron.getId(), "Compras", TipoBilletera.COMPRAS);
            Billetera gayronGastos = billeteraService.crear(
                    gayron.getId(), "Gastos diarios", TipoBilletera.GASTOS_DIARIOS);

            Billetera susanaInversion = billeteraService.crear(
                    susanita.getId(), "Inversion", TipoBilletera.INVERSION);

            // ---------- Recargas iniciales ----------
            transaccionService.recargar(andresAhorro.getId(),     new BigDecimal("200000"));
            transaccionService.recargar(andresTransporte.getId(), new BigDecimal("50000"));
            transaccionService.recargar(gayronCompras.getId(),    new BigDecimal("150000"));
            transaccionService.recargar(gayronGastos.getId(),     new BigDecimal("80000"));
            transaccionService.recargar(susanaInversion.getId(),  new BigDecimal("300000"));

            // ---------- Transferencia interna (no entra al grafo) ----------
            transaccionService.transferir(
                    andresAhorro.getId(), andresTransporte.getId(),
                    new BigDecimal("10000"));

            // ---------- Transferencias externas (alimentan el grafo) ----------
            // Andres -> Gayron, dos envios para acumular peso y conteo
            transaccionService.transferir(
                    andresAhorro.getId(), gayronCompras.getId(),
                    new BigDecimal("30000"));
            transaccionService.transferir(
                    andresAhorro.getId(), gayronCompras.getId(),
                    new BigDecimal("12000"));

            // Gayron -> Susanita
            transaccionService.transferir(
                    gayronGastos.getId(), susanaInversion.getId(),
                    new BigDecimal("15000"));

            // Susanita -> Andres (cierra el ciclo Andres -> Gayron -> Susanita -> Andres)
            transaccionService.transferir(
                    susanaInversion.getId(), andresAhorro.getId(),
                    new BigDecimal("20000"));

            // ---------- Operaciones programadas ----------
            // Una ya vencida, para que el evaluador la pueda "Ejecutar"
            programacionService.programar(
                    TipoTransaccion.RECARGA,
                    new BigDecimal("25000"),
                    null,
                    gayronGastos.getId(),
                    gayron.getId(),
                    LocalDateTime.now().minusHours(1));

            // Una futura, queda pendiente en la cola de prioridad
            programacionService.programar(
                    TipoTransaccion.RECARGA,
                    new BigDecimal("40000"),
                    null,
                    susanaInversion.getId(),
                    susanita.getId(),
                    LocalDateTime.now().plusHours(2));
        };
    }
}
