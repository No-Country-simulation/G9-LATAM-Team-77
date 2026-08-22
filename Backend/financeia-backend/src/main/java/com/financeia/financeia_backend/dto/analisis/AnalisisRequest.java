package com.financeia.financeia_backend.dto.analisis;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record AnalisisRequest(

        @NotNull(message = "El ingreso mensual es obligatorio")
        @PositiveOrZero(message = "El ingreso mensual no puede ser negativo")
        @JsonAlias({"ingreso_mensual", "ingreso", "ingreso_mensual_usd"})
        BigDecimal ingresoMensual,

        @PositiveOrZero(message = "El nivel de endeudamiento no puede ser negativo")
        @JsonAlias({"nivel_endeudamiento", "endeudamiento", "cuota_deuda_mensual_usd"})
        BigDecimal nivelEndeudamiento,

        @JsonAlias({"frecuencia_ahorro"})
        String frecuenciaAhorro,

        List<TransaccionRequest> transacciones,

        String moneda

) {
    public record TransaccionRequest(
            @JsonAlias({"description", "categoria"})
            String descripcion,
            @JsonAlias({"monto", "amount", "precio"})
            BigDecimal valor
    ) {}
}