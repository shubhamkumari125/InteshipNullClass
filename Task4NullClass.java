package Task4;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.Properties;
import java.util.List;

public class Task4NullClass {

    private WebDriver driver;
    private WebDriverWait wait;

    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
    }

    public void tearDown() {
        if (driver != null) driver.quit();
    }

    private double parsePrice(String raw) {
        if (raw == null || raw.isEmpty()) return 0.0;
        String cleaned = raw.replaceAll("[^0-9.,]", "").replaceAll(",", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getPriceFromProductPage(String productUrl) {
        try {
            driver.get(productUrl);

            // Try common Amazon price locators in order
            String[] xpaths = new String[]{
                "//span[@id='priceblock_ourprice']",
                "//span[@id='priceblock_dealprice']",
                "//span[contains(@class,'a-price-whole')]",
                "//span[@data-a-color='price']//span[contains(@class,'a-offscreen')]",
                "//span[contains(@id,'priceblock')]"
            };

            for (String xp : xpaths) {
                try {
                    WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xp)));
                    if (el != null) {
                        String txt = el.getText();
                        double p = parsePrice(txt);
                        if (p > 0.0) return p;
                    }
                } catch (Exception ignore) {
                }
            }

            // Fallback: try to find any element with currency symbol
            List<WebElement> offscreen = driver.findElements(By.xpath("//span[contains(@class,'a-offscreen')]"));
            for (WebElement e : offscreen) {
                double p = parsePrice(e.getText());
                if (p > 0.0) return p;
            }

        } catch (Exception e) {
            System.out.println("Error reading product page: " + e.getMessage());
        }
        return 0.0;
    }

    public void sendEmail(String smtpHost, int smtpPort, final String fromEmail, final String password,
                          String toEmail, String subject, String body) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }

    /**
     * Monitor product price and send an email when price <= threshold.
     * @param productUrl URL of the product page
     * @param threshold threshold price (same currency as page)
     * @param intervalSeconds polling interval in seconds
     * @param maxChecks maximum number of checks before stopping (<=0 for unlimited)
     */
    public void monitorPrice(String productUrl, double threshold, int intervalSeconds, int maxChecks,
                             String smtpHost, int smtpPort, String fromEmail, String fromPassword, String toEmail) {

        int attempts = 0;
        try {
            while (maxChecks <= 0 || attempts < maxChecks) {
                attempts++;
                System.out.println("Checking price (attempt " + attempts + ")...");
                double price = getPriceFromProductPage(productUrl);
                System.out.println("Current price: " + price);
                if (price > 0.0 && price <= threshold) {
                    String subject = "Price Alert: product price dropped to " + price;
                    String body = "The product at " + productUrl + " is now priced at " + price + " which is <= " + threshold;
                    try {
                        sendEmail(smtpHost, smtpPort, fromEmail, fromPassword, toEmail, subject, body);
                        System.out.println("Notification email sent to " + toEmail);
                    } catch (MessagingException me) {
                        System.out.println("Failed to send email: " + me.getMessage());
                    }
                    break;
                }
                try { Thread.sleep(intervalSeconds * 1000L); } catch (InterruptedException ie) { break; }
            }
        } finally {
            System.out.println("Monitoring finished after " + attempts + " attempts.");
        }
    }

    public static void main(String[] args) {
        // Usage:
        // java -cp <classpath> Task4NullClass <productUrl> <threshold> <intervalSeconds> <maxChecks> <smtpHost> <smtpPort> <fromEmail> <fromPassword> <toEmail>

        if (args.length < 9) {
            System.out.println("Usage: java Task4NullClass <productUrl> <threshold> <intervalSeconds> <maxChecks> <smtpHost> <smtpPort> <fromEmail> <fromPassword> <toEmail>");
            return;
        }

        String productUrl = args[0];
        double threshold = Double.parseDouble(args[1]);
        int intervalSeconds = Integer.parseInt(args[2]);
        int maxChecks = Integer.parseInt(args[3]);
        String smtpHost = args[4];
        int smtpPort = Integer.parseInt(args[5]);
        String fromEmail = args[6];
        String fromPassword = args[7];
        String toEmail = args[8];

        Task4NullClass monitor = new Task4NullClass();
        monitor.setUp();
        try {
            monitor.monitorPrice(productUrl, threshold, intervalSeconds, maxChecks, smtpHost, smtpPort, fromEmail, fromPassword, toEmail);
        } finally {
            monitor.tearDown();
        }
    }
}
