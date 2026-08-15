package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.maze.CellContent;
import io.github.sportne.mazegame.model.maze.MazeEditStatus;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import io.github.sportne.mazegame.model.solver.SolverSimulationFactory;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameSession;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Production authoring and independent-run coverage for the fifth level. */
final class MilestoneFiveLevelTest {
  private static final LevelDefinition LEVEL = Levels.milestoneFive();
  private static final LevelSolver RANDOM = LEVEL.solvers().get(0);
  private static final LevelSolver SCOUT = LEVEL.solvers().get(1);
  private static final GridPosition CHEESE = position(3, 3);
  private static final GridPosition ACORN = position(2, 4);
  private static final Map<GridPosition, PlaceableCellType> PASSING_CELLS =
      Map.of(
          position(0, 2), PlaceableCellType.WALL,
          position(3, 2), PlaceableCellType.WALL,
          position(6, 1), PlaceableCellType.WALL,
          position(1, 3), PlaceableCellType.WALL,
          position(4, 0), PlaceableCellType.WALL,
          position(6, 3), PlaceableCellType.SLOW_FLOOR,
          position(2, 2), PlaceableCellType.SLOW_FLOOR,
          position(2, 5), PlaceableCellType.SLOW_FLOOR,
          position(1, 2), PlaceableCellType.SLOW_FLOOR);

  @Test
  void catalogsBothSolversWithTheCheeseCenteredAndAcornDiagonal() {
    assertEquals("milestone-5", LEVEL.id());
    assertEquals("Level 5", LEVEL.name());
    assertEquals(GridSize.square(7), LEVEL.gridSize());
    assertEquals(Duration.ofSeconds(25), LEVEL.buildTime());
    assertEquals(Duration.ofSeconds(5), LEVEL.targetSolveTime());
    assertEquals(Duration.ofSeconds(10), LEVEL.maximumSolveTime());
    assertEquals(CellSupply.finite(5), LEVEL.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(4), LEVEL.supplyFor(PlaceableCellType.SLOW_FLOOR));

    assertEquals(new LevelSolver(position(6, 0), CHEESE, SolverBehavior.RANDOM, 23L), RANDOM);
    assertEquals(new LevelSolver(position(1, 4), ACORN, SolverBehavior.LEFT_PRIORITY, 53L), SCOUT);
    assertFalse(RANDOM.start().equals(SCOUT.start()));
    assertEquals(1, Math.abs(CHEESE.row() - ACORN.row()));
    assertEquals(1, Math.abs(CHEESE.column() - ACORN.column()));
    assertEquals(LEVEL, Levels.catalog().levels().get(4));
  }

  @Test
  void protectsBothStartsAndBothGoalsWhilePreservingBothRoutes() {
    MazeState maze = MazeState.empty(LEVEL);
    for (GridPosition protectedPosition :
        java.util.List.of(RANDOM.start(), RANDOM.goal(), SCOUT.start(), SCOUT.goal())) {
      assertTrue(maze.isProtected(protectedPosition));
      assertEquals(
          MazeEditStatus.REJECTED_PROTECTED_CELL,
          maze.placeOrReplace(PlaceableCellType.WALL, protectedPosition).status());
    }
    assertEquals(CellContent.GOAL, maze.cellContentAt(RANDOM.goal()));
    assertEquals(CellContent.GOAL, maze.cellContentAt(SCOUT.goal()));
    assertTrue(maze.hasPathFromStartToGoal());
  }

  @Test
  void emptyLayoutFailsQuicklyButTheAuthoredFixtureDelaysBothSolversPastTheTarget() {
    MazeState empty = MazeState.empty(LEVEL);
    assertEquals(
        new SolverRunResult(CHEESE, Duration.ofMillis(1500), 6, SolverRunStatus.REACHED_GOAL),
        run(empty, RANDOM));
    assertEquals(
        new SolverRunResult(ACORN, Duration.ofMillis(750), 3, SolverRunStatus.REACHED_GOAL),
        run(empty, SCOUT));

    MazeState passing = new MazeState(LEVEL, PASSING_CELLS);
    SolverRunResult randomResult = run(passing, RANDOM);
    SolverRunResult scoutResult = run(passing, SCOUT);
    assertEquals(
        new SolverRunResult(position(2, 5), Duration.ofSeconds(10), 39, SolverRunStatus.TIMED_OUT),
        randomResult);
    assertEquals(
        new SolverRunResult(ACORN, Duration.ofSeconds(9), 33, SolverRunStatus.REACHED_GOAL),
        scoutResult);
    assertTrue(randomResult.elapsedTime().compareTo(LEVEL.targetSolveTime()) > 0);
    assertTrue(scoutResult.elapsedTime().compareTo(LEVEL.targetSolveTime()) > 0);
  }

  @Test
  void sessionRunsBothSolversAndScoresTheWeakerDelay() {
    GameSession session =
        new GameSession(
            new LevelCatalog(java.util.List.of(LEVEL)), LEVEL.id(), BestResultStore.none());
    assertTrue(session.startLevel(LEVEL.id()));
    PASSING_CELLS.forEach(
        (position, type) -> {
          session.selectCellType(type);
          assertTrue(session.placeOrReplaceCell(position).orElseThrow().accepted());
        });

    session.startRun();
    assertEquals(2, session.solverRunResults().size());
    assertEquals(session.solverRunResults().get(0), session.solverRunResult());
    session.updateSolverRun(10.0F);

    assertEquals(GamePhase.RESULT, session.gamePhase());
    assertTrue(session.resultPassed());
    assertEquals(Duration.ofSeconds(10), session.solverRunResults().get(0).elapsedTime());
    assertEquals(Duration.ofSeconds(9), session.solverRunResults().get(1).elapsedTime());
    assertEquals(session.solverRunResults().get(0), session.solverRunResult());
    assertEquals(new BestResult(Duration.ofSeconds(9), 72), session.bestResult());
    assertFalse(session.hasNextLevel());

    java.util.List<SolverRunResult> firstRun = session.solverRunResults();
    session.replayRun();
    session.updateSolverRun(10.0F);
    assertEquals(firstRun, session.solverRunResults());
  }

  private static SolverRunResult run(MazeState maze, LevelSolver solver) {
    return SolverSimulationFactory.create(maze, solver).update(LEVEL.maximumSolveTime());
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }
}
