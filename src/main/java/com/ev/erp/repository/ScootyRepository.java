package com.ev.erp.repository;

import com.ev.erp.model.Scooty;

import java.util.List;
import java.util.Optional;

public interface ScootyRepository {
    List<Scooty> findAll();
    Optional<Scooty> findById(String id);
    Scooty save(Scooty scooty);             // append a new row
    Optional<Scooty> update(Scooty scooty); // overwrite row with same id
    boolean deleteById(String id);
}
