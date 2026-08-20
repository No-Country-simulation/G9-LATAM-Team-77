package com.financeia.financeia_backend.dto.analisis;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record AnalisisDataScienceRequest(

        @JsonProperty("ingreso_mensual")
        @NotNull(message = "El ingreso mensual es obligatorio")
        @Positive(message = "El ingreso mensual debe ser mayor que cero")
        BigDecimal ingresoMensual,

        @JsonProperty("nivel_endeudamiento")
        @NotNull(message = "El nivel de endeudamiento es obligatorio")
        BigDecimal nivelEndeudamiento,

        @JsonProperty("frecuencia_ahorro")
        @NotBlank(message = "La frecuencia de ahorro es obligatoria")
        String frecuenciaAhorro,

        @JsonProperty("transacciones")
        @NotNull(message = "Las transacciones son obligatorias")
        @Valid
        List<TransaccionAnalisisRequest> transacciones,

        @JsonProperty("moneda")
        String moneda

) {
}
