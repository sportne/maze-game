package io.github.sportne.mazegame.state;

import io.github.sportne.mazegame.model.result.BestResult;
import java.util.Objects;
import java.util.Optional;

/** Persistence boundary for best results keyed by level id. */
public interface BestResultStore {
  /**
   * Loads the saved best result for a level.
   *
   * @param levelId stable level id
   * @return saved best result when one is available
   */
  Optional<BestResult> load(String levelId);

  /**
   * Saves the best result for a level.
   *
   * @param levelId stable level id
   * @param bestResult result to save
   */
  void save(String levelId, BestResult bestResult);

  /**
   * Returns a store that never loads or saves data.
   *
   * @return no-op best result store
   */
  static BestResultStore none() {
    return new BestResultStore() {
      @Override
      public Optional<BestResult> load(String levelId) {
        Objects.requireNonNull(levelId, "levelId");
        return Optional.empty();
      }

      @Override
      public void save(String levelId, BestResult bestResult) {
        Objects.requireNonNull(levelId, "levelId");
        Objects.requireNonNull(bestResult, "bestResult");
      }
    };
  }
}
