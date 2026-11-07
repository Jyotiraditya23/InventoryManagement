package com.ev.erp.service;

import com.ev.erp.dto.InventoryRequest;
import com.ev.erp.dto.InventoryResponse;
import com.ev.erp.exception.DuplicateInventoryItemException;
import com.ev.erp.exception.InsufficientStockException;
import com.ev.erp.exception.InventoryItemNotFoundException;
import com.ev.erp.model.InventoryItem;
import com.ev.erp.model.ItemCategory;
import com.ev.erp.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    // ---------- screen features ----------

    /** Add item (category is mandatory, so the item is always identified correctly). */
    public synchronized InventoryResponse add(InventoryRequest request) {
        ensureNotDuplicate(null, request.name().trim(), request.category());
        InventoryItem item = toModel(UUID.randomUUID().toString(), request);
        return toResponse(repository.save(item));
    }

    /**
     * Tabs: All (category = null) | Scooty | Battery | Others.
     * Optional q filters by name.
     */
    public List<InventoryResponse> list(ItemCategory category, String q) {
        String needle = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(i -> category == null || i.category() == category)
                .filter(i -> needle == null || i.name().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing(i -> i.name().toLowerCase(Locale.ROOT)))
                .map(this::toResponse)
                .toList();
    }

    public InventoryResponse getById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new InventoryItemNotFoundException(id));
    }

    public synchronized InventoryResponse update(String id, InventoryRequest request) {
        ensureNotDuplicate(id, request.name().trim(), request.category());
        return repository.update(toModel(id, request))
                .map(this::toResponse)
                .orElseThrow(() -> new InventoryItemNotFoundException(id));
    }

    public synchronized void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new InventoryItemNotFoundException(id);
        }
    }

    // ---------- used by ScootyService ----------

    /**
     * Takes {@code qty} scooties out of stock. Throws InsufficientStockException if the scooty
     * is not in inventory or there is not enough of it.
     */
    public synchronized InventoryItem reserveScooty(String bikeName, int qty) {
        InventoryItem item = findScooty(bikeName);
        if (item == null) {
            throw new InsufficientStockException(
                    "Insufficient scooty: '" + bikeName + "' is not available in inventory. "
                            + "Add it to inventory under category SCOOTY first.");
        }
        if (item.quantity() < qty) {
            throw new InsufficientStockException(
                    "Insufficient scooty: only " + item.quantity() + " '" + item.name()
                            + "' in stock, " + qty + " required.");
        }
        return repository.update(item.withQuantity(item.quantity() - qty)).orElseThrow();
    }

    /** Puts scooties back into stock (used to undo a reservation if saving the scooty fails). */
    public synchronized void releaseScooty(String bikeName, int qty) {
        InventoryItem item = findScooty(bikeName);
        if (item != null) {
            repository.update(item.withQuantity(item.quantity() + qty));
        }
    }

    // ---------- helpers ----------

    private InventoryItem findScooty(String bikeName) {
        String name = bikeName == null ? "" : bikeName.trim();
        return repository.findAll().stream()
                .filter(i -> i.category() == ItemCategory.SCOOTY)
                .filter(i -> i.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private void ensureNotDuplicate(String ignoreId, String name, ItemCategory category) {
        boolean exists = repository.findAll().stream()
                .anyMatch(i -> !i.id().equals(ignoreId)
                        && i.category() == category
                        && i.name().equalsIgnoreCase(name));
        if (exists) throw new DuplicateInventoryItemException(name, category);
    }

    private InventoryItem toModel(String id, InventoryRequest r) {
        return new InventoryItem(id, r.name().trim(), r.category(), r.quantity(), r.unitPrice(),
                r.description() == null ? "" : r.description().trim());
    }

    private InventoryResponse toResponse(InventoryItem i) {
        return new InventoryResponse(i.id(), i.name(), i.category(), i.quantity(), i.unitPrice(), i.description());
    }
}