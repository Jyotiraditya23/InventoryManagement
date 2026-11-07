package com.ev.erp.dto;

import com.ev.erp.model.ItemCategory;

import java.math.BigDecimal;

public record InventoryResponse(
        String id,
        String name,
        ItemCategory category,
        int quantity,
        BigDecimal unitPrice,
        String description
) {}