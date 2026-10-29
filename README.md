# TestZombie Appium Android Example

A small Appium Android project that demonstrates **TestZombie Self-Healing**, **Discovery**, and optional **Persistent Healing** with `com.testzombie:testzombie-lib-java`.

The example intentionally uses normal Appium tests and Page Objects. The three main demonstrations use the original **Mutation Level 0** locators; there are no TestZombie-specific fallback locators in the test code. The demo app is **hybrid**: it starts in `NATIVE_APP` and finishes in a `WEBVIEW`.

## Quick start — 1, 2, 3

### 1. Install

Clone the project and make sure the following are available:

- **Java 21** or newer and Maven
- Node.js and Appium with the **UiAutomator2** driver
- Android SDK, `adb`, and an Android emulator or connected device
- Chrome / Android System WebView on the Android device
- The bundled `testzombie-demo.apk` in the project root

If you have not installed Appium and the Android driver yet:

```bash
npm install -g appium
appium driver install uiautomator2
```

Start an Android emulator (or connect a physical device), then check that `adb` sees it:

```bash
adb devices
```

The example uses **TestZombie Java SDK 1.3.1** via Maven:

```xml
<dependency>
    <groupId>com.testzombie</groupId>
    <artifactId>testzombie-lib-java</artifactId>
    <version>1.3.1</version>
</dependency>
```

**Start Appium in a separate terminal** before running the tests:

```bash
appium server --allow-insecure uiautomator2:chromedriver_autodownload
```

The `chromedriver_autodownload` permission matters because Appium needs a ChromeDriver compatible with the device's WebView version to switch into the `WEBVIEW` context. The default Appium server URL is `http://127.0.0.1:4723`.

### 2. Configure TestZombie

Create an account at **https://testzombie.ai** and copy your **API key** and **account e-mail**. Choose one of the following ways to configure the credentials.

#### Option 1 — Enter credentials in `pom.xml` (quickest local setup)

Open `pom.xml` and find the `defaults` profile near the bottom. Fill in its two existing, empty credential properties:

```xml
<profiles>
    <profile>
        <id>defaults</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <testzombie.apikey>YOUR_API_KEY</testzombie.apikey>
            <testzombie.email>YOUR_EMAIL</testzombie.email>
        </properties>
    </profile>
</profiles>
```

The existing Maven Surefire configuration passes these values as Java system properties. `AppiumDriverFactory` reads them and calls `TestZombieDriver.setCredentials(...)` before creating the Android session.

With the Appium server running, you can now start a demo **without adding credential arguments**:

```bash
mvn test -Dtest=OnboardingFlowTest
```

> **Security:** This is convenient for a local demo only. **Never commit or share a `pom.xml` containing real credentials.** Restore the two properties to empty values before pushing. For CI/CD, use one of the alternatives below.

#### Option 2 — Pass credentials as Maven parameters

Leave the credential properties in `pom.xml` empty and supply them when starting a test:

```bash
mvn test -Dtest=OnboardingFlowTest \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

#### Option 3 — Set environment variables

Leave the credential properties in `pom.xml` empty and set the environment variables instead.

**Windows CMD**

```cmd
set TESTZOMBIE_API_KEY=YOUR_API_KEY
set TESTZOMBIE_EMAIL=YOUR_EMAIL
```

**PowerShell**

```powershell
$env:TESTZOMBIE_API_KEY="YOUR_API_KEY"
$env:TESTZOMBIE_EMAIL="YOUR_EMAIL"
```

**Linux / macOS**

```bash
export TESTZOMBIE_API_KEY="YOUR_API_KEY"
export TESTZOMBIE_EMAIL="YOUR_EMAIL"
```

`AppiumDriverFactory` looks for Java system properties first, then the corresponding environment variables. All three options use the same `TestZombieDriver.setCredentials(...)` integration.

### 3. Run the Self-Healing demo

With your Android device/emulator running and Appium started in the other terminal:

```bash
mvn test -Dtest=OnboardingFlowTest
```

The same hybrid onboarding flow runs through five mutation levels:

```text
OFF → LIGHT → REALISTIC → HARD → EXTREME
 0      1        2        3        4
