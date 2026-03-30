package com.crud.model;

import java.time.LocalDateTime;

public class ItemDTO {
    private String id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String error;

    public ItemDTO() {}

    public ItemDTO(Item item) {
        if (item != null) {
            this.id = item.getId();
            this.name = item.getName();
            this.description = item.getDescription();
            this.category = item.getCategory();
            this.price = item.getPrice();
            this.quantity = item.getQuantity();
            this.createdAt = item.getCreatedAt();
            this.updatedAt = item.getUpdatedAt();
        }
    }

    public static ItemDTO error(String message) {
        ItemDTO dto = new ItemDTO();
        dto.setError(message);
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Item toEntity() {
        return new Item(id, name, description, category, price, quantity);
    }
}
