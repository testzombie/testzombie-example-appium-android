package ai.testzombie.appium.support;

import com.testzombie.driver.SourceUpdateMode;
import com.testzombie.driver.TestZombieDriver;
import com.testzombie.driver.TestZombieDriverMobile;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/** Creates a TestZombie-wrapped Android session with per-scenario SDK settings. */
public final class AppiumDriverFactory {
    public static final String APP_PACKAGE = "ai.testzombie.hybriddemo";
    private AppiumDriverFactory() {}

    public static AndroidDriver create(SourceUpdateMode sourceUpdateMode, boolean discoveryEnabled) throws Exception {
        String apiKey = setting("testzombie.apikey", "TESTZOMBIE_API_KEY", null);
        String email = setting("testzombie.email", "TESTZOMBIE_EMAIL", null);
        if (apiKey == null || email == null) {
            throw new IllegalStateException("TestZombie credentials missing. Set TESTZOMBIE_API_KEY "
                    + "and TESTZOMBIE_EMAIL, or pass -Dtestzombie.apikey=... "
                    + "-Dtestzombie.email=... .");
        }

        String app = setting("app.apk", "APP_APK", "./testzombie-demo.apk");
        Path apkPath = Path.of(app).toAbsolutePath().normalize();
        if (!Files.isRegularFile(apkPath)) {
            throw new IllegalArgumentException("APK nicht gefunden: " + apkPath);
        }

        // IMPORTANT: configure before creating the Appium/TestZombie session.
        TestZombieDriver.setCredentials(apiKey, email);
        TestZombieDriver.setSourceUpdateMode(sourceUpdateMode);
        NativeDiscovery.configure(discoveryEnabled);

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName(setting("device.name", "DEVICE_NAME", "Pixel 10"))
                .setApp(apkPath.toString())
                .setAppPackage(APP_PACKAGE)
                .setAppActivity(".MainActivity")
                .setFullReset(true)
                .setNoReset(false)
                .setNewCommandTimeout(Duration.ofSeconds(180))
                .setAutoGrantPermissions(true);
        // Requires Appium server --allow-insecure uiautomator2:chromedriver_autodownload
        options.setCapability("appium:chromedriverAutodownload", true);

        String server = setting("appium.server", "APPIUM_SERVER", "http://127.0.0.1:4723");
        AndroidDriver driver = TestZombieDriverMobile.createAndroid(URI.create(server).toURL(), options);
        driver.setSetting("disableIdLocatorAutocompletion", true);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        return driver;
    }

    public static String setting(String systemProperty, String environment, String fallback) {
        String value = System.getProperty(systemProperty);
        if (value != null && !value.isBlank()) return value;
        value = System.getenv(environment);
        return value == null || value.isBlank() ? fallback : value;
    }
}
