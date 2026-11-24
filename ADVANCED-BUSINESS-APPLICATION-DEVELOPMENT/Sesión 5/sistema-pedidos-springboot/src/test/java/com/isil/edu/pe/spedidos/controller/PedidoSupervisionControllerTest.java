package com.isil.edu.pe.spedidos.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.dto.EstadisticasRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionReporteResponse.EstadoRevision;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService.ResultadoRevision;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class PedidoSupervisionControllerTest {

    private static final String SUPERVISION = "/supervision/pedidos";
    private static final String REVISAR = SUPERVISION + "/1/revisar";
    private static final String OBSERVACION = "Datos del pedido verificados";

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PedidoReporteClient reportes;

    @MockitoBean
    private PedidoRevisionService revisiones;

    private MockMvc mockMvc;

    @BeforeEach
    void prepararMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void permiteConsultarLaSupervisionConElRolConsulta() throws Exception {
        var estadisticas = new EstadisticasRevisionResponse(0, 0, 0, BigDecimal.ZERO);
        when(reportes.listarRevisiones(EstadoRevision.PENDIENTE)).thenReturn(List.of());
        when(reportes.obtenerEstadisticasRevisiones()).thenReturn(estadisticas);

        mockMvc.perform(get(SUPERVISION).with(user("consulta").roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(view().name("supervision-pedidos"))
                .andExpect(model().attribute("estado", EstadoRevision.PENDIENTE))
                .andExpect(model().attribute("pedidos", List.of()))
                .andExpect(model().attribute("estadisticas", estadisticas));
    }

    @Test
    void permiteRevisarComoOperadorYRedirigeAlListadoDeRevisados() throws Exception {
        when(revisiones.revisar(1L, OBSERVACION)).thenReturn(ResultadoRevision.REVISADO);

        mockMvc.perform(post(REVISAR)
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .param("observacion", OBSERVACION))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SUPERVISION + "?estado=REVISADO"))
                .andExpect(flash().attributeExists("mensaje"));

        verify(revisiones).revisar(1L, OBSERVACION);
    }

    @Test
    void rechazaLaRevisionConElRolConsultaAunqueTengaCsrf() throws Exception {
        mockMvc.perform(post(REVISAR)
                        .with(user("consulta").roles("CONSULTA"))
                        .with(csrf())
                        .param("observacion", OBSERVACION))
                .andExpect(status().isForbidden());

        verifyNoInteractions(revisiones);
    }

    @Test
    void rechazaLaRevisionDelOperadorSinCsrf() throws Exception {
        mockMvc.perform(post(REVISAR)
                        .with(user("operador").roles("OPERADOR"))
                        .param("observacion", OBSERVACION))
                .andExpect(status().isForbidden());

        verifyNoInteractions(revisiones);
    }
}
