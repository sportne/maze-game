package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Objects;

/** Immutable authoring data for one mouse and its matching goal within a level. */
public record LevelMouse(
    GridPosition start, GridPosition goal, MouseBehavior behavior, long randomSeed) {
  /** Creates a mouse definition with all required values present. */
  public LevelMouse {
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(goal, "goal");
    Objects.requireNonNull(behavior, "behavior");
    if (start.equals(goal)) {
      throw new IllegalArgumentException("mouse start and goal must be different");
    }
  }
}
