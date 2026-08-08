package io.github.sportne.mazegame.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.state.BestResultStore;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/** libGDX Preferences-backed best result store. */
public final class LibGdxBestResultStore implements BestResultStore {
  /** Preferences namespace for Maze Game best results. */
  private static final String PREFERENCES_NAME = "maze-game-best-results";

  /** Key suffix for elapsed milliseconds. */
  private static final String ELAPSED_MILLIS_SUFFIX = ".elapsedMillis";

  /** Key suffix for move count. */
  private static final String MOVE_COUNT_SUFFIX = ".moveCount";

  @Override
  public Optional<BestResult> load(String levelId) {
    Objects.requireNonNull(levelId, "levelId");
    try {
      Preferences preferences = preferences();
      if (preferences == null
          || !preferences.contains(elapsedMillisKey(levelId))
          || !preferences.contains(moveCountKey(levelId))) {
        return Optional.empty();
      }
      long elapsedMillis = preferences.getLong(elapsedMillisKey(levelId));
      int moveCount = preferences.getInteger(moveCountKey(levelId));
      return Optional.of(new BestResult(Duration.ofMillis(elapsedMillis), moveCount));
    } catch (RuntimeException exception) {
      return Optional.empty();
    }
  }

  @Override
  public void save(String levelId, BestResult bestResult) {
    Objects.requireNonNull(levelId, "levelId");
    Objects.requireNonNull(bestResult, "bestResult");
    try {
      Preferences preferences = preferences();
      if (preferences == null) {
        return;
      }
      preferences.putLong(elapsedMillisKey(levelId), bestResult.elapsedTime().toMillis());
      preferences.putInteger(moveCountKey(levelId), bestResult.moveCount());
      preferences.flush();
    } catch (RuntimeException exception) {
      // Persistence is best-effort on platforms where storage can be unavailable.
      return;
    }
  }

  private static Preferences preferences() {
    return Gdx.app == null ? null : Gdx.app.getPreferences(PREFERENCES_NAME);
  }

  private static String elapsedMillisKey(String levelId) {
    return levelId + ELAPSED_MILLIS_SUFFIX;
  }

  private static String moveCountKey(String levelId) {
    return levelId + MOVE_COUNT_SUFFIX;
  }
}
