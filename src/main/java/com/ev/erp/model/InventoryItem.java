package com.ev.erp.model;

import java.math.BigDecimal;

/**
 * One inventory item. Maps to one row in the "Inventory" sheet.
 * Columns: A id | B name | C category | D quantity | E unitPrice | F description
 */
public record InventoryItem(
        String id,
        String name,
        ItemCategory category,
        int quantity,
        BigDecimal unitPrice,   // optional, may be null
        String description
) {
    public InventoryItem withQuantity(int newQuantity) {
        return new InventoryItem(id, name, category, newQuantity, unitPrice, description);
    }
}