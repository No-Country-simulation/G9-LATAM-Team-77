package com.financeia.financeia_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleLoginRequest(
        @NotBlank(message = "La credencial de Google es obligatoria")
        @Size(max = 8192, message = "La credencial de Google no es válida")
        String credential,
        Long paisId,
        Long monedaId
) {
}
