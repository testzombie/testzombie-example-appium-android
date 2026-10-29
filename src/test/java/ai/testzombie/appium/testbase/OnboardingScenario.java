package ai.testzombie.appium.testbase;

import ai.testzombie.appium.pages.HybridOnboardingPage;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Shared, identical business flow for all three TestZombie feature demos. */
public final class OnboardingScenario {
    private OnboardingScenario() {}

    public static void run(AndroidDriver driver, WebDriverWait wait, int level, String scenarioName) {
        System.out.printf("%n=== %s (Mutation Level %d) ===%n", scenarioName, level);
        HybridOnboardingPage app = new HybridOnboardingPage(driver, wait);
        app.chooseMutationLevel(level)
                .enterPassword()
                .enterPerson()
                .chooseEnterprisePlan(level);

        String status = app.completeWebSummary();
        assertTrue(status.toLowerCase(Locale.ROOT).contains("erfolgreich"),
                "Onboarding must succeed at Level " + level + "; status: " + status);
        System.out.printf("=== Mutation Level %d erfolgreich: %s ===%n", level, status);
    }
}
