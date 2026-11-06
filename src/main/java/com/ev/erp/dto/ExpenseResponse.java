package com.ev.erp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        String id,
        String title,
        String category,
        BigDecimal amount,
        LocalDate date,
        String description
) {}
