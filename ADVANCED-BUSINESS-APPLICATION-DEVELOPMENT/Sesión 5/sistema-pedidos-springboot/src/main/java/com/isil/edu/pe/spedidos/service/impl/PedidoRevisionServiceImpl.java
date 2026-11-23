package com.isil.edu.pe.spedidos.service.impl;


import com.isil.edu.pe.spedidos.repository.PedidoRevisionJdbcRepository;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoRevisionServiceImpl implements PedidoRevisionService {

    private final PedidoRevisionJdbcRepository repository;

    public PedidoRevisionServiceImpl(PedidoRevisionJdbcRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ResultadoRevision revisar(long pedidoId, String observacion) {
        if (pedidoId <= 0) {
            throw new IllegalArgumentException("El ID del pedido debe ser mayor que cero.");
        }
        if (observacion == null || observacion.isBlank()) {
            throw new IllegalArgumentException("La observación es obligatoria.");
        }

        if (!repository.marcarRevisadoSiPendiente(pedidoId)) {
            return repository.existePedido(pedidoId)
                    ? ResultadoRevision.YA_REVISADO
                    : ResultadoRevision.PEDIDO_INEXISTENTE;
        }

        repository.insertarRevision(pedidoId, observacion.strip());
        return ResultadoRevision.REVISADO;
    }
}
