package com.financeia.financeia_backend.dto.analisis;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record AnalisisRequest(

        @NotNull(message = "El ingreso mensual es obligatorio")
        @PositiveOrZero(message = "El ingreso mensual no puede ser negativo")
        BigDecimal ingresoMensual,

        @NotNull(message = "El nivel de endeudamiento es obligatorio")
        @PositiveOrZero(message = "El nivel de endeudamiento no puede ser negativo")
        BigDecimal nivelEndeudamiento,

        String frecuenciaAhorro,

        List<TransaccionRequest> transacciones,

        String moneda

) {
    public record TransaccionRequest(
            String descripcion,
            BigDecimal valor
    ) {}
}