package com.financeia.financeia_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        String newPassword
) {}
