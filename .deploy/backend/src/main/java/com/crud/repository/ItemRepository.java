package com.crud.repository;

import com.crud.exception.ResourceNotFoundException;
import com.crud.model.Item;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ItemRepository {
    private final Map<String, Item> items;
    private final AtomicInteger idCounter;

    public ItemRepository() {
        this.items = new ConcurrentHashMap<>();
        this.idCounter = new AtomicInteger(1);
    }

    protected void initializeSampleData() {
    }

    public synchronized String generateId() {
        return String.valueOf(idCounter.getAndIncrement());
    }

    public Item save(Item item) {
        if (item.getId() == null || item.getId().isBlank()) {
            item.setId(generateId());
        }
        item.setUpdatedAt(java.time.LocalDateTime.now());
        items.put(item.getId(), item);
        return item;
    }

    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    public Item findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        Item item = items.get(id);
        if (item == null) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }
        return item;
    }

    public List<Item> findByCategory(String category) {
        if (category == null || category.isBlank()) {
            return findAll();
        }
        return items.values().stream()
                .filter(item -> category.equalsIgnoreCase(item.getCategory()))
                .collect(Collectors.toList());
    }

    public Item update(String id, Item item) {
        if (!items.containsKey(id)) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }
        item.setId(id);
        item.setCreatedAt(items.get(id).getCreatedAt());
        item.setUpdatedAt(java.time.LocalDateTime.now());
        items.put(id, item);
        return item;
    }

    public void delete(String id) {
        if (!items.containsKey(id)) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }
        items.remove(id);
    }

    public boolean exists(String id) {
        return id != null && items.containsKey(id);
    }

    public int count() {
        return items.size();
    }

    public void clear() {
        items.clear();
        idCounter.set(1);
    }
}
