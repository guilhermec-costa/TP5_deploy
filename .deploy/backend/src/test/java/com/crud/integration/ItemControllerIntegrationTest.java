package com.crud.integration;

import com.crud.config.AppConfig;
import com.crud.model.ItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import okhttp3.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class ItemControllerIntegrationTest {
    private static AppConfig appConfig;
    private static int port;
    private static ObjectMapper objectMapper;
    private static OkHttpClient client;
    private static String baseUrl;

    @BeforeAll
    static void setUp() throws Exception {
        appConfig = new AppConfig();
        
        io.javalin.Javalin javalin = appConfig.createApp();
        javalin.start(0);
        port = javalin.port();
        baseUrl = "http://localhost:" + port;
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        client = new OkHttpClient();
        
        Thread.sleep(500);
    }

    @AfterAll
    static void tearDown() {
        appConfig.stop();
    }

    private Response getResponse(String path) throws Exception {
        Request request = new Request.Builder()
            .url(baseUrl + path)
            .get()
            .build();
        return client.newCall(request).execute();
    }

    private String getBody(String path) throws Exception {
        Request request = new Request.Builder()
            .url(baseUrl + path)
            .get()
            .build();
        return client.newCall(request).execute().body().string();
    }

    private Response post(String path, String json) throws Exception {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url(baseUrl + path)
            .post(body)
            .build();
        return client.newCall(request).execute();
    }

    private Response put(String path, String json) throws Exception {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url(baseUrl + path)
            .put(body)
            .build();
        return client.newCall(request).execute();
    }

    private Response delete(String path) throws Exception {
        Request request = new Request.Builder()
            .url(baseUrl + path)
            .delete()
            .build();
        return client.newCall(request).execute();
    }

    @Test
    @DisplayName("Should return health check")
    void testHealthCheck() throws Exception {
        String response = getBody("/api/health");
        
        assertNotNull(response);
        assertTrue(response.contains("UP"));
    }

    @Test
    @DisplayName("Should get all items")
    void testGetAllItems() throws Exception {
        String response = getBody("/api/items");
        
        assertNotNull(response);
        assertTrue(response.contains("["));
    }

    @Test
    @DisplayName("Should create item")
    void testCreateItem() throws Exception {
        String requestBody = """
            {
                "name": "Integration Test Item",
                "description": "Test Description",
                "category": "Test",
                "price": 99.99,
                "quantity": 5
            }
            """;
        
        Response response = post("/api/items", requestBody);
        
        assertEquals(201, response.code());
        String body = response.body().string();
        assertTrue(body.contains("Integration Test Item"));
    }

    @Test
    @DisplayName("Should return 400 for invalid input")
    void testCreateItem_InvalidInput() throws Exception {
        String requestBody = """
            {
                "name": "",
                "price": -100,
                "quantity": -5
            }
            """;
        
        Response response = post("/api/items", requestBody);
        
        assertEquals(400, response.code());
    }

    @Test
    @DisplayName("Should return 404 for non-existent item")
    void testGetItemById_NotFound() throws Exception {
        Response response = getResponse("/api/items/99999");
        
        assertEquals(404, response.code());
    }

    @Test
    @DisplayName("Should return 400 for invalid ID format")
    void testGetItemById_InvalidId() throws Exception {
        Response response = getResponse("/api/items/<script>");
        
        assertEquals(400, response.code());
    }

    @Test
    @DisplayName("Should update item")
    void testUpdateItem() throws Exception {
        ItemDTO created = appConfig.getService().createItem(createValidItemDTO("ToUpdate"));
        
        String requestBody = """
            {
                "name": "Updated Name",
                "description": "Updated Desc",
                "category": "Updated Cat",
                "price": 150.0,
                "quantity": 25
            }
            """;
        
        Response response = put("/api/items/" + created.getId(), requestBody);
        
        assertEquals(200, response.code());
        String body = response.body().string();
        assertTrue(body.contains("Updated Name"));
    }

    @Test
    @DisplayName("Should delete item")
    void testDeleteItem() throws Exception {
        ItemDTO created = appConfig.getService().createItem(createValidItemDTO("ToDelete"));
        
        Response response = delete("/api/items/" + created.getId());
        
        assertEquals(204, response.code());
    }

    @Test
    @DisplayName("Should handle CORS preflight")
    void testCorsPreflight() throws Exception {
        Request request = new Request.Builder()
            .url(baseUrl + "/api/items")
            .method("OPTIONS", null)
            .build();
        Response response = client.newCall(request).execute();
        
        assertEquals(200, response.code());
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
