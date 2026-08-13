package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.maze.MazeEditResult;
import io.github.sportne.mazegame.model.maze.MazeEditStatus;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.mouse.MouseRunStatus;
import io.github.sportne.mazegame.model.mouse.MouseSimulation;
import io.github.sportne.mazegame.model.mouse.MouseSimulationFactory;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Production authoring and deterministic fixture coverage for the fourth level. */
final class MilestoneFourLevelTest {
  private static final LevelDefinition LEVEL = Levels.milestoneFour();
  private static final Set<GridPosition> PASSING_WALLS = positions(0, 0, 1, 1, 2, 2);
  private static final Set<GridPosition> WALL_ONLY_FALLBACK = positions(3, 1, 0, 0, 1, 1, 2, 2);
  private static final Set<GridPosition> PASSING_SLOW_FLOORS = positions(6, 2, 6, 1, 6, 0);
  private static final Set<GridPosition> TIMEOUT_WALLS = positions(0, 1, 1, 2, 2, 1);
  private static final Set<GridPosition> TIMEOUT_SLOW_FLOORS = positions(1, 0, 2, 0, 1, 3);

  @Test
  void catalogsTheExactAcceptedFourthLevelAfterTheReleasedLevels() {
    assertEquals(
        List.of("milestone-1", "milestone-2", "milestone-3", "milestone-4"),
        Levels.catalog().levels().stream().map(LevelDefinition::id).toList());
    assertEquals("Milestone 4", LEVEL.name());
    assertEquals(GridSize.square(7), LEVEL.gridSize());
    assertEquals(position(6, 3), LEVEL.mouseStart());
    assertEquals(position(0, 3), LEVEL.cheese());
    assertEquals(Duration.ofSeconds(25), LEVEL.buildTime());
    assertEquals(Duration.ofMillis(5500), LEVEL.targetSolveTime());
    assertEquals(Duration.ofMillis(6500), LEVEL.maximumSolveTime());
    assertEquals(Duration.ofMillis(250), LEVEL.mouseMoveInterval());
    assertEquals(CellSupply.finite(4), LEVEL.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(3), LEVEL.supplyFor(PlaceableCellType.SLOW_FLOOR));
    assertEquals(MouseBehavior.LEFT_PRIORITY, LEVEL.mouseBehavior());
    assertEquals(53L, LEVEL.randomSeed());
  }

  @Test
  void emptyThreeWallAndSlowOnlyProductionFixturesFailWhileFourWallAndCombinedPass() {
    assertRun(
        MazeState.empty(LEVEL),
        new MouseRunResult(
            LEVEL.cheese(), Duration.ofSeconds(3), 12, MouseRunStatus.REACHED_CHEESE),
        false);
    assertRun(
        maze(PASSING_WALLS, Set.of()),
        new MouseRunResult(
            LEVEL.cheese(), Duration.ofSeconds(5), 20, MouseRunStatus.REACHED_CHEESE),
        false);
    assertRun(
        maze(Set.of(), PASSING_SLOW_FLOORS),
        new MouseRunResult(
            LEVEL.cheese(), Duration.ofMillis(3750), 12, MouseRunStatus.REACHED_CHEESE),
        false);

    MazeState wallOnlyFallback = maze(WALL_ONLY_FALLBACK, Set.of());
    assertEquals(CellSupply.finite(0), wallOnlyFallback.remainingSupply(PlaceableCellType.WALL));
    assertRun(
        wallOnlyFallback,
        new MouseRunResult(
            LEVEL.cheese(), Duration.ofSeconds(6), 24, MouseRunStatus.REACHED_CHEESE),
        true);

    MazeState passing = maze(PASSING_WALLS, PASSING_SLOW_FLOORS);
    assertEquals(CellSupply.finite(1), passing.remainingSupply(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(0), passing.remainingSupply(PlaceableCellType.SLOW_FLOOR));
    assertRun(
        passing,
        new MouseRunResult(
            LEVEL.cheese(), Duration.ofMillis(5750), 20, MouseRunStatus.REACHED_CHEESE),
        true);
  }

  @Test
  void replacementAndMovementReachTheSameAcceptedProductionOutcome() {
    MazeState replacement = MazeState.empty(LEVEL);
    replacement =
        accepted(replacement.placeOrReplace(PlaceableCellType.SLOW_FLOOR, position(0, 0)));
    MazeEditResult replaced = replacement.placeOrReplace(PlaceableCellType.WALL, position(0, 0));
    assertEquals(MazeEditStatus.REPLACED, replaced.status());
    replacement = accepted(replaced);
    replacement =
        placeAll(replacement, PlaceableCellType.WALL, Set.of(position(1, 1), position(2, 2)));
    replacement = placeAll(replacement, PlaceableCellType.SLOW_FLOOR, PASSING_SLOW_FLOORS);

    MazeState movement =
        maze(Set.of(position(0, 1), position(1, 1), position(2, 2)), PASSING_SLOW_FLOORS);
    MazeEditResult moved = movement.move(position(0, 1), position(0, 0));
    assertEquals(MazeEditStatus.MOVED, moved.status());
    movement = accepted(moved);

    assertEquals(replacement, movement);
    assertRun(
        movement,
        new MouseRunResult(
            LEVEL.cheese(), Duration.ofMillis(5750), 20, MouseRunStatus.REACHED_CHEESE),
        true);
  }

  @Test
  void timeoutFixtureStopsDuringTheFinalSlowFloorWait() {
    assertRun(
        maze(TIMEOUT_WALLS, TIMEOUT_SLOW_FLOORS),
        new MouseRunResult(position(1, 3), Duration.ofMillis(6500), 19, MouseRunStatus.TIMED_OUT),
        true);
  }

  private static void assertRun(MazeState maze, MouseRunResult expected, boolean expectedPass) {
    MouseSimulation simulation = MouseSimulationFactory.create(maze);
    MouseRunResult result = simulation.update(LEVEL.maximumSolveTime());

    assertEquals(expected, result);
    assertEquals(
        expectedPass,
        result.status() == MouseRunStatus.TIMED_OUT
            || result.elapsedTime().compareTo(LEVEL.targetSolveTime()) > 0);
  }

  private static MazeState maze(Set<GridPosition> walls, Set<GridPosition> slowFloors) {
    MazeState maze = MazeState.empty(LEVEL);
    maze = placeAll(maze, PlaceableCellType.WALL, walls);
    return placeAll(maze, PlaceableCellType.SLOW_FLOOR, slowFloors);
  }

  private static MazeState placeAll(
      MazeState maze, PlaceableCellType type, Set<GridPosition> positions) {
    MazeState updated = maze;
    for (GridPosition position : positions) {
      updated = accepted(updated.placeOrReplace(type, position));
    }
    return updated;
  }

  private static MazeState accepted(MazeEditResult result) {
    assertTrue(result.accepted(), result.status().toString());
    return result.mazeState();
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }

  private static Set<GridPosition> positions(int... coordinates) {
    java.util.HashSet<GridPosition> positions = new java.util.HashSet<>();
    for (int index = 0; index < coordinates.length; index += 2) {
      positions.add(position(coordinates[index], coordinates[index + 1]));
    }
    return Set.copyOf(positions);
  }
}
