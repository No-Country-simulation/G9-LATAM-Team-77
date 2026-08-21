package com.financeia.financeia_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DataScienceIntegrationTest {

    private static final Path DATA_SCIENCE_ROOT = Path.of("")
            .toAbsolutePath()
            .normalize()
            .resolve("../../financeai-data-science")
            .normalize();

    private static final Path PYTHON_EXECUTABLE = DATA_SCIENCE_ROOT.resolve(
            System.getProperty("os.name").toLowerCase().contains("win")
                    ? ".venv/Scripts/python.exe"
                    : ".venv/bin/python"
    );

    private static final Path PREDICT_SCRIPT = DATA_SCIENCE_ROOT.resolve("src/predict.py");

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureDataScience(DynamicPropertyRegistry registry) {
        registry.add("financeai.python.command", () -> PYTHON_EXECUTABLE.toString());
        registry.add("financeai.data-science.script", () -> PREDICT_SCRIPT.toString());
        registry.add("financeai.data-science.timeout-seconds", () -> "30");
    }

    @Test
    void shouldExecuteDataScienceThroughAuthenticatedEndpoint() throws Exception {
        assumeTrue(Files.isRegularFile(PYTHON_EXECUTABLE), "El .venv de Data Science no está preparado");
        assumeTrue(Files.isRegularFile(PREDICT_SCRIPT), "predict.py no está disponible");

        String request = """
                {
                  "ingreso_mensual": 4500,
                  "nivel_endeudamiento": 25,
                  "frecuencia_ahorro": "Media",
                  "transacciones": [
                    {"descripcion": "Transporte", "valor": 420},
                    {"descripcion": "Educacion", "valor": 500}
                  ]
                }
                """;

        mockMvc.perform(
                        post("/api/v1/analisis-financiero")
                                .with(user("data-science-test"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.perfil_financiero").value("Saludable"))
                .andExpect(jsonPath("$.probabilidad").value(0.96))
                .andExpect(jsonPath("$.total_gastos").value(920.0))
                .andExpect(jsonPath("$.ahorro_estimado").value(3580.0));
    }
}
