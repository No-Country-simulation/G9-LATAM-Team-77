package com.financeia.financeia_backend.dto.catalogo;

public record MonedaResponse(
        Long id,
        String nombre,
        String codigo,
        String simbolo
) {
}
