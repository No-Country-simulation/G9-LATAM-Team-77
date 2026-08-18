package com.financeia.financeia_backend.dto.analisis;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record AnalisisRequest(

        @JsonProperty("ingreso_mensual")
        @NotNull(message = "El ingreso mensual es obligatorio")
        @PositiveOrZero(message = "El ingreso mensual no puede ser negativo")
        BigDecimal ingresoMensual,

        @JsonProperty("nivel_endeudamiento")
        @NotNull(message = "El nivel de endeudamiento es obligatorio")
        @PositiveOrZero(message = "El nivel de endeudamiento no puede ser negativo")
        BigDecimal nivelEndeudamiento,

        @JsonProperty("frecuencia_ahorro")
        String frecuenciaAhorro,

        @JsonProperty("transacciones")
        List<TransaccionRequest> transacciones,

        @JsonProperty("moneda")
        String moneda

) {
    public record TransaccionRequest(
            @JsonProperty("descripcion")
            String descripcion,

            @JsonProperty("valor")
            BigDecimal valor
    ) {}
}