package com.financeia.financeia_backend.dto.historial;

import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;

public record HistorialAnalisisResponse(
        Long id,
        Instant fechaHora,
        String perfilFinanciero,
        BigDecimal probabilidad,
        BigDecimal ingresoMensual,
        BigDecimal totalGastos,
        BigDecimal ahorroEstimado,
        BigDecimal nivelEndeudamiento,
        String frecuenciaAhorro,
        JsonNode resumenGastos,
        JsonNode recomendaciones
) {
}
