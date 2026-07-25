package com.financeia.financeia_backend.dto.auth;

public record RegistroRequest(
        String nombre,
        String email,
        String password,
        Long countryId,
        Long currencyId
) {
}
