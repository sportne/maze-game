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
    boolean random = behavior == SolverBehavior.RANDOM;
    LevelSolver solver =
        new LevelSolver(
            start,
            goal,
            behavior,
            random ? OptionalLong.of(randomSeed) : OptionalLong.empty(),
            random ? SolverAppearance.CLASSIC_MOUSE : SolverAppearance.SCOUT_SQUIRREL,
            random ? GoalType.CHEESE : GoalType.ACORN);
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
