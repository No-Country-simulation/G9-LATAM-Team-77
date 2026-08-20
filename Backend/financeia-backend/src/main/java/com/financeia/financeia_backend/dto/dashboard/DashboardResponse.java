package com.financeia.financeia_backend.dto.dashboard;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardResponse(
        BigDecimal ingresosMesActual,
        BigDecimal gastosMesActual,
        BigDecimal balanceMesActual,

        BigDecimal ingresosMesAnterior,
        BigDecimal gastosMesAnterior,
        BigDecimal balanceMesAnterior,

        BigDecimal variacionGastos,

        Map<String, BigDecimal> gastosPorCategoria
) {
}