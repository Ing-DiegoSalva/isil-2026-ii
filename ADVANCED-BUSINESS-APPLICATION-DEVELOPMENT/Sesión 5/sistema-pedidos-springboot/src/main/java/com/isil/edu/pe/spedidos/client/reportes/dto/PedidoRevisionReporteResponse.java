package com.isil.edu.pe.spedidos.client.reportes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoRevisionReporteResponse(
        long id,
        String cliente,
        String productoCodigo,
        String productoNombre,
        BigDecimal total,
        EstadoRevision estado,
        String observacion,
        LocalDateTime fechaRevision
) {
    public enum EstadoRevision {
        PENDIENTE,
        REVISADO
    }
}
