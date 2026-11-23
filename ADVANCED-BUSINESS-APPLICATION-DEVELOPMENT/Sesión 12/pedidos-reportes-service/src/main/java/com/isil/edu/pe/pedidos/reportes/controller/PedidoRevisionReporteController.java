package com.isil.edu.pe.pedidos.reportes.controller;

import com.isil.edu.pe.pedidos.reportes.service.PedidoRevisionConsultaService;
import com.isil.edu.pe.pedidos.reportes.service.PedidoRevisionConsultaService.EstadisticasRevisiones;
import com.isil.edu.pe.pedidos.reportes.service.PedidoRevisionConsultaService.EstadoRevision;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/pedidos", produces = MediaType.APPLICATION_JSON_VALUE)
public class PedidoRevisionReporteController {

    private final PedidoRevisionConsultaService service;

    public PedidoRevisionReporteController(PedidoRevisionConsultaService service) {
        this.service = service;
    }

    @GetMapping("/revisiones")
    public List<PedidoRevisionResponse> listarRevisiones(
            @RequestParam("estado") EstadoRevision estado) {
        return service.listarPorEstado(estado).stream()
                .map(pedido -> new PedidoRevisionResponse(
                        pedido.id(),
                        pedido.cliente(),
                        pedido.productoCodigo(),
                        pedido.productoNombre(),
                        pedido.total(),
                        estado,
                        pedido.observacion(),
                        pedido.fechaRevision()))
                .toList();
    }

    @GetMapping("/estadisticas/revisiones")
    public EstadisticasRevisiones obtenerEstadisticas() {
        return service.obtenerEstadisticas();
    }

    public record PedidoRevisionResponse(
            long id,
            String cliente,
            String productoCodigo,
            String productoNombre,
            BigDecimal total,
            EstadoRevision estado,
            String observacion,
            LocalDateTime fechaRevision
    ) {}
}