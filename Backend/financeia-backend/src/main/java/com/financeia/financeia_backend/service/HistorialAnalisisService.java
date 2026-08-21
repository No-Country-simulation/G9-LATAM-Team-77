package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.analisis.AnalisisDataScienceRequest;
import com.financeia.financeia_backend.dto.historial.HistorialAnalisisResponse;
import com.financeia.financeia_backend.entity.HistorialAnalisis;
import com.financeia.financeia_backend.entity.User;
import com.financeia.financeia_backend.repository.HistorialAnalisisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialAnalisisService {

    private final HistorialAnalisisRepository historialAnalisisRepository;
    private final JsonMapper jsonMapper;

    @Transactional
    public void registrarAnalisisExitoso(
            User user,
            AnalisisDataScienceRequest request,
            JsonNode response
    ) {
        if (!"success".equals(response.path("status").asString())) {
            return;
        }

        HistorialAnalisis historial = new HistorialAnalisis();
        historial.setUser(user);
        historial.setFechaHora(Instant.now());
        historial.setPerfilFinanciero(requiredText(response, "perfil_financiero"));
        historial.setProbabilidad(requiredDecimal(response, "probabilidad"));
        historial.setIngresoMensual(request.ingresoMensual());
        historial.setTotalGastos(requiredDecimal(response, "total_gastos"));
        historial.setAhorroEstimado(requiredDecimal(response, "ahorro_estimado"));
        historial.setNivelEndeudamiento(request.nivelEndeudamiento());
        historial.setFrecuenciaAhorro(request.frecuenciaAhorro());
        historial.setResumenGastos(jsonValue(response.get("resumen_gastos"), "{}"));
        historial.setRecomendaciones(jsonValue(response.get("recomendaciones"), "[]"));

        historialAnalisisRepository.save(historial);
    }

    @Transactional(readOnly = true)
    public List<HistorialAnalisisResponse> consultarHistorial(User user) {
        return historialAnalisisRepository.findAllByUserOrderByFechaHoraDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private HistorialAnalisisResponse toResponse(HistorialAnalisis historial) {
        return new HistorialAnalisisResponse(
                historial.getId(),
                historial.getFechaHora(),
                historial.getPerfilFinanciero(),
                historial.getProbabilidad(),
                historial.getIngresoMensual(),
                historial.getTotalGastos(),
                historial.getAhorroEstimado(),
                historial.getNivelEndeudamiento(),
                historial.getFrecuenciaAhorro(),
                parseJson(historial.getResumenGastos(), false),
                parseJson(historial.getRecomendaciones(), true)
        );
    }

    private String requiredText(JsonNode response, String field) {
        JsonNode value = response.get(field);
        if (value == null || !value.isString() || value.asString().isBlank()) {
            throw new IllegalStateException("La respuesta del análisis no contiene " + field + ".");
        }
        return value.asString();
    }

    private BigDecimal requiredDecimal(JsonNode response, String field) {
        JsonNode value = response.get(field);
        if (value == null || !value.isNumber()) {
            throw new IllegalStateException("La respuesta del análisis no contiene " + field + ".");
        }
        return value.decimalValue();
    }

    private String jsonValue(JsonNode value, String fallback) {
        return value == null || value.isNull() ? fallback : value.toString();
    }

    private JsonNode parseJson(String value, boolean arrayFallback) {
        try {
            if (value != null && !value.isBlank()) {
                return jsonMapper.readTree(value);
            }
        } catch (Exception ignored) {
            // Los datos dañados se aíslan para que el resto del historial siga disponible.
        }
        return arrayFallback ? jsonMapper.createArrayNode() : jsonMapper.createObjectNode();
    }
}
