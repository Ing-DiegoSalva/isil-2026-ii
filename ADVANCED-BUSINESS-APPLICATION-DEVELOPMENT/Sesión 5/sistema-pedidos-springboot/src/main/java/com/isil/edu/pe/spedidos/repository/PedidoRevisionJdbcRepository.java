package com.isil.edu.pe.spedidos.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoRevisionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public PedidoRevisionJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean marcarRevisadoSiPendiente(long pedidoId) {
        String sql = """
        UPDATE pedido
        SET revisado = TRUE
        WHERE id = ? AND revisado = FALSE
        """;
        return jdbcTemplate.update(sql, pedidoId) == 1;
    }

    public boolean existePedido(long pedidoId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM pedido WHERE id = ?)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, pedidoId));
    }

    public void insertarRevision(long pedidoId, String observacion) {
        String sql = """
        INSERT INTO pedido_revision (pedido_id, observacion)
        VALUES (?, ?)
        """;
        jdbcTemplate.update(sql, pedidoId, observacion);
    }
}
