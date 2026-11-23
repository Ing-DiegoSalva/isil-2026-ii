package com.isil.edu.pe.pedidos.reportes.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoRevisionConsultaJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public PedidoRevisionConsultaJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PedidoRevisionProjection> listarPorEstado(boolean revisado) {
        String sql = """
        SELECT p.id, p.cliente, p.producto_codigo, p.producto_nombre,
               p.total, p.revisado, r.observacion, r.fecha_revision
        FROM pedido p
        LEFT JOIN pedido_revision r ON r.pedido_id = p.id
        WHERE p.revisado = ?
        ORDER BY p.id DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PedidoRevisionProjection(
                rs.getLong("id"),
                rs.getString("cliente"),
                rs.getString("producto_codigo"),
                rs.getString("producto_nombre"),
                rs.getBigDecimal("total"),
                rs.getBoolean("revisado"),
                rs.getString("observacion"),
                rs.getObject("fecha_revision", LocalDateTime.class)
        ), revisado);
    }

    public ConteoRevisionesProjection contarRevisiones() {
        String sql = """
        SELECT COUNT(*) AS total_pedidos,
               COUNT(CASE WHEN revisado = TRUE THEN 1 END) AS total_revisados
        FROM pedido
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new ConteoRevisionesProjection(
                        rs.getLong("total_pedidos"),
                        rs.getLong("total_revisados")
                ));
    }

    public record PedidoRevisionProjection(
            long id,
            String cliente,
            String productoCodigo,
            String productoNombre,
            BigDecimal total,
            boolean revisado,
            String observacion,
            LocalDateTime fechaRevision
    ) {}

    public record ConteoRevisionesProjection(long totalPedidos, long totalRevisados) {}
}