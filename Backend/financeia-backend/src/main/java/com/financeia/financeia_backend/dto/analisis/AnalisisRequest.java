package com.financeia.financeia_backend.dto.analisis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AnalisisRequest(

        @NotNull(message = "El ingreso mensual es obligatorio")
        @Positive(message = "El ingreso mensual debe ser mayor que cero")
        BigDecimal ingresoMensual,

        @NotNull(message = "El gasto mensual es obligatorio")
        @PositiveOrZero(message = "El gasto mensual no puede ser negativo")
        BigDecimal gastoMensual,

        @NotNull(message = "El ahorro mensual es obligatorio")
        @PositiveOrZero(message = "El ahorro mensual no puede ser negativo")
        BigDecimal ahorroMensual,

        @NotNull(message = "La deuda total es obligatoria")
        @PositiveOrZero(message = "La deuda total no puede ser negativa")
        BigDecimal deudaTotal,

        @NotBlank(message = "La moneda es obligatoria")
        String moneda

) {
}