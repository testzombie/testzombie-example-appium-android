package ai.testzombie.appium;

import com.testzombie.driver.TestZombieDriver;
import com.testzombie.driver.TestZombieDriverMobile;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.junit.jupiter.api.AfterEach;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

abstract class AppiumTestBase {
    protected static final String APP_PACKAGE = "ai.testzombie.hybriddemo";

    protected AndroidDriver driver;
    protected WebDriverWait wait;

    protected void startApp() throws Exception {
        String server = property("appium.server", "APPIUM_SERVER", "http://127.0.0.1:4723");
        String device = property("device.name", "DEVICE_NAME", "Pixel 10");
        String app = property("app.apk", "APP_APK", "./testzombie-demo.apk");

        Path apkPath = Path.of(app).toAbsolutePath().normalize();
        if (!Files.isRegularFile(apkPath)) {
            throw new IllegalArgumentException("APK nicht gefunden: " + apkPath);
        }

        // TestZombie credentials:
        // - local quickstart: -Dtestzombie.apikey=... -Dtestzombie.email=...
        // - CI/CD: TESTZOMBIE_API_KEY and TESTZOMBIE_EMAIL environment variables
        String apiKey = firstNonBlank(
                System.getProperty("testzombie.apikey"),
                System.getenv("TESTZOMBIE_API_KEY")
        );
        String email = firstNonBlank(
                System.getProperty("testzombie.email"),
                System.getenv("TESTZOMBIE_EMAIL")
        );

        if (apiKey == null || email == null) {
           System.out.println(
                    "Missing TestZombie credentials. Set testzombie.apikey/testzombie.email "
                            + "or TESTZOMBIE_API_KEY/TESTZOMBIE_EMAIL."
            );
        }

        //Or copy here directly form testzombie.ai Onboarding screen
        TestZombieDriver.setCredentials(apiKey, email);

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName(device)
                .setApp(apkPath.toString())
                .setAppPackage(APP_PACKAGE)
                .setAppActivity(".MainActivity")
                .setFullReset(true)
                .setNoReset(false)
                .setNewCommandTimeout(Duration.ofSeconds(180))
                .setAutoGrantPermissions(true);

        options.setCapability("appium:chromedriverAutodownload", true);

        driver = TestZombieDriverMobile.createAndroid(URI.create(server).toURL(), options);
        driver.setSetting("disableIdLocatorAutocompletion", true);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void stopApp() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void saveScreenshot(String name) {
        if (driver == null) return;
        try {
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(dir.resolve(name + ".png"), png);
        } catch (IOException ignored) {
            // Ein Screenshot-Fehler soll den eigentlichen Testfehler nicht verdecken.
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return null;
    }

    private static String property(String property, String environment, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) value = System.getenv(environment);
        return value == null || value.isBlank() ? fallback : value;
    }
}
