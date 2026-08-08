package io.github.sportne.mazegame.browser;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.openqa.selenium.WebDriver;

/** Bounded retry for transient SafariDriver session-creation failures. */
final class SafariSessionLauncher {
  private static final int MAX_ATTEMPTS = 3;
  private static final Duration RETRY_DELAY = Duration.ofSeconds(1);

  private SafariSessionLauncher() {}

  static WebDriver launch(Supplier<WebDriver> sessionFactory, List<String> evidence) {
    return launch(sessionFactory, evidence, Thread::sleep);
  }

  static WebDriver launch(
      Supplier<WebDriver> sessionFactory, List<String> evidence, Sleeper sleeper) {
    Objects.requireNonNull(sessionFactory, "sessionFactory");
    Objects.requireNonNull(evidence, "evidence");
    Objects.requireNonNull(sleeper, "sleeper");
    RuntimeException firstFailure = null;
    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      try {
        WebDriver driver =
            Objects.requireNonNull(sessionFactory.get(), "SafariDriver session must not be null");
        evidence.add("SafariDriver session attempt " + attempt + ": PASS");
        return driver;
      } catch (RuntimeException failure) {
        evidence.add(
            "SafariDriver session attempt "
                + attempt
                + ": FAIL - "
                + failure.getClass().getSimpleName()
                + ": "
                + failure.getMessage());
        if (firstFailure == null) {
          firstFailure = failure;
        } else {
          firstFailure.addSuppressed(failure);
        }
        if (attempt < MAX_ATTEMPTS) {
          pause(sleeper, firstFailure);
        }
      }
    }
    throw new SafariSessionStartException(Objects.requireNonNull(firstFailure));
  }

  private static void pause(Sleeper sleeper, RuntimeException firstFailure) {
    try {
      sleeper.sleep(RETRY_DELAY.toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      firstFailure.addSuppressed(exception);
      throw new SafariSessionStartException(firstFailure);
    }
  }

  @FunctionalInterface
  interface Sleeper {
    void sleep(long millis) throws InterruptedException;
  }

  static final class SafariSessionStartException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private SafariSessionStartException(RuntimeException cause) {
      super("SafariDriver session creation failed after bounded retries", cause);
    }
  }
}
