package com.financeia.financeia_backend.service;

import com.financeia.financeia_backend.dto.analisis.AnalisisRequest;
import com.financeia.financeia_backend.dto.analisis.AnalisisResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalisisFinancieroService {

    public AnalisisResponse analizar(AnalisisRequest request) {

        int score = 100;

        List<String> alerts = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        BigDecimal porcentajeGastos = request.gastoMensual()
                .divide(request.ingresoMensual(), 4, RoundingMode.HALF_UP);

        BigDecimal porcentajeAhorro = request.ahorroMensual()
                .divide(request.ingresoMensual(), 4, RoundingMode.HALF_UP);

        BigDecimal nivelDeuda = request.deudaTotal()
                .divide(request.ingresoMensual(), 4, RoundingMode.HALF_UP);

        // Evaluar los gastos mensuales
        if (porcentajeGastos.compareTo(new BigDecimal("0.90")) > 0) {
            score -= 30;
            alerts.add("Los gastos superan el 90% de los ingresos.");
            recommendations.add("Reduce gastos no esenciales inmediatamente.");
        } else if (porcentajeGastos.compareTo(new BigDecimal("0.70")) > 0) {
            score -= 20;
            alerts.add("Los gastos representan más del 70% de los ingresos.");
            recommendations.add("Revisa tu presupuesto y reduce gastos variables.");
        } else if (porcentajeGastos.compareTo(new BigDecimal("0.50")) > 0) {
            score -= 10;
            recommendations.add("Intenta mantener tus gastos por debajo del 50% de tus ingresos.");
        }

        // Evaluar el ahorro mensual
        if (porcentajeAhorro.compareTo(new BigDecimal("0.10")) < 0) {
            score -= 20;
            alerts.add("El nivel de ahorro mensual es bajo.");
            recommendations.add("Procura ahorrar al menos el 10% de tus ingresos.");
        } else if (porcentajeAhorro.compareTo(new BigDecimal("0.20")) < 0) {
            score -= 10;
            recommendations.add("Intenta aumentar gradualmente tu ahorro hasta el 20%.");
        }

        // Evaluar el nivel de endeudamiento
        if (nivelDeuda.compareTo(new BigDecimal("6.00")) > 0) {
            score -= 25;
            alerts.add("El nivel de endeudamiento es muy alto.");
            recommendations.add("Prioriza el pago de las deudas con mayor interés.");
        } else if (nivelDeuda.compareTo(new BigDecimal("3.00")) > 0) {
            score -= 15;
            alerts.add("El nivel de endeudamiento es elevado.");
            recommendations.add("Evita adquirir nuevas deudas.");
        } else if (nivelDeuda.compareTo(BigDecimal.ONE) > 0) {
            score -= 5;
            recommendations.add("Mantén un plan constante para reducir tus deudas.");
        }

        // Comprobar si los gastos y el ahorro superan los ingresos
        BigDecimal totalUtilizado = request.gastoMensual()
                .add(request.ahorroMensual());

        if (totalUtilizado.compareTo(request.ingresoMensual()) > 0) {
            score -= 20;
            alerts.add("La suma de gastos y ahorro supera el ingreso mensual.");
            recommendations.add("Ajusta los valores para mantener un presupuesto sostenible.");
        }

        score = Math.max(0, Math.min(100, score));

        String level;

        if (score >= 80) {
            level = "Excelente";
        } else if (score >= 60) {
            level = "Saludable";
        } else if (score >= 40) {
            level = "En riesgo";
        } else {
            level = "Crítico";
        }

        if (alerts.isEmpty()) {
            alerts.add("No se detectaron alertas financieras importantes.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Mantén tus hábitos financieros actuales.");
        }

        return new AnalisisResponse(
                score,
                level,
                alerts,
                recommendations
        );
    }
}