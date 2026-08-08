package io.github.sportne.mazegame.state;

import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.result.BestResult;
import java.util.Objects;

/** Read-only progression state for one authored level. */
public record LevelProgress(
    LevelDefinition levelDefinition, boolean unlocked, BestResult bestResult) {
  /** Validates required progression data. */
  public LevelProgress {
    Objects.requireNonNull(levelDefinition, "levelDefinition");
  }
}
