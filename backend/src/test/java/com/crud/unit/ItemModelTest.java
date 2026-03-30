package com.crud.unit;

import com.crud.model.Item;
import com.crud.model.ItemDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ItemModelTest {

    @Test
    @DisplayName("Should create item with all fields")
    void testItemCreation() {
        Item item = new Item("1", "Test", "Description", "Category", 100.0, 10);
        
        assertEquals("1", item.getId());
        assertEquals("Test", item.getName());
        assertEquals("Description", item.getDescription());
        assertEquals("Category", item.getCategory());
        assertEquals(100.0, item.getPrice());
        assertEquals(10, item.getQuantity());
        assertNotNull(item.getCreatedAt());
        assertNotNull(item.getUpdatedAt());
    }

    @Test
    @DisplayName("Should validate item correctly")
    void testIsValid() {
        Item validItem = new Item("1", "Test", "Desc", "Cat", 100.0, 10);
        assertTrue(validItem.isValid());
        
        Item invalidItemNoId = new Item(null, "Test", "Desc", "Cat", 100.0, 10);
        assertFalse(invalidItemNoId.isValid());
        
        Item invalidItemNoName = new Item("1", "", "Desc", "Cat", 100.0, 10);
        assertFalse(invalidItemNoName.isValid());
        
        Item invalidItemNegativePrice = new Item("1", "Test", "Desc", "Cat", -100.0, 10);
        assertFalse(invalidItemNegativePrice.isValid());
        
        Item invalidItemNegativeQty = new Item("1", "Test", "Desc", "Cat", 100.0, -10);
        assertFalse(invalidItemNegativeQty.isValid());
    }

    @Test
    @DisplayName("Should check equality based on ID")
    void testEquals() {
        Item item1 = new Item("1", "Test1", "Desc1", "Cat1", 100.0, 10);
        Item item2 = new Item("1", "Test2", "Desc2", "Cat2", 200.0, 20);
        Item item3 = new Item("2", "Test1", "Desc1", "Cat1", 100.0, 10);
        
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCode() {
        Item item1 = new Item("1", "Test", "Desc", "Cat", 100.0, 10);
        Item item2 = new Item("1", "Different", "Different", "Different", 0.0, 0);
        
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    @DisplayName("Should convert to DTO and back")
    void testItemDTOConversion() {
        Item item = new Item("1", "Test", "Description", "Category", 100.0, 10);
        
        ItemDTO dto = new ItemDTO(item);
        
        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getCategory(), dto.getCategory());
        assertEquals(item.getPrice(), dto.getPrice());
        assertEquals(item.getQuantity(), dto.getQuantity());
        
        Item converted = dto.toEntity();
        assertEquals(item.getId(), converted.getId());
        assertEquals(item.getName(), converted.getName());
    }

    @Test
    @DisplayName("Should create error DTO")
    void testItemDTOError() {
        ItemDTO errorDto = ItemDTO.error("Test error");
        
        assertEquals("Test error", errorDto.getError());
    }

    @Test
    @DisplayName("Should handle null item in DTO constructor")
    void testItemDTOFromNull() {
        ItemDTO dto = new ItemDTO((Item) null);
        
        assertNull(dto.getId());
    }
}
