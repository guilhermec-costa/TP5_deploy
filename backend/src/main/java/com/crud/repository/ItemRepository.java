package com.crud.repository;

import com.crud.model.Item;
import java.util.List;

public interface ItemRepository {
  Item save(Item item);

  List<Item> findAll();

  Item findById(String id);

  List<Item> findByCategory(String category);

  Item update(String id, Item item);

  void delete(String id);

  boolean exists(String id);

  int count();

  void clear();

  String generateId();
}