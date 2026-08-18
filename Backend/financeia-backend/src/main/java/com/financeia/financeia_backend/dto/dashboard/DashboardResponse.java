package com.financeia.financeia_backend.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardResponse(
        Long id,
        LocalDate fecha,
        BigDecimal ingresoMensual,
        BigDecimal nivelEndeudamiento,
        String frecuenciaAhorro,
        BigDecimal totalGastos,
        BigDecimal ahorroEstimado,
        String scoreFinanciero,
        String resumenCategorias
) {
}
