package com.financeia.financeia_backend.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
