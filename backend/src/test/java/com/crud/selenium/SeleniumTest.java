package com.crud.selenium;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SeleniumTest {
    private WebDriver driver;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:3000";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.setBinary("/opt/google/chrome/google-chrome");
        
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        Assumptions.assumeTrue(isFrontendAvailable(), "Frontend not running at " + baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Should load the main page")
    void testLoadPage() {
        driver.get(baseUrl);
        assertNotNull(driver.getTitle());
    }

    @Test
    @DisplayName("Should display header")
    void testDisplayHeader() {
        driver.get(baseUrl);

        WebElement header = driver.findElement(By.className("header"));
        assertNotNull(header);

        WebElement title = header.findElement(By.tagName("h1"));
        assertEquals("CRUD System", title.getText());
    }

    @Test
    @DisplayName("Should have create button")
    void testCreateButton() {
        driver.get(baseUrl);

        WebElement createBtn = driver.findElement(By.className("btn-primary"));
        assertNotNull(createBtn);
        assertTrue(createBtn.getText().contains("New"));
    }

    @Test
    @DisplayName("Should open create modal")
    void testOpenCreateModal() {
        driver.get(baseUrl);

        WebElement createBtn = driver.findElement(By.className("btn-primary"));
        createBtn.click();

        WebElement modal = driver.findElement(By.className("modal"));
        assertNotNull(modal);
    }

    @Test
    @DisplayName("Should validate form fields")
    void testFormValidation() {
        driver.get(baseUrl);

        WebElement createBtn = driver.findElement(By.className("btn-primary"));
        createBtn.click();

        WebElement submitBtn = driver.findElement(By.xpath("//button[@type='submit']"));
        submitBtn.click();

        WebElement nameError = driver.findElement(By.xpath("//div[contains(@class, 'form-error')]"));
        assertNotNull(nameError);
    }

    @Test
    @DisplayName("Should close modal on cancel")
    void testCloseModal() throws InterruptedException {
        driver.get(baseUrl);

        WebElement createBtn = driver.findElement(By.className("btn-primary"));
        createBtn.click();

        WebElement cancelBtn = driver.findElement(By.xpath("//button[contains(text(), 'Cancel')]"));
        cancelBtn.click();

        Thread.sleep(500);

        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.className("modal"));
        });
    }

    private boolean isFrontendAvailable() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() < 500;
        } catch (Exception e) {
            return false;
        }
    }
}
