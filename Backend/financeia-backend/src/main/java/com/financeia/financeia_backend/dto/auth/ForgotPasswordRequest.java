package com.financeia.financeia_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "El correo electrónico es requerido")
        @Email(message = "El correo electrónico no es válido")
        String email
) {
}
