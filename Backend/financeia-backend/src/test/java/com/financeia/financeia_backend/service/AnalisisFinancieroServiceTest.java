package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.analisis.AnalisisRequest;
import com.financeia.financeia_backend.dto.analisis.AnalisisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AnalisisFinancieroServiceTest {

    private AnalisisFinancieroService analisisFinancieroService;

    @BeforeEach
    void configurarPrueba() {
        analisisFinancieroService = new AnalisisFinancieroService();
    }

    @Test
    void debeRetornarNivelExcelenteCuandoLasFinanzasSonSaludables() {

        AnalisisRequest request = new AnalisisRequest(
                new BigDecimal("25000"),
                new BigDecimal("12000"),
                new BigDecimal("5000"),
                BigDecimal.ZERO,
                "HNL"
        );

        AnalisisResponse response =
                analisisFinancieroService.analizar(request);

        assertEquals(100, response.score());
        assertEquals("Excelente", response.level());
        assertFalse(response.alerts().isEmpty());
        assertFalse(response.recommendations().isEmpty());
    }

    @Test
    void debeRetornarNivelCriticoCuandoLosGastosYDeudasSonAltos() {

        AnalisisRequest request = new AnalisisRequest(
                new BigDecimal("10000"),
                new BigDecimal("9500"),
                new BigDecimal("500"),
                new BigDecimal("70000"),
                "HNL"
        );

        AnalisisResponse response =
                analisisFinancieroService.analizar(request);

        assertEquals(25, response.score());
        assertEquals("Crítico", response.level());
        assertFalse(response.alerts().isEmpty());
        assertFalse(response.recommendations().isEmpty());
    }
}