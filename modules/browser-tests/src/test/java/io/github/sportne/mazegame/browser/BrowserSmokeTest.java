package io.github.sportne.mazegame.browser;

import static io.github.sportne.mazegame.browser.BrowserGameScenario.EDITED_CELL;
import static io.github.sportne.mazegame.browser.BrowserGameScenario.MILESTONE_ONE_RESULT_KEY;
import static io.github.sportne.mazegame.browser.BrowserGameScenario.MILESTONE_THREE_RESULT_KEY;
import static io.github.sportne.mazegame.browser.BrowserGameScenario.MILESTONE_THREE_WALLS;
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
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.sportne.mazegame.browser.BrowserGameScenario.ScreenPoint;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.FixedCell;
import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
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
import java.util.OptionalLong;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.IntConsumer;
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
  private static final GridPosition MOVED_CELL = new GridPosition(2, 3);
  private static final int STARTUP_SAMPLE_COUNT = 5;
  private static final long LEVEL_TWO_STATUS_SIGNATURE = -314_938_194_220_522_274L;
  private static final long LEVEL_THREE_STATUS_SIGNATURE = -2_608_151_536_843_095_784L;
  private static final String SITE_PATH = "/maze-game/";
  private static final String LEVEL_SIX_RESULT_KEY = "maze-game.best-result.level-6";
  private static final String LEVEL_SEVEN_RESULT_KEY = "maze-game.best-result.level-7";
  private static final String LEVEL_EIGHT_RESULT_KEY = "maze-game.best-result.level-8";
  private static final String LEVEL_NINE_RESULT_KEY = "maze-game.best-result.level-9";
  private static final String LEVEL_TEN_RESULT_KEY = "maze-game.best-result.level-10";
  private static final Set<String> COMMON_ASSETS =
      Set.of(
          "styles.css",
          "classic-mouse.png",
          "basic-characters.png",
          "goals.png",
          "exploreMaze_T1.mp3");

  @Test
  @Timeout(360)
  void completesThreeLevelFlowAndLoadsIndependentResultsAfterReload() throws IOException {
    Path webApplication = requiredDirectory("mazeGame.webAppDirectory");
    Path artifactDirectory = requiredDirectory("mazeGame.artifactDirectory");
    Path reportDirectory = Path.of(requiredProperty("mazeGame.browserSmokeReportDirectory"));
    // Headless Chromium can run the asserted AudioContext without a physical output renderer.
    BrowserLog browserLog =
        new BrowserLog(
            requiredAssets(), !Boolean.parseBoolean(requiredProperty("mazeGame.headedBrowser")));
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
        runBuildGestureFixtureFlows(browser, server.uri(), reportDirectory);
        runLevelSixReleaseFlow(browser, server.uri(), reportDirectory);
        runLevelsSevenToTenReleaseFlow(browser, server.uri(), reportDirectory);
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
        GamePhase.MAIN_MENU, Levels.levelOne(), false, MazeGameLayout.MAIN_MENU_START);
    BrowserGameScenario.startMilestoneOne(controls);
    assertAudioResumed(page);
    waitForSavedResult(page, MILESTONE_ONE_RESULT_KEY);
    assertEquals("9000:36", readSavedResult(page, MILESTONE_ONE_RESULT_KEY));

    controls.clickButtonAndWaitForChange(
        GamePhase.RESULT, Levels.levelOne(), true, MazeGameLayout.RESULT_REPLAY);
    controls.waitForButton(GamePhase.RESULT, Levels.levelOne(), true, MazeGameLayout.RESULT_RETRY);
    BrowserGameScenario.startMilestoneTwo(controls);
    waitForSavedResult(page, MILESTONE_TWO_RESULT_KEY);
    assertEquals("4500:18", readSavedResult(page, MILESTONE_TWO_RESULT_KEY));

    String milestoneOneResult = readSavedResult(page, MILESTONE_ONE_RESULT_KEY);
    String milestoneTwoResult = readSavedResult(page, MILESTONE_TWO_RESULT_KEY);
    assertFalse(milestoneOneResult.equals(milestoneTwoResult));
    Files.createDirectories(reportDirectory);
    page.screenshot(
        new Page.ScreenshotOptions().setPath(reportDirectory.resolve("desktop-solver-result.png")));
    assertRenderedSolverPresentation(
        page,
        Levels.levelTwo(),
        Levels.levelTwo().primarySolver().goal(),
        true,
        LEVEL_TWO_STATUS_SIGNATURE,
        true);
    BrowserGameScenario.openMilestoneThree(controls);
    page.reload();
    waitForRenderedControl(page, 640, 280);
    assertEquals(milestoneOneResult, readSavedResult(page, MILESTONE_ONE_RESULT_KEY));
    assertEquals(milestoneTwoResult, readSavedResult(page, MILESTONE_TWO_RESULT_KEY));
    assertEquals(
        null, page.evaluate("key => window.localStorage.getItem(key)", MILESTONE_THREE_RESULT_KEY));
    BrowserGameScenario.startMilestoneThreeFromMainMenu(controls);
    waitForSavedResult(page, MILESTONE_THREE_RESULT_KEY);
    assertEquals("6000:24", readSavedResult(page, MILESTONE_THREE_RESULT_KEY));

    String milestoneThreeResult = readSavedResult(page, MILESTONE_THREE_RESULT_KEY);
    assertFalse(milestoneTwoResult.equals(milestoneThreeResult));
    page.screenshot(
        new Page.ScreenshotOptions().setPath(reportDirectory.resolve("desktop-scout-result.png")));
    assertRenderedSolverPresentation(
        page,
        Levels.levelThree(),
        Levels.levelThree().primarySolver().goal(),
        false,
        LEVEL_THREE_STATUS_SIGNATURE,
        false);
    controls.clickButtonAndWaitForChange(
        GamePhase.RESULT, Levels.levelThree(), true, MazeGameLayout.RESULT_REPLAY);
    controls.waitForButton(
        GamePhase.RESULT, Levels.levelThree(), true, MazeGameLayout.RESULT_RETRY);
    controls.clickButton(GamePhase.RESULT, Levels.levelThree(), true, MazeGameLayout.RESULT_RETRY);
    controls.waitForButton(
        GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
    page.waitForTimeout(100.0);
    page.screenshot(
        new Page.ScreenshotOptions().setPath(reportDirectory.resolve("desktop-palette.png")));
    assertPaletteSupplyBadges(page, Levels.levelThree());
    if (!touchInput()) {
      BufferedImage paletteWithoutTooltip = screenshot(page);
      controls.hoverButton(
          GamePhase.BUILDING,
          Levels.levelThree(),
          false,
          MazeGameLayout.paletteItemId(PlaceableCellType.WALL));
      BufferedImage paletteWithTooltip = screenshot(page);
      ScreenRectangle tooltip =
          paletteTooltipBounds(
              MazeGameLayout.forPhase(
                      GamePhase.BUILDING,
                      VIEWPORT_WIDTH,
                      VIEWPORT_HEIGHT,
                      Levels.levelThree().gridSize(),
                      false,
                      Levels.catalog().levels().size(),
                      false,
                      Levels.levelThree().initiallyAvailableCellTypes())
                  .bounds(MazeGameLayout.paletteItemId(PlaceableCellType.WALL)));
      assertFalse(
          lightPixelSignature(paletteWithoutTooltip, tooltip)
              == lightPixelSignature(paletteWithTooltip, tooltip),
          "expected delayed palette tooltip after desktop hover");
      page.screenshot(
          new Page.ScreenshotOptions()
              .setPath(reportDirectory.resolve("desktop-palette-tooltip.png")));
    }
    controls.dragPaletteToCell(
        GamePhase.BUILDING, Levels.levelThree(), false, PlaceableCellType.WALL, EDITED_CELL);
    assertWallCell(page, controls.cellCenter(Levels.levelThree(), EDITED_CELL));
    GridPosition firstWall = MILESTONE_THREE_WALLS.get(0);
    controls.beginPlacedCellDrag(Levels.levelThree(), EDITED_CELL, firstWall);
    page.screenshot(
        new Page.ScreenshotOptions().setPath(reportDirectory.resolve("desktop-cell-drag.png")));
    controls.finishPlacedCellDrag(Levels.levelThree(), firstWall);
    assertOpenCell(page, controls.cellCenter(Levels.levelThree(), EDITED_CELL));
    assertWallCell(page, controls.cellCenter(Levels.levelThree(), firstWall));
    controls.placeWalls(
        Levels.levelThree(), MILESTONE_THREE_WALLS.subList(1, MILESTONE_THREE_WALLS.size()));
    controls.clickButton(
        GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
    controls.waitForButton(
        GamePhase.RESULT, Levels.levelThree(), true, MazeGameLayout.RESULT_RETRY);

    page.reload();
    waitForRenderedControl(page, 640, 280);

    assertEquals(milestoneOneResult, readSavedResult(page, MILESTONE_ONE_RESULT_KEY));
    assertEquals(milestoneTwoResult, readSavedResult(page, MILESTONE_TWO_RESULT_KEY));
    assertEquals(milestoneThreeResult, readSavedResult(page, MILESTONE_THREE_RESULT_KEY));
    controls.clickButton(
        GamePhase.MAIN_MENU, Levels.levelOne(), false, MazeGameLayout.MAIN_MENU_START);
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(3));
    controls.waitForButton(
        GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
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

  private static void runBuildGestureFixtureFlows(
      Browser browser, URI applicationUri, Path reportDirectory) throws IOException {
    runBuildGestureFixtureFlow(
        browser,
        applicationUri,
        reportDirectory.resolve("desktop-build-gesture-fixture.png"),
        new MobileViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT),
        false);
    runBuildGestureFixtureFlow(
        browser,
        applicationUri,
        reportDirectory.resolve("touch-build-gesture-fixture.png"),
        new MobileViewport(PORTRAIT_WIDTH, PORTRAIT_HEIGHT),
        true);
  }

  private static void runLevelSixReleaseFlow(
      Browser browser, URI applicationUri, Path reportDirectory) throws IOException {
    try (BrowserContext context =
        browser.newContext(
            new Browser.NewContextOptions().setViewportSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT))) {
      // Legacy outcomes deliberately prove that the redesign preserves stable progression keys.
      context.addInitScript(
          "window.localStorage.setItem('maze-game.best-result.milestone-1', '10000:40');"
              + "window.localStorage.setItem('maze-game.best-result.milestone-2', '9500:38');"
              + "window.localStorage.setItem('maze-game.best-result.milestone-3', '6500:26');"
              + "window.localStorage.setItem('maze-game.best-result.milestone-4', '5750:20');"
              + "window.localStorage.setItem('maze-game.best-result.milestone-5', '9000:69');");
      try (Page page = context.newPage()) {
        BrowserLog levelLog = BrowserLog.forAuxiliaryTouchContext();
        levelLog.observe(page);
        page.navigate(applicationUri.toString());
        waitForRenderedControl(page, 640, 280);
        BrowserControls controls =
            new BrowserControls(page, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, false);
        controls.clickButton(
            GamePhase.MAIN_MENU, Levels.levelOne(), false, MazeGameLayout.MAIN_MENU_START);
        controls.waitForButton(
            GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(6));
        controls.clickButton(
            GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(6));
        controls.waitForButton(
            GamePhase.BUILDING, Levels.levelSix(), false, MazeGameLayout.BUILD_START);

        assertWallCell(page, controls.cellCenter(Levels.levelSix(), new GridPosition(0, 1)));
        assertWallCell(page, controls.cellCenter(Levels.levelSix(), new GridPosition(0, 3)));
        controls.placeWalls(Levels.levelSix(), List.of(BrowserGameScenario.LEVEL_SIX_WALL));
        controls.clickButton(
            GamePhase.BUILDING,
            Levels.levelSix(),
            false,
            MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR));
        for (GridPosition slowFloor : BrowserGameScenario.LEVEL_SIX_SLOW_FLOORS) {
          controls.clickCell(Levels.levelSix(), slowFloor);
        }
        page.screenshot(
            new Page.ScreenshotOptions().setPath(reportDirectory.resolve("desktop-level-6.png")));
        controls.clickButton(
            GamePhase.BUILDING, Levels.levelSix(), false, MazeGameLayout.BUILD_START);
        controls.waitForButton(
            GamePhase.RESULT, Levels.levelSix(), true, MazeGameLayout.RESULT_RETRY);

        waitForSavedResult(page, LEVEL_SIX_RESULT_KEY);
        assertEquals("7000:22", readSavedResult(page, LEVEL_SIX_RESULT_KEY));
        assertTrue(
            levelLog.errors().isEmpty(),
            () -> String.join(System.lineSeparator(), levelLog.errors()));
      }
    }
  }

  private static void runLevelsSevenToTenReleaseFlow(
      Browser browser, URI applicationUri, Path reportDirectory) throws IOException {
    try (BrowserContext context =
        browser.newContext(
            new Browser.NewContextOptions().setViewportSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT))) {
      // Legacy outcomes deliberately prove that the redesign preserves stable progression keys.
      context.addInitScript(
          "window.localStorage.setItem('maze-game.best-result.milestone-1', '10000:40');"
              + "window.localStorage.setItem('maze-game.best-result.milestone-2', '9500:38');"
              + "window.localStorage.setItem('maze-game.best-result.milestone-3', '6500:26');"
              + "window.localStorage.setItem('maze-game.best-result.milestone-4', '5750:20');"
              + "window.localStorage.setItem('maze-game.best-result.milestone-5', '9000:69');"
              + "window.localStorage.setItem('maze-game.best-result.level-6', '6500:20');");
      try (Page page = context.newPage()) {
        BrowserLog levelLog = BrowserLog.forAuxiliaryTouchContext();
        levelLog.observe(page);
        page.navigate(applicationUri.toString());
        waitForRenderedControl(page, 640, 280);
        BrowserControls controls =
            new BrowserControls(page, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, false);
        controls.clickButton(
            GamePhase.MAIN_MENU, Levels.levelOne(), false, MazeGameLayout.MAIN_MENU_START);
        controls.waitForButton(
            GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(7));
        controls.clickButton(
            GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(7));
        controls.waitForButton(
            GamePhase.BUILDING, Levels.levelSeven(), false, MazeGameLayout.BUILD_START);

        placePassingCells(
            controls,
            Levels.levelSeven(),
            BrowserGameScenario.LEVEL_SEVEN_WALL,
            BrowserGameScenario.LEVEL_SEVEN_SLOW_FLOORS);
        page.screenshot(
            new Page.ScreenshotOptions().setPath(reportDirectory.resolve("desktop-level-7.png")));
        controls.clickButton(
            GamePhase.BUILDING, Levels.levelSeven(), false, MazeGameLayout.BUILD_START);
        controls.waitForButton(
            GamePhase.RESULT, Levels.levelSeven(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
        waitForSavedResult(page, LEVEL_SEVEN_RESULT_KEY);
        assertEquals("8750:26", readSavedResult(page, LEVEL_SEVEN_RESULT_KEY));
        controls.clickButton(
            GamePhase.RESULT, Levels.levelSeven(), true, MazeGameLayout.RESULT_NEXT_LEVEL);

        controls.waitForButton(
            GamePhase.BUILDING, Levels.levelEight(), false, MazeGameLayout.BUILD_START);
        placePassingCells(
            controls,
            Levels.levelEight(),
            BrowserGameScenario.LEVEL_EIGHT_WALL,
            BrowserGameScenario.LEVEL_EIGHT_SLOW_FLOORS);
        page.screenshot(
            new Page.ScreenshotOptions().setPath(reportDirectory.resolve("desktop-level-8.png")));
        controls.clickButton(
            GamePhase.BUILDING, Levels.levelEight(), false, MazeGameLayout.BUILD_START);
        controls.waitForButton(
            GamePhase.RESULT, Levels.levelEight(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
        waitForSavedResult(page, LEVEL_EIGHT_RESULT_KEY);
        assertEquals("17000:46", readSavedResult(page, LEVEL_EIGHT_RESULT_KEY));
        controls.clickButton(
            GamePhase.RESULT, Levels.levelEight(), true, MazeGameLayout.RESULT_NEXT_LEVEL);

        controls.waitForButton(
            GamePhase.BUILDING, Levels.levelNine(), false, MazeGameLayout.BUILD_START);
        placePassingCells(
            controls,
            Levels.levelNine(),
            BrowserGameScenario.LEVEL_NINE_WALL,
            BrowserGameScenario.LEVEL_NINE_SLOW_FLOORS);
        page.screenshot(
            new Page.ScreenshotOptions().setPath(reportDirectory.resolve("desktop-level-9.png")));
        controls.clickButton(
            GamePhase.BUILDING, Levels.levelNine(), false, MazeGameLayout.BUILD_START);
        controls.waitForButton(
            GamePhase.RESULT, Levels.levelNine(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
        waitForSavedResult(page, LEVEL_NINE_RESULT_KEY);
        assertEquals("17250:54", readSavedResult(page, LEVEL_NINE_RESULT_KEY));
        controls.clickButton(
            GamePhase.RESULT, Levels.levelNine(), true, MazeGameLayout.RESULT_NEXT_LEVEL);

        controls.waitForButton(
            GamePhase.BUILDING, Levels.levelTen(), false, MazeGameLayout.BUILD_START);
        page.screenshot(
            new Page.ScreenshotOptions()
                .setPath(reportDirectory.resolve("desktop-level-10-presets.png")));
        controls.dragPlacedCell(
            Levels.levelTen(),
            BrowserGameScenario.LEVEL_TEN_PRESET_WALL,
            BrowserGameScenario.LEVEL_TEN_PRESET_MOVE_DESTINATION);
        controls.dragPlacedCell(
            Levels.levelTen(),
            BrowserGameScenario.LEVEL_TEN_PRESET_MOVE_DESTINATION,
            BrowserGameScenario.LEVEL_TEN_PRESET_WALL);
        controls.placeWalls(Levels.levelTen(), BrowserGameScenario.LEVEL_TEN_WALLS);
        controls.clickButton(
            GamePhase.BUILDING,
            Levels.levelTen(),
            false,
            MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR));
        for (GridPosition slowFloor : BrowserGameScenario.LEVEL_TEN_SLOW_FLOORS) {
          controls.clickCell(Levels.levelTen(), slowFloor);
        }
        page.screenshot(
            new Page.ScreenshotOptions().setPath(reportDirectory.resolve("desktop-level-10.png")));
        controls.clickButton(
            GamePhase.BUILDING, Levels.levelTen(), false, MazeGameLayout.BUILD_START);
        controls.waitForButton(
            GamePhase.RESULT, Levels.levelTen(), false, MazeGameLayout.RESULT_RETRY);
        waitForSavedResult(page, LEVEL_TEN_RESULT_KEY);
        assertEquals("11500:71", readSavedResult(page, LEVEL_TEN_RESULT_KEY));
        assertTrue(
            levelLog.errors().isEmpty(),
            () -> String.join(System.lineSeparator(), levelLog.errors()));
      }
    }
  }

  private static void placePassingCells(
      BrowserControls controls,
      LevelDefinition level,
      GridPosition wall,
      List<GridPosition> slowFloors)
      throws IOException {
    controls.placeWalls(level, List.of(wall));
    controls.clickButton(
        GamePhase.BUILDING,
        level,
        false,
        MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR));
    for (GridPosition slowFloor : slowFloors) {
      controls.clickCell(level, slowFloor);
    }
  }

  private static void runBuildGestureFixtureFlow(
      Browser browser,
      URI applicationUri,
      Path screenshotPath,
      MobileViewport viewport,
      boolean touch)
      throws IOException {
    Browser.NewContextOptions options =
        new Browser.NewContextOptions().setViewportSize(viewport.width(), viewport.height());
    if (touch) {
      options.setDeviceScaleFactor(3.0).setHasTouch(true);
    }
    try (BrowserContext context = browser.newContext(options)) {
      context.addInitScript(
          "window.sessionStorage.setItem('maze-game.browser-build-gesture-fixture', 'enabled');");
      try (Page page = context.newPage()) {
        BrowserLog fixtureLog = BrowserLog.forAuxiliaryTouchContext();
        fixtureLog.observe(page);
        page.navigate(applicationUri.toString());
        page.waitForCondition(
            () -> page.locator("#loading-state").isHidden(),
            new Page.WaitForConditionOptions().setTimeout(30_000.0));
        LevelDefinition level = buildGestureFixtureLevel();
        BrowserControls controls =
            new BrowserControls(page, viewport.width(), viewport.height(), touch, 1);
        controls.waitForButton(GamePhase.MAIN_MENU, level, false, MazeGameLayout.MAIN_MENU_START);
        controls.clickButton(GamePhase.MAIN_MENU, level, false, MazeGameLayout.MAIN_MENU_START);
        controls.waitForButton(GamePhase.LEVEL_SELECT, level, false, MazeGameLayout.levelCardId(1));
        controls.clickButton(GamePhase.LEVEL_SELECT, level, false, MazeGameLayout.levelCardId(1));
        controls.waitForButton(GamePhase.BUILDING, level, false, MazeGameLayout.BUILD_START);

        GridPosition wallSource = new GridPosition(3, 0);
        GridPosition wallDestination = new GridPosition(2, 0);
        GridPosition slowSource = new GridPosition(3, 4);
        GridPosition slowDestination = new GridPosition(2, 4);
        GridPosition fixedWall = new GridPosition(1, 0);
        GridPosition fixedSlowFloor = new GridPosition(1, 4);
        assertWallCell(page, controls.cellCenter(level, fixedWall));
        assertSlowFloorCell(page, controls.cellCenter(level, fixedSlowFloor));
        controls.dragPaletteToCell(
            GamePhase.BUILDING, level, false, PlaceableCellType.WALL, fixedSlowFloor);
        assertSlowFloorCell(page, controls.cellCenter(level, fixedSlowFloor));
        controls.dragPaletteToCell(
            GamePhase.BUILDING, level, false, PlaceableCellType.WALL, wallSource);
        controls.dragPaletteToCell(
            GamePhase.BUILDING, level, false, PlaceableCellType.SLOW_FLOOR, slowSource);
        controls.dragPlacedCell(level, wallSource, wallDestination);
        controls.dragPlacedCell(level, slowSource, slowDestination);

        Files.createDirectories(Objects.requireNonNull(screenshotPath.getParent()));
        page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath));
        assertOpenCell(page, controls.cellCenter(level, wallSource));
        assertWallCell(page, controls.cellCenter(level, wallDestination));
        assertOpenCell(page, controls.cellCenter(level, slowSource));
        assertSlowFloorCell(page, controls.cellCenter(level, slowDestination));
        controls.dragPlacedCell(level, wallDestination, fixedSlowFloor);
        assertWallCell(page, controls.cellCenter(level, wallDestination));
        assertSlowFloorCell(page, controls.cellCenter(level, fixedSlowFloor));
        controls.clickButton(GamePhase.BUILDING, level, false, MazeGameLayout.BUILD_START);
        controls.waitForButton(GamePhase.RESULT, level, false, MazeGameLayout.RESULT_RETRY);
        assertTrue(
            fixtureLog.errors().isEmpty(),
            () -> String.join(System.lineSeparator(), fixtureLog.errors()));
      }
    }
  }

  private static LevelDefinition buildGestureFixtureLevel() {
    List<PlaceableCellSupply> supplies =
        List.of(
            PlaceableCellSupply.finite(PlaceableCellType.WALL, 2),
            PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 2));
    LevelSolver tracker =
        new LevelSolver(
            new GridPosition(4, 2),
            new GridPosition(0, 2),
            SolverBehavior.LEAST_VISITED,
            OptionalLong.empty(),
            SolverAppearance.TRACKER_RACCOON,
            GoalType.TRASH_CAN);
    LevelSolver seeker =
        new LevelSolver(
            new GridPosition(4, 4),
            new GridPosition(0, 4),
            SolverBehavior.LINE_OF_SIGHT,
            OptionalLong.of(17L),
            SolverAppearance.SEEKER_RABBIT,
            GoalType.CARROT);
    return new LevelDefinition(
        "browser-build-gesture-fixture",
        "Build Gesture Fixture",
        GridSize.square(5),
        Duration.ofSeconds(30),
        Duration.ofMillis(200),
        Duration.ofSeconds(3),
        Duration.ofMillis(50),
        supplies,
        List.of(
            new FixedCell(new GridPosition(1, 0), FixedCellType.WALL),
            new FixedCell(new GridPosition(1, 4), FixedCellType.SLOW_FLOOR)),
        List.of(tracker, seeker));
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
            GamePhase.MAIN_MENU, Levels.levelOne(), false, MazeGameLayout.MAIN_MENU_START);

        resizeAndAssert(page, rotated);
        resizeAndAssert(page, primary);
        primaryControls.clickButton(
            GamePhase.MAIN_MENU, Levels.levelOne(), false, MazeGameLayout.MAIN_MENU_START);
        primaryControls.waitForButton(
            GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(1));
        primaryControls.clickButton(
            GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(2));
        primaryControls.clickButton(
            GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(1));
        primaryControls.waitForButton(
            GamePhase.BUILDING, Levels.levelOne(), false, MazeGameLayout.BUILD_START);

        primaryControls.cancelPaletteDragAtCell(
            GamePhase.BUILDING, Levels.levelOne(), false, PlaceableCellType.WALL, EDITED_CELL);
        assertOpenCell(page, primaryControls.cellCenter(Levels.levelOne(), EDITED_CELL));
        primaryControls.dragPaletteWithSecondaryCancellation(
            GamePhase.BUILDING, Levels.levelOne(), false, PlaceableCellType.WALL, EDITED_CELL);
        assertWallCell(page, primaryControls.cellCenter(Levels.levelOne(), EDITED_CELL));
        primaryControls.clickCell(Levels.levelOne(), EDITED_CELL);
        assertOpenCell(page, primaryControls.cellCenter(Levels.levelOne(), EDITED_CELL));

        assertTrue(
            MazeGameLayout.forPhase(
                    GamePhase.BUILDING,
                    primary.width(),
                    primary.height(),
                    Levels.levelOne().gridSize(),
                    false,
                    Levels.catalog().levels().size(),
                    false,
                    Levels.levelOne().initiallyAvailableCellTypes())
                .element(MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR))
                .isEmpty());
        Files.createDirectories(Objects.requireNonNull(screenshotPath.getParent()));
        page.screenshot(
            new Page.ScreenshotOptions().setPath(paletteScreenshotPath(screenshotPath)));
        primaryControls.dragPaletteToCell(
            GamePhase.BUILDING, Levels.levelOne(), false, PlaceableCellType.WALL, EDITED_CELL);
        assertWallCell(page, primaryControls.cellCenter(Levels.levelOne(), EDITED_CELL));
        primaryControls.beginPlacedCellDrag(Levels.levelOne(), EDITED_CELL, MOVED_CELL);
        page.screenshot(
            new Page.ScreenshotOptions().setPath(cellDragScreenshotPath(screenshotPath)));
        primaryControls.finishPlacedCellDrag(Levels.levelOne(), MOVED_CELL);
        assertOpenCell(page, primaryControls.cellCenter(Levels.levelOne(), EDITED_CELL));
        assertWallCell(page, primaryControls.cellCenter(Levels.levelOne(), MOVED_CELL));
        resizeAndAssert(page, rotated);
        rotatedControls.waitForButton(
            GamePhase.BUILDING, Levels.levelOne(), false, MazeGameLayout.BUILD_START);
        assertWallCell(page, rotatedControls.cellCenter(Levels.levelOne(), MOVED_CELL));
        rotatedControls.dragPlacedCell(Levels.levelOne(), MOVED_CELL, EDITED_CELL);
        assertOpenCell(page, rotatedControls.cellCenter(Levels.levelOne(), MOVED_CELL));
        assertWallCell(page, rotatedControls.cellCenter(Levels.levelOne(), EDITED_CELL));
        rotatedControls.clickCell(Levels.levelOne(), EDITED_CELL);
        assertOpenCell(page, rotatedControls.cellCenter(Levels.levelOne(), EDITED_CELL));
        resizeAndAssert(page, primary);
        primaryControls.waitForButton(
            GamePhase.BUILDING, Levels.levelOne(), false, MazeGameLayout.BUILD_START);
        assertOpenCell(page, primaryControls.cellCenter(Levels.levelOne(), EDITED_CELL));
        primaryControls.placeWalls(Levels.levelOne(), List.of(BrowserGameScenario.LEVEL_ONE_WALL));

        primaryControls.clickButton(
            GamePhase.BUILDING, Levels.levelOne(), false, MazeGameLayout.BUILD_START);
        resizeAndAssert(page, rotated);
        waitForSavedResult(page, MILESTONE_ONE_RESULT_KEY);
        assertEquals("9000:36", readSavedResult(page, MILESTONE_ONE_RESULT_KEY));
        rotatedControls.waitForButton(
            GamePhase.RESULT, Levels.levelOne(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
        rotatedControls.clickButton(
            GamePhase.RESULT, Levels.levelOne(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
        rotatedControls.waitForButton(
            GamePhase.BUILDING, Levels.levelTwo(), false, MazeGameLayout.BUILD_START);
        rotatedControls.placeWalls(Levels.levelTwo(), MILESTONE_TWO_WALLS);
        resizeAndAssert(page, primary);
        primaryControls.waitForButton(
            GamePhase.BUILDING, Levels.levelTwo(), false, MazeGameLayout.BUILD_START);
        primaryControls.clickButton(
            GamePhase.BUILDING, Levels.levelTwo(), false, MazeGameLayout.BUILD_START);
        waitForSavedResult(page, MILESTONE_TWO_RESULT_KEY);
        assertEquals("4500:18", readSavedResult(page, MILESTONE_TWO_RESULT_KEY));
        primaryControls.waitForButton(
            GamePhase.RESULT, Levels.levelTwo(), true, MazeGameLayout.RESULT_NEXT_LEVEL);

        resizeAndAssert(page, rotated);
        rotatedControls.clickButton(
            GamePhase.RESULT, Levels.levelTwo(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
        rotatedControls.waitForButton(
            GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
        rotatedControls.placeWalls(Levels.levelThree(), MILESTONE_THREE_WALLS);
        resizeAndAssert(page, primary);
        primaryControls.waitForButton(
            GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
        primaryControls.clickButton(
            GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
        waitForSavedResult(page, MILESTONE_THREE_RESULT_KEY);
        assertEquals("6000:24", readSavedResult(page, MILESTONE_THREE_RESULT_KEY));
        primaryControls.waitForButton(
            GamePhase.RESULT, Levels.levelThree(), true, MazeGameLayout.RESULT_REPLAY);
        Files.createDirectories(Objects.requireNonNull(screenshotPath.getParent()));
        page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath));

        primaryControls.clickButtonAndWaitForChange(
            GamePhase.RESULT, Levels.levelThree(), true, MazeGameLayout.RESULT_REPLAY);
        primaryControls.waitForButton(
            GamePhase.RESULT, Levels.levelThree(), true, MazeGameLayout.RESULT_RETRY);
        primaryControls.clickButton(
            GamePhase.RESULT, Levels.levelThree(), true, MazeGameLayout.RESULT_RETRY);
        primaryControls.waitForButton(
            GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
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

  private static Path paletteScreenshotPath(Path screenshotPath) {
    String fileName = Objects.requireNonNull(screenshotPath.getFileName()).toString();
    String paletteName =
        fileName.endsWith(".png")
            ? fileName.substring(0, fileName.length() - 4) + "-palette.png"
            : fileName + "-palette.png";
    return screenshotPath.resolveSibling(paletteName);
  }

  private static Path cellDragScreenshotPath(Path screenshotPath) {
    String fileName = Objects.requireNonNull(screenshotPath.getFileName()).toString();
    String dragName =
        fileName.endsWith(".png")
            ? fileName.substring(0, fileName.length() - 4) + "-cell-drag.png"
            : fileName + "-cell-drag.png";
    return screenshotPath.resolveSibling(dragName);
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

  private static void assertSlowFloorCell(Page page, ScreenPoint point) throws IOException {
    double devicePixelRatio = ((Number) page.evaluate("window.devicePixelRatio")).doubleValue();
    BufferedImage image = screenshot(page);
    int centerX = (int) Math.round(point.x() * devicePixelRatio);
    int centerY = (int) Math.round(point.y() * devicePixelRatio);
    int radius = (int) Math.round(12.0 * devicePixelRatio);
    int amberPixels = 0;
    for (int y = centerY - radius; y <= centerY + radius; y++) {
      for (int x = centerX - radius; x <= centerX + radius; x++) {
        int color = image.getRGB(x, y);
        if (red(color) >= 100
            && red(color) >= green(color) + 20
            && red(color) >= blue(color) + 40) {
          amberPixels++;
        }
      }
    }
    assertTrue(amberPixels > 0, "expected Slow Floor amber fill or hourglass mark");
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

  private static void assertRenderedSolverPresentation(
      Page page,
      LevelDefinition level,
      GridPosition finalSolverPosition,
      boolean hasNextLevel,
      long expectedStatusSignature,
      boolean expectScoutMarker)
      throws IOException {
    BufferedImage image = screenshot(page);
    ScreenLayout layout =
        MazeGameLayout.forPhase(
            GamePhase.RESULT,
            VIEWPORT_WIDTH,
            VIEWPORT_HEIGHT,
            level.gridSize(),
            false,
            3,
            hasNextLevel);
    assertEquals(
        expectedStatusSignature,
        lightPixelSignature(image, layout.bounds(MazeGameLayout.RESULT_STATUS)));

    ScreenRectangle grid = layout.bounds(MazeGameLayout.GAME_GRID);
    int cellSize = Math.round(grid.width() / level.gridSize().columns());
    ScreenRectangle solverCell =
        new ScreenRectangle(
            grid.x() + finalSolverPosition.column() * cellSize,
            grid.y() + (level.gridSize().rows() - 1 - finalSolverPosition.row()) * cellSize,
            cellSize,
            cellSize);
    int scoutMarkerPixels = blueMarkerPixelCount(image, solverCell);
    assertEquals(expectScoutMarker, scoutMarkerPixels >= 20);
  }

  private static long lightPixelSignature(BufferedImage image, ScreenRectangle region) {
    long[] signature = {0xcbf29ce484222325L};
    forEachPixel(
        image,
        region,
        color -> {
          boolean light = red(color) >= 120 && green(color) >= 120 && blue(color) >= 120;
          signature[0] ^= light ? 1L : 0L;
          signature[0] *= 0x100000001b3L;
        });
    return signature[0];
  }

  private static void assertPaletteSupplyBadges(Page page, LevelDefinition level)
      throws IOException {
    BufferedImage image = screenshot(page);
    ScreenLayout layout =
        MazeGameLayout.forPhase(
            GamePhase.BUILDING,
            VIEWPORT_WIDTH,
            VIEWPORT_HEIGHT,
            level.gridSize(),
            false,
            Levels.catalog().levels().size(),
            false,
            level.initiallyAvailableCellTypes());
    ScreenRectangle wallBadge =
        paletteSupplyBadgeBounds(
            layout.bounds(MazeGameLayout.paletteItemId(PlaceableCellType.WALL)));

    assertTrue(lightPixelCount(image, inset(wallBadge, 3.0F)) > 0, "expected supply badge");
    assertTrue(
        layout.element(MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR)).isEmpty(),
        "zero-start Slow Floor should not receive palette bounds");
  }

  private static ScreenRectangle paletteSupplyBadgeBounds(ScreenRectangle paletteItem) {
    float iconSize = Math.min(24.0F, paletteItem.height() - 16.0F);
    float iconX = paletteItem.x() + (paletteItem.width() - iconSize) / 2.0F;
    float iconY = paletteItem.y() + (paletteItem.height() - iconSize) / 2.0F;
    return new ScreenRectangle(iconX + iconSize - 9.0F, iconY - 7.0F, 18.0F, 18.0F);
  }

  private static ScreenRectangle inset(ScreenRectangle bounds, float amount) {
    return new ScreenRectangle(
        bounds.x() + amount,
        bounds.y() + amount,
        bounds.width() - 2.0F * amount,
        bounds.height() - 2.0F * amount);
  }

  private static int lightPixelCount(BufferedImage image, ScreenRectangle region) {
    int[] count = {0};
    forEachPixel(
        image,
        region,
        color -> {
          if (red(color) >= 120 && green(color) >= 120 && blue(color) >= 120) {
            count[0]++;
          }
        });
    return count[0];
  }

  private static ScreenRectangle paletteTooltipBounds(ScreenRectangle paletteItem) {
    float width = 176.0F;
    float height = 36.0F;
    return new ScreenRectangle(
        paletteItem.x() + (paletteItem.width() - width) / 2.0F,
        paletteItem.top() + 8.0F,
        width,
        height);
  }

  private static int blueMarkerPixelCount(BufferedImage image, ScreenRectangle region) {
    int[] count = {0};
    forEachPixel(
        image,
        region,
        color -> {
          if (blue(color) >= 150
              && blue(color) >= red(color) + 40
              && blue(color) >= green(color) + 30) {
            count[0]++;
          }
        });
    return count[0];
  }

  private static void forEachPixel(
      BufferedImage image, ScreenRectangle region, IntConsumer consumer) {
    int left = Math.round(region.x());
    int top = image.getHeight() - Math.round(region.top());
    int right = Math.round(region.right());
    int bottom = image.getHeight() - Math.round(region.y());
    for (int y = top; y < bottom; y++) {
      for (int x = left; x < right; x++) {
        consumer.accept(image.getRGB(x, y));
      }
    }
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
    int backgroundColor = pixelColorAfterNavigation(page, 0, 0);
    waitForPixelChange(page, x, y, backgroundColor);
  }

  private static void waitForPixelChange(Page page, int x, int y, int originalColor)
      throws IOException {
    long deadline = System.nanoTime() + Duration.ofSeconds(25).toNanos();
    while (pixelColorAfterNavigation(page, x, y) == originalColor && System.nanoTime() < deadline) {
      page.waitForTimeout(100.0);
    }
    assertFalse(
        pixelColorAfterNavigation(page, x, y) == originalColor, "expected rendered pixel change");
  }

  private static int pixelColorAfterNavigation(Page page, int cssX, int cssY) throws IOException {
    long deadline = System.nanoTime() + Duration.ofSeconds(25).toNanos();
    while (true) {
      try {
        return pixelColor(page, cssX, cssY);
      } catch (PlaywrightException exception) {
        if (!exception.getMessage().contains("Execution context was destroyed")
            || System.nanoTime() >= deadline) {
          throw exception;
        }
        page.waitForTimeout(100.0);
      }
    }
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
    private final int levelCount;

    private BrowserControls(Page page, int width, int height, boolean touch) {
      this(page, width, height, touch, Levels.catalog().levels().size());
    }

    private BrowserControls(Page page, int width, int height, boolean touch, int levelCount) {
      this.page = page;
      this.width = width;
      this.height = height;
      this.touch = touch;
      this.levelCount = levelCount;
    }

    @Override
    public void clickButton(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId) {
      click(buttonCenter(phase, level, hasNextLevel, elementId));
    }

    private void clickButtonAndWaitForChange(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId)
        throws IOException {
      ScreenPoint point = buttonCenter(phase, level, hasNextLevel, elementId);
      int originalColor = pixelColor(page, point.x(), point.y());
      click(point);
      waitForPixelChange(page, point.x(), point.y(), originalColor);
    }

    private void hoverButton(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId) {
      ScreenPoint point = buttonCenter(phase, level, hasNextLevel, elementId);
      page.mouse().move(1.0, 1.0);
      page.waitForTimeout(50.0);
      page.mouse().move(point.x(), point.y());
      page.waitForTimeout(600.0);
    }

    @Override
    public void waitForButton(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId)
        throws IOException {
      ScreenPoint point = buttonCenter(phase, level, hasNextLevel, elementId);
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

    private void dragPaletteToCell(
        GamePhase phase,
        LevelDefinition level,
        boolean hasNextLevel,
        PlaceableCellType type,
        GridPosition position) {
      ScreenPoint start =
          buttonCenter(phase, level, hasNextLevel, MazeGameLayout.paletteItemId(type));
      ScreenPoint destination = cellCenter(level, position);
      if (touch) {
        dispatchTouchDrag(start, destination);
      } else {
        page.mouse().move(start.x(), start.y());
        page.mouse().down();
        page.mouse()
            .move(
                destination.x(),
                destination.y(),
                new com.microsoft.playwright.Mouse.MoveOptions().setSteps(6));
        page.mouse().up();
      }
      page.waitForTimeout(100.0);
    }

    private void dragPlacedCell(
        LevelDefinition level, GridPosition source, GridPosition destination) {
      beginPlacedCellDrag(level, source, destination);
      finishPlacedCellDrag(level, destination);
    }

    private void beginPlacedCellDrag(
        LevelDefinition level, GridPosition source, GridPosition destination) {
      ScreenPoint start = cellCenter(level, source);
      ScreenPoint end = cellCenter(level, destination);
      if (touch) {
        dispatchTouchDragStart(start, end);
      } else {
        page.mouse().move(start.x(), start.y());
        page.mouse().down();
        page.mouse()
            .move(end.x(), end.y(), new com.microsoft.playwright.Mouse.MoveOptions().setSteps(6));
      }
      page.waitForTimeout(100.0);
    }

    private void finishPlacedCellDrag(LevelDefinition level, GridPosition destination) {
      ScreenPoint end = cellCenter(level, destination);
      if (touch) {
        dispatchTouchDragEnd(end);
      } else {
        page.mouse().up();
      }
      page.waitForTimeout(100.0);
    }

    private void dispatchTouchDrag(ScreenPoint start, ScreenPoint destination) {
      dispatchTouchGesture(start, destination, "touchend");
    }

    private void dispatchTouchDragStart(ScreenPoint start, ScreenPoint destination) {
      page.evaluate(
          "points => {"
              + "const canvas = document.querySelector('canvas');"
              + "const touch = (x, y) => new Touch({identifier: 17, target: canvas,"
              + "clientX: x, clientY: y, pageX: x, pageY: y, screenX: x, screenY: y,"
              + "radiusX: 1, radiusY: 1, rotationAngle: 0, force: 1});"
              + "const send = (name, active, changed) => canvas.dispatchEvent(new TouchEvent(name,"
              + "{touches: active, targetTouches: active, changedTouches: changed,"
              + "bubbles: true, cancelable: true}));"
              + "const first = touch(points.startX, points.startY);"
              + "send('touchstart', [first], [first]);"
              + "for (let step = 1; step <= 6; step++) {"
              + "const x = points.startX + (points.endX - points.startX) * step / 6;"
              + "const y = points.startY + (points.endY - points.startY) * step / 6;"
              + "const moved = touch(x, y); send('touchmove', [moved], [moved]);}"
              + "}",
          Map.of(
              "startX", start.x(),
              "startY", start.y(),
              "endX", destination.x(),
              "endY", destination.y()));
    }

    private void dispatchTouchDragEnd(ScreenPoint destination) {
      page.evaluate(
          "point => {"
              + "const canvas = document.querySelector('canvas');"
              + "const last = new Touch({identifier: 17, target: canvas,"
              + "clientX: point.x, clientY: point.y, pageX: point.x, pageY: point.y,"
              + "screenX: point.x, screenY: point.y, radiusX: 1, radiusY: 1,"
              + "rotationAngle: 0, force: 1});"
              + "canvas.dispatchEvent(new TouchEvent('touchend', {touches: [], targetTouches: [],"
              + "changedTouches: [last], bubbles: true, cancelable: true}));"
              + "}",
          Map.of("x", destination.x(), "y", destination.y()));
    }

    private void cancelPaletteDragAtCell(
        GamePhase phase,
        LevelDefinition level,
        boolean hasNextLevel,
        PlaceableCellType type,
        GridPosition position) {
      ScreenPoint start =
          buttonCenter(phase, level, hasNextLevel, MazeGameLayout.paletteItemId(type));
      dispatchTouchGesture(start, cellCenter(level, position), "touchcancel");
    }

    private void dragPaletteWithSecondaryCancellation(
        GamePhase phase,
        LevelDefinition level,
        boolean hasNextLevel,
        PlaceableCellType type,
        GridPosition position) {
      ScreenPoint start =
          buttonCenter(phase, level, hasNextLevel, MazeGameLayout.paletteItemId(type));
      ScreenPoint destination = cellCenter(level, position);
      page.evaluate(
          "points => {"
              + "const canvas = document.querySelector('canvas');"
              + "const touch = (id, x, y) => new Touch({identifier: id, target: canvas,"
              + "clientX: x, clientY: y, pageX: x, pageY: y, screenX: x, screenY: y,"
              + "radiusX: 1, radiusY: 1, rotationAngle: 0, force: 1});"
              + "const send = (name, active, changed) => canvas.dispatchEvent(new TouchEvent(name,"
              + "{touches: active, targetTouches: active, changedTouches: changed,"
              + "bubbles: true, cancelable: true}));"
              + "const outside = touch(31, 1, 1);"
              + "const owner = touch(17, points.startX, points.startY);"
              + "send('touchstart', [outside, owner], [outside, owner]);"
              + "const moved = touch(17, points.endX, points.endY);"
              + "send('touchmove', [outside, moved], [moved]);"
              + "send('touchcancel', [outside], [moved]);"
              + "const replacement = touch(19, points.startX, points.startY);"
              + "send('touchstart', [outside, replacement], [replacement]);"
              + "const replacementMoved = touch(19, points.endX, points.endY);"
              + "send('touchmove', [outside, replacementMoved], [replacementMoved]);"
              + "const second = touch(29, points.startX + 4, points.startY);"
              + "send('touchstart', [outside, replacementMoved, second], [second]);"
              + "send('touchcancel', [outside, replacementMoved], [second]);"
              + "send('touchend', [outside], [replacementMoved]);"
              + "send('touchcancel', [], [outside]);"
              + "}",
          Map.of(
              "startX", start.x(),
              "startY", start.y(),
              "endX", destination.x(),
              "endY", destination.y()));
      page.waitForTimeout(100.0);
    }

    private void dispatchTouchGesture(
        ScreenPoint start, ScreenPoint destination, String completionEvent) {
      page.evaluate(
          "points => {"
              + "const canvas = document.querySelector('canvas');"
              + "const touch = (x, y) => new Touch({identifier: 17, target: canvas,"
              + "clientX: x, clientY: y, pageX: x, pageY: y, screenX: x, screenY: y,"
              + "radiusX: 1, radiusY: 1, rotationAngle: 0, force: 1});"
              + "const send = (name, active, changed) => canvas.dispatchEvent(new TouchEvent(name,"
              + "{touches: active, targetTouches: active, changedTouches: changed,"
              + "bubbles: true, cancelable: true}));"
              + "const first = touch(points.startX, points.startY);"
              + "send('touchstart', [first], [first]);"
              + "for (let step = 1; step <= 6; step++) {"
              + "const x = points.startX + (points.endX - points.startX) * step / 6;"
              + "const y = points.startY + (points.endY - points.startY) * step / 6;"
              + "const moved = touch(x, y); send('touchmove', [moved], [moved]);}"
              + "const last = touch(points.endX, points.endY);"
              + "send(points.completionEvent, [], [last]);"
              + "}",
          Map.of(
              "startX", start.x(),
              "startY", start.y(),
              "endX", destination.x(),
              "endY", destination.y(),
              "completionEvent", completionEvent));
    }

    private ScreenPoint cellCenter(LevelDefinition level, GridPosition position) {
      return BrowserGameScenario.cellCenter(width, height, level, position);
    }

    private ScreenPoint buttonCenter(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId) {
      ScreenRectangle bounds =
          MazeGameLayout.forPhase(
                  phase,
                  width,
                  height,
                  level.gridSize(),
                  false,
                  levelCount,
                  hasNextLevel,
                  level.initiallyAvailableCellTypes())
              .bounds(elementId);
      return new ScreenPoint(
          Math.round(bounds.x() + bounds.width() / 2.0F),
          Math.round(height - bounds.y() - bounds.height() / 2.0F));
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
