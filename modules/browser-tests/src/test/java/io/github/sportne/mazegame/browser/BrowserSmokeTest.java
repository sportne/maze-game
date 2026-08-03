package io.github.sportne.mazegame.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** End-to-end smoke coverage for the generated TeaVM release artifact. */
final class BrowserSmokeTest {
  private static final int VIEWPORT_WIDTH = 1280;
  private static final int VIEWPORT_HEIGHT = 720;
  private static final String RESULT_KEY = "maze-game.best-result.milestone-1";
  private static final String APPLICATION_PATH = "/maze-game/";
  private static final Set<String> REQUIRED_ASSETS =
      Set.of("app.js", "styles.css", "mouse-sprites.png", "exploreMaze_T1.mp3");

  @Test
  @Timeout(45)
  void completesGameFlowAndLoadsSavedResultAfterReload() throws IOException {
    Path webApplication = requiredDirectory("mazeGame.webAppDirectory");
    Path reportDirectory = Path.of(requiredProperty("mazeGame.browserSmokeReportDirectory"));
    BrowserLog browserLog = new BrowserLog();
    Page page = null;

    try (StaticWebServer server = StaticWebServer.start(webApplication);
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch();
        BrowserContext context =
            browser.newContext(
                new Browser.NewContextOptions().setViewportSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT))) {
      page = context.newPage();
      browserLog.observe(page);
      try {
        runGameFlow(page, server.uri(), browserLog);
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

  private static void runGameFlow(Page page, URI applicationUri, BrowserLog browserLog)
      throws IOException {
    page.navigate(applicationUri.toString());
    waitForRenderedControl(page, 640, 280);
    assertCanvas(page);
    assertTrue(page.locator("#loading-state").isHidden());
    assertTrue(page.locator("#failure-state").isHidden());

    page.mouse().click(640, 280);
    waitForRenderedControl(page, 404, 280);
    page.mouse().click(404, 280);
    waitForRenderedControl(page, 738, 656);

    int emptyCellColor = screenshot(page).getRGB(551, 360);
    page.mouse().click(551, 360);
    waitForPixelChange(page, 551, 360, emptyCellColor);
    int wallColor = screenshot(page).getRGB(551, 360);
    page.mouse().click(542, 656);
    page.mouse().click(551, 360);
    waitForPixelChange(page, 551, 360, wallColor);

    page.mouse().click(738, 656);
    waitForSavedResult(page);
    String savedResult = readSavedResult(page);

    page.reload();
    waitForRenderedControl(page, 640, 280);

    assertEquals(savedResult, readSavedResult(page));
    assertTrue(browserLog.observedAssets().containsAll(REQUIRED_ASSETS));
    assertTrue(
        browserLog.errors().isEmpty(),
        () -> String.join(System.lineSeparator(), browserLog.errors()));
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
      for (String asset : REQUIRED_ASSETS) {
        if (response.ok() && response.url().endsWith(asset)) {
          observedAssets.add(asset);
        }
      }
    }

    private List<String> errors() {
      return List.copyOf(errors);
    }

    private Set<String> observedAssets() {
      return Set.copyOf(observedAssets);
    }
  }

  private static final class StaticWebServer implements AutoCloseable {
    private final Path root;
    private final HttpServer server;

    private StaticWebServer(Path root, HttpServer server) {
      this.root = root;
      this.server = server;
    }

    private static StaticWebServer start(Path root) throws IOException {
      HttpServer server =
          HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
      StaticWebServer staticWebServer = new StaticWebServer(root.toRealPath(), server);
      server.createContext("/", staticWebServer::serve);
      server.start();
      return staticWebServer;
    }

    private URI uri() {
      return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + APPLICATION_PATH);
    }

    private void serve(HttpExchange exchange) throws IOException {
      String requestPath = exchange.getRequestURI().getPath();
      String relativePath =
          requestPath.equals(APPLICATION_PATH)
              ? "index.html"
              : requestPath.substring(APPLICATION_PATH.length());
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
