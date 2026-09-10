package ai.testzombie.appium;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;

import java.util.Map;

public class ScrollHelper {

    public static boolean scrollDown(AndroidDriver driver) {
        Dimension size = driver.manage().window().getSize();

        int left = (int) (size.width * 0.10);
        int top = (int) (size.height * 0.20);
        int width = (int) (size.width * 0.80);
        int height = (int) (size.height * 0.60);

        Object result = ((JavascriptExecutor) driver).executeScript(
            "mobile: scrollGesture",
            Map.of(
                "left", left,
                "top", top,
                "width", width,
                "height", height,
                "direction", "down",
                "percent", 0.8
            )
        );

        return Boolean.TRUE.equals(result);
    }
}