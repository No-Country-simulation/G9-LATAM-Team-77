CREATE TABLE historial_analisis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    ingreso_mensual DECIMAL(15, 2),
    nivel_endeudamiento DECIMAL(15, 2),
    frecuencia_ahorro VARCHAR(255),
    total_gastos DECIMAL(15, 2),
    ahorro_estimado DECIMAL(15, 2),
    score_financiero VARCHAR(255),
    resumen_categorias TEXT,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
