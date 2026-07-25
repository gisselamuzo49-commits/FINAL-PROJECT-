package com.uce.functional;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class Hooks {

    // Compartido con StepDefinitions
    static WebDriver driver;

    // Directorio raíz donde se guardan las capturas
    private static final String SCREENSHOTS_DIR = "target/screenshots";

    @Before
    public void setUp(Scenario scenario) throws IOException {
        // Crear directorio de capturas si no existe
        Files.createDirectories(Paths.get(SCREENSHOTS_DIR));

        // ── Logging preferences para capturar logs de consola del browser ──────
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);

        // Configurar WebDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            File chromeBin = new File("/usr/bin/google-chrome");
            if (chromeBin.exists()) {
                options.setBinary(chromeBin);
            }
        }

        // WebDriverManager gestiona automáticamente el chromedriver compatible
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    /**
     * Vuelca los logs de consola del navegador a stdout.
     * Llamar desde StepDefinitions cuando se quiera capturar logs de la transición.
     */
    static void dumpBrowserLogs(String contexto) {
        try {
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
            if (logs.getAll().isEmpty()) {
                System.out.println("[BROWSER LOG] " + contexto + " → (sin mensajes de consola)");
            } else {
                for (LogEntry entry : logs.getAll()) {
                    System.out.printf("[BROWSER LOG] %s | %s | %s%n",
                            contexto, entry.getLevel(), entry.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("[BROWSER LOG ERROR] No se pudo leer la consola del browser: " + e.getMessage());
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        if (driver != null) {
            try {
                // Volcar logs de consola del browser al finalizar el escenario
                dumpBrowserLogs("@After[" + scenario.getName() + "]");

                // Determinar resultado
                String resultado = scenario.isFailed() ? "FAIL" : "PASS";

                // Normalizar nombre del escenario para el nombre de archivo
                String nombreEscenario = scenario.getName()
                        .replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]", "")
                        .replaceAll("\\s+", "-")
                        .toLowerCase();
                if (nombreEscenario.length() > 80) {
                    nombreEscenario = nombreEscenario.substring(0, 80);
                }

                // Timestamp
                String timestamp = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

                String nombreArchivo = nombreEscenario + "-" + resultado + "-" + timestamp + ".png";
                Path destino = Paths.get(SCREENSHOTS_DIR, nombreArchivo);

                // Tomar captura de pantalla
                File captura = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(captura.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("[SCREENSHOT] " + resultado + " → " + destino.toAbsolutePath());

                // Adjuntar al informe de Cucumber (embeds para reportes HTML)
                byte[] capturaBytes = Files.readAllBytes(destino);
                scenario.attach(capturaBytes, "image/png", nombreArchivo);

            } catch (Exception e) {
                System.err.println("[SCREENSHOT ERROR] No se pudo tomar la captura: " + e.getMessage());
            } finally {
                driver.quit();
                driver = null;
            }
        }
    }
}
