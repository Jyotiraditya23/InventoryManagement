package com.ev.erp.service;

import com.ev.erp.dto.ScootyRequest;
import com.ev.erp.dto.ScootyResponse;
import com.ev.erp.exception.ScootyNotFoundException;
import com.ev.erp.model.Scooty;
import com.ev.erp.repository.ScootyRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ScootyService {

    private final ScootyRepository repository;

    public ScootyService(ScootyRepository repository) {
        this.repository = repository;
    }

    /** "Add details" button. */
    public ScootyResponse add(ScootyRequest request) {
        Scooty scooty = toModel(UUID.randomUUID().toString(), request);
        return toResponse(repository.save(scooty));
    }

    /** List of cards on the page (newest first). */
    public List<ScootyResponse> getAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Scooty::date,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    public ScootyResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ScootyNotFoundException(id));
    }

    /** Search bar: matches owner, bike, chassis, motor, charger or any battery number. */
    public List<ScootyResponse> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        String q = query.trim().toLowerCase(Locale.ROOT);
        return getAll().stream()
                .filter(s -> contains(s.ownerName(), q)
                        || contains(s.bikeName(), q)
                        || contains(s.chassisNo(), q)
                        || contains(s.motorNo(), q)
                        || contains(s.chargerNo(), q)
                        || contains(s.batteryNumbers(), q))
                .toList();
    }

    public ScootyResponse update(String id, ScootyRequest request) {
        return repository.update(toModel(id, request))
                .map(this::toResponse)
                .orElseThrow(() -> new ScootyNotFoundException(id));
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new ScootyNotFoundException(id);
        }
    }

    // ---------- mapping ----------

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    private Scooty toModel(String id, ScootyRequest r) {
        return new Scooty(id, r.ownerName().trim(), r.date(), r.amount(),
                r.batteryNumbers().trim(),
                r.bikeName().trim(), r.chassisNo().trim(),
                r.motorNo().trim(), r.chargerNo().trim());
    }

    private ScootyResponse toResponse(Scooty s) {
        return new ScootyResponse(s.id(), s.ownerName(), s.date(), s.amount(),
                s.batteryNumbers(), s.bikeName(), s.chassisNo(), s.motorNo(), s.chargerNo());
    }
}