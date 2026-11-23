package com.isil.edu.pe.spedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:revision_test;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class PedidoRevisionServiceTest {

    @Autowired
    private PedidoRevisionService revisionService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararEsquema() {
        jdbcTemplate.execute("""
        ALTER TABLE pedido
        ADD COLUMN IF NOT EXISTS revisado BOOLEAN NOT NULL DEFAULT FALSE
        """);

        jdbcTemplate.execute("""
        CREATE TABLE IF NOT EXISTS pedido_revision (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            pedido_id BIGINT NOT NULL UNIQUE REFERENCES pedido(id),
            observacion TEXT NOT NULL,
            fecha_revision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT ck_revision_observacion
                CHECK (observacion <> 'FALLO_PRUEBA')
        )
        """);
    }

    @Test
    void confirmaElEstadoYElRegistroDeRevision() {
        long pedidoId = crearPedidoPendiente();

        var resultado = revisionService.revisar(pedidoId, "Datos verificados");

        assertEquals(PedidoRevisionService.ResultadoRevision.REVISADO, resultado);
        assertEquals(Boolean.TRUE, consultarRevisado(pedidoId));
        assertEquals(1L, contarRevisiones(pedidoId));
        assertEquals("Datos verificados", jdbcTemplate.queryForObject(
                "SELECT observacion FROM pedido_revision WHERE pedido_id = ?",
                String.class, pedidoId));
    }

    @Test
    void revierteElEstadoCuandoFallaElInsertDeRevision() {
        long pedidoId = crearPedidoPendiente();

        var error = assertThrows(DataIntegrityViolationException.class,
                () -> revisionService.revisar(pedidoId, "FALLO_PRUEBA"));

        assertTrue(error.getMostSpecificCause().getMessage()
                .toLowerCase(Locale.ROOT).contains("ck_revision_observacion"));
        assertEquals(Boolean.FALSE, consultarRevisado(pedidoId));
        assertEquals(0L, contarRevisiones(pedidoId));
    }

    private long crearPedidoPendiente() {
        BigDecimal precio = new BigDecimal("100.00");
        Pedido pedido = new Pedido("Prueba PA4", "LAP-001", "Laptop", precio, 1, precio);
        return pedidoRepository.saveAndFlush(pedido).getId();
    }

    private Boolean consultarRevisado(long pedidoId) {
        return jdbcTemplate.queryForObject(
                "SELECT revisado FROM pedido WHERE id = ?", Boolean.class, pedidoId);
    }

    private Long contarRevisiones(long pedidoId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pedido_revision WHERE pedido_id = ?", Long.class, pedidoId);
    }
    @Test
    void conservaLaRevisionOriginalCuandoSeIntentaRevisarDosVeces() {
        long pedidoId = crearPedidoPendiente();
        revisionService.revisar(pedidoId, "Observación original");

        var resultado = revisionService.revisar(pedidoId, "Segunda observación");

        assertEquals(PedidoRevisionService.ResultadoRevision.YA_REVISADO, resultado);
        assertEquals(Boolean.TRUE, consultarRevisado(pedidoId));
        assertEquals(1L, contarRevisiones(pedidoId));
        assertEquals("Observación original", jdbcTemplate.queryForObject(
                "SELECT observacion FROM pedido_revision WHERE pedido_id = ?",
                String.class, pedidoId));
    }

    @Test
    void devuelvePedidoInexistenteSinCrearUnaRevision() {
        long pedidoId = Long.MAX_VALUE;

        var resultado = revisionService.revisar(pedidoId, "Datos verificados");

        assertEquals(PedidoRevisionService.ResultadoRevision.PEDIDO_INEXISTENTE, resultado);
        assertEquals(0L, contarRevisiones(pedidoId));
    }

    @Test
    void rechazaElIdInvalidoYLaObservacionEnBlanco() {
        long pedidoId = crearPedidoPendiente();

        assertThrows(IllegalArgumentException.class,
                () -> revisionService.revisar(0L, "Datos verificados"));
        assertThrows(IllegalArgumentException.class,
                () -> revisionService.revisar(pedidoId, "   "));

        assertEquals(Boolean.FALSE, consultarRevisado(pedidoId));
        assertEquals(0L, contarRevisiones(pedidoId));
    }
}
