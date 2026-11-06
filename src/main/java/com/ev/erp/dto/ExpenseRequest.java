package com.ev.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Payload for the "Add" button and for update. */
public record ExpenseRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Category is required") String category,   // e.g. Rent, Electricity, Salary, Tea, Transport
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be greater than 0") BigDecimal amount,
        @NotNull(message = "Date is required") LocalDate date,
        String description                                              // optional
) {}