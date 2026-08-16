package com.financeia.financeia_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistroRequest(
        @NotBlank
        String nombre,
        @Email
        @NotBlank
        String email,
        @NotBlank
        String password,
        @NotNull
        Long paisId,
        @NotNull
        Long monedaId
) {
}
