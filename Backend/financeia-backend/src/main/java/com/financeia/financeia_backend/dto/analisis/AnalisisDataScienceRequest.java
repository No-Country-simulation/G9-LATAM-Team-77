package com.financeia.financeia_backend.dto.analisis;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record AnalisisDataScienceRequest(

        @NotNull(message = "El ingreso mensual es obligatorio")
        @Positive(message = "El ingreso mensual debe ser mayor que cero")
        @JsonProperty("ingreso_mensual")
        @JsonAlias({"ingresoMensual", "ingreso_mensual"})
        BigDecimal ingresoMensual,

        @JsonProperty("nivel_endeudamiento")
        @JsonAlias({"nivelEndeudamiento", "nivel_endeudamiento"})
        BigDecimal nivelEndeudamiento,

        @JsonProperty("frecuencia_ahorro")
        @JsonAlias({"frecuenciaAhorro", "frecuencia_ahorro"})
        String frecuenciaAhorro,

        @NotNull(message = "Las transacciones son obligatorias")
        @Valid
        @JsonProperty("transacciones")
        List<TransaccionAnalisisRequest> transacciones,

        @JsonProperty("moneda")
        String moneda

) {
}
