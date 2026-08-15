package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.maze.MazeEditStatus;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.mouse.MouseRunStatus;
import io.github.sportne.mazegame.model.mouse.MouseSimulationFactory;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameSession;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Production authoring and independent-run coverage for the fifth level. */
final class MilestoneFiveLevelTest {
  private static final LevelDefinition LEVEL = Levels.milestoneFive();
  private static final LevelMouse RANDOM = LEVEL.mice().get(0);
  private static final LevelMouse SCOUT = LEVEL.mice().get(1);
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
  void catalogsBothMiceWithTheCheeseCenteredAndAcornDiagonal() {
    assertEquals("milestone-5", LEVEL.id());
    assertEquals("Level 5", LEVEL.name());
    assertEquals(GridSize.square(7), LEVEL.gridSize());
    assertEquals(Duration.ofSeconds(25), LEVEL.buildTime());
    assertEquals(Duration.ofSeconds(5), LEVEL.targetSolveTime());
    assertEquals(Duration.ofSeconds(10), LEVEL.maximumSolveTime());
    assertEquals(CellSupply.finite(5), LEVEL.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(4), LEVEL.supplyFor(PlaceableCellType.SLOW_FLOOR));

    assertEquals(new LevelMouse(position(6, 0), CHEESE, MouseBehavior.RANDOM, 23L), RANDOM);
    assertEquals(new LevelMouse(position(1, 4), ACORN, MouseBehavior.LEFT_PRIORITY, 53L), SCOUT);
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
    assertTrue(maze.hasPathFromStartToCheese());
  }

  @Test
  void emptyLayoutFailsQuicklyButTheAuthoredFixtureDelaysBothMicePastTheTarget() {
    MazeState empty = MazeState.empty(LEVEL);
    assertEquals(
        new MouseRunResult(CHEESE, Duration.ofMillis(1500), 6, MouseRunStatus.REACHED_CHEESE),
        run(empty, RANDOM));
    assertEquals(
        new MouseRunResult(ACORN, Duration.ofMillis(750), 3, MouseRunStatus.REACHED_CHEESE),
        run(empty, SCOUT));

    MazeState passing = new MazeState(LEVEL, PASSING_CELLS);
    MouseRunResult randomResult = run(passing, RANDOM);
    MouseRunResult scoutResult = run(passing, SCOUT);
    assertEquals(
        new MouseRunResult(position(2, 5), Duration.ofSeconds(10), 39, MouseRunStatus.TIMED_OUT),
        randomResult);
    assertEquals(
        new MouseRunResult(ACORN, Duration.ofSeconds(9), 33, MouseRunStatus.REACHED_CHEESE),
        scoutResult);
    assertTrue(randomResult.elapsedTime().compareTo(LEVEL.targetSolveTime()) > 0);
    assertTrue(scoutResult.elapsedTime().compareTo(LEVEL.targetSolveTime()) > 0);
  }

  @Test
  void sessionRunsBothMiceAndScoresTheWeakerDelay() {
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
    assertEquals(2, session.mouseRunResults().size());
    session.updateMouseRun(10.0F);

    assertEquals(GamePhase.RESULT, session.gamePhase());
    assertTrue(session.resultPassed());
    assertEquals(Duration.ofSeconds(10), session.mouseRunResults().get(0).elapsedTime());
    assertEquals(Duration.ofSeconds(9), session.mouseRunResults().get(1).elapsedTime());
    assertEquals(new BestResult(Duration.ofSeconds(9), 72), session.bestResult());
    assertFalse(session.hasNextLevel());

    java.util.List<MouseRunResult> firstRun = session.mouseRunResults();
    session.replayRun();
    session.updateMouseRun(10.0F);
    assertEquals(firstRun, session.mouseRunResults());
  }

  private static MouseRunResult run(MazeState maze, LevelMouse mouse) {
    return MouseSimulationFactory.create(maze, mouse).update(LEVEL.maximumSolveTime());
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }
}
