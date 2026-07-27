package com.financeia.financeia_backend.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias del endpoint de salud del Back-End.
 */
class HealthControllerTest {

    private MockMvc mockMvc;

    /**
     * Configura MockMvc utilizando únicamente HealthController.
     * No inicia toda la aplicación ni se conecta a MySQL.
     */
    @BeforeEach
    void configurarPrueba() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new HealthController())
                .build();
    }

    /**
     * Verifica que el endpoint responda HTTP 200
     * y devuelva la información esperada.
     */
    @Test
    void debeRetornarEstadoUpYNombreDelServicio() throws Exception {

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(
                        jsonPath("$.service")
                                .value("FinanceAI Back-End")
                );
    }
}