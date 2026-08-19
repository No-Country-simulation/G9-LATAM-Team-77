package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.analisis.AnalisisDataScienceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataScienceService {

    private final JsonMapper jsonMapper;
    private final String pythonCommand;
    private final String predictScript;

    public DataScienceService(
            JsonMapper jsonMapper,
            @Value("${financeai.python.command}") String pythonCommand,
            @Value("${financeai.data-science.script}") String predictScript
    ) {
        this.jsonMapper = jsonMapper;
        this.pythonCommand = pythonCommand;
        this.predictScript = predictScript;
    }

    public JsonNode analizar(AnalisisDataScienceRequest request) {

        try {
            Map<String, Object> payload = new LinkedHashMap<>();

            payload.put("type", "full_analysis");
            payload.put("data", request);

            String jsonEntrada = jsonMapper.writeValueAsString(payload);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    predictScript
            );

            Process process = processBuilder.start();

            try (OutputStreamWriter writer = new OutputStreamWriter(
                    process.getOutputStream(),
                    StandardCharsets.UTF_8
            )) {
                writer.write(jsonEntrada);
                writer.flush();
            }

            String respuestaPython;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            )) {
                respuestaPython = reader.lines()
                        .collect(Collectors.joining());
            }

            String errorPython;

            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(
                            process.getErrorStream(),
                            StandardCharsets.UTF_8
                    )
            )) {
                errorPython = errorReader.lines()
                        .collect(Collectors.joining(System.lineSeparator()));
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IllegalStateException(
                        "Data Science terminó con código "
                                + exitCode
                                + ". Detalle: "
                                + errorPython
                );
            }

            if (respuestaPython == null || respuestaPython.isBlank()) {
                throw new IllegalStateException(
                        "Data Science no devolvió ninguna respuesta."
                );
            }

            return jsonMapper.readTree(respuestaPython);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No fue posible ejecutar el análisis de Data Science.",
                    e
            );
        }
    }
}