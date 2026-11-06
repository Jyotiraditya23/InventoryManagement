package com.ev.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One day-to-day expense. Maps to one row in the "Expenses" sheet.
 * Columns: A id | B title | C category | D amount | E date | F description
 */
public record Expense(
        String id,
        String title,
        String category,
        BigDecimal amount,
        LocalDate date,
        String description
) {}