package com.ev.erp.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Data for the "Total expenses" bar. */
public record ExpenseTotalResponse(
        BigDecimal total,
        int count,
        Map<String, BigDecimal> byCategory
) {}