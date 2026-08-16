package com.financeia.financeia_backend.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        String category,
        String type,
        LocalDate date
) {
}