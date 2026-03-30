package com.crud.controller;

import com.crud.exception.*;
import com.crud.model.ItemDTO;
import com.crud.service.ItemService;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    public void createItem(Context ctx) {
        try {
            ItemDTO dto = ctx.bodyAsClass(ItemDTO.class);
            ItemDTO created = service.createItem(dto);
            ctx.status(201).json(created);
        } catch (ValidationException e) {
            sendError(ctx, 400, e.getMessage(), e.getField());
        } catch (InvalidDataException e) {
            sendError(ctx, 400, e.getMessage(), null);
        } catch (Exception e) {
            sendError(ctx, 500, "Internal server error", null);
        }
    }

    public void getAllItems(Context ctx) {
        try {
            String category = ctx.queryParam("category");
            List<ItemDTO> items = category != null ? 
                service.getItemsByCategory(category) : 
                service.getAllItems();
            ctx.json(items);
        } catch (TimeoutException e) {
            sendError(ctx, 408, "Request timeout", null);
        } catch (ItemService.NetworkException e) {
            sendError(ctx, 503, "Service temporarily unavailable", null);
        } catch (Exception e) {
            sendError(ctx, 500, "Internal server error", null);
        }
    }

    public void getItemById(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            ItemDTO item = service.getItemById(id);
            ctx.json(item);
        } catch (ResourceNotFoundException e) {
            sendError(ctx, 404, e.getMessage(), null);
        } catch (ValidationException e) {
            sendError(ctx, 400, e.getMessage(), e.getField());
        } catch (TimeoutException e) {
            sendError(ctx, 408, "Request timeout", null);
        } catch (ItemService.NetworkException e) {
            sendError(ctx, 503, "Service temporarily unavailable", null);
        } catch (Exception e) {
            sendError(ctx, 500, "Internal server error", null);
        }
    }

    public void updateItem(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            ItemDTO dto = ctx.bodyAsClass(ItemDTO.class);
            ItemDTO updated = service.updateItem(id, dto);
            ctx.json(updated);
        } catch (ResourceNotFoundException e) {
            sendError(ctx, 404, e.getMessage(), null);
        } catch (ValidationException e) {
            sendError(ctx, 400, e.getMessage(), e.getField());
        } catch (InvalidDataException e) {
            sendError(ctx, 400, e.getMessage(), null);
        } catch (TimeoutException e) {
            sendError(ctx, 408, "Request timeout", null);
        } catch (ItemService.NetworkException e) {
            sendError(ctx, 503, "Service temporarily unavailable", null);
        } catch (Exception e) {
            sendError(ctx, 500, "Internal server error", null);
        }
    }

    public void deleteItem(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            service.deleteItem(id);
            ctx.status(204);
        } catch (ResourceNotFoundException e) {
            sendError(ctx, 404, e.getMessage(), null);
        } catch (ValidationException e) {
            sendError(ctx, 400, e.getMessage(), e.getField());
        } catch (TimeoutException e) {
            sendError(ctx, 408, "Request timeout", null);
        } catch (ItemService.NetworkException e) {
            sendError(ctx, 503, "Service temporarily unavailable", null);
        } catch (Exception e) {
            sendError(ctx, 500, "Internal server error", null);
        }
    }

    public void healthCheck(Context ctx) {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("items", service.getItemCount());
        ctx.json(health);
    }

    private void sendError(Context ctx, int statusCode, String message, String field) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("status", statusCode);
        if (field != null) {
            error.put("field", field);
        }
        ctx.status(statusCode).json(error);
    }
}
