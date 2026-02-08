package Task2;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import org.testng.Assert;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

public class Task2NullClass {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.amazon.in/";
    private static final int TIMEOUT = 15;

    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
        driver.manage().window().maximize();
    }

    private boolean isExecutionTimeValid() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(18, 0); // 6 PM
        LocalTime end = LocalTime.of(19, 0);   // 7 PM
        return (!now.isBefore(start)) && now.isBefore(end);
    }

    private boolean validateUserName(String username) {
        if (username == null) return false;
        if (username.length() != 10) return false;
        return username.matches("[A-Za-z0-9]{10}");
    }

    private double addProductToCartAndReturnPrice(String searchQuery) throws InterruptedException {
        // Perform search
        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("twotabsearchtextbox")));
        searchInput.clear();
        searchInput.sendKeys(searchQuery);
        WebElement searchBtn = driver.findElement(By.id("nav-search-submit-button"));
        searchBtn.click();
        Thread.sleep(2500);

        // Find first search result
        List<WebElement> results = driver.findElements(By.xpath("//div[@data-component-type='s-search-result']"));
        if (results.isEmpty()) throw new RuntimeException("No search results for: " + searchQuery);

        WebElement first = results.get(0);
        WebElement link = first.findElement(By.xpath(".//h2//a"));
        String productTitle = first.findElement(By.xpath(".//h2//a//span")).getText();
        System.out.println("Opening product: " + productTitle);
        link.click();

        // Switch to the new tab if opened
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        if (tabs.size() > 1) driver.switchTo().window(tabs.get(tabs.size() - 1));

        // Wait for price to be visible
        double price = 0.0;
        try {
            // Amazon shows price in different locators; try common ones
            WebElement priceWhole = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='a-price-whole']")));
            String whole = priceWhole.getText();
            String fraction = "0";
            List<WebElement> fracElems = driver.findElements(By.xpath("//span[@class='a-price-fraction']"));
            if (!fracElems.isEmpty()) fraction = fracElems.get(0).getText();
            String combined = (whole + "." + fraction).replaceAll("[,₹\\s]", "");
            price = Double.parseDouble(combined);
        } catch (Exception e) {
            System.out.println("Couldn't read price on product page: " + e.getMessage());
        }

        // Click Add to Cart
        try {
            WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-button")));
            addToCart.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("Add to cart failed: " + e.getMessage());
        }

        // Close product tab if it was opened in a new tab and switch back
        tabs = new ArrayList<>(driver.getWindowHandles());
        if (tabs.size() > 1) {
            driver.close();
            driver.switchTo().window(tabs.get(0));
        }

        return price;
    }

    private double getCartSubtotalFromCartPage() {
        try {
            driver.get("https://www.amazon.in/gp/cart/view.html?ref_=nav_cart");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sc-subtotal-amount-activecart")));
            WebElement subtotalElem = driver.findElement(By.id("sc-subtotal-amount-activecart"));
            String text = subtotalElem.getText(); // e.g. "₹ 2,345.00"
            String cleaned = text.replaceAll("[^0-9.]", "");
            if (cleaned.isEmpty()) return 0.0;
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            System.out.println("Could not read cart subtotal: " + e.getMessage());
            return 0.0;
        }
    }

    public void tearDown() {
        if (driver != null) driver.quit();
    }

    public static void main(String[] args) {
        Task2NullClass test = new Task2NullClass();
        test.setUp();

        // Validate execution time
        if (!test.isExecutionTimeValid()) {
            System.out.println("This test runs only between 6 PM and 7 PM (local time).");
            test.tearDown();
            return;
        }

        // Validate username (example). Replace with real username if available.
        String username = "UserName123"; // change as needed
        if (!test.validateUserName(username)) {
            System.out.println("Invalid username. It must be exactly 10 alphanumeric characters with no special characters.");
            test.tearDown();
            return;
        }

        try {
            // Open base URL
            test.driver.get(test.BASE_URL);

            // Define products to add (you can change these search terms)
            String[] products = {"stainless steel water bottle", "notebook ruled", "wooden cutting board"};
            double sumPrices = 0.0;

            for (String p : products) {
                double price = test.addProductToCartAndReturnPrice(p);
                System.out.println("Added product '" + p + "' price captured: " + price);
                sumPrices += price;
                // small wait between additions
                Thread.sleep(1500);
            }

            System.out.println("Sum of captured product prices: " + sumPrices);

            // Verify cart subtotal (preferred) and compare
            double cartSubtotal = test.getCartSubtotalFromCartPage();
            System.out.println("Cart subtotal read from cart page: " + cartSubtotal);

            double finalAmount = (cartSubtotal > 0.0) ? cartSubtotal : sumPrices;

            // Verify amount is more than 2000 rupees
            Assert.assertTrue(finalAmount > 2000.0, "Total amount should be more than 2000 INR. Actual: " + finalAmount);
            System.out.println("✓ Total verified greater than ₹2000: " + finalAmount);

        } catch (AssertionError ae) {
            System.out.println("Assertion failed: " + ae.getMessage());
        } catch (Exception ex) {
            System.out.println("Test failed: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            test.tearDown();
        }
    }
}

