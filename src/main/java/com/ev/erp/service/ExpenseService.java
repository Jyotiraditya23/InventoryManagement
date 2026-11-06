package com.ev.erp.service;


import com.ev.erp.dto.ExpenseRequest;
import com.ev.erp.dto.ExpenseResponse;
import com.ev.erp.dto.ExpenseTotalResponse;
import com.ev.erp.exception.ExpenseNotFoundException;
import com.ev.erp.model.Expense;
import com.ev.erp.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    /** "Add" button. */
    public ExpenseResponse add(ExpenseRequest request) {
        return toResponse(repository.save(toModel(UUID.randomUUID().toString(), request)));
    }

    /**
     * Cards list (newest first). All filters are optional:
     * q = text in title/category/description, from/to = date range (inclusive).
     */
    public List<ExpenseResponse> list(String q, LocalDate from, LocalDate to) {
        return filter(q, from, to).stream()
                .sorted(Comparator.comparing(Expense::date, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    /** "Total expenses" bar, optionally for a date range (e.g. today, this month). */
    public ExpenseTotalResponse total(LocalDate from, LocalDate to) {
        List<Expense> expenses = filter(null, from, to);
        BigDecimal total = expenses.stream()
                .map(Expense::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> byCategory = new TreeMap<>();
        for (Expense e : expenses) {
            byCategory.merge(e.category(), e.amount(), BigDecimal::add);
        }
        return new ExpenseTotalResponse(total, expenses.size(), byCategory);
    }

    public ExpenseResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    public ExpenseResponse update(String id, ExpenseRequest request) {
        return repository.update(toModel(id, request))
                .map(this::toResponse)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new ExpenseNotFoundException(id);
        }
    }

    // ---------- helpers ----------

    private List<Expense> filter(String q, LocalDate from, LocalDate to) {
        String needle = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(e -> from == null || (e.date() != null && !e.date().isBefore(from)))
                .filter(e -> to == null || (e.date() != null && !e.date().isAfter(to)))
                .filter(e -> needle == null
                        || contains(e.title(), needle)
                        || contains(e.category(), needle)
                        || contains(e.description(), needle))
                .toList();
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private Expense toModel(String id, ExpenseRequest r) {
        return new Expense(id, r.title().trim(), r.category().trim(), r.amount(), r.date(),
                r.description() == null ? "" : r.description().trim());
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.id(), e.title(), e.category(), e.amount(), e.date(), e.description());
    }
}