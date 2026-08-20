package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.dashboard.DashboardResponse;
import com.financeia.financeia_backend.entity.TransactionType;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private DashboardService dashboardService;

    private User user;

    @BeforeEach
    void setUp() {

        dashboardService = new DashboardService(transactionRepository);

        user = new User();
        user.setId(1L);
    }

    @Test
    void shouldCalculateCurrentAndPreviousMonthSummary() {

        LocalDate hoy = LocalDate.now();

        LocalDate inicioMesActual = hoy.withDayOfMonth(1);
        LocalDate finMesActual = hoy.withDayOfMonth(
                hoy.lengthOfMonth()
        );

        LocalDate inicioMesAnterior = inicioMesActual.minusMonths(1);
        LocalDate finMesAnterior = inicioMesActual.minusDays(1);


        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user,
                TransactionType.INGRESO,
                inicioMesActual,
                finMesActual
        )).thenReturn(new BigDecimal("850000"));

        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user,
                TransactionType.GASTO,
                inicioMesActual,
                finMesActual
        )).thenReturn(new BigDecimal("520000"));

        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user,
                TransactionType.INGRESO,
                inicioMesAnterior,
                finMesAnterior
        )).thenReturn(new BigDecimal("800000"));

        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user,
                TransactionType.GASTO,
                inicioMesAnterior,
                finMesAnterior
        )).thenReturn(new BigDecimal("450000"));


        when(transactionRepository.sumAmountByCategoryAndUserAndTypeAndDateBetween(
                user,
                TransactionType.GASTO,
                inicioMesActual,
                finMesActual
        )).thenReturn(List.of(
                new Object[]{"Alimentación", new BigDecimal("180000")},
                new Object[]{"Transporte", new BigDecimal("90000")},
                new Object[]{"Entretenimiento", new BigDecimal("70000")}
        ));


        DashboardResponse response =
                dashboardService.getSummary(user);


        assertEquals(
                new BigDecimal("850000"),
                response.ingresosMesActual()
        );

        assertEquals(
                new BigDecimal("520000"),
                response.gastosMesActual()
        );

        assertEquals(
                new BigDecimal("330000"),
                response.balanceMesActual()
        );


        assertEquals(
                new BigDecimal("800000"),
                response.ingresosMesAnterior()
        );

        assertEquals(
                new BigDecimal("450000"),
                response.gastosMesAnterior()
        );

        assertEquals(
                new BigDecimal("350000"),
                response.balanceMesAnterior()
        );


        assertEquals(
                new BigDecimal("15.56"),
                response.variacionGastos()
        );


        assertEquals(
                new BigDecimal("180000"),
                response.gastosPorCategoria().get("Alimentación")
        );

        assertEquals(
                new BigDecimal("90000"),
                response.gastosPorCategoria().get("Transporte")
        );

        assertEquals(
                new BigDecimal("70000"),
                response.gastosPorCategoria().get("Entretenimiento")
        );
    }


    @Test
    void shouldReturnZeroVariationWhenThereAreNoExpensesInEitherMonth() {

        LocalDate hoy = LocalDate.now();

        LocalDate inicioMesActual = hoy.withDayOfMonth(1);
        LocalDate finMesActual = hoy.withDayOfMonth(
                hoy.lengthOfMonth()
        );

        LocalDate inicioMesAnterior = inicioMesActual.minusMonths(1);
        LocalDate finMesAnterior = inicioMesActual.minusDays(1);


        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user,
                TransactionType.INGRESO,
                inicioMesActual,
                finMesActual
        )).thenReturn(BigDecimal.ZERO);

        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user,
                TransactionType.GASTO,
                inicioMesActual,
                finMesActual
        )).thenReturn(BigDecimal.ZERO);

        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user,
                TransactionType.INGRESO,
                inicioMesAnterior,
                finMesAnterior
        )).thenReturn(BigDecimal.ZERO);

        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user,
                TransactionType.GASTO,
                inicioMesAnterior,
                finMesAnterior
        )).thenReturn(BigDecimal.ZERO);

        when(transactionRepository.sumAmountByCategoryAndUserAndTypeAndDateBetween(
                user,
                TransactionType.GASTO,
                inicioMesActual,
                finMesActual
        )).thenReturn(List.of());


        DashboardResponse response =
                dashboardService.getSummary(user);


        assertEquals(
                BigDecimal.ZERO,
                response.ingresosMesActual()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.gastosMesActual()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.balanceMesActual()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.ingresosMesAnterior()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.gastosMesAnterior()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.balanceMesAnterior()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.variacionGastos()
        );

        assertTrue(
                response.gastosPorCategoria().isEmpty()
        );
    }
}