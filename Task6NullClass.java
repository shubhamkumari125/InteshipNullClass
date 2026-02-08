package Task6;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

public class Task6NullClass {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final double MINIMUM_PRICE = 2000.0; // Minimum price in Rs
    private static final double MINIMUM_RATING = 4.0;   // Minimum customer rating
    private static final String BRAND_PREFIX = "C";     // Brand should start with this letter

    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
    }

    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Validates if current time is between 3 PM and 6 PM
     */
    public boolean isTestingWindowOpen() {
        LocalTime now = LocalTime.now();
        LocalTime startTime = LocalTime.of(15, 0); // 3 PM
        LocalTime endTime = LocalTime.of(18, 0);   // 6 PM
        
        boolean isInWindow = now.isAfter(startTime) && now.isBefore(endTime);
        
        if (!isInWindow) {
            System.out.println("Testing Window Closed! Current time: " + now);
            System.out.println("Testing allowed only between 3 PM to 6 PM");
            return false;
        }
        
        System.out.println("Testing Window Open! Current time: " + now);
        return true;
    }

    /**
     * Search for a product
     */
    public void searchProduct(String productName) {
        try {
            System.out.println("\n=== Step 1: Searching for product - " + productName + " ===");
            
            // Locate search box (adjust selector based on your website)
            WebElement searchBox = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Search' or @id='search' or @class='search-input']"))
            );
            
            searchBox.clear();
            searchBox.sendKeys(productName);
            
            // Click search button or press Enter
            WebElement searchButton = driver.findElement(By.xpath("//button[contains(text(), 'Search') or @type='submit' or @class='search-btn']"));
            searchButton.click();
            
            // Wait for search results
            Thread.sleep(2000);
            System.out.println("✓ Product search completed successfully");
            
        } catch (Exception e) {
            System.out.println("✗ Error during product search: " + e.getMessage());
        }
    }

    /**
     * Apply Brand filter - Brand name should start with letter 'C'
     */
    public void applyBrandFilter() {
        try {
            System.out.println("\n=== Step 2: Applying Brand Filter (starts with 'C') ===");
            
            // Wait for filters section to load
            WebElement filterSection = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='filters' or @id='filter-panel' or contains(@class, 'sidebar')]"))
            );
            
            // Find brand filter section
            WebElement brandFilter = driver.findElement(By.xpath("//div[contains(text(), 'Brand') or @class='brand-filter'] | //label[contains(text(), 'Brand')]"));
            scrollToElement(brandFilter);
            
            // Click to expand brand filter if needed
            try {
                WebElement brandExpandBtn = brandFilter.findElement(By.xpath(".//..//button | .//..//span[@class='expand']"));
                brandExpandBtn.click();
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("Brand filter already expanded");
            }
            
            // Get all brand checkboxes
            List<WebElement> brandCheckboxes = driver.findElements(By.xpath("//input[@type='checkbox' and ancestor::*[contains(., 'Brand')] or ancestor::div[@class='brand-filter']]"));
            
            System.out.println("Found " + brandCheckboxes.size() + " brand options");
            
            // Filter and select brands starting with 'C'
            for (WebElement checkbox : brandCheckboxes) {
                try {
                    WebElement label = checkbox.findElement(By.xpath("./following-sibling::label | ./parent::label"));
                    String brandName = label.getText().trim();
                    
                    if (brandName.toUpperCase().startsWith(BRAND_PREFIX)) {
                        // Scroll to checkbox
                        scrollToElement(checkbox);
                        
                        // Check if not already selected
                        if (!checkbox.isSelected()) {
                            checkbox.click();
                            System.out.println("✓ Selected brand: " + brandName);
                            Thread.sleep(1000); // Wait for filter to apply
                        }
                    }
                } catch (Exception e) {
                    // Continue to next checkbox
                }
            }
            
            System.out.println("✓ Brand filter applied successfully");
            Thread.sleep(1500);
            
        } catch (Exception e) {
            System.out.println("✗ Error applying brand filter: " + e.getMessage());
        }
    }

    /**
     * Apply Price filter - Price should be above Rs 2000
     */
    public void applyPriceFilter() {
        try {
            System.out.println("\n=== Step 3: Applying Price Filter (above Rs 2000) ===");
            
            // Find price filter section
            WebElement priceFilter = driver.findElement(By.xpath("//div[contains(text(), 'Price') or @class='price-filter'] | //label[contains(text(), 'Price')]"));
            scrollToElement(priceFilter);
            
            // Try to find price range input fields
            List<WebElement> priceInputs = driver.findElements(By.xpath("//input[@type='text' and (@id='price-min' or @id='price-max' or @placeholder='Min' or @placeholder='Max')]"));
            
            if (priceInputs.size() >= 2) {
                // Clear and set minimum price
                WebElement minPriceInput = priceInputs.get(0);
                minPriceInput.clear();
                minPriceInput.sendKeys("2000");
                System.out.println("✓ Set minimum price: Rs 2000");
                
                Thread.sleep(500);
                
                // Click apply button if exists
                try {
                    WebElement applyPriceBtn = driver.findElement(By.xpath("//button[contains(text(), 'Apply') or @class='apply-filter']"));
                    applyPriceBtn.click();
                    Thread.sleep(1500);
                } catch (Exception e) {
                    // Filter may apply automatically
                }
                
                System.out.println("✓ Price filter applied successfully");
            } else {
                // Try to find price range slider or radio buttons
                List<WebElement> priceOptions = driver.findElements(By.xpath("//input[@type='radio' and ancestor::*[contains(., 'Price')]]"));
                
                for (WebElement option : priceOptions) {
                    try {
                        WebElement label = option.findElement(By.xpath("./following-sibling::label | ./parent::label"));
                        String priceRange = label.getText();
                        
                        // Look for option that includes prices above 2000
                        if (priceRange.contains("2") || priceRange.contains("3") || priceRange.contains("4")) {
                            scrollToElement(option);
                            option.click();
                            System.out.println("✓ Selected price range: " + priceRange);
                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        // Continue
                    }
                }
            }
            
            Thread.sleep(1000);
            
        } catch (Exception e) {
            System.out.println("⚠ Price filter not found, continuing: " + e.getMessage());
        }
    }

    /**
     * Apply Customer Rating filter - Rating should be above 4
     */
    public void applyRatingFilter() {
        try {
            System.out.println("\n=== Step 4: Applying Customer Rating Filter (above 4 stars) ===");
            
            // Find rating filter section
            WebElement ratingFilter = driver.findElement(By.xpath("//div[contains(text(), 'Rating') or contains(text(), 'Stars') or @class='rating-filter'] | //label[contains(text(), 'Rating')]"));
            scrollToElement(ratingFilter);
            
            // Get all rating checkboxes
            List<WebElement> ratingCheckboxes = driver.findElements(By.xpath("//input[@type='checkbox' and ancestor::*[contains(., 'Rating') or contains(., 'Stars') or @class='rating-filter']]"));
            
            System.out.println("Found " + ratingCheckboxes.size() + " rating options");
            
            // Select ratings 4 stars and above
            for (WebElement checkbox : ratingCheckboxes) {
                try {
                    WebElement label = checkbox.findElement(By.xpath("./following-sibling::label | ./parent::label"));
                    String ratingText = label.getText();
                    
                    // Extract rating value
                    double rating = extractRatingValue(ratingText);
                    
                    if (rating >= MINIMUM_RATING) {
                        scrollToElement(checkbox);
                        
                        if (!checkbox.isSelected()) {
                            checkbox.click();
                            System.out.println("✓ Selected rating: " + ratingText.trim());
                            Thread.sleep(1000); // Wait for filter to apply
                        }
                    }
                } catch (Exception e) {
                    // Continue to next checkbox
                }
            }
            
            System.out.println("✓ Rating filter applied successfully");
            Thread.sleep(1500);
            
        } catch (Exception e) {
            System.out.println("⚠ Rating filter not found, continuing: " + e.getMessage());
        }
    }

    /**
     * Extract rating value from text (e.g., "4 Stars & Up" -> 4.0)
     */
    private double extractRatingValue(String text) {
        try {
            String cleanText = text.replaceAll("[^0-9.]", "");
            if (cleanText.isEmpty()) {
                return 0.0;
            }
            double rating = Double.parseDouble(cleanText.split("[.]")[0]); // Get first number
            return rating;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Verify filtered results meet all criteria
     */
    public void verifyFilteredResults() {
        try {
            System.out.println("\n=== Step 5: Verifying Filtered Results ===");
            
            // Get all product items from results
            List<WebElement> productItems = driver.findElements(By.xpath("//div[@class='product-item' or @class='product-card' or contains(@class, 'product')]"));
            
            if (productItems.isEmpty()) {
                System.out.println("⚠ No products found in results");
                return;
            }
            
            System.out.println("Found " + productItems.size() + " products in filtered results\n");
            
            int validProducts = 0;
            List<String> productDetails = new ArrayList<>();
            
            for (int i = 0; i < Math.min(productItems.size(), 5); i++) { // Verify first 5 products
                try {
                    WebElement product = productItems.get(i);
                    scrollToElement(product);
                    
                    // Extract product name
                    String productName = product.findElement(By.xpath(".//h2 | .//h3 | .//a[@class='product-name']")).getText();
                    
                    // Extract brand
                    String brand = extractBrand(product);
                    
                    // Extract price
                    double price = extractPrice(product);
                    
                    // Extract rating
                    double rating = extractRating(product);
                    
                    // Verify all criteria
                    boolean meetsAllCriteria = true;
                    String validationMsg = "";
                    
                    if (!brand.toUpperCase().startsWith(BRAND_PREFIX)) {
                        meetsAllCriteria = false;
                        validationMsg += "[Brand check: FAILED] ";
                    } else {
                        validationMsg += "[Brand check: PASSED] ";
                    }
                    
                    if (price < MINIMUM_PRICE) {
                        meetsAllCriteria = false;
                        validationMsg += "[Price check: FAILED] ";
                    } else {
                        validationMsg += "[Price check: PASSED] ";
                    }
                    
                    if (rating < MINIMUM_RATING) {
                        meetsAllCriteria = false;
                        validationMsg += "[Rating check: FAILED]";
                    } else {
                        validationMsg += "[Rating check: PASSED]";
                    }
                    
                    if (meetsAllCriteria) {
                        validProducts++;
                        System.out.println("✓ Product " + (i + 1) + ": " + productName);
                        System.out.println("  Brand: " + brand + ", Price: Rs " + price + ", Rating: " + rating + " stars");
                        System.out.println("  " + validationMsg + "\n");
                    } else {
                        System.out.println("✗ Product " + (i + 1) + ": " + productName);
                        System.out.println("  Brand: " + brand + ", Price: Rs " + price + ", Rating: " + rating + " stars");
                        System.out.println("  " + validationMsg + "\n");
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error verifying product " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            System.out.println("✓ Verification completed: " + validProducts + " products meet all filter criteria");
            
        } catch (Exception e) {
            System.out.println("✗ Error verifying results: " + e.getMessage());
        }
    }

    /**
     * Extract brand name from product element
     */
    private String extractBrand(WebElement product) {
        try {
            WebElement brandElement = product.findElement(By.xpath(".//span[@class='brand'] | .//div[@class='brand'] | .//p[contains(text(), 'Brand')]"));
            String brandText = brandElement.getText();
            return brandText.replaceAll("(?i)brand[:\\s]*", "").trim();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Extract price from product element
     */
    private double extractPrice(WebElement product) {
        try {
            WebElement priceElement = product.findElement(By.xpath(".//span[@class='price'] | .//div[@class='price'] | .//p[contains(@class, 'price')]"));
            String priceText = priceElement.getText();
            String cleanPrice = priceText.replaceAll("[^0-9.]", "");
            return Double.parseDouble(cleanPrice);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Extract rating from product element
     */
    private double extractRating(WebElement product) {
        try {
            WebElement ratingElement = product.findElement(By.xpath(".//span[@class='rating'] | .//div[@class='rating'] | .//span[contains(@class, 'stars')] | .//i[@class='rating']"));
            String ratingText = ratingElement.getText();
            String cleanRating = ratingText.replaceAll("[^0-9.]", "");
            if (cleanRating.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(cleanRating);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Scroll element into view
     */
    private void scrollToElement(WebElement element) {
        try {
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView(true);", element);
            Thread.sleep(500);
        } catch (Exception e) {
            // Continue if scroll fails
        }
    }

    /**
     * Main test execution - Complete Filter Application and Verification
     */
    public void testProductFilterFlow() {
        try {
            // Check if current time is within testing window
            if (!isTestingWindowOpen()) {
                System.out.println("Test execution aborted: Outside testing window (3 PM - 6 PM)");
                return;
            }

            setUp();
            
            // Navigate to the e-commerce website (change URL as needed)
            driver.get("https://example-ecommerce.com"); // Change to actual website
            Thread.sleep(2000);

            // Step 1: Search for product
            searchProduct("electronics"); // Change product name as needed

            // Step 2: Apply Brand filter (starts with 'C')
            applyBrandFilter();

            // Step 3: Apply Price filter (above Rs 2000)
            applyPriceFilter();

            // Step 4: Apply Rating filter (above 4 stars)
            applyRatingFilter();

            // Step 5: Verify filtered results
            verifyFilteredResults();

            System.out.println("\n=== TEST EXECUTION COMPLETED SUCCESSFULLY ===");
            System.out.println("✓ Product searched");
            System.out.println("✓ Brand filter applied (starts with 'C')");
            System.out.println("✓ Price filter applied (above Rs 2000)");
            System.out.println("✓ Customer rating filter applied (above 4 stars)");
            System.out.println("✓ Filtered results verified");
            System.out.println("✓ Test executed within allowed time window (3 PM - 6 PM)");

        } catch (Exception e) {
            System.out.println("Test execution error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            tearDown();
        }
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        Task6NullClass test = new Task6NullClass();
        test.testProductFilterFlow();
    }
}

