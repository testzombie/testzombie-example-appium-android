package ai.testzombie.appium.pages;

import ai.testzombie.appium.support.MutationLevel;
import ai.testzombie.appium.support.ScrollHelper;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

/**
 * Android Page Object for the hybrid demo application.
 *
 * IMPORTANT: The three TestZombie feature tests ALWAYS use the literal Level-0
 * locators below, including when the running app uses mutation levels 1-4.
 * Healing and source updates belong to TestZombie, not to this Page Object.
 *
 * Deliberately use By.id("literal") constants: the original locator expressions
 * remain easy to find when TestZombie writes healed locators back to Java source.
 */
public final class HybridOnboardingPage {
    private static final By MUTATION_START = By.id("mutation-start");
     private static final By PASSWORD = By.id("password-input");
     private static final By PASSWORD_CONFIRM = By.id("password-confirm-input");
     private static final By PASSWORD_NEXT = By.id("password-next");
     private static final By FIRST_NAME = By.id("first-name-input");
     private static final By LAST_NAME = By.id("last-name-input");
     private static final By EMAIL = By.id("email-input");
     private static final By PERSON_NEXT = By.id("person-next");
     private static final By ENTERPRISE_PLAN = By.id("plan-enterprise");
    private static final By PLAN_SELECTOR = By.id("plan-selector");
     private static final By PLAN_NEXT = By.id("plan-next");
     private static final By TERMS = By.cssSelector("#terms");
     private static final By FINISH = By.cssSelector("#finishButton");
     private static final By STATUS = By.cssSelector("section.card.demo > p");

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public HybridOnboardingPage(AndroidDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public HybridOnboardingPage chooseMutationLevel(int level) {
        if (level < 0 || level > 4) {
            throw new IllegalArgumentException("Mutation Level muss zwischen 0 und 4 liegen: " + level);
        }
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.id("mutation-level-" + level))).click();
        ScrollHelper.scrollDown(driver);
        clickNative(MUTATION_START);
        return this;
    }

    /** Baseline locators for all Self-Healing, Discovery and Code-Update runs. */
    public HybridOnboardingPage enterPassword() {
        typeNative(PASSWORD, "Test1234");
        typeNative(PASSWORD_CONFIRM, "Test1234");
        clickNative(PASSWORD_NEXT);
        return this;
    }

    public HybridOnboardingPage enterPerson() {
        typeNative(FIRST_NAME, "TestZombieAI");
        typeNative(LAST_NAME, "Tester");
        typeNative(EMAIL, "demo@example.com");
        clickNative(PERSON_NEXT);
        return this;
    }

    public HybridOnboardingPage chooseEnterprisePlan(int mutationLevel) {
        // Level 4 replaces the plan controls with a dropdown. Open it using a
        // baseline locator so that TestZombie can heal this element as well.
        if (mutationLevel == 4) {
            clickNative(PLAN_SELECTOR);
        }
        clickNative(ENTERPRISE_PLAN);
        clickNative(PLAN_NEXT);
        return this;
    }

    public String completeWebSummary() {
        switchToWebView();
        wait.until(ExpectedConditions.elementToBeClickable(TERMS)).click();
        wait.until(ExpectedConditions.elementToBeClickable(FINISH)).click();
        return wait.until(ExpectedConditions.visibilityOfElementLocated(STATUS)).getText();
    }

    /**
     * Diagnostic control only: uses known level-specific selectors to check the APK.
     * Not used by the three TestZombie feature tests (would bypass healing).
     */
    public String completeWithKnownLocators(MutationLevel level) {
        typeNative(AppiumBy.id(level.password()), "Test1234");
        typeNative(AppiumBy.id(level.passwordConfirm()), "Test1234");
        clickNative(AppiumBy.id(level.passwordNext()));

        typeNative(AppiumBy.id(level.firstName()), "TestZombieAI");
        typeNative(AppiumBy.id(level.lastName()), "Tester");
        typeNative(AppiumBy.id(level.email()), "demo@example.com");
        clickNative(AppiumBy.id(level.personNext()));

        if (level.usesPlanDropdown()) {
            clickNative(AppiumBy.id(level.planSelector()));
        }
        clickNative(AppiumBy.id(level.enterprisePlan()));
        clickNative(AppiumBy.id(level.planNext()));

        switchToWebView();
        By terms = level.value() == 0
                ? By.id(level.termsCss()) : By.cssSelector(level.termsCss());
        By finish = level.value() == 0
                ? By.id(level.finishCss()) : By.cssSelector(level.finishCss());
        wait.until(ExpectedConditions.elementToBeClickable(terms)).click();
        wait.until(ExpectedConditions.elementToBeClickable(finish)).click();
        return wait.until(ExpectedConditions.visibilityOfElementLocated(STATUS)).getText();
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

    private void clickNative(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    private void typeNative(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(value);
    }
}
