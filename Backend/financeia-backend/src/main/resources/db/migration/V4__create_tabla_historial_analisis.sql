CREATE TABLE historial_analisis (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    fecha_hora TIMESTAMP(6) NOT NULL,
    perfil_financiero VARCHAR(100) NOT NULL,
    probabilidad DECIMAL(10,6) NOT NULL,
    ingreso_mensual DECIMAL(19,2) NOT NULL,
    total_gastos DECIMAL(19,2) NOT NULL,
    ahorro_estimado DECIMAL(19,2) NOT NULL,
    nivel_endeudamiento DECIMAL(10,2) NOT NULL,
    frecuencia_ahorro VARCHAR(50) NOT NULL,
    resumen_gastos TEXT,
    recomendaciones TEXT,

    PRIMARY KEY (id),

    CONSTRAINT fk_historial_analisis_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
);

CREATE INDEX idx_historial_analisis_usuario_fecha
    ON historial_analisis (usuario_id, fecha_hora);
