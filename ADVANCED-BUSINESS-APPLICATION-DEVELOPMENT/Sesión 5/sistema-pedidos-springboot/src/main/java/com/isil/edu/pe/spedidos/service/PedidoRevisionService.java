package com.isil.edu.pe.spedidos.service;

public interface PedidoRevisionService {

    ResultadoRevision revisar(long pedidoId, String observacion);

    enum ResultadoRevision {
        REVISADO,
        PEDIDO_INEXISTENTE,
        YA_REVISADO
    }
}
