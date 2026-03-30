package com.crud.repository;

import com.crud.exception.ResourceNotFoundException;
import com.crud.model.Item;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryItemRepository implements ItemRepository {
  private final Map<String, Item> items;
  private final AtomicInteger idCounter;

  public InMemoryItemRepository() {
    this.items = new ConcurrentHashMap<>();
    this.idCounter = new AtomicInteger(1);
    initializeSampleData();
  }

  private void initializeSampleData() {
    save(new Item(null, "Laptop", "High-performance laptop", "Electronics", 2500.0, 10));
  }

  @Override
  public synchronized String generateId() {
    return String.valueOf(idCounter.getAndIncrement());
  }

  @Override
  public Item save(Item item) {
    if (item.getId() == null || item.getId().isBlank()) {
      item.setId(generateId());
      item.setCreatedAt(java.time.LocalDateTime.now());
    }
    item.setUpdatedAt(java.time.LocalDateTime.now());
    items.put(item.getId(), item);
    return item;
  }

  @Override
  public List<Item> findAll() {
    return new ArrayList<>(items.values());
  }

  @Override
  public Item findById(String id) {
    if (id == null || id.isBlank())
      throw new IllegalArgumentException("ID cannot be null");
    Item item = items.get(id);
    if (item == null)
      throw new ResourceNotFoundException("Item not found: " + id);
    return item;
  }

  @Override
  public List<Item> findByCategory(String category) {
    if (category == null || category.isBlank())
      return findAll();
    return items.values().stream()
        .filter(item -> category.equalsIgnoreCase(item.getCategory()))
        .collect(Collectors.toList());
  }

  @Override
  public Item update(String id, Item item) {
    Item existing = findById(id);
    item.setId(id);
    item.setCreatedAt(existing.getCreatedAt());
    item.setUpdatedAt(java.time.LocalDateTime.now());
    items.put(id, item);
    return item;
  }

  @Override
  public void delete(String id) {
    if (!items.containsKey(id))
      throw new ResourceNotFoundException("Item not found: " + id);
    items.remove(id);
  }

  @Override
  public boolean exists(String id) {
    return id != null && items.containsKey(id);
  }

  @Override
  public int count() {
    return items.size();
  }

  @Override
  public void clear() {
    items.clear();
    idCounter.set(1);
  }
}