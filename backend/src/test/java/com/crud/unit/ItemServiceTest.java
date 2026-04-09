package com.crud.unit;

import com.crud.exception.ResourceNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.model.ItemDTO;
import com.crud.repository.InMemoryItemRepository;
import com.crud.repository.ItemRepository;
import com.crud.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {
    private ItemRepository repository;
    private ItemService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryItemRepository();
        service = new ItemService(repository);
    }

    @Test
    @DisplayName("Should create item successfully")
    void testCreateItem_Success() {
        ItemDTO dto = new ItemDTO();
        dto.setName("Test Item");
        dto.setDescription("Test Description");
        dto.setCategory("Test Category");
        dto.setPrice(100.0);
        dto.setQuantity(10);

        ItemDTO result = service.createItem(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals(100.0, result.getPrice());
    }

    @Test
    @DisplayName("Should get all items")
    void testGetAllItems() {
        List<ItemDTO> items = service.getAllItems();

        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    @DisplayName("Should get item by ID")
    void testGetItemById_Success() {
        ItemDTO created = service.createItem(createValidItemDTO("GetTest"));

        ItemDTO result = service.getItemById(created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals("GetTest", result.getName());
    }

    @Test
    @DisplayName("Should throw exception for invalid ID")
    void testGetItemById_InvalidId() {
        assertThrows(ValidationException.class, () -> service.getItemById(""));
        assertThrows(ValidationException.class, () -> service.getItemById(null));
    }

    @Test
    @DisplayName("Should throw exception for non-existent ID")
    void testGetItemById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.getItemById("99999"));
    }

    @Test
    @DisplayName("Should update item successfully")
    void testUpdateItem_Success() {
        ItemDTO created = service.createItem(createValidItemDTO("Original"));

        ItemDTO updateDto = new ItemDTO();
        updateDto.setName("Updated");
        updateDto.setDescription("New Description");
        updateDto.setCategory("New Category");
        updateDto.setPrice(200.0);
        updateDto.setQuantity(20);

        ItemDTO result = service.updateItem(created.getId(), updateDto);

        assertNotNull(result);
        assertEquals("Updated", result.getName());
        assertEquals(200.0, result.getPrice());
    }

    @Test
    @DisplayName("Should delete item successfully")
    void testDeleteItem_Success() {
        ItemDTO created = service.createItem(createValidItemDTO("ToDelete"));

        assertDoesNotThrow(() -> service.deleteItem(created.getId()));
        
        assertThrows(ResourceNotFoundException.class, () -> service.getItemById(created.getId()));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent item")
    void testDeleteItem_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.deleteItem("99999"));
    }

    @Test
    @DisplayName("Should validate null price")
    void testCreateItem_NullPrice() {
        ItemDTO dto = createValidItemDTO("Test");
        dto.setPrice(null);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.createItem(dto));
        assertTrue(ex.getMessage().contains("Price"));
    }

    @Test
    @DisplayName("Should validate negative price")
    void testCreateItem_NegativePrice() {
        ItemDTO dto = createValidItemDTO("Test");
        dto.setPrice(-10.0);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.createItem(dto));
        assertTrue(ex.getMessage().contains("Price"));
    }

    @Test
    @DisplayName("Should validate price too high")
    void testCreateItem_PriceTooHigh() {
        ItemDTO dto = createValidItemDTO("Test");
        dto.setPrice(1000000.0);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.createItem(dto));
        assertTrue(ex.getMessage().contains("Price"));
    }

    @Test
    @DisplayName("Should validate null quantity")
    void testCreateItem_NullQuantity() {
        ItemDTO dto = createValidItemDTO("Test");
        dto.setQuantity(null);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.createItem(dto));
        assertTrue(ex.getMessage().contains("Quantity"));
    }

    @Test
    @DisplayName("Should validate negative quantity")
    void testCreateItem_NegativeQuantity() {
        ItemDTO dto = createValidItemDTO("Test");
        dto.setQuantity(-5);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.createItem(dto));
        assertTrue(ex.getMessage().contains("Quantity"));
    }

    @Test
    @DisplayName("Should validate quantity too high")
    void testCreateItem_QuantityTooHigh() {
        ItemDTO dto = createValidItemDTO("Test");
        dto.setQuantity(1000000);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.createItem(dto));
        assertTrue(ex.getMessage().contains("Quantity"));
    }

    @Test
    @DisplayName("Should validate name is required")
    void testCreateItem_InvalidName() {
        ItemDTO dto = createValidItemDTO("");
        
        ValidationException ex = assertThrows(ValidationException.class, () -> service.createItem(dto));
        assertEquals("name", ex.getField());
    }

    @Test
    @DisplayName("Should validate null name")
    void testCreateItem_NullName() {
        ItemDTO dto = createValidItemDTO("Valid");
        dto.setName(null);
        
        ValidationException ex = assertThrows(ValidationException.class, () -> service.createItem(dto));
        assertEquals("name", ex.getField());
    }

    @Test
    @DisplayName("Should validate name exceeds max length")
    void testCreateItem_NameTooLong() {
        ItemDTO dto = createValidItemDTO("a".repeat(101));

        ValidationException ex = assertThrows(ValidationException.class, () -> service.createItem(dto));
        assertTrue(ex.getMessage().contains("100"));
    }

    @Test
    @DisplayName("Should filter items by category")
    void testGetItemsByCategory() {
        service.createItem(createValidItemDTO("Item1"));
        
        ItemDTO item2 = createValidItemDTO("Item2");
        item2.setCategory("Electronics");
        service.createItem(item2);

        List<ItemDTO> electronics = service.getItemsByCategory("Electronics");
        
        assertTrue(electronics.stream().allMatch(i -> "Electronics".equals(i.getCategory())));
    }

    @Test
    @DisplayName("Should throw exception for invalid ID characters")
    void testGetItemById_InvalidCharacters() {
        assertThrows(ValidationException.class, () -> service.getItemById("<script>alert(1)</script>"));
        assertThrows(ValidationException.class, () -> service.getItemById("id; DROP TABLE"));
    }

    @Test
    @DisplayName("Should throw exception when null body provided")
    void testCreateItem_NullBody() {
        assertThrows(ValidationException.class, () -> service.createItem(null));
    }

    @Test
    @DisplayName("Should simulate network error")
    void testNetworkError() {
        service.enableNetworkError(true);
        
        assertThrows(ItemService.NetworkException.class, () -> service.getAllItems());
        
        service.enableNetworkError(false);
    }

    @Test
    @DisplayName("Should simulate timeout")
    void testTimeout() {
        service.enableTimeout(true, 100);
        
        assertThrows(com.crud.exception.TimeoutException.class, () -> service.getAllItems());
        
        service.enableTimeout(false, 0);
    }

    private ItemDTO createValidItemDTO(String name) {
        ItemDTO dto = new ItemDTO();
        dto.setName(name);
        dto.setDescription("Description");
        dto.setCategory("Category");
        dto.setPrice(100.0);
        dto.setQuantity(10);
        return dto;
    }
}
