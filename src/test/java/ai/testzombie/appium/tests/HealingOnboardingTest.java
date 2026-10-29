package ai.testzombie.appium.tests;

import ai.testzombie.appium.testbase.BaseAndroidTest;
import ai.testzombie.appium.testbase.OnboardingScenario;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Self-Healing: five independent sessions, all using unmodified Level-0 locators. */
class HealingOnboardingTest extends BaseAndroidTest {
    @ParameterizedTest(name = "Self-Healing at mutation level {0}")
    @ValueSource(ints = {0, 1, 2, 3, 4})
    void completeOnboardingOnEveryMutationLevel(int level) {
        try {
            OnboardingScenario.run(driver, wait, level, "Self-Healing Level " + level);
        } catch (Throwable error) {
            saveScreenshot("healing-level-" + level + "-failed");
            throw error;
        }
    }
}
