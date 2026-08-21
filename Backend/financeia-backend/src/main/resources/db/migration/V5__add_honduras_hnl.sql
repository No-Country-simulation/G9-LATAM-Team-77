INSERT INTO paises (nombre, codigo)
SELECT 'Honduras', 'HN'
WHERE NOT EXISTS (
    SELECT 1
    FROM paises
    WHERE codigo = 'HN'
);

INSERT INTO monedas (nombre, codigo, simbolo)
SELECT 'Lempira hondureño', 'HNL', 'L'
WHERE NOT EXISTS (
    SELECT 1
    FROM monedas
    WHERE codigo = 'HNL'
);
