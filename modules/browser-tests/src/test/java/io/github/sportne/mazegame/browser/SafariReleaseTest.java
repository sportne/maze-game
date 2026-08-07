package io.github.sportne.mazegame.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Live release validation in Apple's branded Safari browser. */
@EnabledOnOs(OS.MAC)
@EnabledIfSystemProperty(named = "mazeGame.safariReleaseUrl", matches = ".+")
final class SafariReleaseTest {
  private static final int REFERENCE_WIDTH = 1280;
  private static final int REFERENCE_HEIGHT = 720;
  private static final String RESULT_KEY = "maze-game.best-result.milestone-1";
  private static final Set<String> JAVASCRIPT_ASSETS =
      Set.of("app.js", "styles.css", "mouse-sprites.png", "exploreMaze_T1.mp3");
  private static final Set<String> WEBASSEMBLY_ASSETS =
      Set.of(
          "app.wasm",
          "app.wasm-runtime.js",
          "styles.css",
          "mouse-sprites.png",
          "exploreMaze_T1.mp3");

  @Test
  @Timeout(180)
  void completesLiveGameFlowAndPersistsResult() throws IOException {
    Path reportDirectory = Path.of(requiredProperty("mazeGame.safariReportDirectory"));
    List<String> evidence = new ArrayList<>();
    WebDriver driver = null;
    try {
      driver = new SafariDriver(new SafariOptions());
      driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(15));
      recordBrowser(evidence, driver);
      runGameFlow(
          driver,
          requiredProperty("mazeGame.safariReleaseUrl"),
          "JavaScript",
          JAVASCRIPT_ASSETS,
          evidence);
      String wasmReleaseUrl = System.getProperty("mazeGame.safariWasmReleaseUrl");
      if (wasmReleaseUrl != null) {
        runGameFlow(driver, wasmReleaseUrl, "WebAssembly", WEBASSEMBLY_ASSETS, evidence);
      }
      captureScreenshot(driver, reportDirectory.resolve("result.png"));
      evidence.add("Result: PASS");
    } catch (Throwable failure) {
      evidence.add("Result: FAIL");
      evidence.add(failure.toString());
      if (driver != null) {
        captureScreenshot(driver, reportDirectory.resolve("failure.png"));
      }
      throw failure;
    } finally {
      if (driver != null) {
        driver.quit();
      }
      Files.createDirectories(reportDirectory);
      Files.write(reportDirectory.resolve("safari-release.txt"), evidence, StandardCharsets.UTF_8);
    }
  }

  private static void runGameFlow(
      WebDriver driver,
      String releaseUrl,
      String target,
      Set<String> requiredAssets,
      List<String> evidence)
      throws IOException {
    driver.manage().window().setSize(new org.openqa.selenium.Dimension(1280, 800));
    String separator = releaseUrl.contains("?") ? "&" : "?";
    driver.get(releaseUrl + separator + "safari-release=" + System.nanoTime());
    javascript(driver).executeScript("window.localStorage.removeItem(arguments[0]);", RESULT_KEY);
    driver.navigate().refresh();
    waitForRenderedControl(driver, 640, 280);
    assertPageStarted(driver);
    assertReleaseLocation(driver, releaseUrl);
    installRuntimeErrorCapture(driver);

    clickCanvas(driver, 640, 280);
    waitForRenderedControl(driver, 404, 280);
    assertAudioResumed(driver);
    clickCanvas(driver, 404, 280);
    waitForRenderedControl(driver, 738, 656);

    int emptyCellColor = screenshot(driver).getRGB(551, 360);
    clickCanvas(driver, 551, 360);
    waitForPixelChange(driver, 551, 360, emptyCellColor);
    int wallColor = screenshot(driver).getRGB(551, 360);
    clickCanvas(driver, 542, 656);
    clickCanvas(driver, 551, 360);
    waitForPixelChange(driver, 551, 360, wallColor);

    clickCanvas(driver, 738, 656);
    JavascriptExecutor javascript = javascript(driver);
    new WebDriverWait(driver, Duration.ofSeconds(15))
        .until(
            ignored ->
                javascript.executeScript(
                        "return window.localStorage.getItem(arguments[0]);", RESULT_KEY)
                    != null);
    String savedResult = readSavedResult(driver);
    assertRequiredAssetsReachable(driver, requiredAssets);
    assertRuntimeErrorsEmpty(driver);

    driver.navigate().refresh();
    waitForRenderedControl(driver, 640, 280);
    assertEquals(savedResult, readSavedResult(driver));
    assertPageStarted(driver);
    assertReleaseLocation(driver, releaseUrl);
    installRuntimeErrorCapture(driver);
    clickCanvas(driver, 640, 280);
    waitForRenderedControl(driver, 404, 280);
    assertAudioResumed(driver);
    assertRequiredAssetsReachable(driver, requiredAssets);
    assertRuntimeErrorsEmpty(driver);

    evidence.add(target + " release URL: " + releaseUrl);
    evidence.add(target + " saved result: " + savedResult);
    evidence.add(target + " required assets: HTTP 2xx with expected MIME types");
    evidence.add(target + " audio context after interaction: running");
    evidence.add(target + " refresh, interaction, and persistence: PASS");
    evidence.add(target + " runtime errors after initialization: none");
  }

  private static void recordBrowser(List<String> evidence, WebDriver driver) {
    Capabilities capabilities = ((SafariDriver) driver).getCapabilities();
    evidence.add("Browser: " + capabilities.getBrowserName());
    evidence.add("Browser version: " + capabilities.getBrowserVersion());
    evidence.add("Platform: " + capabilities.getPlatformName());
  }

  private static void assertPageStarted(WebDriver driver) {
    assertEquals(1, driver.findElements(By.cssSelector("canvas")).size());
    org.openqa.selenium.Dimension canvasSize = driver.findElement(By.id("canvas")).getSize();
    assertTrue(canvasSize.getWidth() > 0);
    assertTrue(canvasSize.getHeight() > 0);
    assertFalse(driver.findElement(By.id("loading-state")).isDisplayed());
    assertFalse(driver.findElement(By.id("failure-state")).isDisplayed());
  }

  private static void assertReleaseLocation(WebDriver driver, String releaseUrl) {
    URI expected = URI.create(releaseUrl);
    URI actual = URI.create(driver.getCurrentUrl());
    assertEquals(expected.getScheme(), actual.getScheme());
    assertEquals(expected.getAuthority(), actual.getAuthority());
    assertEquals(expected.getPath(), actual.getPath());
  }

  private static void installRuntimeErrorCapture(WebDriver driver) {
    javascript(driver)
        .executeScript(
            "window.__mazeGameSafariErrors = [];"
                + "window.addEventListener('error', function(event) {"
                + "window.__mazeGameSafariErrors.push(String(event.message));"
                + "});"
                + "window.addEventListener('unhandledrejection', function(event) {"
                + "window.__mazeGameSafariErrors.push(String(event.reason));"
                + "});");
  }

  private static void assertRuntimeErrorsEmpty(WebDriver driver) {
    Object errors =
        javascript(driver).executeScript("return window.__mazeGameSafariErrors.slice();");
    assertEquals(List.of(), errors);
  }

  private static void assertAudioResumed(WebDriver driver) {
    Object audioState =
        javascript(driver)
            .executeScript(
                "return window.Howler && window.Howler.ctx"
                    + " ? window.Howler.ctx.state : 'unavailable';");
    assertEquals("running", audioState);
  }

  private static void assertRequiredAssetsReachable(WebDriver driver, Set<String> requiredAssets) {
    Object result =
        javascript(driver)
            .executeScript(
                "return performance.getEntriesByType('resource').map(function(entry) {"
                    + "return entry.name;"
                    + "});");
    assertTrue(result instanceof List<?>);
    List<?> resources = (List<?>) result;
    List<String> requiredAssetUrls = new ArrayList<>();
    for (String asset : requiredAssets) {
      String assetUrl =
          resources.stream()
              .map(Object::toString)
              .filter(resource -> resource.endsWith(asset))
              .findFirst()
              .orElseGet(
                  () -> {
                    if (asset.endsWith(".wasm")) {
                      return URI.create(driver.getCurrentUrl()).resolve(asset).toString();
                    }
                    throw new AssertionError("missing loaded asset: " + asset);
                  });
      requiredAssetUrls.add(assetUrl);
    }
    Object responses =
        javascript(driver)
            .executeAsyncScript(
                "var urls = arguments[0];"
                    + "var done = arguments[arguments.length - 1];"
                    + "Promise.all(urls.map(function(url) {"
                    + "return fetch(url, {cache: 'no-store'}).then(function(response) {"
                    + "return {url: url, status: response.status, ok: response.ok,"
                    + "type: response.headers.get('content-type') || ''};"
                    + "}).catch(function(error) {"
                    + "return {url: url, status: 0, ok: false, type: '',"
                    + "error: String(error)};"
                    + "});"
                    + "})).then(done);",
                requiredAssetUrls);
    assertTrue(responses instanceof List<?>);
    for (Object response : (List<?>) responses) {
      assertTrue(response instanceof Map<?, ?>);
      Map<?, ?> fields = (Map<?, ?>) response;
      String url = fields.get("url").toString();
      assertTrue(Boolean.TRUE.equals(fields.get("ok")), () -> "asset request failed: " + fields);
      assertTrue(
          expectedContentType(url).stream()
              .anyMatch(type -> fields.get("type").toString().startsWith(type)),
          () -> "unexpected asset content type: " + fields);
    }
  }

  private static Set<String> expectedContentType(String url) {
    if (url.endsWith(".css")) {
      return Set.of("text/css");
    }
    if (url.endsWith(".png")) {
      return Set.of("image/png");
    }
    if (url.endsWith(".mp3")) {
      return Set.of("audio/mpeg", "audio/mp3");
    }
    if (url.endsWith(".wasm")) {
      return Set.of("application/wasm");
    }
    return Set.of("application/javascript", "text/javascript");
  }

  private static String readSavedResult(WebDriver driver) {
    Object value =
        javascript(driver)
            .executeScript("return window.localStorage.getItem(arguments[0]);", RESULT_KEY);
    assertNotNull(value);
    String savedResult = value.toString();
    assertTrue(savedResult.matches("[0-9]+:[0-9]+"));
    return savedResult;
  }

  private static void clickCanvas(WebDriver driver, int referenceX, int referenceY) {
    WebElement canvas = driver.findElement(By.id("canvas"));
    int offsetX = scale(referenceX, canvas.getSize().getWidth(), REFERENCE_WIDTH);
    int offsetY = scale(referenceY, canvas.getSize().getHeight(), REFERENCE_HEIGHT);
    new Actions(driver)
        .moveToElement(
            canvas,
            offsetX - canvas.getSize().getWidth() / 2,
            offsetY - canvas.getSize().getHeight() / 2)
        .click()
        .perform();
  }

  private static void waitForRenderedControl(WebDriver driver, int x, int y) throws IOException {
    int backgroundColor = screenshot(driver).getRGB(0, 0);
    waitForPixelChange(driver, x, y, backgroundColor);
  }

  private static void waitForPixelChange(WebDriver driver, int x, int y, int originalColor)
      throws IOException {
    long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
    while (pixelAt(driver, x, y) == originalColor && System.nanoTime() < deadline) {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new AssertionError("interrupted while waiting for Safari rendering", exception);
      }
    }
    assertFalse(pixelAt(driver, x, y) == originalColor, "expected rendered pixel change");
  }

  private static int pixelAt(WebDriver driver, int referenceX, int referenceY) throws IOException {
    BufferedImage image = screenshot(driver);
    int x = scale(referenceX, image.getWidth(), REFERENCE_WIDTH);
    int y = scale(referenceY, image.getHeight(), REFERENCE_HEIGHT);
    return image.getRGB(x, y);
  }

  private static int scale(int value, int actualSize, int referenceSize) {
    return Math.min(actualSize - 1, Math.round(value * actualSize / (float) referenceSize));
  }

  private static BufferedImage screenshot(WebDriver driver) throws IOException {
    byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
    assertNotNull(image);
    return image;
  }

  private static void captureScreenshot(WebDriver driver, Path destination) {
    try {
      Files.createDirectories(destination.getParent());
      Files.write(destination, ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
    } catch (RuntimeException | IOException ignored) {
      // Preserve the primary validation failure when diagnostic capture is unavailable.
    }
  }

  private static JavascriptExecutor javascript(WebDriver driver) {
    return (JavascriptExecutor) driver;
  }

  private static String requiredProperty(String name) {
    String value = System.getProperty(name);
    assertNotNull(value, name + " must be configured");
    return value;
  }
}
