package com.financeia.financeia_backend.dto.user;

import jakarta.validation.constraints.NotNull;

public record UserUpdateRequest(
        @NotNull
        Long paisId,

        @NotNull
        Long monedaId
) {
}