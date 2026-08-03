package io.github.sportne.mazegame.teavm;

import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.state.BestResultStore;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.teavm.jso.JSBody;

/** Browser best-result store that writes each result atomically to local storage. */
final class TeaVMBestResultStore implements BestResultStore {
  private static final String KEY_PREFIX = "maze-game.best-result.";
  private static final String FIELD_SEPARATOR = ":";

  private final KeyValueStorage storage;

  private TeaVMBestResultStore(KeyValueStorage storage) {
    this.storage = Objects.requireNonNull(storage, "storage");
  }

  static TeaVMBestResultStore create() {
    return new TeaVMBestResultStore(
        new KeyValueStorage() {
          @Override
          public String getItem(String key) {
            return getBrowserItem(key);
          }

          @Override
          public void setItem(String key, String value) {
            setBrowserItem(key, value);
          }
        });
  }

  static TeaVMBestResultStore using(KeyValueStorage storage) {
    return new TeaVMBestResultStore(storage);
  }

  @Override
  public Optional<BestResult> load(String levelId) {
    Objects.requireNonNull(levelId, "levelId");
    try {
      String storedResult = storage.getItem(key(levelId));
      if (storedResult == null) {
        return Optional.empty();
      }
      String[] fields = storedResult.split(FIELD_SEPARATOR, -1);
      if (fields.length != 2) {
        return Optional.empty();
      }
      return Optional.of(
          new BestResult(
              Duration.ofMillis(Long.parseLong(fields[0])), Integer.parseInt(fields[1])));
    } catch (RuntimeException exception) {
      return Optional.empty();
    }
  }

  @Override
  public void save(String levelId, BestResult bestResult) {
    Objects.requireNonNull(levelId, "levelId");
    Objects.requireNonNull(bestResult, "bestResult");
    String serialized =
        bestResult.elapsedTime().toMillis() + FIELD_SEPARATOR + bestResult.moveCount();
    try {
      storage.setItem(key(levelId), serialized);
    } catch (RuntimeException exception) {
      // Browser persistence is optional; an individual setItem failure leaves the prior value
      // intact.
      return;
    }
  }

  private static String key(String levelId) {
    return KEY_PREFIX + levelId;
  }

  @JSBody(
      params = "key",
      script =
          "try { return window.localStorage.getItem(key); } catch (storageError) { return null; }")
  private static native String getBrowserItem(String key);

  @JSBody(
      params = {"key", "value"},
      script = "try { window.localStorage.setItem(key, value); } catch (storageError) { return; }")
  private static native void setBrowserItem(String key, String value);

  interface KeyValueStorage {
    String getItem(String key);

    void setItem(String key, String value);
  }
}
