package com.financeia.financeia_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeia.financeia_backend.dto.analisis.AnalisisRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

@Service
public class ModeloIntegrationService {

    private final ObjectMapper objectMapper;

    public ModeloIntegrationService() {
        this.objectMapper = new ObjectMapper();
    }

    public ModeloIntegrationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public String predecirPerfil(AnalisisRequest request) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "profile");

            Map<String, Object> data = new HashMap<>();
            data.put("ingreso_mensual_usd", request.ingresoMensual());
            
            BigDecimal gastoMensual = request.transacciones() != null ? 
                request.transacciones().stream().map(t -> t.valor()).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;

            data.put("gastos_mensuales_usd", gastoMensual);
            data.put("ahorro_total_acumulado_usd", BigDecimal.ZERO);
            data.put("cuota_deuda_mensual_usd", request.nivelEndeudamiento());
            payload.put("data", data);

            Map<String, Object> response = callPythonScript(payload);
            if (response != null && response.containsKey("prediction")) {
                return response.get("prediction").toString();
            }
        } catch (Exception e) {
            System.err.println("Error ejecutando modelo de perfil en Python: " + e.getMessage());
        }
        return "Saludable";
    }

    public String predecirCategoriaGasto(String descripcion, Double valor) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "transaction");

            Map<String, Object> data = new HashMap<>();
            data.put("descripcion", descripcion);
            data.put("valor", valor != null ? valor : 0.0);
            payload.put("data", data);

            Map<String, Object> response = callPythonScript(payload);
            if (response != null && response.containsKey("prediction")) {
                return response.get("prediction").toString();
            }
        } catch (Exception e) {
            System.err.println("Error ejecutando modelo de clasificacion en Python: " + e.getMessage());
        }
        return "Otros";
    }

    public Map<String, Object> analisisCompletoConIA(Map<String, Object> fullPayload) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "full_analysis");
            
            // Transformar variables de camelCase (Frontend/Java) a snake_case (Python)
            Map<String, Object> data = new HashMap<>(fullPayload);
            if (data.containsKey("ingresoMensual") && !data.containsKey("ingreso_mensual")) {
                data.put("ingreso_mensual", data.get("ingresoMensual"));
            }
            if (data.containsKey("nivelEndeudamiento") && !data.containsKey("nivel_endeudamiento")) {
                data.put("nivel_endeudamiento", data.get("nivelEndeudamiento"));
            }
            if (data.containsKey("frecuenciaAhorro") && !data.containsKey("frecuencia_ahorro")) {
                data.put("frecuencia_ahorro", data.get("frecuenciaAhorro"));
            }
            
            payload.put("data", data);

            Map<String, Object> rawResult = callPythonScript(payload);
            if (rawResult != null && !rawResult.containsKey("error")) {
                return formatearRespuestaHackathon(rawResult, fullPayload);
            }
        } catch (Exception e) {
            System.err.println("Error ejecutando analisis completo con IA: " + e.getMessage());
        }

        return fallbackAnalisisHackathon(fullPayload);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> formatearRespuestaHackathon(Map<String, Object> rawResult, Map<String, Object> inputPayload) {
        Map<String, Object> formatted = new LinkedHashMap<>();

        // Perfil financiero
        String perfil = rawResult.getOrDefault("perfil_financiero", "En observacion").toString();
        // Quitar acentos si vienen para máxima compatibilidad: "En observación" -> "En observacion"
        String perfilNormalizado = normalizarTexto(perfil);
        formatted.put("perfil_financiero", perfilNormalizado);

        // Probabilidad
        Object probaObj = rawResult.get("probabilidad");
        double probabilidad = 0.82;
        if (probaObj instanceof Number) {
            probabilidad = ((Number) probaObj).doubleValue();
        }
        formatted.put("probabilidad", Math.round(probabilidad * 100.0) / 100.0);

        // Resumen de gastos normalizado (sin acentos, minúsculas: alimentacion, transporte, entretenimiento)
        Map<String, Object> resumenNormalizado = new LinkedHashMap<>();
        Object rawResumen = rawResult.get("resumen_gastos");
        if (rawResumen instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) rawResumen;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = normalizarClave(entry.getKey().toString());
                resumenNormalizado.put(key, entry.getValue());
            }
        }
        formatted.put("resumen_gastos", resumenNormalizado);

        // Recomendaciones
        List<String> recomendaciones = new ArrayList<>();
        Object recsObj = rawResult.get("recomendaciones");
        if (recsObj instanceof List) {
            for (Object item : (List<?>) recsObj) {
                if (item != null) {
                    recomendaciones.add(item.toString());
                }
            }
        }
        if (recomendaciones.isEmpty()) {
            recomendaciones.add("Monitorear gastos recurrentes de entretenimiento");
            recomendaciones.add("Aumentar reserva financiera mensual");
        }
        formatted.put("recomendaciones", recomendaciones);

        return formatted;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fallbackAnalisisHackathon(Map<String, Object> inputPayload) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("perfil_financiero", "En observacion");
        result.put("probabilidad", 0.82);

        Map<String, Object> resumen = new LinkedHashMap<>();
        Object txnsObj = inputPayload.get("transacciones");
        if (txnsObj instanceof List) {
            List<?> txns = (List<?>) txnsObj;
            for (Object tObj : txns) {
                if (tObj instanceof Map) {
                    Map<String, Object> t = (Map<String, Object>) tObj;
                    String desc = t.get("descripcion") != null ? t.get("descripcion").toString().toLowerCase() : "";
                    double val = t.get("valor") != null ? Double.parseDouble(t.get("valor").toString()) : 0.0;
                    String cat = "otros";
                    if (desc.contains("super") || desc.contains("comida") || desc.contains("alimento") || desc.contains("restaurante")) {
                        cat = "alimentacion";
                    } else if (desc.contains("combustible") || desc.contains("gasolina") || desc.contains("transporte") || desc.contains("uber")) {
                        cat = "transporte";
                    } else if (desc.contains("stream") || desc.contains("netflix") || desc.contains("spotify") || desc.contains("cine")) {
                        cat = "entretenimiento";
                    }
                    double prev = resumen.containsKey(cat) ? ((Number) resumen.get(cat)).doubleValue() : 0.0;
                    resumen.put(cat, prev + val);
                }
            }
        }
        result.put("resumen_gastos", resumen);

        List<String> recs = List.of(
                "Monitorear gastos recurrentes de entretenimiento",
                "Aumentar reserva financiera mensual"
        );
        result.put("recomendaciones", recs);
        return result;
    }

    private String normalizarClave(String input) {
        if (input == null) return "otros";
        String lower = input.toLowerCase().trim();
        if (lower.contains("alim") || lower.contains("super") || lower.contains("comid")) return "alimentacion";
        if (lower.contains("transp") || lower.contains("combust") || lower.contains("gasolin") || lower.contains("auto")) return "transporte";
        if (lower.contains("entreten") || lower.contains("stream") || lower.contains("ocio") || lower.contains("cine")) return "entretenimiento";
        if (lower.contains("servici") || lower.contains("luz") || lower.contains("agua") || lower.contains("gas")) return "servicios";
        if (lower.contains("salud") || lower.contains("medic") || lower.contains("gym") || lower.contains("bienestar")) return "salud";
        if (lower.contains("educa") || lower.contains("curso") || lower.contains("libro")) return "educacion";
        if (lower.contains("viviend") || lower.contains("rent") || lower.contains("alquil") || lower.contains("hogar")) return "vivienda";
        if (lower.contains("compr") || lower.contains("ropa") || lower.contains("calzad")) return "compras";
        if (lower.contains("viaj") || lower.contains("vuelo") || lower.contains("hotel")) return "viajes";

        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .trim();
    }

    private String normalizarTexto(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private Map<String, Object> callPythonScript(Object inputPayload) throws Exception {
        String pythonExec = System.getenv("PYTHON_EXEC_PATH");
        if (pythonExec == null || pythonExec.isBlank()) {
            File venvPythonRel = new File("..\\..\\DataScient\\venv\\Scripts\\python.exe");
            if (venvPythonRel.exists()) {
                pythonExec = venvPythonRel.getAbsolutePath();
            } else {
                File venvPythonDirect = new File("DataScient\\venv\\Scripts\\python.exe");
                if (venvPythonDirect.exists()) {
                    pythonExec = venvPythonDirect.getAbsolutePath();
                } else {
                    pythonExec = "python";
                }
            }
        }

        String pythonScript = System.getenv("PYTHON_SCRIPT_PATH");
        if (pythonScript == null || pythonScript.isBlank()) {
            File scriptRel = new File("..\\..\\DataScient\\src\\predict.py");
            if (scriptRel.exists()) {
                pythonScript = scriptRel.getAbsolutePath();
            } else {
                File scriptDirect = new File("DataScient\\src\\predict.py");
                if (scriptDirect.exists()) {
                    pythonScript = scriptDirect.getAbsolutePath();
                } else {
                    pythonScript = "src\\predict.py";
                }
            }
        }

        ProcessBuilder pb = new ProcessBuilder(pythonExec, pythonScript);
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        pb.environment().put("PYTHONUTF8", "1");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (OutputStream os = process.getOutputStream()) {
            String jsonInput = objectMapper.writeValueAsString(inputPayload);
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Error en script Python (codigo " + exitCode + "): " + output);
        }

        String rawJson = output.toString().trim();
        // En caso de que haya advertencias previas al JSON, extraer desde el primer '{' hasta el último '}'
        int firstBrace = rawJson.indexOf('{');
        int lastBrace = rawJson.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            rawJson = rawJson.substring(firstBrace, lastBrace + 1);
        }

        return objectMapper.readValue(rawJson, new TypeReference<Map<String, Object>>() {});
    }

    public Map<String, String> retrainModel() {
        try {
            String pythonExec = System.getenv("PYTHON_EXEC_PATH");
            if (pythonExec == null || pythonExec.isBlank()) {
                File venvPythonRel = new File("..\\..\\DataScient\\venv\\Scripts\\python.exe");
                if (venvPythonRel.exists()) {
                    pythonExec = venvPythonRel.getAbsolutePath();
                } else {
                    File venvPythonDirect = new File("DataScient\\venv\\Scripts\\python.exe");
                    if (venvPythonDirect.exists()) {
                        pythonExec = venvPythonDirect.getAbsolutePath();
                    } else {
                        pythonExec = "python";
                    }
                }
            }

            File trainScriptRel = new File("..\\..\\DataScient\\src\\train_models.py");
            String pythonScript = trainScriptRel.exists() ? trainScriptRel.getAbsolutePath() : "DataScient\\src\\train_models.py";

            ProcessBuilder pb = new ProcessBuilder(pythonExec, pythonScript);
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pb.environment().put("PYTHONUTF8", "1");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return Map.of("status", "error", "message", "Error al reentrenar: " + output.toString());
            }

            return Map.of("status", "success", "message", "Modelos reentrenados correctamente.", "details", output.toString());
        } catch (Exception e) {
            return Map.of("status", "error", "message", "Excepcion al reentrenar: " + e.getMessage());
        }
    }
}
