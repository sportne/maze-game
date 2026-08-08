package io.github.sportne.mazegame.browser;

import static io.github.sportne.mazegame.browser.BrowserGameScenario.MILESTONE_ONE_RESULT_KEY;
import static io.github.sportne.mazegame.browser.BrowserGameScenario.MILESTONE_THREE_RESULT_KEY;
import static io.github.sportne.mazegame.browser.BrowserGameScenario.MILESTONE_TWO_RESULT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.browser.BrowserGameScenario.ScreenPoint;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.state.GamePhase;
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
  private static final Set<String> JAVASCRIPT_ASSETS =
      Set.of("app.js", "styles.css", "mouse-sprites.png", "scout-mouse.png", "exploreMaze_T1.mp3");
  private static final Set<String> WEBASSEMBLY_ASSETS =
      Set.of(
          "app.wasm",
          "app.wasm-runtime.js",
          "styles.css",
          "mouse-sprites.png",
          "scout-mouse.png",
          "exploreMaze_T1.mp3");

  @Test
  @Timeout(300)
  void completesLiveThreeLevelFlowAndPersistsResults() throws IOException {
    Path reportDirectory = Path.of(requiredProperty("mazeGame.safariReportDirectory"));
    List<String> evidence = new ArrayList<>();
    WebDriver driver = null;
    Throwable primaryFailure = null;
    try {
      driver = SafariSessionLauncher.launch(() -> new SafariDriver(new SafariOptions()), evidence);
      driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(15));
      recordBrowser(evidence, driver);
      String javascriptReleaseUrl = requiredProperty("mazeGame.safariReleaseUrl");
      runGameFlow(driver, javascriptReleaseUrl, null, "JavaScript", JAVASCRIPT_ASSETS, evidence);
      String wasmReleaseUrl = System.getProperty("mazeGame.safariWasmReleaseUrl");
      if (wasmReleaseUrl != null) {
        runGameFlow(
            driver,
            wasmReleaseUrl,
            javascriptReleaseUrl,
            "WebAssembly",
            WEBASSEMBLY_ASSETS,
            evidence);
      }
      captureScreenshot(driver, reportDirectory.resolve("result.png"));
      evidence.add("Result: PASS");
    } catch (Throwable failure) {
      primaryFailure = failure;
      evidence.add("Result: FAIL");
      evidence.add(failure.toString());
      if (driver != null) {
        captureScreenshot(driver, reportDirectory.resolve("failure.png"));
      }
    }
    RuntimeException cleanupFailure = quitDriver(driver, evidence);
    IOException evidenceFailure = writeEvidence(reportDirectory, evidence);
    if (primaryFailure != null) {
      addSuppressed(primaryFailure, cleanupFailure);
      addSuppressed(primaryFailure, evidenceFailure);
      rethrow(primaryFailure);
    }
    if (evidenceFailure != null) {
      addSuppressed(evidenceFailure, cleanupFailure);
      throw evidenceFailure;
    }
    if (cleanupFailure != null) {
      throw new SafariCleanupException(cleanupFailure);
    }
  }

  private static IOException writeEvidence(Path reportDirectory, List<String> evidence) {
    try {
      Files.createDirectories(reportDirectory);
      Files.write(reportDirectory.resolve("safari-release.txt"), evidence, StandardCharsets.UTF_8);
      return null;
    } catch (IOException failure) {
      return failure;
    }
  }

  private static void addSuppressed(Throwable primary, Throwable secondary) {
    if (secondary != null) {
      primary.addSuppressed(secondary);
    }
  }

  private static void rethrow(Throwable failure) throws IOException {
    if (failure instanceof IOException ioFailure) {
      throw ioFailure;
    }
    if (failure instanceof RuntimeException runtimeFailure) {
      throw new SafariReleaseValidationException(runtimeFailure);
    }
    if (failure instanceof Error error) {
      throw error;
    }
    throw new AssertionError("unexpected checked failure", failure);
  }

  private static final class SafariReleaseValidationException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private SafariReleaseValidationException(RuntimeException cause) {
      super("Safari release validation failed", cause);
    }
  }

  private static final class SafariCleanupException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private SafariCleanupException(RuntimeException cause) {
      super("SafariDriver cleanup failed after validation", cause);
    }
  }

  static RuntimeException quitDriver(WebDriver driver, List<String> evidence) {
    if (driver == null) {
      return null;
    }
    try {
      driver.quit();
      return null;
    } catch (RuntimeException failure) {
      evidence.add("SafariDriver cleanup: FAIL - " + failure);
      return failure;
    }
  }

  private static void runGameFlow(
      WebDriver driver,
      String releaseUrl,
      String fallbackReleaseUrl,
      String target,
      Set<String> requiredAssets,
      List<String> evidence)
      throws IOException {
    driver.manage().window().setSize(new org.openqa.selenium.Dimension(1280, 800));
    String separator = releaseUrl.contains("?") ? "&" : "?";
    driver.get(releaseUrl + separator + "safari-release=" + System.nanoTime());
    javascript(driver)
        .executeScript(
            "arguments[0].forEach(function(key) { window.localStorage.removeItem(key); });",
            List.of(
                MILESTONE_ONE_RESULT_KEY, MILESTONE_TWO_RESULT_KEY, MILESTONE_THREE_RESULT_KEY));
    driver.navigate().refresh();
    waitForRenderedControl(driver, 640, 280);
    assertPageStarted(driver);
    String activeReleaseUrl = activeReleaseUrl(driver, releaseUrl, fallbackReleaseUrl);
    Set<String> activeRequiredAssets =
        activeReleaseUrl.equals(releaseUrl) ? requiredAssets : JAVASCRIPT_ASSETS;
    installRuntimeErrorCapture(driver);
    SafariControls controls = new SafariControls(driver);

    BrowserGameScenario.startMilestoneOne(controls);
    assertAudioResumed(driver);
    waitForSavedResult(driver, MILESTONE_ONE_RESULT_KEY);
    String milestoneOneResult = readSavedResult(driver, MILESTONE_ONE_RESULT_KEY);
    assertEquals("10000:40", milestoneOneResult);

    BrowserGameScenario.startMilestoneTwo(controls);
    waitForSavedResult(driver, MILESTONE_TWO_RESULT_KEY);
    String milestoneTwoResult = readSavedResult(driver, MILESTONE_TWO_RESULT_KEY);
    assertEquals("15000:60", milestoneTwoResult);
    assertFalse(milestoneOneResult.equals(milestoneTwoResult));

    BrowserGameScenario.openMilestoneThree(controls);
    driver.navigate().refresh();
    waitForRenderedControl(driver, 640, 280);
    assertEquals(milestoneOneResult, readSavedResult(driver, MILESTONE_ONE_RESULT_KEY));
    assertEquals(milestoneTwoResult, readSavedResult(driver, MILESTONE_TWO_RESULT_KEY));
    assertEquals(
        null,
        javascript(driver)
            .executeScript(
                "return window.localStorage.getItem(arguments[0]);", MILESTONE_THREE_RESULT_KEY));
    installRuntimeErrorCapture(driver);
    BrowserGameScenario.startMilestoneThreeFromMainMenu(controls);
    waitForSavedResult(driver, MILESTONE_THREE_RESULT_KEY);
    String milestoneThreeResult = readSavedResult(driver, MILESTONE_THREE_RESULT_KEY);
    assertEquals("6500:26", milestoneThreeResult);
    assertFalse(milestoneTwoResult.equals(milestoneThreeResult));
    assertRequiredAssetsReachable(driver, activeRequiredAssets);
    assertRuntimeErrorsEmpty(driver);
    recordResponsiveLayouts(driver, target, evidence);

    driver.manage().window().setSize(new org.openqa.selenium.Dimension(1280, 800));
    driver.navigate().refresh();
    waitForRenderedControl(driver, 640, 280);
    assertEquals(milestoneOneResult, readSavedResult(driver, MILESTONE_ONE_RESULT_KEY));
    assertEquals(milestoneTwoResult, readSavedResult(driver, MILESTONE_TWO_RESULT_KEY));
    assertEquals(milestoneThreeResult, readSavedResult(driver, MILESTONE_THREE_RESULT_KEY));
    assertPageStarted(driver);
    assertReleaseLocation(driver, activeReleaseUrl);
    installRuntimeErrorCapture(driver);
    clickCanvas(driver, 640, 280);
    waitForRenderedControl(driver, 404, 280);
    assertAudioResumed(driver);
    assertRequiredAssetsReachable(driver, activeRequiredAssets);
    assertRuntimeErrorsEmpty(driver);

    evidence.add(target + " release URL: " + releaseUrl);
    if (!activeReleaseUrl.equals(releaseUrl)) {
      evidence.add(target + " fallback URL: " + activeReleaseUrl);
    }
    evidence.add(target + " Milestone 1 saved result: " + milestoneOneResult);
    evidence.add(target + " Milestone 2 saved result: " + milestoneTwoResult);
    evidence.add(target + " Milestone 3 Scout saved result: " + milestoneThreeResult);
    evidence.add(target + " required assets: HTTP 2xx with expected MIME types");
    evidence.add(target + " audio context after interaction: running");
    evidence.add(target + " three-level progression, refresh, interaction, and persistence: PASS");
    evidence.add(target + " runtime errors after initialization: none");
  }

  private static void recordBrowser(List<String> evidence, WebDriver driver) {
    Capabilities capabilities = ((SafariDriver) driver).getCapabilities();
    evidence.add("Browser: " + capabilities.getBrowserName());
    evidence.add("Browser version: " + capabilities.getBrowserVersion());
    evidence.add("Platform: " + capabilities.getPlatformName());
  }

  private static void recordResponsiveLayouts(
      WebDriver driver, String target, List<String> evidence) {
    recordResponsiveLayout(
        driver, target, "portrait", new org.openqa.selenium.Dimension(500, 900), evidence);
    recordResponsiveLayout(
        driver, target, "landscape", new org.openqa.selenium.Dimension(900, 500), evidence);
  }

  private static void recordResponsiveLayout(
      WebDriver driver,
      String target,
      String orientation,
      org.openqa.selenium.Dimension windowSize,
      List<String> evidence) {
    driver.manage().window().setSize(windowSize);
    String responsiveLayoutReady =
        "return !document.getElementById('viewport-guidance').offsetParent"
            + " && document.getElementById('canvas').width === window.innerWidth"
            + " && document.getElementById('canvas').height === window.innerHeight;";
    new WebDriverWait(driver, Duration.ofSeconds(10))
        .until(
            ignored ->
                Boolean.TRUE.equals(javascript(driver).executeScript(responsiveLayoutReady)));
    Object viewport =
        javascript(driver).executeScript("return window.innerWidth + 'x' + window.innerHeight;");
    evidence.add(target + " " + orientation + " responsive viewport: " + viewport + " PASS");
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

  private static String activeReleaseUrl(
      WebDriver driver, String releaseUrl, String fallbackReleaseUrl) {
    if (releaseLocationMatches(driver, releaseUrl)) {
      return releaseUrl;
    }
    if (fallbackReleaseUrl != null && releaseLocationMatches(driver, fallbackReleaseUrl)) {
      return fallbackReleaseUrl;
    }
    assertReleaseLocation(driver, releaseUrl);
    return releaseUrl;
  }

  private static boolean releaseLocationMatches(WebDriver driver, String releaseUrl) {
    URI expected = URI.create(releaseUrl);
    URI actual = URI.create(driver.getCurrentUrl());
    return expected.getScheme().equals(actual.getScheme())
        && expected.getAuthority().equals(actual.getAuthority())
        && expected.getPath().equals(actual.getPath());
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

  private static String readSavedResult(WebDriver driver, String resultKey) {
    Object value =
        javascript(driver)
            .executeScript("return window.localStorage.getItem(arguments[0]);", resultKey);
    assertNotNull(value);
    String savedResult = value.toString();
    assertTrue(savedResult.matches("[0-9]+:[0-9]+"));
    return savedResult;
  }

  private static void waitForSavedResult(WebDriver driver, String resultKey) {
    JavascriptExecutor javascript = javascript(driver);
    new WebDriverWait(driver, Duration.ofSeconds(25))
        .until(
            ignored ->
                javascript.executeScript(
                        "return window.localStorage.getItem(arguments[0]);", resultKey)
                    != null);
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
    long deadline = System.nanoTime() + Duration.ofSeconds(25).toNanos();
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

  private static final class SafariControls implements BrowserGameScenario.Controls {
    private final WebDriver driver;

    private SafariControls(WebDriver driver) {
      this.driver = driver;
    }

    @Override
    public void clickButton(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId) {
      ScreenPoint point =
          BrowserGameScenario.buttonCenter(
              REFERENCE_WIDTH, REFERENCE_HEIGHT, phase, level, hasNextLevel, elementId);
      clickCanvas(driver, point.x(), point.y());
    }

    @Override
    public void waitForButton(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId)
        throws IOException {
      ScreenPoint point =
          BrowserGameScenario.buttonCenter(
              REFERENCE_WIDTH, REFERENCE_HEIGHT, phase, level, hasNextLevel, elementId);
      waitForRenderedControl(driver, point.x(), point.y());
    }

    @Override
    public void placeAndClearWall(LevelDefinition level, GridPosition position) throws IOException {
      ScreenPoint point = cellCenter(level, position);
      int emptyColor = pixelAt(driver, point.x(), point.y());
      clickCanvas(driver, point.x(), point.y());
      waitForPixelChange(driver, point.x(), point.y(), emptyColor);
      int wallColor = pixelAt(driver, point.x(), point.y());
      clickCanvas(driver, point.x(), point.y());
      waitForPixelChange(driver, point.x(), point.y(), wallColor);
    }

    @Override
    public void placeWalls(LevelDefinition level, List<GridPosition> walls) {
      for (GridPosition wall : walls) {
        ScreenPoint point = cellCenter(level, wall);
        clickCanvas(driver, point.x(), point.y());
      }
    }

    private static ScreenPoint cellCenter(LevelDefinition level, GridPosition position) {
      return BrowserGameScenario.cellCenter(REFERENCE_WIDTH, REFERENCE_HEIGHT, level, position);
    }
  }

  private static String requiredProperty(String name) {
    String value = System.getProperty(name);
    assertNotNull(value, name + " must be configured");
    return value;
  }
}
