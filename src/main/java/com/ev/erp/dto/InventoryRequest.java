package com.ev.erp.dto;

import com.ev.erp.model.ItemCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** Payload for adding / updating an inventory item. */
public record InventoryRequest(
        @NotBlank(message = "Item name is required") String name,
        @NotNull(message = "Category is required (SCOOTY, BATTERY or OTHERS)") ItemCategory category,
        @NotNull(message = "Quantity is required") @Min(value = 0, message = "Quantity cannot be negative") Integer quantity,
        @PositiveOrZero(message = "Unit price cannot be negative") BigDecimal unitPrice, // optional
        String description                                                               // optional
) {}