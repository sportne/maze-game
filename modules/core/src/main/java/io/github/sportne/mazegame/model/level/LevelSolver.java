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
  /** Compatibility constructor that supplies the released behavior-to-presentation mapping. */
  public LevelSolver(
      GridPosition start, GridPosition goal, SolverBehavior behavior, long randomSeed) {
    this(
        start,
        goal,
        behavior,
        behavior == SolverBehavior.RANDOM ? OptionalLong.of(randomSeed) : OptionalLong.empty(),
        behavior == SolverBehavior.RANDOM
            ? SolverAppearance.CLASSIC_MOUSE
            : SolverAppearance.SCOUT_SQUIRREL,
        behavior == SolverBehavior.RANDOM ? GoalType.CHEESE : GoalType.ACORN);
  }

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
    if (behavior == SolverBehavior.RANDOM && randomSeed.isEmpty()) {
      throw new IllegalArgumentException("random behavior requires a seed");
    }
    if (behavior != SolverBehavior.RANDOM && randomSeed.isPresent()) {
      throw new IllegalArgumentException("non-random behavior must not define a random seed");
    }
  }
}
