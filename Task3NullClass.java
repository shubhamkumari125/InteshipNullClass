package Task3;

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

public class Task3NullClass {

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
        LocalTime start = LocalTime.of(12, 0); // 12 PM
        LocalTime end = LocalTime.of(15, 0);   // 3 PM
        return (!now.isBefore(start)) && now.isBefore(end);
    }

    private void login(String username, String password) throws InterruptedException {
        driver.get(BASE_URL);

        // Click on account/sign-in
        try {
            WebElement accountLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-link-accountList")));
            accountLink.click();
        } catch (Exception e) {
            System.out.println("Could not click account link: " + e.getMessage());
        }

        // Enter email/phone
        try {
            WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ap_email")));
            email.clear();
            email.sendKeys(username);
            WebElement cont = driver.findElement(By.id("continue"));
            cont.click();
        } catch (Exception e) {
            System.out.println("Email input not found: " + e.getMessage());
        }

        // Enter password
        try {
            WebElement pwd = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ap_password")));
            pwd.clear();
            pwd.sendKeys(password);
            WebElement signIn = driver.findElement(By.id("signInSubmit"));
            signIn.click();
        } catch (Exception e) {
            System.out.println("Password input / sign in failed: " + e.getMessage());
        }

        // Small wait for login to complete
        Thread.sleep(3000);
    }

    private String readProfileName() {
        try {
            // Try top-nav profile name
            List<WebElement> navName = driver.findElements(By.id("nav-link-accountList-nav-line-1"));
            if (!navName.isEmpty()) {
                String text = navName.get(0).getText().trim();
                if (!text.isEmpty()) return text;
            }

            // Try profile name on profile page
            List<WebElement> profileSpans = driver.findElements(By.xpath("//span[@class='a-profile-name']"));
            if (!profileSpans.isEmpty()) return profileSpans.get(0).getText().trim();

            // As fallback, go to account page
            driver.get("https://www.amazon.in/gp/css/homepage.html?ref_=nav_youraccount_btn");
            List<WebElement> acctNames = driver.findElements(By.xpath("//div[@id='ya-myab-content']//h2"));
            if (!acctNames.isEmpty()) return acctNames.get(0).getText().trim();

        } catch (Exception e) {
            System.out.println("Error reading profile name: " + e.getMessage());
        }
        return null;
    }

    private boolean isProfileNameValid(String profileName) {
        if (profileName == null || profileName.isEmpty()) return false;
        String forbidden = "ACGILK";
        String upper = profileName.toUpperCase();
        for (char c : forbidden.toCharArray()) {
            if (upper.indexOf(c) >= 0) return false;
        }
        return true;
    }

    public void tearDown() {
        if (driver != null) driver.quit();
    }

    public static void main(String[] args) {
        Task3NullClass test = new Task3NullClass();
        test.setUp();

        if (!test.isExecutionTimeValid()) {
            System.out.println("This test runs only between 12 PM and 3 PM local time.");
            test.tearDown();
            return;
        }

        String username = null;
        String password = null;
        if (args.length >= 2) {
            username = args[0];
            password = args[1];
        } else {
            System.out.println("No credentials provided via args; please pass username and password as arguments.");
            test.tearDown();
            return;
        }

        try {
            test.login(username, password);

            String profileName = test.readProfileName();
            System.out.println("Profile name read: " + profileName);

            Assert.assertNotNull(profileName, "Profile name should be present after login");
            boolean valid = test.isProfileNameValid(profileName);
            Assert.assertTrue(valid, "Profile name contains forbidden characters (A, C, G, I, L, K)");
            System.out.println("✓ Profile name is present and does not contain forbidden characters");

        } catch (AssertionError ae) {
            System.out.println("Assertion failed: " + ae.getMessage());
        } catch (Exception e) {
            System.out.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            test.tearDown();
        }
    }
}
