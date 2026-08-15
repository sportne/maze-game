package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Objects;
import java.util.OptionalLong;

/** Immutable authoring data for one solver and its matching goal within a level. */
public record LevelSolver(
    GridPosition start,
    GridPosition goal,
    SolverBehavior behavior,
    OptionalLong randomSeed,
    SolverAppearance appearance,
    GoalType goalType) {
  /** Creates a solver definition with all required values present. */
  public LevelSolver {
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(goal, "goal");
    Objects.requireNonNull(behavior, "behavior");
    Objects.requireNonNull(randomSeed, "randomSeed");
    Objects.requireNonNull(appearance, "appearance");
    Objects.requireNonNull(goalType, "goalType");
    if (start.equals(goal)) {
      throw new IllegalArgumentException("solver start and goal must be different");
    }
    if (behavior.requiresRandomSeed() && randomSeed.isEmpty()) {
      throw new IllegalArgumentException("seeded behavior requires a seed");
    }
    if (!behavior.requiresRandomSeed() && randomSeed.isPresent()) {
      throw new IllegalArgumentException("non-random behavior must not define a random seed");
    }
  }
}