```

JUnit executes a separate test/session for each level. At higher levels the original locators may no longer work. TestZombie attempts to identify the correct element so the Appium action can continue.

> **Why select a test class?** `mvn test` runs all discovered tests, including Discovery and the Code Update demo. Code Update uses `APPLY` by default and may change your Java source files. Prefer the explicit commands throughout this README.

---

## Discovery — learn a working business flow

Self-Healing fixes broken locators. **Discovery has a different job:** it observes a successful test flow and collects useful context about what happened.

Run the dedicated Discovery demo:

```bash
mvn test -Dtest=DiscoveryLevel0Test
```

This example executes **one stable onboarding flow at Mutation Level 0 / OFF** — just like the Playwright Discovery example. Nothing is intentionally broken: TestZombie should observe the application's normal behavior.

### What happens during Discovery?

In simple terms:

```text
Existing Appium test
        ↓
successful native + WebView user flow
        ↓
TestZombie Discovery
        ↓
pages + elements + interactions + context
        ↓
AI Test Design
        ↓
business test cases
        ↓
selected test cases
        ↓
automation code
```

Discovery can collect context from the actual successful user journey, including visited screens/pages, relevant elements, interactions, and their sequence. This can support future AI-based test design and automation generation.

The dedicated test is in:

```text
src/test/java/ai/testzombie/appium/tests/DiscoveryLevel0Test.java
```

The important part is deliberately small:

```java
@Override
protected boolean discoveryEnabled() {
    return true;
}
```

Before the Appium session is created, `BaseAndroidTest` passes this flag to `AppiumDriverFactory`, which calls:

```java
NativeDiscovery.configure(discoveryEnabled);
```

This helper configures **the TestZombie SDK's native Discovery**, rather than writing a separate test-side JSON trace. With the corresponding SDK setters, the equivalent configuration is:

```java
TestZombieDriver.setDiscoveryEnabled(true);
TestZombieDriver.setDiscoveryFileEnabled(true);
TestZombieDriver.setDiscoveryFilePath(
    "test-results/testzombie-discovery-{testRunId}.json"
);
```

The provided `NativeDiscovery` helper checks for the public setters at runtime. **Local JSON output is requested, not guaranteed:** it depends on the file-related setters being available in the SDK. When supported, the output path is:

```text
test-results/testzombie-discovery-<testRunId>.json
```

The test itself remains normal Appium/Page-Object code.

### Why Mutation Level 0?

The regular demo intentionally changes locator-relevant UI properties to demonstrate Self-Healing. The Discovery demo uses the stable baseline so the recorded context describes a **valid business process**.

```text
Discovery     → understand the working flow
Self-Healing  → keep the flow running when locators change
```

---

## Code Update — run Level 0, heal Level 1 and update the source

Discovery learns a working flow. The **Code Update** demo shows the maintenance step that follows a UI change: TestZombie heals a broken locator and can persist the replacement in the Java Page Object.

Run the dedicated demo:

```bash
mvn test -Dtest=CodeUpdateLevel0And1Test
```

Unlike the Playwright example, which starts at Level 1, this Android example runs the same onboarding journey **twice in one Appium/TestZombie session**:

1. **Mutation Level 0 / OFF** — establish a successful baseline.
2. **Restart the demo app**, without replacing the Appium/TestZombie session.
3. **Mutation Level 1 / LIGHT** — repeat the same steps using the original Level-0 locators, allowing TestZombie to heal and write verified updates.

### What should happen?

```text
Existing Android Appium test
        ↓
successful Level-0 baseline
        ↓
restart app (same Appium session)
        ↓
Mutation Level 1 changes the UI / locators
        ↓
original locator no longer works
        ↓
TestZombie heals the element at runtime
        ↓
action succeeds with the healed locator
        ↓
