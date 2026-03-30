package com.crud.unit;

import com.crud.controller.ItemController;
import com.crud.exception.*;
import com.crud.model.ItemDTO;
import com.crud.service.ItemService;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemControllerTest {
    private ItemController controller;
    private ItemService service;
    private Context ctx;

    @BeforeEach
    void setUp() {
        service = mock(ItemService.class);
        controller = new ItemController(service);
        ctx = mock(Context.class);
        when(ctx.status(anyInt())).thenReturn(ctx);
    }

    @Test
    @DisplayName("Should create item successfully")
    void testCreateItem_Success() {
        ItemDTO inputDto = new ItemDTO();
        inputDto.setName("Test");
        inputDto.setPrice(100.0);
        
        ItemDTO createdDto = new ItemDTO();
        createdDto.setId("1");
        createdDto.setName("Test");
        createdDto.setPrice(100.0);
        
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(inputDto);
        when(service.createItem(inputDto)).thenReturn(createdDto);
        
        controller.createItem(ctx);
        
        verify(ctx).status(201);
        verify(ctx).json(createdDto);
    }

    @Test
    @DisplayName("Should handle ValidationException on create")
    void testCreateItem_ValidationException() {
        ItemDTO inputDto = new ItemDTO();
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(inputDto);
        when(service.createItem(inputDto)).thenThrow(new ValidationException("Name required", "name"));
        
        controller.createItem(ctx);
        
        verify(ctx).status(400);
        ArgumentCaptor<Map> errorCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(errorCaptor.capture());
        assertEquals("Name required", errorCaptor.getValue().get("error"));
        assertEquals("name", errorCaptor.getValue().get("field"));
    }

    @Test
    @DisplayName("Should handle InvalidDataException on create")
    void testCreateItem_InvalidDataException() {
        ItemDTO inputDto = new ItemDTO();
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(inputDto);
        when(service.createItem(inputDto)).thenThrow(new InvalidDataException("Invalid data"));
        
        controller.createItem(ctx);
        
        verify(ctx).status(400);
        ArgumentCaptor<Map> errorCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(errorCaptor.capture());
        assertEquals("Invalid data", errorCaptor.getValue().get("error"));
        assertNull(errorCaptor.getValue().get("field"));
    }

    @Test
    @DisplayName("Should handle generic Exception on create")
    void testCreateItem_GenericException() {
        ItemDTO inputDto = new ItemDTO();
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(inputDto);
        when(service.createItem(inputDto)).thenThrow(new RuntimeException("Unexpected error"));
        
        controller.createItem(ctx);
        
        verify(ctx).status(500);
        ArgumentCaptor<Map> errorCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(errorCaptor.capture());
        assertEquals("Internal server error", errorCaptor.getValue().get("error"));
    }

    @Test
    @DisplayName("Should get all items")
    void testGetAllItems() {
        List<ItemDTO> items = List.of(new ItemDTO(), new ItemDTO());
        when(ctx.queryParam("category")).thenReturn(null);
        when(service.getAllItems()).thenReturn(items);
        
        controller.getAllItems(ctx);
        
        verify(ctx).json(items);
    }

    @Test
    @DisplayName("Should get items by category")
    void testGetAllItems_WithCategory() {
        List<ItemDTO> items = List.of(new ItemDTO());
        when(ctx.queryParam("category")).thenReturn("Electronics");
        when(service.getItemsByCategory("Electronics")).thenReturn(items);
        
        controller.getAllItems(ctx);
        
        verify(service).getItemsByCategory("Electronics");
        verify(ctx).json(items);
    }

    @Test
    @DisplayName("Should handle TimeoutException on getAllItems")
    void testGetAllItems_TimeoutException() {
        when(ctx.queryParam("category")).thenReturn(null);
        when(service.getAllItems()).thenThrow(new TimeoutException("Request timeout"));
        
        controller.getAllItems(ctx);
        
        verify(ctx).status(408);
        ArgumentCaptor<Map> errorCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(errorCaptor.capture());
        assertEquals("Request timeout", errorCaptor.getValue().get("error"));
    }

    @Test
    @DisplayName("Should handle NetworkException on getAllItems")
    void testGetAllItems_NetworkException() {
        when(ctx.queryParam("category")).thenReturn(null);
        when(service.getAllItems()).thenThrow(new ItemService.NetworkException("Network error"));
        
        controller.getAllItems(ctx);
        
        verify(ctx).status(503);
        ArgumentCaptor<Map> errorCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(errorCaptor.capture());
        assertEquals("Service temporarily unavailable", errorCaptor.getValue().get("error"));
    }

    @Test
    @DisplayName("Should get item by ID successfully")
    void testGetItemById_Success() {
        ItemDTO item = new ItemDTO();
        item.setId("1");
        when(ctx.pathParam("id")).thenReturn("1");
        when(service.getItemById("1")).thenReturn(item);
        
        controller.getItemById(ctx);
        
        verify(ctx).json(item);
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException on getItemById")
    void testGetItemById_NotFound() {
        when(ctx.pathParam("id")).thenReturn("999");
        when(service.getItemById("999")).thenThrow(new ResourceNotFoundException("Item not found"));
        
        controller.getItemById(ctx);
        
        verify(ctx).status(404);
        ArgumentCaptor<Map> errorCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(errorCaptor.capture());
        assertEquals("Item not found", errorCaptor.getValue().get("error"));
    }

    @Test
    @DisplayName("Should handle ValidationException on getItemById")
    void testGetItemById_ValidationException() {
        when(ctx.pathParam("id")).thenReturn("<script>");
        when(service.getItemById("<script>")).thenThrow(new ValidationException("Invalid ID", "id"));
        
        controller.getItemById(ctx);
        
        verify(ctx).status(400);
        ArgumentCaptor<Map> errorCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(errorCaptor.capture());
        assertEquals("Invalid ID", errorCaptor.getValue().get("error"));
        assertEquals("id", errorCaptor.getValue().get("field"));
    }

    @Test
    @DisplayName("Should handle TimeoutException on getItemById")
    void testGetItemById_TimeoutException() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(service.getItemById("1")).thenThrow(new TimeoutException("Timeout"));
        
        controller.getItemById(ctx);
        
        verify(ctx).status(408);
    }

    @Test
    @DisplayName("Should handle NetworkException on getItemById")
    void testGetItemById_NetworkException() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(service.getItemById("1")).thenThrow(new ItemService.NetworkException("Network error"));
        
        controller.getItemById(ctx);
        
        verify(ctx).status(503);
    }

    @Test
    @DisplayName("Should update item successfully")
    void testUpdateItem_Success() {
        ItemDTO updateDto = new ItemDTO();
        updateDto.setName("Updated");
        
        ItemDTO result = new ItemDTO();
        result.setId("1");
        result.setName("Updated");
        
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(updateDto);
        when(service.updateItem("1", updateDto)).thenReturn(result);
        
        controller.updateItem(ctx);
        
        verify(ctx).json(result);
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException on update")
    void testUpdateItem_NotFound() {
        ItemDTO updateDto = new ItemDTO();
        when(ctx.pathParam("id")).thenReturn("999");
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(updateDto);
        when(service.updateItem("999", updateDto)).thenThrow(new ResourceNotFoundException("Not found"));
        
        controller.updateItem(ctx);
        
        verify(ctx).status(404);
    }

    @Test
    @DisplayName("Should handle ValidationException on update")
    void testUpdateItem_ValidationException() {
        ItemDTO updateDto = new ItemDTO();
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(updateDto);
        when(service.updateItem("1", updateDto)).thenThrow(new ValidationException("Invalid", "name"));
        
        controller.updateItem(ctx);
        
        verify(ctx).status(400);
        ArgumentCaptor<Map> errorCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(errorCaptor.capture());
        assertEquals("name", errorCaptor.getValue().get("field"));
    }

    @Test
    @DisplayName("Should handle InvalidDataException on update")
    void testUpdateItem_InvalidDataException() {
        ItemDTO updateDto = new ItemDTO();
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(updateDto);
        when(service.updateItem("1", updateDto)).thenThrow(new InvalidDataException("Invalid data"));
        
        controller.updateItem(ctx);
        
        verify(ctx).status(400);
    }

    @Test
    @DisplayName("Should handle TimeoutException on update")
    void testUpdateItem_TimeoutException() {
        ItemDTO updateDto = new ItemDTO();
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(updateDto);
        when(service.updateItem("1", updateDto)).thenThrow(new TimeoutException("Timeout"));
        
        controller.updateItem(ctx);
        
        verify(ctx).status(408);
    }

    @Test
    @DisplayName("Should handle NetworkException on update")
    void testUpdateItem_NetworkException() {
        ItemDTO updateDto = new ItemDTO();
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.bodyAsClass(ItemDTO.class)).thenReturn(updateDto);
        when(service.updateItem("1", updateDto)).thenThrow(new ItemService.NetworkException("Network error"));
        
        controller.updateItem(ctx);
        
        verify(ctx).status(503);
    }

    @Test
    @DisplayName("Should delete item successfully")
    void testDeleteItem_Success() {
        when(ctx.pathParam("id")).thenReturn("1");
        
        controller.deleteItem(ctx);
        
        verify(service).deleteItem("1");
        verify(ctx).status(204);
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException on delete")
    void testDeleteItem_NotFound() {
        when(ctx.pathParam("id")).thenReturn("999");
        doThrow(new ResourceNotFoundException("Not found")).when(service).deleteItem("999");
        
        controller.deleteItem(ctx);
        
        verify(ctx).status(404);
    }

    @Test
    @DisplayName("Should handle ValidationException on delete")
    void testDeleteItem_ValidationException() {
        when(ctx.pathParam("id")).thenReturn("<script>");
        doThrow(new ValidationException("Invalid ID", "id")).when(service).deleteItem("<script>");
        
        controller.deleteItem(ctx);
        
        verify(ctx).status(400);
        ArgumentCaptor<Map> errorCaptor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(errorCaptor.capture());
        assertEquals("id", errorCaptor.getValue().get("field"));
    }

    @Test
    @DisplayName("Should handle TimeoutException on delete")
    void testDeleteItem_TimeoutException() {
        when(ctx.pathParam("id")).thenReturn("1");
        doThrow(new TimeoutException("Timeout")).when(service).deleteItem("1");
        
        controller.deleteItem(ctx);
        
        verify(ctx).status(408);
    }

    @Test
    @DisplayName("Should handle NetworkException on delete")
    void testDeleteItem_NetworkException() {
        when(ctx.pathParam("id")).thenReturn("1");
        doThrow(new ItemService.NetworkException("Network error")).when(service).deleteItem("1");
        
        controller.deleteItem(ctx);
        
        verify(ctx).status(503);
    }

    @Test
    @DisplayName("Should return health check with status UP")
    void testHealthCheck() {
        when(service.getItemCount()).thenReturn(10);
        
        controller.healthCheck(ctx);
        
        verify(ctx).json(any(Map.class));
    }
}
