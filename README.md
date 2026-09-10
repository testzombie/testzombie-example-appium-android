# TestZombie + Appium Android – Quickstart

Dieses Beispielprojekt zeigt die Verwendung von **TestZombie** mit **Appium** für eine Android-Hybrid-App.

Die App enthält neben nativen Android-Elementen auch **Web-Inhalte**. Während der Tests wird deshalb vom nativen App-Kontext (`NATIVE_APP`) in einen `WEBVIEW`-Kontext gewechselt.

Für den WebView-Zugriff benötigt Appium einen zur installierten Chrome-/WebView-Version passenden **ChromeDriver**. Deshalb muss der Appium-Server mit aktiviertem automatischem ChromeDriver-Download gestartet werden.

---

## Voraussetzungen

Für die Ausführung werden benötigt:

- Java 17+
- Maven
- Appium
- Appium UiAutomator2 Driver
- Android SDK / ADB
- Android Emulator oder physisches Android-Gerät
- Chrome bzw. Android System WebView auf dem verwendeten Gerät
- TestZombie API Key und E-Mail-Adresse

Die Beispiel-APK `testzombie-demo.apk` liegt standardmäßig im Projektverzeichnis.

---


## 1. Appium starten

Die Demo-App ist eine **Hybrid-App** und enthält Web-Inhalte.

Im Test wird deshalb unter anderem in einen `WEBVIEW`-Kontext gewechselt. Für diesen Zugriff verwendet Appium ChromeDriver.

Die Android-Testkonfiguration enthält bereits:

Zusätzlich muss der Appium-Server den automatischen ChromeDriver-Download erlauben.

Appium deshalb mit folgendem Befehl starten:

```bash
appium --allow-insecure uiautomator2:chromedriver_autodownload
```

> **Wichtig:** Appium für dieses Projekt nicht nur mit `appium` starten.  
> Ohne `chromedriver_autodownload` kann der Wechsel in den WebView fehlschlagen, wenn kein kompatibler ChromeDriver verfügbar ist.

Appium wird vom Projekt standardmäßig unter folgender Adresse erwartet:

---

## 2. TestZombie Credentials

Für die Verwendung von TestZombie werden ein **API Key** und die zugehörige **E-Mail-Adresse** benötigt.

Die Credentials können auf verschiedene Arten gesetzt werden.

### Variante 1 – Credentials direkt im Java-Code setzen

Für einen einfachen lokalen Test oder zum schnellen Ausprobieren können die Credentials direkt bei der Initialisierung gesetzt werden.

Zum Beispiel in `AppiumTestBase`:

```java
TestZombieDriver.setCredentials(
        "YOUR_API_KEY",
        "YOUR_EMAIL"
);
```

Beispiel im Zusammenhang mit der Driver-Erzeugung:

```java
@BeforeEach
void setUp() {
    TestZombieDriver.setCredentials(
            "YOUR_API_KEY",
            "YOUR_EMAIL"
    );

    // Appium / TestZombie Driver anschließend wie gewohnt erzeugen
}
```

Danach können die Tests ohne zusätzliche Credential-Parameter gestartet werden:

```bash
mvn test
```

> Diese Variante eignet sich vor allem zum schnellen lokalen Ausprobieren. Echte API Keys sollten nicht in Git eingecheckt werden.

### Variante 2 – Credentials in der `pom.xml` setzen

Die Credentials können auch im Maven-Profil der `pom.xml` hinterlegt werden.

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

Damit die Werte im Testprozess als System Properties verfügbar sind, müssen sie über das Maven Surefire Plugin weitergegeben werden.

Zum Beispiel:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <testzombie.apikey>${testzombie.apikey}</testzombie.apikey>
            <testzombie.email>${testzombie.email}</testzombie.email>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

Im Java-Code können die Werte anschließend über die System Properties gelesen werden:

```java
String apiKey = System.getProperty("testzombie.apikey");
String email = System.getProperty("testzombie.email");

TestZombieDriver.setCredentials(apiKey, email);
```

Danach reicht zum Starten der Tests:

```bash
mvn test
```

> Auch hier gilt: Eine `pom.xml` mit echten Zugangsdaten sollte nicht in ein öffentliches oder gemeinsam genutztes Git-Repository eingecheckt werden.

### Variante 3 – Credentials über Maven übergeben

Die Credentials können direkt beim Maven-Aufruf als System Properties gesetzt werden:

