package com.isil.edu.pe.pedidos.reportes.service;

import com.isil.edu.pe.pedidos.reportes.repository.PedidoRevisionConsultaJdbcRepository;
import com.isil.edu.pe.pedidos.reportes.repository.PedidoRevisionConsultaJdbcRepository.PedidoRevisionProjection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PedidoRevisionConsultaService {

    private final PedidoRevisionConsultaJdbcRepository repository;

    public PedidoRevisionConsultaService(
            PedidoRevisionConsultaJdbcRepository repository) {
        this.repository = repository;
    }

    public List<PedidoRevisionProjection> listarPorEstado(EstadoRevision estado) {
        return repository.listarPorEstado(estado == EstadoRevision.REVISADO);
    }

    public EstadisticasRevisiones obtenerEstadisticas() {
        var conteo = repository.contarRevisiones();
        long total = conteo.totalPedidos();
        long revisados = conteo.totalRevisados();
        long pendientes = total - revisados;

        BigDecimal porcentaje = total == 0
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(revisados)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        return new EstadisticasRevisiones(total, revisados, pendientes, porcentaje);
    }

    public enum EstadoRevision {
        PENDIENTE,
        REVISADO
    }

    public record EstadisticasRevisiones(
            long totalPedidos,
            long totalRevisados,
            long totalPendientes,
            BigDecimal porcentajeRevisado
    ) {}
}