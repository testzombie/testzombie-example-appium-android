package ai.testzombie.appium.tests;

import ai.testzombie.appium.testbase.BaseAndroidTest;
import ai.testzombie.appium.testbase.OnboardingScenario;
import com.testzombie.driver.SourceUpdateMode;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Persistent Healing: Level 0 -> restart app -> Level 1 in ONE Appium session.
 * The Level-1 pass uses the same baseline locator declarations in the Page Object.
 */
class CodeUpdateLevel0And1Test extends BaseAndroidTest {
    @Override
    protected SourceUpdateMode sourceUpdateMode() {
        String value = System.getProperty("testzombie.codeupdate.mode", "apply").trim();
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "apply" -> SourceUpdateMode.APPLY;
            case "preview" -> SourceUpdateMode.PREVIEW;
            default -> throw new IllegalArgumentException(
                    "testzombie.codeupdate.mode must be apply or preview");
        };
    }

    @Test
    void completeBaselineThenHealAndPersistLevelOneLocators() {
        try {
            System.out.println("Code Update 1/2: baseline on mutation level 0");
            OnboardingScenario.run(driver, wait, 0, "Code Update Level 0");

            System.out.println("Code Update: restarting the app (same TestZombie driver)");
            resetDemoState();

            System.out.println("Code Update 2/2: healing on mutation level 1");
            OnboardingScenario.run(driver, wait, 1, "Code Update Level 1");
            System.out.println("Source changes: git diff -- src/test/java/ai/testzombie/appium/pages");
        } catch (Throwable error) {
            saveScreenshot("codeupdate-level-0-and-1-failed");
            throw error;
        }
    }
}
