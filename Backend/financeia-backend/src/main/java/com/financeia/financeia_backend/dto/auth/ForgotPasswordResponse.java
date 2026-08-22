package com.financeia.financeia_backend.dto.auth;

public record ForgotPasswordResponse(
        String email,
        String resetToken,
        String message
) {}
