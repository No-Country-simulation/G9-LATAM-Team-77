package com.financeia.financeia_backend.dto.analisis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TransaccionAnalisisRequest(

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "El valor es obligatorio")
        @PositiveOrZero(message = "El valor no puede ser negativo")
        BigDecimal valor

) {
}