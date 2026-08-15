package io.github.sportne.mazegame;

import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import java.time.Duration;
import java.util.List;
import java.util.OptionalLong;

/** Concise canonical level fixtures shared by focused unit tests. */
public final class TestLevels {
  private TestLevels() {}

  /** Creates a single-solver fixture using the project's conventional presentation pairings. */
  public static LevelDefinition singleSolverLevel(
      String id,
      String name,
      GridSize gridSize,
      GridPosition start,
      GridPosition goal,
      Duration buildTime,
      Duration targetSolveTime,
      Duration maximumSolveTime,
      Duration moveInterval,
      List<PlaceableCellSupply> supplies,
      SolverBehavior behavior,
      long randomSeed) {
    OptionalLong randomSeedValue =
        behavior == SolverBehavior.RANDOM ? OptionalLong.of(randomSeed) : OptionalLong.empty();
    SolverAppearance appearance =
        switch (behavior) {
          case RANDOM -> SolverAppearance.CLASSIC_MOUSE;
          case LEFT_PRIORITY -> SolverAppearance.SCOUT_SQUIRREL;
          case LEAST_VISITED -> SolverAppearance.TRACKER_RACCOON;
        };
    GoalType goalType =
        switch (behavior) {
          case RANDOM -> GoalType.CHEESE;
          case LEFT_PRIORITY -> GoalType.ACORN;
          case LEAST_VISITED -> GoalType.TRASH_CAN;
        };
    LevelSolver solver =
        new LevelSolver(start, goal, behavior, randomSeedValue, appearance, goalType);
    return new LevelDefinition(
        id,
        name,
        gridSize,
        buildTime,
        targetSolveTime,
        maximumSolveTime,
        moveInterval,
        supplies,
        List.of(solver));
  }
}
