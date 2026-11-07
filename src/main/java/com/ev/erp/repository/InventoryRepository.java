package com.ev.erp.repository;

import com.ev.erp.model.InventoryItem;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    List<InventoryItem> findAll();
    Optional<InventoryItem> findById(String id);
    InventoryItem save(InventoryItem item);
    Optional<InventoryItem> update(InventoryItem item);
    boolean deleteById(String id);
}