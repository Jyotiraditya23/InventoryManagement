package com.ev.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Payload for "Add details" and update. */
public record ScootyRequest(
        @NotBlank(message = "Owner name is required") String ownerName,
        @NotNull(message = "Date is required") LocalDate date,
        @NotNull(message = "Amount is required") @PositiveOrZero BigDecimal amount,
        // any number of batteries (4/5/6/...)
        @NotEmpty(message = "At least one battery number is required") String batteryNumbers,
        // value chosen from inventory (fetched via the inventory module later)
        @NotBlank(message = "Bike name is required") String bikeName,
        @NotBlank(message = "Chassis number is required") String chassisNo,
        @NotBlank(message = "Motor number is required") String motorNo,
        @NotBlank(message = "Charger number is required") String chargerNo
) {}