```bash
mvn test \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

PowerShell:

```powershell
mvn test `
  -Dtestzombie.apikey=YOUR_API_KEY `
  -Dtestzombie.email=YOUR_EMAIL
```

Die Werte werden in Java über

```java
System.getProperty("testzombie.apikey")
System.getProperty("testzombie.email")
```

gelesen.

### Variante 4 – Credentials über Environment Variables

Linux / macOS:

```bash
export TESTZOMBIE_API_KEY="YOUR_API_KEY"
export TESTZOMBIE_EMAIL="YOUR_EMAIL"

mvn test
```

PowerShell:

```powershell
$env:TESTZOMBIE_API_KEY="YOUR_API_KEY"
$env:TESTZOMBIE_EMAIL="YOUR_EMAIL"

mvn test
```

Die Environment Variables werden in Java über

```java
System.getenv("TESTZOMBIE_API_KEY")
System.getenv("TESTZOMBIE_EMAIL")
```

gelesen.

### Credential-Auflösung im Beispielprojekt

Wenn die Credentials nicht direkt im Code gesetzt werden, verwendet `AppiumTestBase` die vorhandene Auflösung über System Properties und Environment Variables:

```java
String apiKey = firstNonBlank(
        System.getProperty("testzombie.apikey"),
        System.getenv("TESTZOMBIE_API_KEY")
);

String email = firstNonBlank(
        System.getProperty("testzombie.email"),
        System.getenv("TESTZOMBIE_EMAIL")
);

TestZombieDriver.setCredentials(apiKey, email);
```

Dabei haben die System Properties Vorrang vor den Environment Variables:

```text
-Dtestzombie.apikey
-Dtestzombie.email
        ↓
TESTZOMBIE_API_KEY
TESTZOMBIE_EMAIL
```

Für lokale Entwicklung und CI/CD sind Maven-Properties oder Environment Variables meist die bessere Wahl, damit keine Secrets im Quellcode oder in eingecheckten Konfigurationsdateien liegen.

---

## 3. Tests starten

Appium zuerst in einem Terminal mit aktiviertem ChromeDriver-Autodownload starten:

```bash
appium server --allow-insecure uiautomator2:chromedriver_autodownload
```

Anschließend können die Tests – abhängig von der gewählten Credential-Variante – in einem zweiten Terminal gestartet werden.

### Credentials direkt im Java-Code oder in der `pom.xml`

Wenn die Credentials bereits direkt im Java-Code oder im `defaults`-Profil der `pom.xml` gesetzt wurden:

```bash
mvn test
```

### Credentials über Maven

```bash
mvn test \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

### Credentials über Environment Variables

Credentials einmal setzen:

```bash
export TESTZOMBIE_API_KEY="YOUR_API_KEY"
export TESTZOMBIE_EMAIL="YOUR_EMAIL"
```

Tests starten:

```bash
mvn test
```

---

## 4. APK konfigurieren

Standardmäßig verwendet das Projekt:

```text
./testzombie-demo.apk
```

Eine andere APK kann über eine System Property angegeben werden:

```bash
mvn test \
  -Dapp.apk=/path/to/testzombie-demo.apk \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

Alternativ kann der APK-Pfad über eine Environment Variable gesetzt werden:

Linux / macOS:

```bash
export APP_APK="/path/to/testzombie-demo.apk"
```

PowerShell:

```powershell
$env:APP_APK="C:\path\to\testzombie-demo.apk"
```

---

## 5. Weitere Konfiguration

Die wichtigsten Einstellungen können entweder über System Properties oder Environment Variables überschrieben werden:

| Einstellung | System Property | Environment Variable | Standard |
|---|---|---|---|
| Appium Server | `-Dappium.server=...` | `APPIUM_SERVER` | `http://127.0.0.1:4723` |
| Device Name | `-Ddevice.name=...` | `DEVICE_NAME` | `Pixel 4` |
| APK | `-Dapp.apk=...` | `APP_APK` | `./testzombie-demo.apk` |
| TestZombie API Key | `-Dtestzombie.apikey=...` | `TESTZOMBIE_API_KEY` | – |
| TestZombie E-Mail | `-Dtestzombie.email=...` | `TESTZOMBIE_EMAIL` | – |

Beispiel:

