package com.ev.erp.controller;

import com.ev.erp.dto.InventoryRequest;
import com.ev.erp.dto.InventoryResponse;
import com.ev.erp.model.ItemCategory;
import com.ev.erp.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    /** Add item (category required in body). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse add(@Valid @RequestBody InventoryRequest request) {
        return service.add(request);
    }

    /**
     * Tabs:
     *   All     -> GET /api/inventory
     *   Scooty  -> GET /api/inventory?category=SCOOTY
     *   Battery -> GET /api/inventory?category=BATTERY
     *   Others  -> GET /api/inventory?category=OTHERS
     * Optional name search: &q=ather
     * Also use ?category=SCOOTY to fill the "bike name" dropdown on the Scooty form.
     */
    @GetMapping
    public List<InventoryResponse> list(@RequestParam(required = false) ItemCategory category,
                                        @RequestParam(required = false) String q) {
        return service.list(category, q);
    }

    @GetMapping("/{id}")
    public InventoryResponse get(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public InventoryResponse update(@PathVariable String id, @Valid @RequestBody InventoryRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}