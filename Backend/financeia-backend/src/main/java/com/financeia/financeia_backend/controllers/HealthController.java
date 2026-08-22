package com.financeia.financeia_backend.controllers;

import com.financeia.financeia_backend.dto.health.HealthResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador encargado de verificar el estado
 * actual del Back-End de FinanceAI.
 */
@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
public class HealthController {

    /**
     * Atiende solicitudes HTTP GET en /api/v1/health.
     *
     * @return respuesta con el estado actual del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> obtenerEstado() {

        HealthResponse respuesta = new HealthResponse(
                "UP",
                "FinanceAI Back-End"
        );

        return ResponseEntity.ok(respuesta);
    }
}