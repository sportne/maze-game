package io.github.sportne.mazegame.browser;

import static io.github.sportne.mazegame.browser.BrowserGameScenario.EDITED_CELL;
import static io.github.sportne.mazegame.browser.BrowserGameScenario.MILESTONE_ONE_RESULT_KEY;
import static io.github.sportne.mazegame.browser.BrowserGameScenario.MILESTONE_TWO_RESULT_KEY;
import static io.github.sportne.mazegame.browser.BrowserGameScenario.MILESTONE_TWO_WALLS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.sportne.mazegame.browser.BrowserGameScenario.ScreenPoint;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.state.GamePhase;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** End-to-end smoke coverage for the generated TeaVM release artifact. */
final class BrowserSmokeTest {
  private static final int VIEWPORT_WIDTH = 1280;
  private static final int VIEWPORT_HEIGHT = 720;
  private static final int PORTRAIT_WIDTH = 390;
  private static final int PORTRAIT_HEIGHT = 844;
  private static final int MOBILE_SAFARI_LANDSCAPE_WIDTH = 844;
  private static final int MOBILE_SAFARI_LANDSCAPE_HEIGHT = 286;
  private static final int STARTUP_SAMPLE_COUNT = 5;
  private static final String SITE_PATH = "/maze-game/";
  private static final Set<String> COMMON_ASSETS =
      Set.of("styles.css", "mouse-sprites.png", "exploreMaze_T1.mp3");

  @Test
  @Timeout(240)
  void completesTwoLevelFlowAndLoadsIndependentResultsAfterReload() throws IOException {
    Path webApplication = requiredDirectory("mazeGame.webAppDirectory");
    Path artifactDirectory = requiredDirectory("mazeGame.artifactDirectory");
    Path reportDirectory = Path.of(requiredProperty("mazeGame.browserSmokeReportDirectory"));
    BrowserLog browserLog = new BrowserLog(requiredAssets());
    Page page = null;

    try (StaticWebServer server =
            StaticWebServer.start(webApplication, requiredProperty("mazeGame.applicationPath"));
        Playwright playwright = Playwright.create();
        Browser browser = launchBrowser(playwright);
        BrowserContext context =
            browser.newContext(
                new Browser.NewContextOptions()
                    .setViewportSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
                    .setHasTouch(touchInput()))) {
      if (Boolean.parseBoolean(requiredProperty("mazeGame.disableWebAssemblyGc"))) {
        context.addInitScript(
            "const originalCompileStreaming = WebAssembly.compileStreaming.bind(WebAssembly);"
                + "WebAssembly.compileStreaming = (source, options) => {"
                + "if (options && options.builtins && options.builtins.includes('js-string')) {"
                + "return Promise.reject(new WebAssembly.CompileError('WasmGC disabled by test'));"
                + "} return originalCompileStreaming(source, options); };");
        assertWasmFallback(context, server.siteUri("wasm/"), server.uri());
      }
      page = context.newPage();
      browserLog.observe(page);
      try {
        runGameFlow(page, server.uri(), browserLog, artifactDirectory, reportDirectory, browser);
        runMobileTouchFlows(browser, server.uri(), reportDirectory);
      } catch (Throwable failure) {
        captureFailure(page, browserLog, reportDirectory, failure);
        throw failure;
      }
    } catch (Throwable failure) {
      if (page == null) {
        captureFailure(null, browserLog, reportDirectory, failure);
      }
      throw failure;
    }
  }

  private static Browser launchBrowser(Playwright playwright) {
    String cdpEndpoint = System.getProperty("mazeGame.browserCdpEndpoint");
    if (cdpEndpoint != null) {
      return playwright.chromium().connectOverCDP(cdpEndpoint);
    }
    BrowserType browserType =
        switch (requiredProperty("mazeGame.browserEngine")) {
          case "chromium" -> playwright.chromium();
          case "firefox" -> playwright.firefox();
          case "webkit" -> playwright.webkit();
          default -> throw new IllegalArgumentException("unsupported browser engine");
        };
    return browserType.launch(
        new BrowserType.LaunchOptions()
            .setHeadless(!Boolean.parseBoolean(requiredProperty("mazeGame.headedBrowser"))));
  }

