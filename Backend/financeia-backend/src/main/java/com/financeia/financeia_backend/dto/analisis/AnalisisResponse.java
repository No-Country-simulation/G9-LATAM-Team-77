package com.financeia.financeia_backend.dto.analisis;

import java.util.List;

public record AnalisisResponse(
        Integer score,
        String level,
        List<String> alerts,
        List<String> recommendations
) {
}
