package com.ev.erp.repository;

import com.ev.erp.model.OtherIncome;

import java.util.List;
import java.util.Optional;

public interface OtherIncomeRepository {
    List<OtherIncome> findAll();
    Optional<OtherIncome> findById(String id);
    OtherIncome save(OtherIncome income);
    Optional<OtherIncome> update(OtherIncome income);
    boolean deleteById(String id);
}
