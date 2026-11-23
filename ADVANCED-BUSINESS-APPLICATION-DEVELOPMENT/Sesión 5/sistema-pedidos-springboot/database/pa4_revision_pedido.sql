USE pedidosdb;

ALTER TABLE pedido
    ADD COLUMN revisado BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE pedido_revision (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    observacion TEXT NOT NULL,
    fecha_revision DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_pedido_revision_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_pedido_revision_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id)
) ENGINE = InnoDB;
