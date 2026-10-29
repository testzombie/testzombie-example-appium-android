package ai.testzombie.appium.tests;

import ai.testzombie.appium.testbase.BaseAndroidTest;
import ai.testzombie.appium.testbase.OnboardingScenario;
import org.junit.jupiter.api.Test;

/** Same as Playwright and Selenium: SDK Discovery on, one stable Level-0 flow. */
class DiscoveryLevel0Test extends BaseAndroidTest {
    @Override
    protected boolean discoveryEnabled() {
        return true;
    }

    @Test
    void discoverStableOnboardingAtLevelZero() {
        try {
            OnboardingScenario.run(driver, wait, 0, "Discovery Level 0");
        } catch (Throwable error) {
            saveScreenshot("discovery-level-0-failed");
            throw error;
        }
    }
}