Code Update writes the working locator back to the Page Object
```

The dedicated test is in:

```text
src/test/java/ai/testzombie/appium/tests/CodeUpdateLevel0And1Test.java
```

This demo selects `APPLY` by default and also accepts `PREVIEW`:

```java
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
```

`AppiumDriverFactory` configures this mode **before** creating the session:

```java
TestZombieDriver.setSourceUpdateMode(sourceUpdateMode);
```

To preview changes without modifying Java files:

```bash
mvn test -Dtest=CodeUpdateLevel0And1Test -Dtestzombie.codeupdate.mode=preview
```

After an `APPLY` run, inspect the Page Object changes:

```bash
git diff -- src/test/java/ai/testzombie/appium/pages
```

To restore the original Page Object locators for another demonstration:

```bash
git restore -- src/test/java/ai/testzombie/appium/pages
```

> `git restore` discards **all uncommitted changes** in these files. Review `git diff` first if you have made manual edits.

The scenarios stay intentionally separate:

```text
Discovery Level 0        → learn a valid business flow
Code Update Level 0 → 1  → establish baseline, heal changed UI, persist locators
```

---

## How TestZombie is integrated

The central integration is intentionally small.

Normal Appium:

```java
AndroidDriver driver = new AndroidDriver(serverUrl, options);
```

With TestZombie:

```java
TestZombieDriver.setCredentials(apiKey, email);
TestZombieDriver.setSourceUpdateMode(sourceUpdateMode);
AndroidDriver driver = TestZombieDriverMobile.createAndroid(serverUrl, options);
```

The example centralizes this in:

```text
src/test/java/ai/testzombie/appium/support/AppiumDriverFactory.java
```

The Android session uses `UiAutomator2Options` and the `ai.testzombie.hybriddemo` package. Its default configuration includes `Pixel 10` as the device name, `./testzombie-demo.apk` as the app path, and `http://127.0.0.1:4723` as the Appium server.

Existing Page Object APIs and Selenium-style `By` locators remain familiar:

```java
private static final By PASSWORD = By.id("password-input");
```

The three feature tests **do not switch to level-specific locators** when the app changes. TestZombie is responsible for healing. The separate `MutationLevelMatrixTest` is a diagnostic control that intentionally uses known locators for each level.

### Hybrid app: `NATIVE_APP` → `WEBVIEW`

The onboarding process consists of:

```text
Select mutation level
        ↓
Enter and confirm password           (NATIVE_APP)
        ↓
Enter personal details               (NATIVE_APP)
        ↓
Choose Enterprise plan               (NATIVE_APP)
        ↓
Switch to WEBVIEW
        ↓
Accept terms and finish onboarding   (WEBVIEW)
        ↓
Verify success status
```

The WebView step requires a compatible ChromeDriver. Start Appium with the `uiautomator2:chromedriver_autodownload` insecure feature enabled, as shown in **Quick start → Install**.

---

## Optional: Persistent Healing modes

The dedicated `CodeUpdateLevel0And1Test` enables `APPLY` by default. Other example tests use `OFF` for source updates.

| Mode | Behavior |
| --- | --- |
| `OFF` | Do not update Java source files. |
| `PREVIEW` | Log planned locator replacements without writing files. |
| `APPLY` | Write successfully healed locators back into the Java source. |

For this repository, select the Code Update mode with the test-specific property:

```bash
# Preview only
mvn test -Dtest=CodeUpdateLevel0And1Test -Dtestzombie.codeupdate.mode=preview

# Apply verified changes (the default)
mvn test -Dtest=CodeUpdateLevel0And1Test -Dtestzombie.codeupdate.mode=apply
```

The Java SDK also recognizes the `TESTZOMBIE_SOURCE_UPDATE_MODE` environment variable when no explicit Java mode overrides it. **In this example**, `AppiumDriverFactory` explicitly calls `TestZombieDriver.setSourceUpdateMode(...)`, so the selected mode for each test takes precedence over that environment variable.

---

## Useful commands

| Command | Purpose |
| --- | --- |
| `appium server --allow-insecure uiautomator2:chromedriver_autodownload` | Start Appium with WebView ChromeDriver download enabled |
| `adb devices` | Confirm a device or emulator is connected |
| `mvn test -Dtest=OnboardingFlowTest` | Self-Healing on levels 0–4 |
| `mvn test -Dtest=DiscoveryLevel0Test` | Native Discovery on stable Level 0 |
| `mvn test -Dtest=CodeUpdateLevel0And1Test` | Level 0 → Level 1, with source updates in `APPLY` mode |
| `mvn test -Dtest=CodeUpdateLevel0And1Test -Dtestzombie.codeupdate.mode=preview` | Preview source edits without writing files |
| `mvn test -Dtest=MutationLevelMatrixTest` | Diagnostic app control using known level-specific locators |
| `mvn test -Dtest=OnboardingFlowTest -Ddevice.name="Pixel 10"` | Select another device name |
| `mvn test -Dtest=OnboardingFlowTest -Dapp.apk=/path/to/app.apk` | Select a different APK |
| `mvn test` | Run **all** tests (including the source-modifying Code Update demo) |

