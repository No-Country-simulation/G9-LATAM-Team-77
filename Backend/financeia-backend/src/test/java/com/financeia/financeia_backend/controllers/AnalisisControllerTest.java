package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.analisis.AnalisisDataScienceRequest;
import com.financeia.financeia_backend.dto.analisis.TransaccionAnalisisRequest;
import com.financeia.financeia_backend.service.DataScienceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalisisControllerTest {

    private MockMvc mockMvc;
    private JsonMapper jsonMapper;
    private DataScienceService dataScienceService;

    @BeforeEach
    void configurarPrueba() {

        dataScienceService = mock(DataScienceService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AnalisisController(dataScienceService))
                .build();

        jsonMapper = new JsonMapper();
    }

    @Test
    void debeRetornarAnalisisFinanciero() throws Exception {

        AnalisisDataScienceRequest request =
                new AnalisisDataScienceRequest(
                        new BigDecimal("4500"),
                        new BigDecimal("25"),
                        "Media",
                        List.of(
                                new TransaccionAnalisisRequest(
                                        "Supermercado",
                                        new BigDecimal("420")
                                ),
                                new TransaccionAnalisisRequest(
                                        "Gasolina",
                                        new BigDecimal("250")
                                )
                        )
                );

        String respuestaJson = """
                {
                  "status": "success",
                  "perfil_financiero": "Saludable",
                  "probabilidad": 0.95,
                  "ingreso_mensual": 4500.0,
                  "total_gastos": 670.0,
                  "ahorro_estimado": 3830.0,
                  "nivel_endeudamiento": 25.0,
                  "frecuencia_ahorro": "Media",
                  "recomendaciones": [
                    "Mantener hábitos financieros saludables"
                  ]
                }
                """;

        JsonNode respuesta =
                jsonMapper.readTree(respuestaJson);

        when(dataScienceService.analizar(
                any(AnalisisDataScienceRequest.class)
        )).thenReturn(respuesta);

        mockMvc.perform(
                        post("/api/v1/analisis-financiero")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.perfil_financiero").value("Saludable"))
                .andExpect(jsonPath("$.probabilidad").value(0.95))
                .andExpect(jsonPath("$.ingreso_mensual").value(4500.0));
    }
}