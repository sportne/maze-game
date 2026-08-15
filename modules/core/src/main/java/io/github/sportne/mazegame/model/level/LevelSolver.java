package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Objects;

/** Immutable authoring data for one solver and its matching goal within a level. */
public record LevelSolver(
    GridPosition start, GridPosition goal, SolverBehavior behavior, long randomSeed) {
  /** Creates a solver definition with all required values present. */
  public LevelSolver {
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(goal, "goal");
    Objects.requireNonNull(behavior, "behavior");
    if (start.equals(goal)) {
      throw new IllegalArgumentException("solver start and goal must be different");
    }
  }
}