```bash
mvn test \
  -Ddevice.name="Pixel 8" \
  -Dapp.apk=/path/to/testzombie-demo.apk \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

---

## 6. Einzelne Tests ausführen

Nur die Healing-Demo:

```bash
mvn -Dtest=BaselineHealingDemoTest test
```

Nur die Mutation-Level-Matrix:

```bash
mvn -Dtest=MutationLevelMatrixTest test
```

Wenn die Credentials nicht bereits direkt im Java-Code, in der `pom.xml` oder als Environment Variables gesetzt wurden, müssen sie beim Maven-Aufruf zusätzlich angegeben werden.

Beispiel:

```bash
mvn \
  -Dtest=BaselineHealingDemoTest \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL \
  test
```

---

## 7. Was zeigt die Demo?

### `BaselineHealingDemoTest`

Der Test verwendet für alle Mutation Levels die Baseline-Locators aus Level 0.

Der eigentliche Testablauf bleibt unverändert, während TestZombie die Healing-Funktion übernimmt.

### `MutationLevelMatrixTest`

Der Kontrolltest enthält die erwarteten Locators für die verschiedenen Mutation Levels.

Damit kann unabhängig von der Healing-Demo geprüft werden, ob die jeweiligen Varianten der Demo-App grundsätzlich bedienbar sind.

Der Ablauf ist:

```text
Mutation Level auswählen
        ↓
Passwortdaten erfassen
        ↓
Personendaten erfassen
        ↓
Enterprise-Plan auswählen
        ↓
von NATIVE_APP in WEBVIEW wechseln
        ↓
Bestätigung ausführen
        ↓
Erfolgsstatus prüfen
```

---

## 8. Projektstruktur

```text
testzombie-example-appium-android/
├── testzombie-demo.apk
├── pom.xml
├── README.md
├── scripts/
└── src/
    └── test/
        └── java/
            └── ai/
                └── testzombie/
                    └── appium/
                        ├── AppiumTestBase.java
                        ├── BaselineHealingDemoTest.java
                        ├── HybridOnboardingPage.java
                        ├── MutationLevel.java
                        ├── MutationLevelMatrixTest.java
                        └── ScrollHelper.java
```

### `AppiumTestBase`

Enthält:

- Appium-Server-Konfiguration
- Device-Konfiguration
- APK-Konfiguration
- TestZombie Credential-Auflösung
- Android Driver-Erzeugung
- ChromeDriver-Autodownload-Capability
- Screenshot-Erzeugung

### `HybridOnboardingPage`

Enthält die vorhandenen nativen Android- und WebView-Interaktionen.

### `MutationLevel`

Enthält die Locator-Konfiguration für die Mutation Levels.

---

## 9. Troubleshooting

### `Missing TestZombie credentials`

Es wurden keine TestZombie-Credentials gefunden.

Prüfen, ob die Credentials über eine der beschriebenen Varianten gesetzt wurden:

1. direkt im Java-Code
2. im `defaults`-Profil der `pom.xml`
3. über Maven mit `-Dtestzombie.apikey` und `-Dtestzombie.email`
4. über `TESTZOMBIE_API_KEY` und `TESTZOMBIE_EMAIL`

Beispiel über Maven:

```bash
mvn test \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

### `APK nicht gefunden`

Prüfen, ob `testzombie-demo.apk` im Projektroot liegt.

Alternativ einen eigenen Pfad setzen:

```bash
-Dapp.apk=/path/to/testzombie-demo.apk
```


### WebView / ChromeDriver Fehler

Wenn beim Wechsel in den `WEBVIEW` ein Fehler wie `No Chromedriver found` auftritt:

1. prüfen, ob Chrome bzw. Android System WebView auf dem verwendeten Gerät vorhanden ist
2. Appium mit aktiviertem ChromeDriver-Autodownload starten:

```bash
appium server --allow-insecure uiautomator2:chromedriver_autodownload
```

3. sicherstellen, dass Appium bei Bedarf einen kompatiblen ChromeDriver herunterladen kann

---

## Quickstart

```text
1. Credentials setzen
   - direkt im Java-Code
   - oder in der pom.xml
   - alternativ über Maven / Environment Variables
2. Appium mit chromedriver_autodownload starten
3. mvn test ausführen
```

Einfachste Variante für einen lokalen Test:

```java
TestZombieDriver.setCredentials(
        "YOUR_API_KEY",
        "YOUR_EMAIL"
);
```

Appium starten:

```bash
appium server --allow-insecure uiautomator2:chromedriver_autodownload
```

In einem zweiten Terminal:

```bash
mvn test
```

Die Tests bleiben normale Appium-Tests. TestZombie wird zentral bei der Driver-Erzeugung in `AppiumTestBase` integriert.
