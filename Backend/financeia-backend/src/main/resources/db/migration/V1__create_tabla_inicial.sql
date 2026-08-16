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

    CONSTRAINT fk_usuarios_pais
        FOREIGN KEY (pais_id)
        REFERENCES paises(id),

    CONSTRAINT fk_usuarios_moneda
        FOREIGN KEY (moneda_id)
        REFERENCES monedas(id)
);