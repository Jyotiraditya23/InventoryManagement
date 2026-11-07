package com.ev.erp.dto;

import java.math.BigDecimal;

/** One month of the month-wise report. */
public record MonthlyIncomeResponse(
        String month,        // "2026-01"
        String monthName,    // "January"
        BigDecimal scootyIncome,
        int scootiesSold,
        BigDecimal otherIncome,
        BigDecimal totalIncome
) {}
