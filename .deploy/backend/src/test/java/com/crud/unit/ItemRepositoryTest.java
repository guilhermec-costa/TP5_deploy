package com.crud.unit;

import com.crud.exception.ResourceNotFoundException;
import com.crud.model.Item;
import com.crud.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRepositoryTest {
    private ItemRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ItemRepository();
    }

    @Test
    @DisplayName("Should save item and generate ID")
    void testSave() {
        Item item = new Item(null, "Test", "Desc", "Cat", 100.0, 10);
        
        Item saved = repository.save(item);
        
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Should find all items")
    void testFindAll() {
        List<Item> items = repository.findAll();
        
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    @DisplayName("Should find item by ID")
    void testFindById() {
        Item saved = repository.save(new Item(null, "Test", "Desc", "Cat", 100.0, 10));
        
        Item found = repository.findById(saved.getId());
        
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @DisplayName("Should throw exception for null ID")
    void testFindById_NullId() {
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }

    @Test
    @DisplayName("Should throw exception for empty ID")
    void testFindById_EmptyId() {
        assertThrows(IllegalArgumentException.class, () -> repository.findById(""));
    }

    @Test
    @DisplayName("Should throw exception when item not found")
    void testFindById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> repository.findById("99999"));
    }

    @Test
    @DisplayName("Should update item")
    void testUpdate() {
        Item saved = repository.save(new Item(null, "Original", "Desc", "Cat", 100.0, 10));
        
        Item updated = new Item(saved.getId(), "Updated", "New Desc", "New Cat", 200.0, 20);
        repository.update(saved.getId(), updated);
        
        Item found = repository.findById(saved.getId());
        
        assertEquals("Updated", found.getName());
        assertEquals(200.0, found.getPrice());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent item")
    void testUpdate_NotFound() {
        Item item = new Item("99999", "Test", "Desc", "Cat", 100.0, 10);
        
        assertThrows(ResourceNotFoundException.class, () -> repository.update("99999", item));
    }

    @Test
    @DisplayName("Should delete item")
    void testDelete() {
        Item saved = repository.save(new Item(null, "Test", "Desc", "Cat", 100.0, 10));
        
        repository.delete(saved.getId());
        
        assertThrows(ResourceNotFoundException.class, () -> repository.findById(saved.getId()));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent item")
    void testDelete_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> repository.delete("99999"));
    }

    @Test
    @DisplayName("Should find by category")
    void testFindByCategory() {
        repository.save(new Item(null, "Item1", "Desc", "Electronics", 100.0, 10));
        repository.save(new Item(null, "Item2", "Desc", "Books", 50.0, 5));
        
        List<Item> electronics = repository.findByCategory("Electronics");
        
        assertTrue(electronics.stream().allMatch(i -> "Electronics".equals(i.getCategory())));
    }

    @Test
    @DisplayName("Should return all items for null category")
    void testFindByCategory_Null() {
        List<Item> items = repository.findByCategory(null);
        
        assertFalse(items.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list for non-existent category")
    void testFindByCategory_NotFound() {
        List<Item> items = repository.findByCategory("NonExistent");
        
        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Should check if item exists")
    void testExists() {
        Item saved = repository.save(new Item(null, "Test", "Desc", "Cat", 100.0, 10));
        
        assertTrue(repository.exists(saved.getId()));
        assertFalse(repository.exists("99999"));
    }

    @Test
    @DisplayName("Should return correct count")
    void testCount() {
        int initialCount = repository.count();
        repository.save(new Item(null, "Test", "Desc", "Cat", 100.0, 10));
        
        assertEquals(initialCount + 1, repository.count());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    @DisplayName("Should handle blank IDs in findById")
    void testFindById_BlankId(String id) {
        assertThrows(IllegalArgumentException.class, () -> repository.findById(id));
    }

    @Test
    @DisplayName("Should preserve createdAt on update")
    void testUpdate_PreservesCreatedAt() {
        Item saved = repository.save(new Item(null, "Original", "Desc", "Cat", 100.0, 10));
        java.time.LocalDateTime originalCreatedAt = saved.getCreatedAt();
        
        Item updated = new Item(saved.getId(), "Updated", "New Desc", "New Cat", 200.0, 20);
        repository.update(saved.getId(), updated);
        
        Item found = repository.findById(saved.getId());
        
        assertEquals(originalCreatedAt, found.getCreatedAt());
    }
}
