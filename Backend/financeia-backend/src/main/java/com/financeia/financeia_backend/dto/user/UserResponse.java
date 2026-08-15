package com.financeia.financeia_backend.dto.user;

public record UserResponse(
        Long id,
        String nombre,
        String email,
        Long paisId,
        String pais,
        Long monedaId,
        String moneda,
        String codigoMoneda,
        String simboloMoneda
) {
}