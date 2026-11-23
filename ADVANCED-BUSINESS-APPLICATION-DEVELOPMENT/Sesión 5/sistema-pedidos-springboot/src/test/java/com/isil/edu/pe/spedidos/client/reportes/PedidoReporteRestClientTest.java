package com.isil.edu.pe.spedidos.client.reportes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionReporteResponse.EstadoRevision;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PedidoReporteRestClientTest {

    private static final String BASE_URL =
            "http://localhost:8083/pedidos-reportes-service/api/v1/pedidos";

    private MockRestServiceServer servidor;
    private PedidoReporteRestClient cliente;

    @BeforeEach
    void prepararCliente() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new PedidoReporteRestClient(builder.build(), BASE_URL);
    }

    @Test
    void leeElListadoDePedidosRevisados() {
        servidor.expect(requestTo(BASE_URL + "/revisiones?estado=REVISADO"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
            [{
              "id": 1,
              "cliente": "Prueba-PA4",
              "productoCodigo": "LAP-001",
              "productoNombre": "Laptop",
              "total": 2500.00,
              "estado": "REVISADO",
              "observacion": "Datos verificados",
              "fechaRevision": "2026-11-22T17:30:00"
            }]
            """, MediaType.APPLICATION_JSON));

        var pedidos = cliente.listarRevisiones(EstadoRevision.REVISADO);

        assertEquals(1, pedidos.size());
        var pedido = pedidos.getFirst();
        assertEquals(1L, pedido.id());
        assertEquals(EstadoRevision.REVISADO, pedido.estado());
        assertEquals("Datos verificados", pedido.observacion());
        assertEquals(LocalDateTime.of(2026, 11, 22, 17, 30), pedido.fechaRevision());
        servidor.verify();
    }

    @Test
    void leeLasEstadisticasDeRevision() {
        servidor.expect(requestTo(BASE_URL + "/estadisticas/revisiones"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
            {
              "totalPedidos": 3,
              "totalRevisados": 1,
              "totalPendientes": 2,
              "porcentajeRevisado": 33.33
            }
            """, MediaType.APPLICATION_JSON));

        var estadisticas = cliente.obtenerEstadisticasRevisiones();

        assertEquals(3L, estadisticas.totalPedidos());
        assertEquals(1L, estadisticas.totalRevisados());
        assertEquals(2L, estadisticas.totalPendientes());
        assertEquals(new BigDecimal("33.33"), estadisticas.porcentajeRevisado());
        servidor.verify();
    }

    @Test
    void informaIndisponibilidadCuandoReportesDevuelve503() {
        servidor.expect(requestTo(BASE_URL + "/estadisticas/revisiones"))
                .andRespond(withServiceUnavailable());

        assertThrows(PedidoReporteNoDisponibleException.class,
                cliente::obtenerEstadisticasRevisiones);
        servidor.verify();
    }

    @Test
    void informaIndisponibilidadCuandoSeRechazaLaConexion() {
        verificarFalloDeConexion(new ConnectException("Conexión rechazada"));
    }

    @Test
    void informaIndisponibilidadCuandoSeAgotaElTiempoDeEspera() {
        verificarFalloDeConexion(new SocketTimeoutException("Tiempo de espera agotado"));
    }

    private void verificarFalloDeConexion(IOException causa) {
        servidor.expect(requestTo(BASE_URL + "/revisiones?estado=PENDIENTE"))
                .andRespond(request -> {
                    throw causa;
                });

        var error = assertThrows(PedidoReporteNoDisponibleException.class,
                () -> cliente.listarRevisiones(EstadoRevision.PENDIENTE));

        assertSame(causa, error.getCause().getCause());
        servidor.verify();
    }
}
