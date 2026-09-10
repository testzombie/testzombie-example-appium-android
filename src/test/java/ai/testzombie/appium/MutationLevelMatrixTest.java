package ai.testzombie.appium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Kontrolltest: kennt die erwarteten Locator-Änderungen je Mutationslevel.
 * Damit lässt sich prüfen, ob alle Varianten der Demo-App technisch funktionieren.
 */
class MutationLevelMatrixTest extends AppiumTestBase {

    @ParameterizedTest(name = "Mutation Level {0}")
    @ValueSource(ints = {0, 1, 2, 3, 4})
    @DisplayName("Alle Mutationslevel mit level-spezifischen Locators")
    void runsEveryMutationLevel(int levelValue) throws Exception {
        try {
            startApp();
            MutationLevel level = MutationLevel.of(levelValue);
            HybridOnboardingPage app = new HybridOnboardingPage(driver, wait);

            System.out.printf("%n=== Starte Mutation Level %d ===%n", levelValue);
            app.chooseMutationLevel(levelValue);
            app.enterPassword(level);
            app.enterPerson(level);
            app.chooseEnterprisePlan(level);
            app.completeWebSummary(level);
            System.out.printf("=== Mutation Level %d erfolgreich ===%n", levelValue);
        } catch (Throwable error) {
            saveScreenshot("mutation-level-" + levelValue + "-failed");
            throw error;
        }
    }
}
