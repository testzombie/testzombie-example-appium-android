package ai.testzombie.appium.testbase;

import ai.testzombie.appium.support.AppiumDriverFactory;
import ai.testzombie.appium.support.NativeDiscovery;
import com.testzombie.driver.SourceUpdateMode;
import com.testzombie.driver.TestZombieDriver;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/** Common JUnit lifecycle; each demo selects features by overriding two methods. */
public abstract class BaseAndroidTest {
    protected AndroidDriver driver;
    protected WebDriverWait wait;

    protected SourceUpdateMode sourceUpdateMode() {
        return SourceUpdateMode.OFF;
    }

    protected boolean discoveryEnabled() {
        return false;
    }

    @BeforeEach
    void startApp(TestInfo info) throws Exception {
        System.out.printf("%n=== %s | sourceUpdate=%s | discovery=%s ===%n",
                info.getDisplayName(), sourceUpdateMode(), discoveryEnabled());
        driver = AppiumDriverFactory.create(sourceUpdateMode(), discoveryEnabled());
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Restart the demo app WITHOUT creating a new driver / TestZombie session.
     * This allows CodeUpdate to perform Level 0 and Level 1 back-to-back.
     * A process restart is used instead of a second Appium session, matching
     * the Selenium example's "same session, reset application" structure.
     */
    protected void resetDemoState() {
        driver.context("NATIVE_APP");
        driver.terminateApp(AppiumDriverFactory.APP_PACKAGE);
        driver.activateApp(AppiumDriverFactory.APP_PACKAGE);
        wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.id("mutation-level-0")));
    }

    @AfterEach
    void stopApp() {
        try {
            if (driver != null) driver.quit();
        } finally {
            driver = null;
            wait = null;
            NativeDiscovery.configure(false);
            TestZombieDriver.setSourceUpdateMode(SourceUpdateMode.OFF);
        }
    }

    protected void saveScreenshot(String name) {
        if (driver == null) return;
        try {
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            Files.write(dir.resolve(name + ".png"), ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
        } catch (IOException | RuntimeException ignored) {
            // Screenshot errors should not mask the original test failure.
        }
    }
}
