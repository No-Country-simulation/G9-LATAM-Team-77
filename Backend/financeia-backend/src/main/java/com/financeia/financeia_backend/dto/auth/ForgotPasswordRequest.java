package com.financeia.financeia_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe ser un correo electrónico válido")
        String email
) {}
