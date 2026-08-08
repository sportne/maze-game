package io.github.sportne.mazegame.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

final class SafariSessionLauncherTest {
  @Test
  void retriesOnlySessionCreationUpToTheBound() {
    AtomicInteger attempts = new AtomicInteger();
    List<String> evidence = new ArrayList<>();
    WebDriver expected = new StubWebDriver();

    WebDriver actual =
        SafariSessionLauncher.launch(
            () -> {
              if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("transient startup failure");
              }
              return expected;
            },
            evidence,
            ignored -> {});

    assertSame(expected, actual);
    assertEquals(3, attempts.get());
    assertEquals(3, evidence.size());
  }

  @Test
  void exhaustsWithTheOriginalFailureAndLaterEvidence() {
    AtomicInteger attempts = new AtomicInteger();
    List<String> evidence = new ArrayList<>();
    IllegalStateException first = new IllegalStateException("first startup failure");

    SafariSessionLauncher.SafariSessionStartException thrown =
        assertThrows(
            SafariSessionLauncher.SafariSessionStartException.class,
            () ->
                SafariSessionLauncher.launch(
                    () -> {
                      if (attempts.getAndIncrement() == 0) {
                        throw first;
                      }
                      throw new IllegalStateException("later startup failure");
                    },
                    evidence,
                    ignored -> {}));

    assertSame(first, thrown.getCause());
    assertEquals(3, attempts.get());
    assertEquals(2, first.getSuppressed().length);
    assertEquals(3, evidence.size());
  }

  @Test
  void reportsDriverCleanupFailureWithoutThrowingIt() {
    IllegalStateException failure = new IllegalStateException("quit failed");
    List<String> evidence = new ArrayList<>();
    StubWebDriver driver =
        new StubWebDriver() {
          @Override
          public void quit() {
            throw failure;
          }
        };

    RuntimeException reported = SafariReleaseTest.quitDriver(driver, evidence);

    assertSame(failure, reported);
    assertEquals(List.of("SafariDriver cleanup: FAIL - " + failure), evidence);
  }

  private static class StubWebDriver implements WebDriver {
    @Override
    public void get(String url) {}

    @Override
    public String getCurrentUrl() {
      return "";
    }

    @Override
    public String getTitle() {
      return "";
    }

    @Override
    public List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By by) {
      return List.of();
    }

    @Override
    public org.openqa.selenium.WebElement findElement(org.openqa.selenium.By by) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getPageSource() {
      return "";
    }

    @Override
    public void close() {}

    @Override
    public void quit() {}

    @Override
    public java.util.Set<String> getWindowHandles() {
      return java.util.Set.of();
    }

    @Override
    public String getWindowHandle() {
      return "";
    }

    @Override
    public TargetLocator switchTo() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Navigation navigate() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Options manage() {
      throw new UnsupportedOperationException();
    }
  }
}
