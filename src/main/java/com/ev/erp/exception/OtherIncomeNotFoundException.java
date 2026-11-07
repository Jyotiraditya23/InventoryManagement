package com.ev.erp.exception;

public class OtherIncomeNotFoundException extends RuntimeException {
    public OtherIncomeNotFoundException(String id) {
        super("Income entry not found with id: " + id);
    }
}
