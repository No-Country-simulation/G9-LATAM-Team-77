CREATE TABLE transacciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    category VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    usuario_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transacciones_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
);