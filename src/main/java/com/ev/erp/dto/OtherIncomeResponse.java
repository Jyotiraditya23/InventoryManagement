package com.ev.erp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OtherIncomeResponse(
        String id,
        String name,
        BigDecimal amount,
        LocalDate date
) {}
