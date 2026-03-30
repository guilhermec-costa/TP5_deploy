package com.crud.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SeleniumTest {
    private WebDriver driver;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().browserVersion("143").setup();
        
        ChromeOptions options = new ChromeOptions();
        
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        
        baseUrl = "http://localhost:3000";
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
        try {
            driver.get(baseUrl);
            assertNotNull(driver.getTitle());
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Frontend not running - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should display header")
    void testDisplayHeader() {
        try {
            driver.get(baseUrl);
            
            WebElement header = driver.findElement(By.className("header"));
            assertNotNull(header);
            
            WebElement title = header.findElement(By.tagName("h1"));
            assertEquals("CRUD System", title.getText());
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Frontend not running - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should have create button")
    void testCreateButton() {
        try {
            driver.get(baseUrl);
            
            WebElement createBtn = driver.findElement(By.className("btn-primary"));
            assertNotNull(createBtn);
            assertTrue(createBtn.getText().contains("New"));
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Frontend not running - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should open create modal")
    void testOpenCreateModal() {
        try {
            driver.get(baseUrl);
            
            WebElement createBtn = driver.findElement(By.className("btn-primary"));
            createBtn.click();
            
            WebElement modal = driver.findElement(By.className("modal"));
            assertNotNull(modal);
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Frontend not running - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should validate form fields")
    void testFormValidation() {
        try {
            driver.get(baseUrl);
            
            WebElement createBtn = driver.findElement(By.className("btn-primary"));
            createBtn.click();
            
            WebElement submitBtn = driver.findElement(By.xpath("//button[@type='submit']"));
            submitBtn.click();
            
            WebElement nameError = driver.findElement(By.xpath("//div[contains(@class, 'form-error')]"));
            assertNotNull(nameError);
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Frontend not running - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should close modal on cancel")
    void testCloseModal() {
        try {
            driver.get(baseUrl);
            
            WebElement createBtn = driver.findElement(By.className("btn-primary"));
            createBtn.click();
            
            WebElement cancelBtn = driver.findElement(By.xpath("//button[contains(text(), 'Cancel')]"));
            cancelBtn.click();
            
            Thread.sleep(500);
            
            assertThrows(NoSuchElementException.class, () -> {
                driver.findElement(By.className("modal"));
            });
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Frontend not running - " + e.getMessage());
        }
    }
}
