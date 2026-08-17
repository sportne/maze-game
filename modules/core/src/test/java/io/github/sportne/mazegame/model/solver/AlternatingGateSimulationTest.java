package io.github.sportne.mazegame.model.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.FixedCell;
import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.CellContent;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

final class AlternatingGateSimulationTest {
  private static final GridPosition START = new GridPosition(2, 0);
  private static final GridPosition GATE = new GridPosition(1, 0);
  private static final GridPosition GOAL = new GridPosition(0, 0);

  @Test
  void gateStartsOpenAndUsesExactOneSecondPhaseBoundaries() {
    MazeState maze = maze(SolverBehavior.LEFT_PRIORITY);

    assertEquals(CellContent.ALTERNATING_GATE, maze.cellContentAt(GATE));
    assertTrue(maze.isTraversable(GATE), "topological validation treats an eventual gate as open");
    assertTrue(maze.isTraversableAt(GATE, Duration.ZERO));
    assertTrue(maze.isTraversableAt(GATE, Duration.ofMillis(999)));
    assertFalse(maze.isTraversableAt(GATE, Duration.ofSeconds(1)));
    assertFalse(maze.isTraversableAt(GATE, Duration.ofMillis(1999)));
    assertTrue(maze.isTraversableAt(GATE, Duration.ofSeconds(2)));
    assertThrows(
        IllegalArgumentException.class, () -> maze.isTraversableAt(GATE, Duration.ofMillis(-1)));
  }

  @Test
  void fixedAndPlayerPlacedGatesShareTheSameRuntimeEffect() {
    MazeState placed = maze(SolverBehavior.LEFT_PRIORITY);
    LevelSolver solver = placed.levelDefinition().primarySolver();
    LevelDefinition fixedLevel =
        new LevelDefinition(
            "fixed-alternating-gate-test",
            "Fixed Alternating Gate Test",
            placed.levelDefinition().gridSize(),
            placed.levelDefinition().buildTime(),
            placed.levelDefinition().targetSolveTime(),
            placed.levelDefinition().maximumSolveTime(),
            placed.levelDefinition().solverMoveInterval(),
            List.of(
                PlaceableCellSupply.finite(PlaceableCellType.WALL, 0),
                PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 0),
                PlaceableCellSupply.finite(PlaceableCellType.ALTERNATING_GATE, 0)),
            List.of(new FixedCell(GATE, FixedCellType.ALTERNATING_GATE)),
            List.of(),
            List.of(solver));
    MazeState fixed = MazeState.empty(fixedLevel);

    assertEquals(CellContent.ALTERNATING_GATE, fixed.cellContentAt(GATE));
    for (Duration elapsed : List.of(Duration.ZERO, Duration.ofSeconds(1), Duration.ofSeconds(2))) {
      assertEquals(placed.isTraversableAt(GATE, elapsed), fixed.isTraversableAt(GATE, elapsed));
    }
  }

  @ParameterizedTest
  @EnumSource(SolverBehavior.class)
  void everySolverWaitsWhenTheOnlyExitIsClosedAndEntersWhenItReopens(SolverBehavior behavior) {
    SolverSimulation simulation = SolverSimulationFactory.create(maze(behavior));

    assertEquals(
        new SolverRunResult(START, Duration.ofSeconds(1), 1, SolverRunStatus.RUNNING),
        simulation.update(Duration.ofSeconds(1)));
    assertEquals(
        new SolverRunResult(GATE, Duration.ofSeconds(2), 2, SolverRunStatus.RUNNING),
        simulation.update(Duration.ofSeconds(1)));
  }

  @Test
  void leavingAGateIsAllowedEvenWhenItsPhaseCloses() {
    SolverSimulation scout = SolverSimulationFactory.create(maze(SolverBehavior.LEFT_PRIORITY));

    assertEquals(
        new SolverRunResult(GOAL, Duration.ofSeconds(3), 3, SolverRunStatus.REACHED_GOAL),
        scout.update(Duration.ofSeconds(3)));
  }

  @ParameterizedTest
  @EnumSource(SolverBehavior.class)
  void wholeAndChunkedUpdatesObserveTheSameGatePhases(SolverBehavior behavior) {
    MazeState maze = maze(behavior);
    SolverSimulation whole = SolverSimulationFactory.create(maze);
    SolverSimulation chunked = SolverSimulationFactory.create(maze);

    SolverRunResult expected = whole.update(Duration.ofSeconds(3));
    for (int update = 0; update < 30; update++) {
      chunked.update(Duration.ofMillis(100));
    }

    assertEquals(expected, chunked.result());
    assertEquals(whole.lastDirection(), chunked.lastDirection());
    assertEquals(whole.decisionState(), chunked.decisionState());
  }

  private static MazeState maze(SolverBehavior behavior) {
    OptionalLong seed = behavior.requiresRandomSeed() ? OptionalLong.of(7L) : OptionalLong.empty();
    LevelDefinition level =
        new LevelDefinition(
            "alternating-gate-test-" + behavior.name().toLowerCase(java.util.Locale.ROOT),
            "Alternating Gate Test",
            new GridSize(3, 1),
            Duration.ofSeconds(10),
            Duration.ofSeconds(2),
            Duration.ofSeconds(5),
            Duration.ofSeconds(1),
            List.of(
                PlaceableCellSupply.finite(PlaceableCellType.WALL, 0),
                PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 0),
                PlaceableCellSupply.finite(PlaceableCellType.ALTERNATING_GATE, 1)),
            List.of(
                new LevelSolver(
                    START, GOAL, behavior, seed, SolverAppearance.CLASSIC_MOUSE, GoalType.CHEESE)));
    return new MazeState(level, Map.of(GATE, PlaceableCellType.ALTERNATING_GATE));
  }
}
