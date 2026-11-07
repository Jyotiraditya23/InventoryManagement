package com.ev.erp.service;

import com.ev.erp.dto.*;
import com.ev.erp.exception.OtherIncomeNotFoundException;
import com.ev.erp.model.OtherIncome;
import com.ev.erp.model.Scooty;
import com.ev.erp.repository.OtherIncomeRepository;
import com.ev.erp.repository.ScootyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Income = income from scooty sales (read from ScootyRepository, never stored twice)
 *        + other incomes (stored in their own Excel file).
 */
@Service
public class IncomeService {

    private final ScootyRepository scootyRepository;
    private final OtherIncomeRepository otherIncomeRepository;

    public IncomeService(ScootyRepository scootyRepository, OtherIncomeRepository otherIncomeRepository) {
        this.scootyRepository = scootyRepository;
        this.otherIncomeRepository = otherIncomeRepository;
    }

    // ---------- summary (Total Income + Total income from scooties) ----------

    /** month = null -> all time. */
    public IncomeSummaryResponse summary(YearMonth month) {
        List<Scooty> scooties = scootyRepository.findAll();
        List<OtherIncome> others = otherIncomeRepository.findAll();

        BigDecimal scootyIncome = sumScooties(scooties, month);
        BigDecimal otherIncome = sumOthers(others, month);
        return new IncomeSummaryResponse(
                month == null ? "ALL" : month.toString(),
                scootyIncome,
                countScooties(scooties, month),
                otherIncome,
                scootyIncome.add(otherIncome));
    }

    // ---------- month-wise report (Jan..Dec of a year) ----------

    public YearlyIncomeResponse monthly(Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        List<Scooty> scooties = scootyRepository.findAll();
        List<OtherIncome> others = otherIncomeRepository.findAll();

        List<MonthlyIncomeResponse> months = new ArrayList<>();
        BigDecimal yearScooty = BigDecimal.ZERO;
        BigDecimal yearOther = BigDecimal.ZERO;

        for (Month m : Month.values()) {
            YearMonth ym = YearMonth.of(y, m);
            BigDecimal s = sumScooties(scooties, ym);
            BigDecimal o = sumOthers(others, ym);
            months.add(new MonthlyIncomeResponse(
                    ym.toString(),
                    m.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    s, countScooties(scooties, ym), o, s.add(o)));
            yearScooty = yearScooty.add(s);
            yearOther = yearOther.add(o);
        }
        return new YearlyIncomeResponse(y, months, yearScooty, yearOther, yearScooty.add(yearOther));
    }

    // ---------- other incomes (cards + ADD button) ----------

    public OtherIncomeResponse add(OtherIncomeRequest request) {
        return toResponse(otherIncomeRepository.save(toModel(UUID.randomUUID().toString(), request)));
    }

    /** Cards, newest first. Optional month filter (e.g. 2026-11) and name search. */
    public List<OtherIncomeResponse> listOther(YearMonth month, String q) {
        String needle = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);
        return otherIncomeRepository.findAll().stream()
                .filter(i -> inMonth(i.date(), month))
                .filter(i -> needle == null || i.name().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing(OtherIncome::date, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    public OtherIncomeResponse getOther(String id) {
        return otherIncomeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new OtherIncomeNotFoundException(id));
    }

    public OtherIncomeResponse updateOther(String id, OtherIncomeRequest request) {
        return otherIncomeRepository.update(toModel(id, request))
                .map(this::toResponse)
                .orElseThrow(() -> new OtherIncomeNotFoundException(id));
    }

    public void deleteOther(String id) {
        if (!otherIncomeRepository.deleteById(id)) {
            throw new OtherIncomeNotFoundException(id);
        }
    }

    // ---------- helpers ----------

    private boolean inMonth(LocalDate date, YearMonth month) {
        return month == null || (date != null && YearMonth.from(date).equals(month));
    }

    private BigDecimal sumScooties(List<Scooty> scooties, YearMonth month) {
        return scooties.stream()
                .filter(s -> inMonth(s.date(), month))
                .map(Scooty::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int countScooties(List<Scooty> scooties, YearMonth month) {
        return (int) scooties.stream().filter(s -> inMonth(s.date(), month)).count();
    }

    private BigDecimal sumOthers(List<OtherIncome> others, YearMonth month) {
        return others.stream()
                .filter(i -> inMonth(i.date(), month))
                .map(OtherIncome::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OtherIncome toModel(String id, OtherIncomeRequest r) {
        return new OtherIncome(id, r.name().trim(), r.amount(), r.date());
    }

    private OtherIncomeResponse toResponse(OtherIncome i) {
        return new OtherIncomeResponse(i.id(), i.name(), i.amount(), i.date());
    }
}
