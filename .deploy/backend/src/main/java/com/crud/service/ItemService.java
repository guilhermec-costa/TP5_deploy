package com.crud.service;

import com.crud.exception.*;
import com.crud.model.Item;
import com.crud.model.ItemDTO;
import com.crud.repository.ItemRepository;
import java.util.List;
import java.util.regex.Pattern;

public class ItemService {
    private static final Pattern SAFE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_CATEGORY_LENGTH = 50;
    private static final double MAX_PRICE = 999999.99;
    private static final int MAX_QUANTITY = 999999;

    private final ItemRepository repository;
    private boolean simulateNetworkError = false;
    private boolean simulateTimeout = false;
    private int timeoutDelay = 0;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public void enableNetworkError(boolean enable) {
        this.simulateNetworkError = enable;
    }

    public void enableTimeout(boolean enable, int delayMs) {
        this.simulateTimeout = enable;
        this.timeoutDelay = delayMs;
    }

    private void simulateNetworkDelay() throws TimeoutException {
        if (simulateNetworkError) {
            throw new NetworkException("Network error simulated");
        }
        if (simulateTimeout) {
            try {
                Thread.sleep(timeoutDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TimeoutException("Operation timed out", e);
            }
            throw new TimeoutException("Operation timed out after " + timeoutDelay + "ms");
        }
    }

    public ItemDTO createItem(ItemDTO dto) {
        validateInput(dto);
        
        simulateNetworkDelay();
        
        Item item = dto.toEntity();
        item.setId(repository.generateId());
        Item saved = repository.save(item);
        
        return new ItemDTO(saved);
    }

    public List<ItemDTO> getAllItems() {
        simulateNetworkDelay();
        
        return repository.findAll().stream()
                .map(ItemDTO::new)
                .collect(java.util.stream.Collectors.toList());
    }

    public ItemDTO getItemById(String id) {
        validateId(id);
        simulateNetworkDelay();
        
        Item item = repository.findById(id);
        return new ItemDTO(item);
    }

    public List<ItemDTO> getItemsByCategory(String category) {
        if (category != null) {
            validateField("category", category, MAX_CATEGORY_LENGTH);
        }
        simulateNetworkDelay();
        
        return repository.findByCategory(category).stream()
                .map(ItemDTO::new)
                .collect(java.util.stream.Collectors.toList());
    }

    public ItemDTO updateItem(String id, ItemDTO dto) {
        validateId(id);
        validateInput(dto);
        simulateNetworkDelay();
        
        Item item = dto.toEntity();
        Item updated = repository.update(id, item);
        
        return new ItemDTO(updated);
    }

    public void deleteItem(String id) {
        validateId(id);
        simulateNetworkDelay();
        
        repository.delete(id);
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("ID is required", "id");
        }
        if (id.length() > 50) {
            throw new ValidationException("ID too long (max 50 characters)", "id");
        }
        if (!SAFE_ID_PATTERN.matcher(id).matches()) {
            throw new ValidationException("ID contains invalid characters", "id");
        }
    }

    private void validateInput(ItemDTO dto) {
        if (dto == null) {
            throw new ValidationException("Request body is required");
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("Name is required", "name");
        }
        validateField("name", dto.getName(), MAX_NAME_LENGTH);
        validateField("description", dto.getDescription(), MAX_DESCRIPTION_LENGTH);
        validateField("category", dto.getCategory(), MAX_CATEGORY_LENGTH);

        if (dto.getPrice() == null) {
            throw new ValidationException("Price is required", "price");
        }
        if (dto.getPrice() < 0) {
            throw new ValidationException("Price cannot be negative", "price");
        }
        if (dto.getPrice() > MAX_PRICE) {
            throw new ValidationException("Price exceeds maximum allowed (" + MAX_PRICE + ")", "price");
        }

        if (dto.getQuantity() == null) {
            throw new ValidationException("Quantity is required", "quantity");
        }
        if (dto.getQuantity() < 0) {
            throw new ValidationException("Quantity cannot be negative", "quantity");
        }
        if (dto.getQuantity() > MAX_QUANTITY) {
            throw new ValidationException("Quantity exceeds maximum allowed (" + MAX_QUANTITY + ")", "quantity");
        }
    }

    private void validateField(String fieldName, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(
                fieldName + " exceeds maximum length (" + maxLength + " characters)",
                fieldName
            );
        }
    }

    public int getItemCount() {
        return repository.count();
    }

    public static class NetworkException extends RuntimeException {
        public NetworkException(String message) {
            super(message);
        }
    }
}
