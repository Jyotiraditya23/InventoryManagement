package com.ev.erp.exception;

import com.ev.erp.model.ItemCategory;

public class DuplicateInventoryItemException extends RuntimeException {
    public DuplicateInventoryItemException(String name, ItemCategory category) {
        super("Item '" + name + "' already exists in category " + category
                + ". Edit its quantity instead of adding it again.");
    }
}