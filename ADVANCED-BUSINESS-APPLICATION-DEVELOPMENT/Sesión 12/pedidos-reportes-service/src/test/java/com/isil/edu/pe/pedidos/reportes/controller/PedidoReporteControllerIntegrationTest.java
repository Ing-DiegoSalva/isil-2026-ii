package com.isil.edu.pe.pedidos.reportes.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoReporteControllerIntegrationTest {

    private static final String PEDIDOS = "/api/v1/pedidos";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        jdbcTemplate.update("DELETE FROM pedido");
        insertarPedido("Ana Torres", "LAP-001", "Laptop", "2500.00", 2);
        insertarPedido("Carlos Perez", "MOU-001", "Mouse", "50.00", 3);
    }

    @Test
    void devuelveResumenDePedidosEnJson() throws Exception {
        mockMvc.perform(get(PEDIDOS + "/estadisticas/resumen"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPedidos").value(2))
                .andExpect(jsonPath("$.totalUnidades").value(5));
    }

    @Test
    void filtraPedidosPorCliente() throws Exception {
        mockMvc.perform(get(PEDIDOS).param("cliente", "Ana"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cliente").value("Ana Torres"));
    }

    @Test
    void devuelveProductosOrdenadosPorUnidadesVendidas() throws Exception {
        mockMvc.perform(get(PEDIDOS + "/estadisticas/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productoCodigo").value("MOU-001"))
                .andExpect(jsonPath("$[0].totalUnidades").value(3))
                .andExpect(jsonPath("$[1].productoCodigo").value("LAP-001"));
    }

    @Test
    void rechazaUnIdentificadorNoNumerico() throws Exception {
        mockMvc.perform(get(PEDIDOS + "/buscar"))
                .andExpect(status().isBadRequest());
    }

    private void insertarPedido(String cliente, String codigo, String producto,
                                String precio, int cantidad) {
        BigDecimal precioUnitario = new BigDecimal(precio);
        BigDecimal total = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

        jdbcTemplate.update("""
        INSERT INTO pedido (
          cliente, producto_codigo, producto_nombre, precio_unitario,
          cantidad, total, fecha
        ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """, cliente, codigo, producto, precioUnitario, cantidad, total);
    }
}
