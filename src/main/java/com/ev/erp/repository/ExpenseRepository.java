package com.ev.erp.repository;

import com.ev.erp.model.Expense;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository {
    List<Expense> findAll();
    Optional<Expense> findById(String id);
    Expense save(Expense expense);
    Optional<Expense> update(Expense expense);
    boolean deleteById(String id);
}