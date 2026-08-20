package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.dashboard.DashboardResponse;
import com.financeia.financeia_backend.entity.TransactionType;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public DashboardResponse getSummary(User user) {

        // ==============================
        // FECHAS
        // ==============================

        LocalDate hoy = LocalDate.now();

        LocalDate inicioMesActual = hoy.withDayOfMonth(1);
        LocalDate finMesActual = hoy.withDayOfMonth(
                hoy.lengthOfMonth()
        );

        LocalDate inicioMesAnterior = inicioMesActual.minusMonths(1);
        LocalDate finMesAnterior = inicioMesActual.minusDays(1);


        // ==============================
        // MES ACTUAL
        // ==============================

        BigDecimal ingresosMesActual =
                transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                        user,
                        TransactionType.INGRESO,
                        inicioMesActual,
                        finMesActual
                );

        BigDecimal gastosMesActual =
                transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                        user,
                        TransactionType.GASTO,
                        inicioMesActual,
                        finMesActual
                );


        // ==============================
        // MES ANTERIOR
        // ==============================

        BigDecimal ingresosMesAnterior =
                transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                        user,
                        TransactionType.INGRESO,
                        inicioMesAnterior,
                        finMesAnterior
                );

        BigDecimal gastosMesAnterior =
                transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                        user,
                        TransactionType.GASTO,
                        inicioMesAnterior,
                        finMesAnterior
                );


        // ==============================
        // BALANCES
        // ==============================

        BigDecimal balanceMesActual =
                ingresosMesActual.subtract(gastosMesActual);

        BigDecimal balanceMesAnterior =
                ingresosMesAnterior.subtract(gastosMesAnterior);


        // ==============================
        // VARIACIÓN DE GASTOS
        // ==============================

        BigDecimal variacionGastos =
                calcularVariacion(
                        gastosMesAnterior,
                        gastosMesActual
                );


        // ==============================
        // GASTOS POR CATEGORÍA
        // ==============================

        List<Object[]> resultadosCategorias =
                transactionRepository.sumAmountByCategoryAndUserAndTypeAndDateBetween(
                        user,
                        TransactionType.GASTO,
                        inicioMesActual,
                        finMesActual
                );

        Map<String, BigDecimal> gastosPorCategoria =
                new LinkedHashMap<>();

        for (Object[] resultado : resultadosCategorias) {

            String categoria = (String) resultado[0];

            BigDecimal monto = (BigDecimal) resultado[1];

            gastosPorCategoria.put(categoria, monto);
        }


        // ==============================
        // RESPONSE
        // ==============================

        return new DashboardResponse(
                ingresosMesActual,
                gastosMesActual,
                balanceMesActual,

                ingresosMesAnterior,
                gastosMesAnterior,
                balanceMesAnterior,

                variacionGastos,

                gastosPorCategoria
        );
    }


    private BigDecimal calcularVariacion(
            BigDecimal valorAnterior,
            BigDecimal valorActual
    ) {

        if (valorAnterior.compareTo(BigDecimal.ZERO) == 0) {

            if (valorActual.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            return BigDecimal.valueOf(100);
        }

        return valorActual
                .subtract(valorAnterior)
                .divide(
                        valorAnterior,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}