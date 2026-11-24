package com.isil.edu.pe.pedidos.reportes.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:contrato_revisiones;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
@Transactional
class PedidoRevisionReporteControllerTest {

    private static final String PEDIDOS = "/api/v1/pedidos";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void prepararPedidos() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        jdbcTemplate.update("""
        INSERT INTO pedido (
            id, cliente, producto_codigo, producto_nombre, precio_unitario,
            cantidad, total, fecha, revisado
        ) VALUES
            (101, 'Ana', 'LAP-001', 'Laptop', 2500.00, 1, 2500.00, CURRENT_TIMESTAMP, FALSE),
            (102, 'Luis', 'LAP-001', 'Laptop', 2500.00, 1, 2500.00, CURRENT_TIMESTAMP, TRUE),
            (103, 'Rosa', 'LAP-001', 'Laptop', 2500.00, 1, 2500.00, CURRENT_TIMESTAMP, FALSE)
        """);

        jdbcTemplate.update("""
        INSERT INTO pedido_revision (pedido_id, observacion, fecha_revision)
        VALUES (102, 'Datos verificados', TIMESTAMP '2026-11-22 17:30:45')
        """);
    }

    @Test
    void listaSoloLosPedidosPendientes() throws Exception {
        mockMvc.perform(get(PEDIDOS + "/revisiones").param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[1].estado").value("PENDIENTE"));
    }

    @Test
    void listaElPedidoRevisadoConObservacionYFecha() throws Exception {
        mockMvc.perform(get(PEDIDOS + "/revisiones").param("estado", "REVISADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(102))
                .andExpect(jsonPath("$[0].estado").value("REVISADO"))
                .andExpect(jsonPath("$[0].observacion").value("Datos verificados"))
                .andExpect(jsonPath("$[0].fechaRevision").value("2026-11-22T17:30:45"));
    }

    @Test
    void devuelveLasEstadisticasDeRevision() throws Exception {
        mockMvc.perform(get(PEDIDOS + "/estadisticas/revisiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPedidos").value(3))
                .andExpect(jsonPath("$.totalRevisados").value(1))
                .andExpect(jsonPath("$.totalPendientes").value(2))
                .andExpect(jsonPath("$.porcentajeRevisado").value(33.33));
    }

    @Test
    void rechazaElEstadoInvalido() throws Exception {
        mockMvc.perform(get(PEDIDOS + "/revisiones").param("estado", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }
}