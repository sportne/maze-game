package io.github.sportne.mazegame.teavm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.result.BestResult;
import java.time.Duration;
import org.junit.jupiter.api.Test;

final class TeaVMBestResultStoreTest {
  private static final String LEVEL_ID = "milestone-1";
  private static final BestResult RESULT = new BestResult(Duration.ofSeconds(5L), 12);

  @Test
  void savesAndLoadsOneNamespacedResult() {
    RecordingStorage storage = new RecordingStorage();
    TeaVMBestResultStore store = TeaVMBestResultStore.using(storage);

    store.save(LEVEL_ID, RESULT);

    assertEquals("maze-game.best-result.milestone-1", storage.key);
    assertEquals(RESULT, store.load(LEVEL_ID).orElseThrow());
  }

  @Test
  void unavailableAndMalformedStorageProducesNoResult() {
    TeaVMBestResultStore unavailable =
        TeaVMBestResultStore.using(
            new TeaVMBestResultStore.KeyValueStorage() {
              @Override
              public String getItem(String key) {
                throw new IllegalStateException("storage blocked");
              }

              @Override
              public void setItem(String key, String value) {
                throw new IllegalStateException("storage blocked");
              }
            });
    RecordingStorage malformedStorage = new RecordingStorage();
    malformedStorage.value = "not-a-result";

    assertTrue(unavailable.load(LEVEL_ID).isEmpty());
    unavailable.save(LEVEL_ID, RESULT);
    assertTrue(TeaVMBestResultStore.using(malformedStorage).load(LEVEL_ID).isEmpty());
  }

  @Test
  void failedSavePreservesPreviouslyStoredResult() {
    RecordingStorage storage = new RecordingStorage();
    TeaVMBestResultStore store = TeaVMBestResultStore.using(storage);
    store.save(LEVEL_ID, RESULT);
    storage.failWrites = true;

    store.save(LEVEL_ID, new BestResult(Duration.ofSeconds(4L), 10));

    storage.failWrites = false;
    assertEquals(RESULT, store.load(LEVEL_ID).orElseThrow());
  }

  private static final class RecordingStorage implements TeaVMBestResultStore.KeyValueStorage {
    private String key;
    private String value;
    private boolean failWrites;

    @Override
    public String getItem(String requestedKey) {
      return requestedKey.equals(key) ? value : null;
    }

    @Override
    public void setItem(String requestedKey, String requestedValue) {
      if (failWrites) {
        throw new IllegalStateException("storage full");
      }
      key = requestedKey;
      value = requestedValue;
    }
  }
}
