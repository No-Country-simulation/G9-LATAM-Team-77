package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.analisis.AnalisisDataScienceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class DataScienceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataScienceService.class);
    private static final int MAX_STDOUT_CHARS = 1_000_000;
    private static final int MAX_STDERR_CHARS = 20_000;
    private static final long STREAM_READ_TIMEOUT_SECONDS = 2;

    private final JsonMapper jsonMapper;
    private final String pythonCommand;
    private final String predictScript;
    private final long timeoutSeconds;

    public DataScienceService(
            JsonMapper jsonMapper,
            @Value("${financeai.python.command}") String pythonCommand,
            @Value("${financeai.data-science.script}") String predictScript,
            @Value("${financeai.data-science.timeout-seconds:30}") long timeoutSeconds
    ) {
        this.jsonMapper = jsonMapper;
        this.pythonCommand = pythonCommand;
        this.predictScript = predictScript;
        this.timeoutSeconds = Math.max(1, timeoutSeconds);
    }

    public JsonNode analizar(AnalisisDataScienceRequest request) {

        Process process = null;
        Future<String> stdoutFuture = null;
        Future<String> stderrFuture = null;
        ExecutorService streamExecutor = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "financeai-data-science-stream");
            thread.setDaemon(true);
            return thread;
        });

        try {
            Map<String, Object> payload = new LinkedHashMap<>();

            payload.put("type", "full_analysis");
            payload.put("data", request);

            String jsonEntrada = jsonMapper.writeValueAsString(payload);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonCommand,
                    predictScript
            );

            process = processBuilder.start();

            Process runningProcess = process;
            stdoutFuture = streamExecutor.submit(
                    () -> readStream(runningProcess.getInputStream(), MAX_STDOUT_CHARS)
            );
            stderrFuture = streamExecutor.submit(
                    () -> readStream(runningProcess.getErrorStream(), MAX_STDERR_CHARS)
            );

            try (OutputStreamWriter writer = new OutputStreamWriter(
                    process.getOutputStream(),
                    StandardCharsets.UTF_8
            )) {
                writer.write(jsonEntrada);
                writer.flush();
            }

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                LOGGER.warn("Data Science process exceeded the configured timeout of {} seconds", timeoutSeconds);
                terminateProcess(process);
                throw new IllegalStateException(
                        "El análisis financiero excedió el tiempo máximo de ejecución."
                );
            }

            String respuestaPython = getStreamOutput(stdoutFuture);
            String errorPython = getStreamOutput(stderrFuture);
            int exitCode = process.exitValue();

            if (exitCode != 0) {
                LOGGER.warn(
                        "Data Science process finished with exit code {} and {} stderr characters",
                        exitCode,
                        errorPython.length()
                );
                throw new IllegalStateException(
                        "El motor de análisis financiero no pudo completar la solicitud."
                );
            }

            if (respuestaPython == null || respuestaPython.isBlank()) {
                throw new IllegalStateException(
                        "El motor de análisis financiero devolvió una respuesta vacía."
                );
            }

            JsonNode response = jsonMapper.readTree(respuestaPython);

            if (response == null) {
                throw new IllegalStateException(
                        "El motor de análisis financiero devolvió una respuesta inválida."
                );
            }

            return response;

        } catch (IllegalStateException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "El análisis financiero fue interrumpido antes de finalizar."
            );
        } catch (Exception e) {
            LOGGER.error("Unable to execute the Data Science process", e);
            throw new IllegalStateException(
                    "No fue posible ejecutar el análisis de Data Science.",
                    e
            );
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (stdoutFuture != null) {
                stdoutFuture.cancel(true);
            }
            if (stderrFuture != null) {
                stderrFuture.cancel(true);
            }
            streamExecutor.shutdownNow();
        }
    }

    private String readStream(InputStream inputStream, int maxChars) throws IOException {
        StringBuilder output = new StringBuilder();
        char[] buffer = new char[4096];

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        )) {
            int read;
            while ((read = reader.read(buffer)) != -1) {
                int remaining = maxChars - output.length();
                if (remaining > 0) {
                    output.append(buffer, 0, Math.min(read, remaining));
                }
            }
        }

        return output.toString();
    }

    private String getStreamOutput(Future<String> future) throws Exception {
        try {
            return future.get(STREAM_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException exception) {
            throw new IllegalStateException(
                    "No fue posible leer la respuesta del motor de análisis financiero."
            );
        }
    }

    private void terminateProcess(Process process) throws InterruptedException {
        process.destroy();
        if (!process.waitFor(1, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            process.waitFor(1, TimeUnit.SECONDS);
        }
    }
}
