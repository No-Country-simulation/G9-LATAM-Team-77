CREATE TABLE paises (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    codigo VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE monedas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    codigo VARCHAR(255) NOT NULL,
    simbolo VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    pais_id BIGINT,
    moneda_id BIGINT,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT fk_usuarios_pais FOREIGN KEY (pais_id) REFERENCES paises(id),
    CONSTRAINT fk_usuarios_moneda FOREIGN KEY (moneda_id) REFERENCES monedas(id)
);

CREATE TABLE transacciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    category VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transacciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

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

