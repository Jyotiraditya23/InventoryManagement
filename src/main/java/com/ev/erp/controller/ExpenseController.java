package com.ev.erp.controller;


import com.ev.erp.dto.ExpenseRequest;
import com.ev.erp.dto.ExpenseResponse;
import com.ev.erp.dto.ExpenseTotalResponse;
import com.ev.erp.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    /** Add */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse add(@Valid @RequestBody ExpenseRequest request) {
        return service.add(request);
    }

    /** Cards. Optional: ?q=tea&from=2026-11-01&to=2026-11-30 */
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.list(q, from, to);
    }

    /** Total expenses bar. Optional: ?from=2026-11-01&to=2026-11-30 */
    @GetMapping("/total")
    public ExpenseTotalResponse total(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.total(from, to);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable String id, @Valid @RequestBody ExpenseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}