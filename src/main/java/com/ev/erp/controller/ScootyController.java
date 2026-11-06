package com.ev.erp.controller;

import com.ev.erp.dto.ScootyRequest;
import com.ev.erp.dto.ScootyResponse;
import com.ev.erp.service.ScootyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scooties")
public class ScootyController {

    private final ScootyService service;

    public ScootyController(ScootyService service) {
        this.service = service;
    }

    /** Add details */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScootyResponse add(@Valid @RequestBody ScootyRequest request) {
        return service.add(request);
    }

    /** List all, or search: GET /api/scooties?q=Ravi */
    @GetMapping
    public List<ScootyResponse> list(@RequestParam(required = false) String q) {
        return service.search(q);
    }

    @GetMapping("/{id}")
    public ScootyResponse get(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ScootyResponse update(@PathVariable String id, @Valid @RequestBody ScootyRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
