package io.github.sportne.mazegame.model.solver;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.FixedCell;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class LineOfSightSolverSimulationTest {
  private static final Duration MOVE_INTERVAL = Duration.ofMillis(250);

  @Test
  void detectsClearGoalLinesInEveryDirection() {
    GridPosition center = position(2, 2);

    assertEquals(CardinalDirection.NORTH, visible(center, position(0, 2), Set.of()));
    assertEquals(CardinalDirection.SOUTH, visible(center, position(4, 2), Set.of()));
    assertEquals(CardinalDirection.WEST, visible(center, position(2, 0), Set.of()));
    assertEquals(CardinalDirection.EAST, visible(center, position(2, 4), Set.of()));
    assertNull(visible(center, position(0, 4), Set.of()));
    assertNull(visible(center, center, Set.of()));
  }

  @Test
  void everyCellBetweenSolverAndGoalMustBeOpen() {
    GridPosition start = position(2, 0);
    GridPosition goal = position(2, 4);

    for (GridPosition obstruction : List.of(position(2, 1), position(2, 2), position(2, 3), goal)) {
      assertNull(visible(start, goal, Set.of(obstruction)));
    }
    assertEquals(CardinalDirection.EAST, visible(start, goal, Set.of(position(1, 2))));
  }

  @Test
  void followsAVisibleGoalWithoutConsumingRandomChoices() {
    LevelDefinition level = level(position(4, 2), position(0, 2), 17L);
    LineOfSightSolverSimulation seeker = new LineOfSightSolverSimulation(MazeState.empty(level));

    assertEquals(
        new SolverRunResult(position(0, 2), Duration.ofSeconds(1), 4, SolverRunStatus.REACHED_GOAL),
        seeker.update(Duration.ofSeconds(10)));
    assertEquals(java.util.Optional.of(CardinalDirection.NORTH), seeker.lastDirection());
  }

  @Test
  void usesTheReleasedRandomDecisionOrderWhileGoalIsNotVisible() {
    LevelDefinition seekerLevel = level(position(2, 0), position(0, 2), 7L);
    LevelDefinition randomLevel =
        singleSolverLevel(
            "random-comparison",
            "Random Comparison",
            seekerLevel.gridSize(),
            seekerLevel.primarySolver().start(),
            seekerLevel.primarySolver().goal(),
            seekerLevel.buildTime(),
            seekerLevel.targetSolveTime(),
            seekerLevel.maximumSolveTime(),
            seekerLevel.solverMoveInterval(),
            seekerLevel.placeableCellSupplies(),
            SolverBehavior.RANDOM,
            7L);
    LineOfSightSolverSimulation seeker =
        new LineOfSightSolverSimulation(MazeState.empty(seekerLevel));
    RandomSolverSimulation random = new RandomSolverSimulation(MazeState.empty(randomLevel));

    assertEquals(random.update(MOVE_INTERVAL), seeker.update(MOVE_INTERVAL));
    assertEquals(random.lastDirection(), seeker.lastDirection());
  }

  @Test
  void placedAndFixedWallsBlockSightAndTriggerExploration() {
    GridPosition start = position(2, 0);
    GridPosition goal = position(2, 4);
    GridPosition obstruction = position(2, 2);
    LevelDefinition placedLevel = level(start, goal, 7L);
    MazeState placedMaze = new MazeState(placedLevel, Map.of(obstruction, PlaceableCellType.WALL));
    LevelDefinition fixedLevel = withFixedWall(placedLevel, obstruction);

    LineOfSightSolverSimulation placed = new LineOfSightSolverSimulation(placedMaze);
    LineOfSightSolverSimulation fixed =
        new LineOfSightSolverSimulation(MazeState.empty(fixedLevel));

    assertEquals(placed.update(MOVE_INTERVAL), fixed.update(MOVE_INTERVAL));
    assertEquals(placed.lastDirection(), fixed.lastDirection());
  }

  @Test
  void slowFloorDoesNotBlockSightButDelaysTheFollowingDecision() {
    LevelDefinition level = level(position(4, 2), position(0, 2), 17L);
    MazeState maze = new MazeState(level, Map.of(position(3, 2), PlaceableCellType.SLOW_FLOOR));
    LineOfSightSolverSimulation seeker = new LineOfSightSolverSimulation(maze);

    assertEquals(
        new SolverRunResult(
            position(0, 2), Duration.ofMillis(1250), 4, SolverRunStatus.REACHED_GOAL),
        seeker.update(Duration.ofSeconds(10)));
  }

  @Test
  void seededRunsAreStableAcrossReplayAndUpdateChunking() {
    LevelDefinition level = level(position(4, 0), position(0, 4), 53L);
    MazeState maze = MazeState.empty(level);
    LineOfSightSolverSimulation whole = new LineOfSightSolverSimulation(maze);
    LineOfSightSolverSimulation replay = new LineOfSightSolverSimulation(maze);
    LineOfSightSolverSimulation chunked = new LineOfSightSolverSimulation(maze);

    SolverRunResult expected = whole.update(Duration.ofSeconds(10));
    replay.update(Duration.ofSeconds(10));
    for (int index = 0; index < 100; index++) {
      chunked.update(Duration.ofMillis(100));
    }

    assertEquals(expected, replay.result());
    assertEquals(expected, chunked.result());
    assertEquals(whole.lastDirection(), replay.lastDirection());
    assertEquals(whole.lastDirection(), chunked.lastDirection());
  }

  @Test
  void goalMoveWinsAtTimeoutBoundaryAndNegativeDeltasAreRejected() {
    LevelDefinition level = level(position(1, 0), position(0, 0), 5L, MOVE_INTERVAL);
    LineOfSightSolverSimulation seeker = new LineOfSightSolverSimulation(MazeState.empty(level));

    assertThrows(IllegalArgumentException.class, () -> seeker.update(Duration.ofMillis(-1)));
    assertEquals(
        new SolverRunResult(position(0, 0), MOVE_INTERVAL, 1, SolverRunStatus.REACHED_GOAL),
        seeker.update(Duration.ofSeconds(1)));
    assertEquals(seeker.result(), seeker.update(Duration.ofSeconds(1)));
  }

  @Test
  void visibilityContractRejectsMissingInputs() {
    GridPosition position = position(1, 1);

    assertThrows(
        NullPointerException.class,
        () -> LineOfSightSolverSimulation.visibleGoalDirection(null, position, ignored -> true));
    assertThrows(
        NullPointerException.class,
        () -> LineOfSightSolverSimulation.visibleGoalDirection(position, null, ignored -> true));
    assertThrows(
        NullPointerException.class,
        () -> LineOfSightSolverSimulation.visibleGoalDirection(position, position(0, 1), null));
  }

  private static CardinalDirection visible(
      GridPosition start, GridPosition goal, Set<GridPosition> obstructions) {
    return LineOfSightSolverSimulation.visibleGoalDirection(
        start, goal, candidate -> !obstructions.contains(candidate));
  }

  private static LevelDefinition level(GridPosition start, GridPosition goal, long seed) {
    return level(start, goal, seed, Duration.ofSeconds(10));
  }

  private static LevelDefinition level(
      GridPosition start, GridPosition goal, long seed, Duration maximumSolveTime) {
    return singleSolverLevel(
        "line-of-sight",
        "Line of Sight",
        GridSize.square(5),
        start,
        goal,
        Duration.ofSeconds(1),
        MOVE_INTERVAL,
        maximumSolveTime,
        MOVE_INTERVAL,
        List.of(
            new PlaceableCellSupply(PlaceableCellType.WALL, CellSupply.infinite()),
            new PlaceableCellSupply(PlaceableCellType.SLOW_FLOOR, CellSupply.infinite())),
        SolverBehavior.LINE_OF_SIGHT,
        seed);
  }

  private static LevelDefinition withFixedWall(LevelDefinition source, GridPosition obstruction) {
    LevelSolver solver = source.primarySolver();
    return new LevelDefinition(
        source.id(),
        source.name(),
        source.gridSize(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.solverMoveInterval(),
        source.placeableCellSupplies(),
        List.of(new FixedCell(obstruction, FixedCellType.WALL)),
        List.of(solver));
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }
}
