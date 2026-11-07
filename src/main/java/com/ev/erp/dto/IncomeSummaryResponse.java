package com.ev.erp.dto;

import java.math.BigDecimal;

/**
 * Feeds the top "Total Income" bar and the "Total income from scooties" box.
 * period = "2026-11" for a month, or "ALL" for all time.
 */
public record IncomeSummaryResponse(
        String period,
        BigDecimal scootyIncome,
        int scootiesSold,
        BigDecimal otherIncome,
        BigDecimal totalIncome
) {}
