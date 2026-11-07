package com.ev.erp.controller;

import com.ev.erp.dto.*;
import com.ev.erp.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/income")
public class IncomeController {

    private final IncomeService service;

    public IncomeController(IncomeService service) {
        this.service = service;
    }

    /**
     * "Total Income" bar + "Total income from scooties" box.
     * All time: GET /api/income/summary
     * One month: GET /api/income/summary?month=2026-11
     */
    @GetMapping("/summary")
    public IncomeSummaryResponse summary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.summary(month);
    }

    /**
     * Month-wise tracking: January..December of a year.
     * GET /api/income/monthly?year=2026   (defaults to the current year)
     */
    @GetMapping("/monthly")
    public YearlyIncomeResponse monthly(@RequestParam(required = false) Integer year) {
        return service.monthly(year);
    }

    // ---------- other incomes ----------

    /** ADD button */
    @PostMapping("/other")
    @ResponseStatus(HttpStatus.CREATED)
    public OtherIncomeResponse add(@Valid @RequestBody OtherIncomeRequest request) {
        return service.add(request);
    }

    /** "Other incomes" cards. Optional: ?month=2026-11&q=service */
    @GetMapping("/other")
    public List<OtherIncomeResponse> listOther(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) String q) {
        return service.listOther(month, q);
    }

    @GetMapping("/other/{id}")
    public OtherIncomeResponse getOther(@PathVariable String id) {
        return service.getOther(id);
    }

    @PutMapping("/other/{id}")
    public OtherIncomeResponse updateOther(@PathVariable String id, @Valid @RequestBody OtherIncomeRequest request) {
        return service.updateOther(id, request);
    }

    @DeleteMapping("/other/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOther(@PathVariable String id) {
        service.deleteOther(id);
    }
}
