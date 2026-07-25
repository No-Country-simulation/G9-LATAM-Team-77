package com.financeia.financeia_backend.dto.auth;

public record LoginResponse(
        String token,
        String tipo
) {
}
