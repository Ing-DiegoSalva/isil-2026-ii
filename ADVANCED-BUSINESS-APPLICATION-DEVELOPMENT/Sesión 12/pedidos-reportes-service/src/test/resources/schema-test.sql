CREATE TABLE pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente VARCHAR(120) NOT NULL,
    producto_codigo VARCHAR(30) NOT NULL,
    producto_nombre VARCHAR(120) NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    cantidad INT NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    fecha TIMESTAMP NOT NULL
);