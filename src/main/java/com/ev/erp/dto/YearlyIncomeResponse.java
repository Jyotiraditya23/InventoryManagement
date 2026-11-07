package com.ev.erp.dto;

import java.math.BigDecimal;
import java.util.List;

/** Jan-Dec breakdown for one year, plus the year's totals. */
public record YearlyIncomeResponse(
        int year,
        List<MonthlyIncomeResponse> months,
        BigDecimal scootyIncome,
        BigDecimal otherIncome,
        BigDecimal totalIncome
) {}
