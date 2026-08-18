package com.financeia.financeia_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeia.financeia_backend.dto.analisis.AnalisisRequest;
import com.financeia.financeia_backend.service.AnalisisFinancieroService;
import com.financeia.financeia_backend.service.ModeloIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalisisControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void configurarPrueba() {
        AnalisisFinancieroService service =
                new AnalisisFinancieroService();

        ModeloIntegrationService mockModelo = mock(ModeloIntegrationService.class);
        when(mockModelo.analisisCompletoConIA(any())).thenReturn(null);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AnalisisController(service, mockModelo))
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void debeRetornarAnalisisFinancieroConDatosValidos() throws Exception {

        AnalisisRequest request = new AnalisisRequest(
                new BigDecimal("25000"),
                BigDecimal.ZERO,
                "Mensual",
                java.util.List.of(new AnalisisRequest.TransaccionRequest("Gasto", new BigDecimal("5000"))),
                "HNL"
        );

        mockMvc.perform(post("/api/v1/analisis-financiero")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(100))
                .andExpect(jsonPath("$.level").value("Excelente"))
                .andExpect(jsonPath("$.alerts").isArray())
                .andExpect(jsonPath("$.recommendations").isArray());
    }

    @Test
    void debeRetornarError400CuandoElIngresoEsCero() throws Exception {

        String solicitudInvalida = """
                {
                  "ingreso_mensual": -10,
                  "nivel_endeudamiento": 5000,
                  "frecuencia_ahorro": "Mensual",
                  "transacciones": [],
                  "moneda": "HNL"
                }
                """;

        mockMvc.perform(post("/api/v1/analisis-financiero")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitudInvalida))
                .andExpect(status().isBadRequest());
    }
}