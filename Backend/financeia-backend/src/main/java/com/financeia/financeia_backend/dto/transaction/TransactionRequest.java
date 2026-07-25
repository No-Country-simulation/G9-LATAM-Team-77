package com.financeia.financeia_backend.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        String description,
        BigDecimal amount,
        LocalDate date,
        String category
) {
}
