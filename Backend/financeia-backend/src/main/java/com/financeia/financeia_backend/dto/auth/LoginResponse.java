package com.financeia.financeia_backend.dto.auth;

public record LoginResponse(
        String token,
        Long userId,
        String nombre,
        String email
) {
}