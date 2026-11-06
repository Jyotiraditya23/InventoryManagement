package com.ev.erp.exception;

public class ScootyNotFoundException extends RuntimeException {
    public ScootyNotFoundException(String id) {
        super("Scooty not found with id: " + id);
    }
}