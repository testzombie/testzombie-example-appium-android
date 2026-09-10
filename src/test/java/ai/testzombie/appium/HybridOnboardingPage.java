package ai.testzombie.appium;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

final class HybridOnboardingPage {
    private final AndroidDriver driver;
    private final WebDriverWait wait;

    HybridOnboardingPage(AndroidDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    void chooseMutationLevel(int level) {
        clickNative("mutation-level-" + level);
        ScrollHelper.scrollDown(driver);
        clickNative("mutation-start");
    }

    void enterPassword(MutationLevel level) {
        typeNative(level.password(), "Test1234");
        typeNative(level.passwordConfirm(), "Test1234");
        clickNative(level.passwordNext());
    }

    void enterPerson(MutationLevel level) {
        typeNative(level.firstName(), "TestZombieAI");
        typeNative(level.lastName(), "Tester");
        typeNative(level.email(), "demo@example.com");
        clickNative(level.personNext());
    }

    void chooseEnterprisePlan(MutationLevel level) {
        if (level.usesPlanDropdown()) {
            clickNative(level.planSelector());
        }
        clickNative(level.enterprisePlan());
        clickNative(level.planNext());
    }

    void completeWebSummary(MutationLevel level) {
        switchToWebView();
        wait.until(ExpectedConditions.elementToBeClickable(By.id(level.termsCss()))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id(level.finishCss()))).click();
        String status = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status"))).getText();
        if (!status.toLowerCase().contains("erfolgreich")) {
            throw new AssertionError("Unerwarteter WebView-Status: " + status);
        }
    }

    private void switchToWebView() {
        WebDriverWait contextWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        contextWait.until(ignored -> driver.getContextHandles().stream()
                .anyMatch(context -> context.startsWith("WEBVIEW")));
        Set<String> contexts = driver.getContextHandles();
        String webView = contexts.stream()
                .filter(context -> context.startsWith("WEBVIEW"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Kein WEBVIEW-Kontext gefunden: " + contexts));
        driver.context(webView);
    }

    private void clickNative(String testTag) {
        nativeElement(testTag).click();
    }

    private void typeNative(String testTag, String value) {
        WebElement element = nativeElement(testTag);
        element.clear();
        element.sendKeys(value);
    }

    private WebElement nativeElement(String testTag) {
        By id = AppiumBy.id(testTag);
//        return driver.findElement(id);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(id));
    }
}
