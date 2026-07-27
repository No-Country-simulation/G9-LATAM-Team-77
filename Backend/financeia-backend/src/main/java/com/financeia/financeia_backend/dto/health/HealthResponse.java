package com.financeia.financeia_backend.dto.health;

/**
 * Representa la respuesta enviada por el endpoint
 * de salud del Back-End de FinanceAI.
 *
 * @param status estado actual del servicio
 * @param service nombre del servicio
 */
public record HealthResponse(
        String status,
        String service
) {
}