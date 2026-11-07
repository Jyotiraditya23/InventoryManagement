package com.ev.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Payload for the "ADD" button and for update. */
public record OtherIncomeRequest(
        @NotBlank(message = "Name is required") String name,
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be greater than 0") BigDecimal amount,
        @NotNull(message = "Date is required") LocalDate date
) {}
