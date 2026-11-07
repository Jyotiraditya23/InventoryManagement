package com.ev.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Any income that is NOT a scooty sale (e.g. service, accessories, scrap, commission).
 * Maps to one row in the "OtherIncome" sheet.
 * Columns: A id | B name | C amount | D date
 */
public record OtherIncome(
        String id,
        String name,
        BigDecimal amount,
        LocalDate date
) {}