  private static void assertWasmFallback(
      BrowserContext context, URI wasmApplicationUri, URI javascriptApplicationUri)
      throws IOException {
    try (Page fallbackPage = context.newPage()) {
      fallbackPage.navigate(wasmApplicationUri.toString());
      fallbackPage.waitForCondition(
          () -> fallbackPage.url().equals(javascriptApplicationUri.toString()));
      waitForRenderedControl(fallbackPage, 640, 280);
    }
  }

  private static void runGameFlow(
      Page page,
      URI applicationUri,
      BrowserLog browserLog,
      Path artifactDirectory,
      Path reportDirectory,
      Browser browser)
      throws IOException {
    List<Long> firstFrameMillis = new ArrayList<>();
    List<Double> responseEndMillis = new ArrayList<>();
    firstFrameMillis.add(navigateAndWaitForFirstFrame(page, applicationUri));
    responseEndMillis.add(navigationResponseEnd(page));
    assertCanvas(page);
    assertTrue(page.locator("#loading-state").isHidden());
    assertTrue(page.locator("#failure-state").isHidden());
    Object usedHeap =
        page.evaluate("() => performance.memory ? performance.memory.usedJSHeapSize : null");
    for (int sample = 1; sample < STARTUP_SAMPLE_COUNT; sample++) {
      firstFrameMillis.add(reloadAndWaitForFirstFrame(page));
      responseEndMillis.add(navigationResponseEnd(page));
    }
    BrowserMetrics.capture(
        page,
        artifactDirectory,
        reportDirectory,
        requiredProperty("mazeGame.browserTarget"),
        requiredProperty("mazeGame.browserEngine"),
        browser.version(),
        firstFrameMillis,
        responseEndMillis,
        usedHeap);
    assertResponsiveViewportSupport(page);
    BrowserControls controls =
        new BrowserControls(page, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, touchInput());

    controls.waitForButton(
        GamePhase.MAIN_MENU, Levels.milestoneOne(), false, MazeGameLayout.MAIN_MENU_START);
    BrowserGameScenario.startMilestoneOne(controls);
    assertAudioResumed(page);
    waitForSavedResult(page, MILESTONE_ONE_RESULT_KEY);

    controls.clickButtonAndWaitForChange(
        GamePhase.RESULT, Levels.milestoneOne(), true, MazeGameLayout.RESULT_REPLAY);
    controls.waitForButton(
        GamePhase.RESULT, Levels.milestoneOne(), true, MazeGameLayout.RESULT_RETRY);
    BrowserGameScenario.startMilestoneTwo(controls);
    waitForSavedResult(page, MILESTONE_TWO_RESULT_KEY);

    String milestoneOneResult = readSavedResult(page, MILESTONE_ONE_RESULT_KEY);
    String milestoneTwoResult = readSavedResult(page, MILESTONE_TWO_RESULT_KEY);
    assertFalse(milestoneOneResult.equals(milestoneTwoResult));
    controls.clickButtonAndWaitForChange(
        GamePhase.RESULT, Levels.milestoneTwo(), true, MazeGameLayout.RESULT_REPLAY);
    controls.waitForButton(
        GamePhase.RESULT, Levels.milestoneTwo(), true, MazeGameLayout.RESULT_RETRY);
    controls.clickButton(
        GamePhase.RESULT, Levels.milestoneTwo(), true, MazeGameLayout.RESULT_RETRY);
    controls.waitForButton(
        GamePhase.BUILDING, Levels.milestoneTwo(), false, MazeGameLayout.BUILD_START);

    page.reload();
    waitForRenderedControl(page, 640, 280);

    assertEquals(milestoneOneResult, readSavedResult(page, MILESTONE_ONE_RESULT_KEY));
    assertEquals(milestoneTwoResult, readSavedResult(page, MILESTONE_TWO_RESULT_KEY));
    controls.clickButton(
        GamePhase.MAIN_MENU, Levels.milestoneOne(), false, MazeGameLayout.MAIN_MENU_START);
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.milestoneOne(), false, MazeGameLayout.levelCardId(2));
    controls.waitForButton(
        GamePhase.BUILDING, Levels.milestoneTwo(), false, MazeGameLayout.BUILD_START);
    assertTrue(browserLog.observedAssets().containsAll(browserLog.requiredAssets()));
    if (browserLog.requiredAssets().contains("app.wasm")) {
      assertEquals("application/wasm", browserLog.contentType("app.wasm"));
    }
    assertTrue(
        browserLog.errors().isEmpty(),
        () -> String.join(System.lineSeparator(), browserLog.errors()));
  }

  private static long navigateAndWaitForFirstFrame(Page page, URI applicationUri)
      throws IOException {
    long started = System.nanoTime();
    page.navigate(applicationUri.toString());
    waitForRenderedControl(page, 640, 280);
    return Duration.ofNanos(System.nanoTime() - started).toMillis();
  }

  private static long reloadAndWaitForFirstFrame(Page page) throws IOException {
    long started = System.nanoTime();
    page.reload();
    waitForRenderedControl(page, 640, 280);
    return Duration.ofNanos(System.nanoTime() - started).toMillis();
  }

  private static double navigationResponseEnd(Page page) {
    return ((Number)
            page.evaluate("() => performance.getEntriesByType('navigation')[0].responseEnd"))
        .doubleValue();
  }

  private static boolean touchInput() {
    return Boolean.parseBoolean(requiredProperty("mazeGame.touchInput"));
  }

  private static void assertResponsiveViewportSupport(Page page) throws IOException {
    page.setViewportSize(PORTRAIT_WIDTH, PORTRAIT_HEIGHT);
    page.waitForCondition(() -> page.locator("#viewport-guidance").isHidden());
    waitForRenderedControl(page, 195, 342);
    page.setViewportSize(MOBILE_SAFARI_LANDSCAPE_WIDTH, MOBILE_SAFARI_LANDSCAPE_HEIGHT);
    page.waitForCondition(() -> page.locator("#viewport-guidance").isHidden());
    waitForRenderedControl(page, 326, 151);
    page.setViewportSize(200, 400);
    page.waitForCondition(() -> page.locator("#viewport-guidance").isVisible());
    page.setViewportSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    page.waitForCondition(() -> page.locator("#viewport-guidance").isHidden());
    waitForRenderedControl(page, 640, 280);
  }

  private static void runMobileTouchFlows(Browser browser, URI applicationUri, Path reportDirectory)
      throws IOException {
    MobileViewport portrait = new MobileViewport(PORTRAIT_WIDTH, PORTRAIT_HEIGHT);
    MobileViewport landscape =
        new MobileViewport(MOBILE_SAFARI_LANDSCAPE_WIDTH, MOBILE_SAFARI_LANDSCAPE_HEIGHT);
    runMobileTouchFlow(
        browser,
        applicationUri,
        reportDirectory.resolve("mobile-portrait.png"),
        portrait,
        landscape);
    runMobileTouchFlow(
        browser,
        applicationUri,
        reportDirectory.resolve("mobile-landscape.png"),
        landscape,
        portrait);
  }

  private static void runMobileTouchFlow(
      Browser browser,
      URI applicationUri,
      Path screenshotPath,
      MobileViewport primary,
      MobileViewport rotated)
      throws IOException {
    try (BrowserContext context =
            browser.newContext(
                new Browser.NewContextOptions()
                    .setViewportSize(primary.width(), primary.height())
                    .setDeviceScaleFactor(3.0)
                    .setHasTouch(true));
        Page page = context.newPage()) {
      BrowserLog mobileLog = BrowserLog.forAuxiliaryTouchContext();
      mobileLog.observe(page);
      try {
        page.navigate(applicationUri.toString());
        assertLogicalCanvasSize(page, primary);
        BrowserControls primaryControls =
            new BrowserControls(page, primary.width(), primary.height(), true);
        BrowserControls rotatedControls =
            new BrowserControls(page, rotated.width(), rotated.height(), true);
        primaryControls.waitForButton(
            GamePhase.MAIN_MENU, Levels.milestoneOne(), false, MazeGameLayout.MAIN_MENU_START);

        resizeAndAssert(page, rotated);
        resizeAndAssert(page, primary);
        primaryControls.clickButton(
            GamePhase.MAIN_MENU, Levels.milestoneOne(), false, MazeGameLayout.MAIN_MENU_START);
        primaryControls.waitForButton(
            GamePhase.LEVEL_SELECT, Levels.milestoneOne(), false, MazeGameLayout.levelCardId(1));
        primaryControls.clickButton(
            GamePhase.LEVEL_SELECT, Levels.milestoneOne(), false, MazeGameLayout.levelCardId(2));
        primaryControls.clickButton(
            GamePhase.LEVEL_SELECT, Levels.milestoneOne(), false, MazeGameLayout.levelCardId(1));
        primaryControls.waitForButton(
            GamePhase.BUILDING, Levels.milestoneOne(), false, MazeGameLayout.BUILD_START);

        primaryControls.clickCell(Levels.milestoneOne(), EDITED_CELL);
        assertWallCell(page, primaryControls.cellCenter(Levels.milestoneOne(), EDITED_CELL));
        resizeAndAssert(page, rotated);
        rotatedControls.waitForButton(
            GamePhase.BUILDING, Levels.milestoneOne(), false, MazeGameLayout.BUILD_START);
        assertWallCell(page, rotatedControls.cellCenter(Levels.milestoneOne(), EDITED_CELL));
        rotatedControls.clickCell(Levels.milestoneOne(), EDITED_CELL);
        assertOpenCell(page, rotatedControls.cellCenter(Levels.milestoneOne(), EDITED_CELL));
        resizeAndAssert(page, primary);
        primaryControls.waitForButton(
            GamePhase.BUILDING, Levels.milestoneOne(), false, MazeGameLayout.BUILD_START);
        assertOpenCell(page, primaryControls.cellCenter(Levels.milestoneOne(), EDITED_CELL));

        primaryControls.clickButton(
            GamePhase.BUILDING, Levels.milestoneOne(), false, MazeGameLayout.BUILD_START);
        resizeAndAssert(page, rotated);
        waitForSavedResult(page, MILESTONE_ONE_RESULT_KEY);
        rotatedControls.waitForButton(
            GamePhase.RESULT, Levels.milestoneOne(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
        rotatedControls.clickButton(
            GamePhase.RESULT, Levels.milestoneOne(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
        rotatedControls.waitForButton(
            GamePhase.BUILDING, Levels.milestoneTwo(), false, MazeGameLayout.BUILD_START);
        rotatedControls.placeWalls(Levels.milestoneTwo(), MILESTONE_TWO_WALLS);
        resizeAndAssert(page, primary);
        primaryControls.waitForButton(
            GamePhase.BUILDING, Levels.milestoneTwo(), false, MazeGameLayout.BUILD_START);
        primaryControls.clickButton(
            GamePhase.BUILDING, Levels.milestoneTwo(), false, MazeGameLayout.BUILD_START);
        waitForSavedResult(page, MILESTONE_TWO_RESULT_KEY);
        primaryControls.waitForButton(
            GamePhase.RESULT, Levels.milestoneTwo(), true, MazeGameLayout.RESULT_REPLAY);
        Files.createDirectories(Objects.requireNonNull(screenshotPath.getParent()));
        page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath));

        primaryControls.clickButtonAndWaitForChange(
            GamePhase.RESULT, Levels.milestoneTwo(), true, MazeGameLayout.RESULT_REPLAY);
        primaryControls.waitForButton(
            GamePhase.RESULT, Levels.milestoneTwo(), true, MazeGameLayout.RESULT_RETRY);
        primaryControls.clickButton(
            GamePhase.RESULT, Levels.milestoneTwo(), true, MazeGameLayout.RESULT_RETRY);
        primaryControls.waitForButton(
            GamePhase.BUILDING, Levels.milestoneTwo(), false, MazeGameLayout.BUILD_START);
        assertTrue(
            mobileLog.errors().isEmpty(),
            () -> String.join(System.lineSeparator(), mobileLog.errors()));
      } catch (RuntimeException | IOException | Error failure) {
        captureFailure(page, mobileLog, mobileFailureDirectory(screenshotPath), failure);
        throw failure;
      }
    }
  }

  private static Path mobileFailureDirectory(Path screenshotPath) {
    String fileName = Objects.requireNonNull(screenshotPath.getFileName()).toString();
    String reportName =
        fileName.endsWith(".png") ? fileName.substring(0, fileName.length() - 4) : fileName;
    return screenshotPath.resolveSibling(reportName + "-failure");
  }

  private static void resizeAndAssert(Page page, MobileViewport viewport) {
    page.setViewportSize(viewport.width(), viewport.height());
    assertLogicalCanvasSize(page, viewport);
    assertTrue(page.locator("#viewport-guidance").isHidden());
  }

  private static void assertLogicalCanvasSize(Page page, MobileViewport viewport) {
    page.waitForCondition(
        () ->
            ((Number) page.locator("canvas").evaluate("canvas => canvas.width")).intValue()
                    == viewport.width()
                && ((Number) page.locator("canvas").evaluate("canvas => canvas.height")).intValue()
                    == viewport.height());
    assertEquals(viewport.width(), page.locator("canvas").boundingBox().width);
    assertEquals(viewport.height(), page.locator("canvas").boundingBox().height);
  }

  private static void assertWallCell(Page page, ScreenPoint point) throws IOException {
    int color = pixelColor(page, point.x(), point.y());
    assertTrue(red(color) > 220 && green(color) > 220 && blue(color) > 220);
  }

  private static void assertOpenCell(Page page, ScreenPoint point) throws IOException {
    int color = pixelColor(page, point.x(), point.y());
    assertTrue(red(color) < 80 && green(color) < 80 && blue(color) < 80);
  }

  private static int red(int color) {
    return color >> 16 & 0xFF;
  }

  private static int green(int color) {
    return color >> 8 & 0xFF;
  }

  private static int blue(int color) {
    return color & 0xFF;
  }

  private static void assertAudioResumed(Page page) {
    Object audioState =
        page.evaluate(
            "window.Howler && window.Howler.ctx" + " ? window.Howler.ctx.state : 'unavailable'");
    assertEquals("running", audioState);
  }

  private static Set<String> requiredAssets() {
    Set<String> assets = new HashSet<>(COMMON_ASSETS);
    StringTokenizer tokens =
        new StringTokenizer(requiredProperty("mazeGame.browserProgramAssets"), ",");
    while (tokens.hasMoreTokens()) {
      assets.add(tokens.nextToken());
    }
    return Set.copyOf(assets);
  }

  private static void assertCanvas(Page page) {
    assertEquals(1, page.locator("canvas").count());
    assertTrue(page.locator("canvas").isVisible());
    assertEquals(VIEWPORT_WIDTH, page.locator("canvas").evaluate("canvas => canvas.width"));
    assertEquals(VIEWPORT_HEIGHT, page.locator("canvas").evaluate("canvas => canvas.height"));
  }

  private static String readSavedResult(Page page, String resultKey) {
    String value = (String) page.evaluate("key => window.localStorage.getItem(key)", resultKey);
    assertNotNull(value);
    assertTrue(value.matches("[0-9]+:[0-9]+"));
    return value;
  }

  private static void waitForSavedResult(Page page, String resultKey) {
    page.waitForCondition(
        () -> page.evaluate("key => window.localStorage.getItem(key)", resultKey) != null,
        new Page.WaitForConditionOptions().setTimeout(20_000.0));
  }

  private static void waitForRenderedControl(Page page, int x, int y) throws IOException {
    int backgroundColor = pixelColor(page, 0, 0);
    waitForPixelChange(page, x, y, backgroundColor);
  }

  private static void waitForPixelChange(Page page, int x, int y, int originalColor)
      throws IOException {
    long deadline = System.nanoTime() + Duration.ofSeconds(25).toNanos();
    while (pixelColor(page, x, y) == originalColor && System.nanoTime() < deadline) {
      page.waitForTimeout(100.0);
    }
    assertFalse(pixelColor(page, x, y) == originalColor, "expected rendered pixel change");
  }

  private static int pixelColor(Page page, int cssX, int cssY) throws IOException {
    double devicePixelRatio = ((Number) page.evaluate("window.devicePixelRatio")).doubleValue();
    return screenshot(page)
        .getRGB(
            (int) Math.round(cssX * devicePixelRatio), (int) Math.round(cssY * devicePixelRatio));
  }

  private static BufferedImage screenshot(Page page) throws IOException {
    BufferedImage image =
        ImageIO.read(new ByteArrayInputStream(page.screenshot(new Page.ScreenshotOptions())));
    assertNotNull(image);
    return image;
  }

  private static void captureFailure(
      Page page, BrowserLog browserLog, Path reportDirectory, Throwable failure) {
    try {
      Files.createDirectories(reportDirectory);
      List<String> evidence = new ArrayList<>(browserLog.errors());
      evidence.add(failure.toString());
      Files.write(reportDirectory.resolve("browser.log"), evidence, StandardCharsets.UTF_8);
      if (page != null && !page.isClosed()) {
        page.screenshot(
            new Page.ScreenshotOptions()
                .setPath(reportDirectory.resolve("failure.png"))
                .setFullPage(true));
      }
    } catch (RuntimeException | IOException captureFailure) {
      failure.addSuppressed(captureFailure);
    }
  }

  private static Path requiredDirectory(String propertyName) {
    Path directory = Path.of(requiredProperty(propertyName));
    assertTrue(Files.isDirectory(directory), () -> "missing directory: " + directory);
    return directory;
  }

  private static String requiredProperty(String propertyName) {
    String property = System.getProperty(propertyName);
    assertNotNull(property, propertyName + " must be configured");
    return property;
  }

  private static final class BrowserControls implements BrowserGameScenario.Controls {
    private final Page page;
    private final int width;
    private final int height;
    private final boolean touch;

    private BrowserControls(Page page, int width, int height, boolean touch) {
      this.page = page;
      this.width = width;
      this.height = height;
      this.touch = touch;
    }

    @Override
    public void clickButton(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId) {
      click(BrowserGameScenario.buttonCenter(width, height, phase, level, hasNextLevel, elementId));
    }

    private void clickButtonAndWaitForChange(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId)
        throws IOException {
      ScreenPoint point =
          BrowserGameScenario.buttonCenter(width, height, phase, level, hasNextLevel, elementId);
      int originalColor = pixelColor(page, point.x(), point.y());
      click(point);
      waitForPixelChange(page, point.x(), point.y(), originalColor);
    }

    @Override
    public void waitForButton(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId)
        throws IOException {
      ScreenPoint point =
          BrowserGameScenario.buttonCenter(width, height, phase, level, hasNextLevel, elementId);
      waitForRenderedControl(page, point.x(), point.y());
    }

    @Override
    public void placeAndClearWall(LevelDefinition level, GridPosition position) throws IOException {
      ScreenPoint point = cellCenter(level, position);
      int emptyColor = pixelColor(page, point.x(), point.y());
      click(point);
      waitForPixelChange(page, point.x(), point.y(), emptyColor);
      int wallColor = pixelColor(page, point.x(), point.y());
      click(point);
      waitForPixelChange(page, point.x(), point.y(), wallColor);
    }

    @Override
    public void placeWalls(LevelDefinition level, List<GridPosition> walls) throws IOException {
      for (GridPosition wall : walls) {
        click(cellCenter(level, wall));
      }
      assertWallCell(page, cellCenter(level, walls.get(walls.size() - 1)));
    }

    private void clickCell(LevelDefinition level, GridPosition position) {
      click(cellCenter(level, position));
    }

    private ScreenPoint cellCenter(LevelDefinition level, GridPosition position) {
      return BrowserGameScenario.cellCenter(width, height, level, position);
    }

    private void click(ScreenPoint point) {
      if (touch) {
        page.touchscreen().tap(point.x(), point.y());
      } else {
        page.mouse().click(point.x(), point.y());
      }
    }
  }

  private static final class BrowserLog {
    private static final String AUDIO_DEVICE_ERROR =
        "The AudioContext encountered an error from the audio device or the WebAudio renderer.";
    private final List<String> errors = new ArrayList<>();
    private final Set<String> observedAssets = new HashSet<>();
    private final Map<String, String> contentTypes = new HashMap<>();
    private final Set<String> requiredAssets;
    private final boolean ignoreAudioDeviceErrors;

    private BrowserLog(Set<String> requiredAssets) {
      this(requiredAssets, false);
    }

    private BrowserLog(Set<String> requiredAssets, boolean ignoreAudioDeviceErrors) {
      this.requiredAssets = requiredAssets;
      this.ignoreAudioDeviceErrors = ignoreAudioDeviceErrors;
    }

    private static BrowserLog forAuxiliaryTouchContext() {
      return new BrowserLog(Set.of(), true);
    }

    private void observe(Page page) {
      page.onPageError(message -> errors.add("page: " + message));
      page.onConsoleMessage(this::recordConsoleMessage);
      page.onRequestFailed(request -> errors.add("request: " + request.url()));
      page.onResponse(this::recordResponse);
    }

    private void recordConsoleMessage(ConsoleMessage message) {
      if ("error".equals(message.type())
          && !(ignoreAudioDeviceErrors && AUDIO_DEVICE_ERROR.equals(message.text()))) {
        errors.add("console: " + message.text());
      }
    }

    private void recordResponse(Response response) {
      if (response.status() >= 400) {
        errors.add("response " + response.status() + ": " + response.url());
      }
      for (String asset : requiredAssets) {
        if (response.ok() && response.url().endsWith(asset)) {
          observedAssets.add(asset);
          contentTypes.put(asset, response.headers().get("content-type"));
        }
      }
    }

    private List<String> errors() {
      return List.copyOf(errors);
    }

    private Set<String> observedAssets() {
      return Set.copyOf(observedAssets);
    }

    private Set<String> requiredAssets() {
      return requiredAssets;
    }

    private String contentType(String asset) {
      return contentTypes.get(asset);
    }
  }

  private record MobileViewport(int width, int height) {}

  private static final class StaticWebServer implements AutoCloseable {
    private final String applicationPath;
    private final Path root;
    private final HttpServer server;

    private StaticWebServer(String applicationPath, Path root, HttpServer server) {
      this.applicationPath = applicationPath;
      this.root = root;
      this.server = server;
    }

    private static StaticWebServer start(Path root, String applicationPath) throws IOException {
      if (!applicationPath.startsWith(SITE_PATH) || !applicationPath.endsWith("/")) {
        throw new IllegalArgumentException("application path must be below " + SITE_PATH);
      }
      HttpServer server =
          HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
      StaticWebServer staticWebServer =
          new StaticWebServer(applicationPath, root.toRealPath(), server);
      server.createContext("/", staticWebServer::serve);
      server.start();
      return staticWebServer;
    }

    private URI uri() {
      return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + applicationPath);
    }

    private URI siteUri(String relativePath) {
      return URI.create(
          "http://127.0.0.1:" + server.getAddress().getPort() + SITE_PATH + relativePath);
    }

    private void serve(HttpExchange exchange) throws IOException {
      String requestPath = exchange.getRequestURI().getPath();
      if (!requestPath.startsWith(SITE_PATH)) {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
        return;
      }
      String relativePath = requestPath.substring(SITE_PATH.length());
      if (relativePath.isEmpty() || relativePath.endsWith("/")) {
        relativePath += "index.html";
      }
      Path requestedFile = root.resolve(relativePath).normalize();
      if (!requestedFile.startsWith(root) || !Files.isRegularFile(requestedFile)) {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
        return;
      }
      exchange.getResponseHeaders().set("Content-Type", contentType(requestedFile));
      exchange.getResponseHeaders().set("Cache-Control", "no-store");
      exchange.sendResponseHeaders(200, Files.size(requestedFile));
      try (exchange;
          var responseBody = exchange.getResponseBody()) {
        Files.copy(requestedFile, responseBody);
      }
    }

    private static String contentType(Path file) {
      String name = file.toString();
      if (name.endsWith(".html")) {
        return "text/html; charset=utf-8";
      }
      if (name.endsWith(".js")) {
        return "text/javascript; charset=utf-8";
      }
      if (name.endsWith(".wasm")) {
        return "application/wasm";
      }
      if (name.endsWith(".css")) {
        return "text/css; charset=utf-8";
      }
      if (name.endsWith(".svg")) {
        return "image/svg+xml";
      }
      if (name.endsWith(".png")) {
        return "image/png";
      }
      if (name.endsWith(".mp3")) {
        return "audio/mpeg";
      }
      return "application/octet-stream";
    }

    @Override
    public void close() {
      server.stop(0);
    }
  }
}
