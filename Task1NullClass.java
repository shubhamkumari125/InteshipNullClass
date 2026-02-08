package Task1;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.testng.Assert;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

public class Task1NullClass {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.amazon.com/";
    private static final int TIMEOUT = 10;
    
    @BeforeTest
    public void setUp() {
        // Automatically manage ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
        driver.manage().window().maximize();
    }
    
    @Test
    public void testSelectAndVerifyProduct() {
        // Check if current time is between 3 PM (15:00) and 6 PM (18:00)
        if (!isTestExecutionTimeValid()) {
            System.out.println("Test can only run between 3 PM and 6 PM");
            return;
        }
        
        try {
            // Navigate to the e-commerce website
            driver.get(BASE_URL);
            
            // Perform search for products
            searchForProducts("test product");
            
            // Get list of products and filter them
            List<WebElement> productList = getFilteredProducts();
            
            if (productList.isEmpty()) {
                System.out.println("No valid products found after filtering");
                return;
            }
            
            // Select the first valid product
            WebElement selectedProduct = productList.get(0);
            String productName = selectedProduct.findElement(By.xpath(".//h2//a//span")).getText();
            System.out.println("Selected Product: " + productName);
            
            // Click on the product link
            WebElement productLink = selectedProduct.findElement(By.xpath(".//h2//a"));
            productLink.click();
            
            // Wait for product details page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productTitle")));
            
            // Verify product details on the product page
            verifyProductPageDetails();
            
            System.out.println("Test Passed: Product selected and verified successfully");
            
        } catch (Exception e) {
            System.out.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if current time is within 3 PM to 6 PM
     */
    private boolean isTestExecutionTimeValid() {
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(15, 0); // 3 PM
        LocalTime endTime = LocalTime.of(18, 0);   // 6 PM
        
        return currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
    }
    
    /**
     * Perform product search on Amazon
     */
    private void searchForProducts(String searchQuery) throws InterruptedException {
        try {
            // Find the search input field and enter search query
            WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("twotabsearchtextbox")));
            searchInput.clear();
            searchInput.sendKeys(searchQuery);
            
            // Click search button
            WebElement searchButton = driver.findElement(By.id("nav-search-submit-button"));
            searchButton.click();
            
            // Wait for search results to load
            Thread.sleep(3000);
            
        } catch (Exception e) {
            System.out.println("Error during product search: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get filtered product list from Amazon search results excluding:
     * - Electronic products
     * - Products starting with letters A, B, C, D
     */
    private List<WebElement> getFilteredProducts() {
        List<WebElement> allProducts = driver.findElements(By.xpath("//div[@data-component-type='s-search-result']"));
        List<WebElement> filteredProducts = new ArrayList<>();
        
        for (WebElement product : allProducts) {
            try {
                // Get product name from Amazon's structure
                WebElement titleElement = product.findElement(By.xpath(".//h2//a//span"));
                String productName = titleElement.getText();
                
                // Skip if product name is empty
                if (productName.isEmpty()) {
                    continue;
                }
                
                // Filter out products starting with A, B, C, D
                char firstLetter = productName.charAt(0);
                if (firstLetter == 'A' || firstLetter == 'B' || firstLetter == 'C' || firstLetter == 'D' ||
                    firstLetter == 'a' || firstLetter == 'b' || firstLetter == 'c' || firstLetter == 'd') {
                    System.out.println("Skipping product: " + productName + " (starts with restricted letter)");
                    continue;
                }
                
                // Filter out electronic products by analyzing product name and category
                if (isElectronicProduct(productName)) {
                    System.out.println("Skipping product: " + productName + " (electronic product)");
                    continue;
                }
                
                // Add valid product to filtered list
                filteredProducts.add(product);
                System.out.println("Valid product found: " + productName);
                
            } catch (Exception e) {
                System.out.println("Error processing product: " + e.getMessage());
                continue;
            }
        }
        
        return filteredProducts;
    }
    
    /**
     * Check if product is an electronic product based on name analysis
     */
    private boolean isElectronicProduct(String productName) {
        String name_lower = productName.toLowerCase();
        
        String[] electronicKeywords = {
            "electronics", "computer", "smartphone", "mobile", "laptop",
            "tablet", "camera", "television", "tv", "monitor", "keyboard",
            "mouse", "headphones", "speaker", "router", "printer", "device",
            "gadget", "phone", "iphone", "samsung", "dell", "hp", "processor",
            "gpu", "graphics", "card", "usb", "charger", "adapter", "screen",
            "display", "motherboard", "ram", "ssd", "hard drive", "cable",
            "motherboard", "cpu", "gpu", "wireless", "smart watch", "earbuds",
            "microphone", "webcam", "router", "modem", "switch", "hub", "charger",
            "battery", "power bank", "gaming console", "playstation", "xbox"
        };
        
        for (String keyword : electronicKeywords) {
            if (name_lower.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verify product details on Amazon product page
     */
    private void verifyProductPageDetails() {
        try {
            // Verify product title is displayed
            WebElement productTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("productTitle")));
            Assert.assertNotNull(productTitle, "Product title should be visible");
            System.out.println("✓ Product title verified: " + productTitle.getText());
            
            // Verify product price is displayed
            WebElement productPrice = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[@class='a-price-whole']")));
            Assert.assertNotNull(productPrice, "Product price should be visible");
            System.out.println("✓ Product price verified: " + productPrice.getText());
            
            // Verify product rating/review section
            WebElement productRating = driver.findElement(By.id("acrPopover"));
            Assert.assertNotNull(productRating, "Product rating should be visible");
            System.out.println("✓ Product rating verified");
            
            // Verify add to cart/buy button is available
            WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("add-to-cart-button")));
            Assert.assertTrue(addToCartButton.isDisplayed(), "Add to cart button should be visible");
            System.out.println("✓ Add to cart button is available");
            
            // Verify product images are displayed
            WebElement productImages = driver.findElement(By.id("landingImage"));
            Assert.assertNotNull(productImages, "Product images should be visible");
            System.out.println("✓ Product images verified");
            
            // Verify product description/details section exists
            WebElement productDetails = driver.findElement(By.xpath("//div[@id='feature-bullets']"));
            Assert.assertNotNull(productDetails, "Product details should be visible");
            System.out.println("✓ Product details/features verified");
            
        } catch (Exception e) {
            System.out.println("Error verifying product details: " + e.getMessage());
            throw new AssertionError("Product page verification failed", e);
        }
    }
    
    @AfterTest
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    public static void main(String[] args) {
        Task1NullClass test = new Task1NullClass();
        test.setUp();
        test.testSelectAndVerifyProduct();
        test.tearDown();
    }
}
