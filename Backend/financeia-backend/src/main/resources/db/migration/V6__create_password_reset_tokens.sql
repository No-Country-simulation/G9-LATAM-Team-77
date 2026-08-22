CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    fecha_expiracion TIMESTAMP(6) NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_password_reset_tokens_hash UNIQUE (token_hash),

    CONSTRAINT fk_password_reset_tokens_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_usuario_estado
    ON password_reset_tokens (usuario_id, usado, fecha_expiracion);