You can also configure the device and server through either Maven properties or environment variables:

| Setting | Java system property | Environment variable | Default |
| --- | --- | --- | --- |
| Appium server | `appium.server` | `APPIUM_SERVER` | `http://127.0.0.1:4723` |
| Device name | `device.name` | `DEVICE_NAME` | `Pixel 10` |
| APK path | `app.apk` | `APP_APK` | `./testzombie-demo.apk` |
| API key | `testzombie.apikey` | `TESTZOMBIE_API_KEY` | No value |
| E-mail | `testzombie.email` | `TESTZOMBIE_EMAIL` | No value |
| Discovery file path | `testzombie.discovery.file` | `TESTZOMBIE_DISCOVERY_FILE` | `test-results/testzombie-discovery-{testRunId}.json` |

---

## Project structure

```text
testzombie-example-appium-android/
├── pom.xml
├── README.md
├── testzombie-demo.apk
├── scripts/
└── src/test/java/ai/testzombie/appium/
    ├── pages/
    │   └── HybridOnboardingPage.java
    ├── support/
    │   ├── AppiumDriverFactory.java
    │   ├── MutationLevel.java
    │   ├── NativeDiscovery.java
    │   └── ScrollHelper.java
    ├── testbase/
    │   ├── BaseAndroidTest.java
    │   └── OnboardingScenario.java
    └── tests/
        ├── CodeUpdateLevel0And1Test.java
        ├── DiscoveryLevel0Test.java
        ├── MutationLevelMatrixTest.java
        └── OnboardingFlowTest.java
```

`BaseAndroidTest` manages the Appium session lifecycle, sets per-test TestZombie options, and captures screenshots in `target/screenshots` on failure. `OnboardingScenario` runs the same onboarding steps in all three TestZombie demos. `HybridOnboardingPage` owns the native and WebView interactions, and `ScrollHelper` handles scrolling during the native flow.

### Optional control: MutationLevelMatrixTest

The matrix test is **not** another Self-Healing demo. It executes levels 0–4 with the app's known level-specific selectors to help isolate UI/app issues from locator-healing problems:

```bash
mvn test -Dtest=MutationLevelMatrixTest
```

It is a diagnostic test in the same project; the feature demos remain Self-Healing, Discovery, and Code Update.

---

## Android device and WebView troubleshooting

**`No Chromedriver found` or WebView context switch fails**

Ensure Android Chrome / System WebView is present and start Appium with:

```bash
appium server --allow-insecure uiautomator2:chromedriver_autodownload
```

Appium must be allowed to download a compatible ChromeDriver when one is not already available.

**`APK nicht gefunden`**

Confirm that `testzombie-demo.apk` exists in the working directory, or pass an explicit path:

```bash
mvn test -Dtest=OnboardingFlowTest -Dapp.apk=/absolute/path/to/testzombie-demo.apk
```

**`TestZombie credentials missing`**

Check the `defaults` profile in `pom.xml` or pass `-Dtestzombie.apikey` and `-Dtestzombie.email` to Maven. Alternatively, set `TESTZOMBIE_API_KEY` and `TESTZOMBIE_EMAIL`.

**No device or emulator detected**

Run `adb devices` and confirm the device appears as `device` rather than `offline` or `unauthorized`.

---

## Security

Never commit API keys, account credentials, or sensitive test artifacts to source control. For a first local test you may place credentials into the `pom.xml` `defaults` profile, but **restore the empty values before committing or sharing** the file. In shared repositories and CI/CD, use securely supplied runtime properties or environment variables.

## More validation

The five mutation levels let you distinguish an unchanged baseline from healing under progressively changed locators and UI structures. The three main tests demonstrate the SDK features; the matrix test provides a separate check of the app's known locator variants.

If Self-Healing unexpectedly fails, run the Level-0 baseline and the matching matrix level to investigate the underlying app behavior. For source-update runs, review `git diff` before committing the updated Page Objects.
