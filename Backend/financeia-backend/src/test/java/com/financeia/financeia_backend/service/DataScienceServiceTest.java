package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.analisis.AnalisisDataScienceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DataScienceServiceTest {

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

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @TempDir
    Path tempDirectory;

    @Test
    void shouldTerminateProcessWhenTimeoutIsExceeded() throws Exception {
        assumePythonIsAvailable();

        Path script = createScript("""
                import sys
                import time
                sys.stdin.read()
                time.sleep(10)
                """);

        DataScienceService service = new DataScienceService(
                jsonMapper,
                PYTHON_EXECUTABLE.toString(),
                script.toString(),
                1
        );

        long startedAt = System.nanoTime();
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.analizar(validRequest())
        );
        long elapsedSeconds = (System.nanoTime() - startedAt) / 1_000_000_000;

        assertEquals(
                "El análisis financiero excedió el tiempo máximo de ejecución.",
                exception.getMessage()
        );
        assertTrue(elapsedSeconds < 5, "El proceso no terminó dentro del margen esperado");
    }

    @Test
    void shouldDrainLargeStderrWithoutBlockingStdout() throws Exception {
        assumePythonIsAvailable();

        Path script = createScript("""
                import sys
                sys.stdin.read()
                sys.stderr.write("x" * 100000)
                sys.stderr.flush()
                print('{"status":"success"}')
                """);

        DataScienceService service = new DataScienceService(
                jsonMapper,
                PYTHON_EXECUTABLE.toString(),
                script.toString(),
                5
        );

        JsonNode response = service.analizar(validRequest());

        assertEquals("success", response.get("status").asString());
    }

    @Test
    void shouldNotExposePythonErrorOutput() throws Exception {
        assumePythonIsAvailable();

        Path script = createScript("""
                import sys
                sys.stdin.read()
                sys.stderr.write("detalle interno que no debe llegar al cliente")
                sys.exit(2)
                """);

        DataScienceService service = new DataScienceService(
                jsonMapper,
                PYTHON_EXECUTABLE.toString(),
                script.toString(),
                5
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.analizar(validRequest())
        );

        assertEquals(
                "El motor de análisis financiero no pudo completar la solicitud.",
                exception.getMessage()
        );
        assertFalse(exception.getMessage().contains("detalle interno"));
    }

    private Path createScript(String content) throws Exception {
        Path script = tempDirectory.resolve("data_science_test.py");
        Files.writeString(script, content, StandardCharsets.UTF_8);
        return script;
    }

    private void assumePythonIsAvailable() {
        assumeTrue(Files.isRegularFile(PYTHON_EXECUTABLE), "El .venv de Data Science no está preparado");
    }

    private AnalisisDataScienceRequest validRequest() {
        return new AnalisisDataScienceRequest(
                new BigDecimal("4500"),
                new BigDecimal("25"),
                "Media",
                List.of()
        );
    }
}
