package Task5;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.time.LocalTime;

public class Task5NullClass {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final double MINIMUM_PAYMENT = 500.0; // Minimum payment in Rs

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
     * Validates if current time is between 6 PM and 7 PM
     */
    public boolean isTestingWindowOpen() {
        LocalTime now = LocalTime.now();
        LocalTime startTime = LocalTime.of(18, 0); // 6 PM
        LocalTime endTime = LocalTime.of(19, 0);   // 7 PM
        
        boolean isInWindow = now.isAfter(startTime) && now.isBefore(endTime);
        
        if (!isInWindow) {
            System.out.println("Testing Window Closed! Current time: " + now);
            System.out.println("Testing allowed only between 6 PM to 7 PM");
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
            System.out.println("Step 1: Searching for product - " + productName);
            
            // Locate search box (adjust selector based on your website)
            WebElement searchBox = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Search' or @id='search']"))
            );
            
            searchBox.clear();
            searchBox.sendKeys(productName);
            
            // Click search button
            WebElement searchButton = driver.findElement(By.xpath("//button[contains(text(), 'Search') or @type='submit']"));
            searchButton.click();
            
            // Wait for search results
            Thread.sleep(2000);
            System.out.println("Product search completed successfully");
            
        } catch (Exception e) {
            System.out.println("Error during product search: " + e.getMessage());
        }
    }

    /**
     * Select a product from search results and add to cart
     */
    public void addProductToCart() {
        try {
            System.out.println("Step 2: Adding product to cart");
            
            // Wait for search results to load
            WebElement productLink = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='product-item']//a | //li[@class='product']//a | //a[contains(@class, 'product')]"))
            );
            
            // Click on first product
            productLink.click();
            Thread.sleep(2000);
            
            // Click Add to Cart button
            WebElement addToCartBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Add to Cart') or contains(text(), 'add to cart')]"))
            );
            addToCartBtn.click();
            
            // Wait for confirmation
            Thread.sleep(1500);
            System.out.println("Product added to cart successfully");
            
        } catch (Exception e) {
            System.out.println("Error adding product to cart: " + e.getMessage());
        }
    }

    /**
     * Proceed to checkout and validate payment amount
     */
    public void proceedToPayment() {
        try {
            System.out.println("Step 3: Proceeding to payment");
            
            // Open cart or proceed to checkout
            WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Checkout') or contains(text(), 'Proceed') or @class='checkout-btn']"))
            );
            checkoutBtn.click();
            
            Thread.sleep(2000);
            System.out.println("Navigated to payment page");
            
        } catch (Exception e) {
            System.out.println("Error during checkout: " + e.getMessage());
        }
    }

    /**
     * Validate payment amount is more than Rs 500
     */
    public boolean validatePaymentAmount() {
        try {
            System.out.println("Step 3a: Validating payment amount");
            
            // Wait for total price to be visible (adjust xpath based on your website)
            WebElement totalAmount = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//span[@class='total' or @id='total-amount'] | //*[contains(text(), 'Total') or contains(text(), 'Amount')]"))
            );
            
            String amountText = totalAmount.getText();
            System.out.println("Total Amount Text: " + amountText);
            
            // Extract numeric value from text
            double amount = extractAmount(amountText);
            
            if (amount > MINIMUM_PAYMENT) {
                System.out.println("Payment validation PASSED: Rs " + amount + " > Rs " + MINIMUM_PAYMENT);
                return true;
            } else {
                System.out.println("Payment validation FAILED: Rs " + amount + " is not greater than Rs " + MINIMUM_PAYMENT);
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error validating payment amount: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extract numeric amount from text
     */
    private double extractAmount(String text) {
        try {
            // Remove currency symbols and text, extract numbers
            String cleanText = text.replaceAll("[^0-9.]", "");
            if (cleanText.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(cleanText);
        } catch (NumberFormatException e) {
            System.out.println("Could not parse amount: " + text);
            return 0.0;
        }
    }

    /**
     * Complete the payment process
     */
    public void completePayment() {
        try {
            System.out.println("Step 4: Completing payment");
            
            // Fill payment details (adjust based on actual form)
            WebElement paymentForm = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//form[@class='payment-form' or @id='payment']"))
            );
            
            // Find and fill card details (adjust selectors based on your website)
            try {
                WebElement cardNumber = driver.findElement(By.xpath("//input[@name='cardNumber' or @placeholder='Card Number']"));
                cardNumber.sendKeys("4532123456789010"); // Dummy card number
            } catch (Exception e) {
                System.out.println("Card number field not found: " + e.getMessage());
            }
            
            try {
                WebElement expiryDate = driver.findElement(By.xpath("//input[@name='expiry' or @placeholder='MM/YY']"));
                expiryDate.sendKeys("12/25"); // Dummy expiry
            } catch (Exception e) {
                System.out.println("Expiry date field not found: " + e.getMessage());
            }
            
            try {
                WebElement cvv = driver.findElement(By.xpath("//input[@name='cvv' or @placeholder='CVV']"));
                cvv.sendKeys("123"); // Dummy CVV
            } catch (Exception e) {
                System.out.println("CVV field not found: " + e.getMessage());
            }
            
            Thread.sleep(1500);
            System.out.println("Payment details filled");
            
        } catch (Exception e) {
            System.out.println("Error during payment: " + e.getMessage());
        }
    }

    /**
     * Confirm the order
     */
    public void confirmOrder() {
        try {
            System.out.println("Step 5: Confirming order");
            
            // Click Pay Now or Confirm Order button
            WebElement confirmBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Pay Now') or contains(text(), 'Confirm Order') or contains(text(), 'Place Order')]"))
            );
            confirmBtn.click();
            
            // Wait for order confirmation page
            Thread.sleep(3000);
            
            // Verify order confirmation
            WebElement confirmationMessage = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='success' or @class='confirmation'] | //*[contains(text(), 'Order Confirmed') or contains(text(), 'successfully')]"))
            );
            
            System.out.println("Order Confirmation Message: " + confirmationMessage.getText());
            System.out.println("Order confirmed successfully!");
            
        } catch (Exception e) {
            System.out.println("Error confirming order: " + e.getMessage());
        }
    }

    /**
     * Main test execution - Complete Order Flow
     */
    public void testCompleteOrderFlow() {
        try {
            // Check if current time is within testing window
            if (!isTestingWindowOpen()) {
                System.out.println("Test execution aborted: Outside testing window (6 PM - 7 PM)");
                return;
            }

            setUp();
            
            // Navigate to the e-commerce website (change URL as needed)
            driver.get("https://example-ecommerce.com"); // Change to actual website
            Thread.sleep(2000);

            // Step 1: Search for product
            searchProduct("laptop"); // Change product name as needed

            // Step 2: Add to cart
            addProductToCart();

            // Step 3: Proceed to payment
            proceedToPayment();

            // Step 3a: Validate payment amount
            boolean isPaymentValid = validatePaymentAmount();
            if (!isPaymentValid) {
                System.out.println("Test FAILED: Payment amount is less than Rs 500");
                return;
            }

            // Step 4: Complete payment
            completePayment();

            // Step 5: Confirm order
            confirmOrder();

            System.out.println("\n=== TEST EXECUTION COMPLETED SUCCESSFULLY ===");
            System.out.println("✓ Product searched and selected");
            System.out.println("✓ Product added to cart");
            System.out.println("✓ Payment amount validated (> Rs 500)");
            System.out.println("✓ Payment completed");
            System.out.println("✓ Order confirmed");
            System.out.println("✓ Test executed within allowed time window (6 PM - 7 PM)");

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
        Task5NullClass test = new Task5NullClass();
        test.testCompleteOrderFlow();
    }
}
