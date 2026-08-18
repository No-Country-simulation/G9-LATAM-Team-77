package com.financeia.financeia_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeia.financeia_backend.dto.analisis.AnalisisRequest;
import com.financeia.financeia_backend.dto.analisis.AnalisisResponse;
import com.financeia.financeia_backend.service.AnalisisFinancieroService;
import com.financeia.financeia_backend.service.ModeloIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/analisis-financiero", "/analisis-financiero"})
public class AnalisisController {

    private final AnalisisFinancieroService analisisFinancieroService;
    private final ModeloIntegrationService modeloIntegrationService;

    public AnalisisController(
            AnalisisFinancieroService analisisFinancieroService
    ) {
        this(analisisFinancieroService, new ModeloIntegrationService(new ObjectMapper()));
    }

    @Autowired
    public AnalisisController(
            AnalisisFinancieroService analisisFinancieroService,
            ModeloIntegrationService modeloIntegrationService
    ) {
        this.analisisFinancieroService = analisisFinancieroService;
        this.modeloIntegrationService = modeloIntegrationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> analizar(
            @RequestBody Map<String, Object> body
    ) {
        try {
            // Validar que el ingreso no sea cero o negativo
            java.math.BigDecimal ingreso = body.get("ingresoMensual") != null 
                    ? new java.math.BigDecimal(body.get("ingresoMensual").toString())
                    : (body.get("ingreso_mensual") != null ? new java.math.BigDecimal(body.get("ingreso_mensual").toString()) : null);

            if (ingreso == null || ingreso.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "El ingreso mensual debe ser mayor que cero"));
            }

            // Si el cuerpo contiene una lista de transacciones o estructura de análisis completo con IA:
            if (body.containsKey("transacciones") || body.containsKey("frecuencia_ahorro")) {
                Map<String, Object> aiResult = modeloIntegrationService.analisisCompletoConIA(body);
                if (aiResult != null && !aiResult.containsKey("error")) {
                    return ResponseEntity.ok(aiResult);
                }
            }

            java.math.BigDecimal gasto = body.get("gastoMensual") != null 
                    ? new java.math.BigDecimal(body.get("gastoMensual").toString())
                    : (body.get("total_gastos") != null ? new java.math.BigDecimal(body.get("total_gastos").toString()) : java.math.BigDecimal.ZERO);
            
            java.math.BigDecimal ahorro = body.get("ahorroMensual") != null 
                    ? new java.math.BigDecimal(body.get("ahorroMensual").toString())
                    : (body.get("ahorro_estimado") != null ? new java.math.BigDecimal(body.get("ahorro_estimado").toString()) : java.math.BigDecimal.ZERO);
            
            java.math.BigDecimal deuda = body.get("deudaTotal") != null 
                    ? new java.math.BigDecimal(body.get("deudaTotal").toString())
                    : (body.get("nivel_endeudamiento") != null ? new java.math.BigDecimal(body.get("nivel_endeudamiento").toString()) : java.math.BigDecimal.ZERO);
            
            String moneda = body.get("moneda") != null ? body.get("moneda").toString() : "USD";

            String frecuenciaAhorro = body.get("frecuenciaAhorro") != null ? body.get("frecuenciaAhorro").toString() : "Media";

            AnalisisRequest request = new AnalisisRequest(ingreso, deuda, frecuenciaAhorro, null, moneda);
            AnalisisResponse response = analisisFinancieroService.analizar(request);
            
            return ResponseEntity.ok(Map.of(
                    "score", response.score(),
                    "level", response.level(),
                    "alerts", response.alerts(),
                    "recommendations", response.recommendations()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error procesando solicitud de análisis: " + e.getMessage()));
        }
    }

    @PostMapping("/retrain")
    public ResponseEntity<?> retrainModel() {
        try {
            Map<String, String> result = modeloIntegrationService.retrainModel();
            if ("error".equals(result.get("status"))) {
                return ResponseEntity.status(500).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No se pudo reentrenar el modelo.",
                    "details", e.getMessage()
            ));
        }
    }

    @PostMapping("/ia")
    public ResponseEntity<Map<String, Object>> analizarConIA(@RequestBody Map<String, Object> payload) {
        Map<String, Object> aiResult = modeloIntegrationService.analisisCompletoConIA(payload);
        if (aiResult != null) {
            return ResponseEntity.ok(aiResult);
        }
        return ResponseEntity.internalServerError().body(Map.of("error", "No fue posible ejecutar el modelo de IA"));
    }

    @PostMapping("/clasificar")
    public ResponseEntity<Map<String, String>> clasificar(@RequestBody Map<String, Object> payload) {
        String descripcion = payload.getOrDefault("descripcion", "").toString();
        Double valor = payload.get("valor") != null ? Double.parseDouble(payload.get("valor").toString()) : 0.0;
        String categoria = modeloIntegrationService.predecirCategoriaGasto(descripcion, valor);
        return ResponseEntity.ok(Map.of("categoria", categoria, "descripcion", descripcion));
    }
}