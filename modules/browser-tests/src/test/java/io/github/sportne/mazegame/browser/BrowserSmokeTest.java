package io.github.sportne.mazegame.browser;

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
import java.util.Set;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** End-to-end smoke coverage for the generated TeaVM release artifact. */
final class BrowserSmokeTest {
  private static final int VIEWPORT_WIDTH = 1280;
  private static final int VIEWPORT_HEIGHT = 720;
  private static final int STARTUP_SAMPLE_COUNT = 5;
  private static final String RESULT_KEY = "maze-game.best-result.milestone-1";
  private static final String SITE_PATH = "/maze-game/";
  private static final Set<String> COMMON_ASSETS =
      Set.of("styles.css", "mouse-sprites.png", "exploreMaze_T1.mp3");

  @Test
  @Timeout(90)
  void completesGameFlowAndLoadsSavedResultAfterReload() throws IOException {
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
    assertResizeGuidance(page);

    click(page, 640, 280);
    waitForRenderedControl(page, 404, 280);
    assertAudioResumed(page);
    click(page, 404, 280);
    waitForRenderedControl(page, 738, 656);

    int emptyCellColor = screenshot(page).getRGB(551, 360);
    click(page, 551, 360);
    waitForPixelChange(page, 551, 360, emptyCellColor);
    int wallColor = screenshot(page).getRGB(551, 360);
    click(page, 542, 656);
    click(page, 551, 360);
    waitForPixelChange(page, 551, 360, wallColor);

    click(page, 738, 656);
    waitForSavedResult(page);
    String savedResult = readSavedResult(page);

    page.reload();
    waitForRenderedControl(page, 640, 280);

    assertEquals(savedResult, readSavedResult(page));
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

  private static void click(Page page, double x, double y) {
    if (touchInput()) {
      page.touchscreen().tap(x, y);
    } else {
      page.mouse().click(x, y);
    }
  }

  private static boolean touchInput() {
    return Boolean.parseBoolean(requiredProperty("mazeGame.touchInput"));
  }

  private static void assertResizeGuidance(Page page) {
    page.setViewportSize(390, 844);
    page.waitForCondition(() -> page.locator("#viewport-guidance").isVisible());
    page.setViewportSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    page.waitForCondition(() -> page.locator("#viewport-guidance").isHidden());
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

  private static String readSavedResult(Page page) {
    String value = (String) page.evaluate("key => window.localStorage.getItem(key)", RESULT_KEY);
    assertNotNull(value);
    assertTrue(value.matches("[0-9]+:[0-9]+"));
    return value;
  }

  private static void waitForSavedResult(Page page) {
    page.waitForCondition(
        () -> page.evaluate("key => window.localStorage.getItem(key)", RESULT_KEY) != null,
        new Page.WaitForConditionOptions().setTimeout(15_000.0));
  }

  private static void waitForRenderedControl(Page page, int x, int y) throws IOException {
    int backgroundColor = screenshot(page).getRGB(0, 0);
    waitForPixelChange(page, x, y, backgroundColor);
  }

  private static void waitForPixelChange(Page page, int x, int y, int originalColor)
      throws IOException {
    long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
    while (screenshot(page).getRGB(x, y) == originalColor && System.nanoTime() < deadline) {
      page.waitForTimeout(100.0);
    }
    assertFalse(screenshot(page).getRGB(x, y) == originalColor, "expected rendered pixel change");
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

  private static final class BrowserLog {
    private final List<String> errors = new ArrayList<>();
    private final Set<String> observedAssets = new HashSet<>();
    private final Map<String, String> contentTypes = new HashMap<>();
    private final Set<String> requiredAssets;

    private BrowserLog(Set<String> requiredAssets) {
      this.requiredAssets = requiredAssets;
    }

    private void observe(Page page) {
      page.onPageError(message -> errors.add("page: " + message));
      page.onConsoleMessage(this::recordConsoleMessage);
      page.onRequestFailed(request -> errors.add("request: " + request.url()));
      page.onResponse(this::recordResponse);
    }

    private void recordConsoleMessage(ConsoleMessage message) {
      if ("error".equals(message.type())) {
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
