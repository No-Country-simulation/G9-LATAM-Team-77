package com.financeia.financeia_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeia.financeia_backend.dto.analisis.AnalisisRequest;
import com.financeia.financeia_backend.service.DataScienceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalisisControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DataScienceService dataScienceService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalisisController(dataScienceService)).build();
    }

    @Test
    void analizar_shouldReturnOk() throws Exception {
        AnalisisRequest request = new AnalisisRequest(
                new BigDecimal("5000.00"),
                new BigDecimal("2.5"),
                "Alta",
                List.of(),
                "USD"
        );

        JsonNode mockResponse = JsonNodeFactory.instance.objectNode().put("score", 85);
        when(dataScienceService.analizar(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/analisis-financiero")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}

