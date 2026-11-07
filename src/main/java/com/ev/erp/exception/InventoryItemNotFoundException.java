package com.ev.erp.exception;

public class InventoryItemNotFoundException extends RuntimeException {
    public InventoryItemNotFoundException(String id) {
        super("Inventory item not found with id: " + id);
    }
}