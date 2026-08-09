package io.github.sportne.mazegame.model.mouse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.MouseBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

final class SlowFloorSimulationTest {
  private static final Duration MOVE_INTERVAL = Duration.ofMillis(250);
  private static final Duration TIMEOUT = Duration.ofMillis(6500);
  private static final GridPosition START = position(6, 3);
  private static final GridPosition CHEESE = position(0, 3);

  private static final Set<GridPosition> PASSING_WALLS =
      Set.of(position(0, 0), position(1, 1), position(2, 2));
  private static final Set<GridPosition> PASSING_SLOW_FLOORS =
      Set.of(position(6, 2), position(6, 1), position(6, 0));
  private static final Set<GridPosition> TIMEOUT_WALLS =
      Set.of(position(0, 1), position(1, 2), position(2, 1));
  private static final Set<GridPosition> TIMEOUT_SLOW_FLOORS =
      Set.of(position(1, 0), position(2, 0), position(1, 3));

  private static final List<GridPosition> PASSING_TRACE =
      positions(
          6, 3, 6, 2, 6, 1, 6, 0, 5, 0, 4, 0, 3, 0, 2, 0, 1, 0, 2, 0, 2, 1, 3, 1, 3, 2, 3, 3, 2, 3,
          1, 3, 1, 2, 0, 2, 0, 1, 0, 2, 0, 3);

  @Test
  void productionScoutReproducesEveryAcceptedLevelFourTimingFixture() {
    LevelDefinition level = level(MouseBehavior.LEFT_PRIORITY, 53L, TIMEOUT);

    assertEquals(
        new MouseRunResult(CHEESE, Duration.ofSeconds(3), 12, MouseRunStatus.REACHED_CHEESE),
        run(maze(level, Set.of(), Set.of())));
    assertEquals(
        new MouseRunResult(CHEESE, Duration.ofSeconds(5), 20, MouseRunStatus.REACHED_CHEESE),
        run(maze(level, PASSING_WALLS, Set.of())));
    assertEquals(
        new MouseRunResult(CHEESE, Duration.ofMillis(3750), 12, MouseRunStatus.REACHED_CHEESE),
        run(maze(level, Set.of(), PASSING_SLOW_FLOORS)));
    assertEquals(
        new MouseRunResult(CHEESE, Duration.ofMillis(5750), 20, MouseRunStatus.REACHED_CHEESE),
        run(maze(level, PASSING_WALLS, PASSING_SLOW_FLOORS)));
    assertEquals(
        new MouseRunResult(position(1, 3), TIMEOUT, 19, MouseRunStatus.TIMED_OUT),
        run(maze(level, TIMEOUT_WALLS, TIMEOUT_SLOW_FLOORS)));
  }

  @Test
  void scoutSlowFloorsPreserveLiteralRouteAndAddOnlyDocumentedWaits() {
    LevelDefinition level = level(MouseBehavior.LEFT_PRIORITY, 53L, TIMEOUT);
    Trace normal = trace(maze(level, PASSING_WALLS, Set.of()));
    Trace slowed = trace(maze(level, PASSING_WALLS, PASSING_SLOW_FLOORS));

    assertEquals(PASSING_TRACE, normal.positions());
    assertEquals(normal.positions(), slowed.positions());
    assertEquals(normal.result().moveCount(), slowed.result().moveCount());
    assertEquals(
        List.of(
            250L, 750L, 1250L, 1750L, 2000L, 2250L, 2500L, 2750L, 3000L, 3250L, 3500L, 3750L, 4000L,
            4250L, 4500L, 4750L, 5000L, 5250L, 5500L, 5750L),
        slowed.decisionTimesMillis());
  }

  @Test
  void seededRandomSlowFloorPreservesRouteAndMoveCount() {
    LevelDefinition level = level(MouseBehavior.RANDOM, 41L, TIMEOUT);
    Trace normal = trace(maze(level, PASSING_WALLS, Set.of()));
    Trace slowed = trace(maze(level, PASSING_WALLS, PASSING_SLOW_FLOORS));

    assertEquals(
        positions(
            6, 3, 6, 2, 5, 2, 4, 2, 5, 2, 4, 2, 3, 2, 3, 1, 4, 1, 4, 2, 4, 3, 4, 4, 3, 4, 2, 4, 1,
            4, 0, 4, 0, 3),
        slowed.positions());
    assertEquals(normal.positions(), slowed.positions());
    assertEquals(normal.result().moveCount(), slowed.result().moveCount());
    assertEquals(
        Duration.ofMillis(250), slowed.result().elapsedTime().minus(normal.result().elapsedTime()));
    assertEquals(
        new MouseRunResult(CHEESE, Duration.ofMillis(4250), 16, MouseRunStatus.REACHED_CHEESE),
        slowed.result());
  }

  @Test
  void productionRandomReproducesSeedFiftyThreeDesignFixtures() {
    LevelDefinition level = level(MouseBehavior.RANDOM, 53L, TIMEOUT);

    assertEquals(
        new MouseRunResult(position(4, 3), TIMEOUT, 26, MouseRunStatus.TIMED_OUT),
        run(maze(level, Set.of(), Set.of())));
    assertEquals(
        new MouseRunResult(position(4, 4), TIMEOUT, 25, MouseRunStatus.TIMED_OUT),
        run(maze(level, PASSING_WALLS, PASSING_SLOW_FLOORS)));
  }

  @ParameterizedTest
  @MethodSource("behaviors")
  void wholeFractionalOversizedAndChunkedUpdatesAreEquivalent(MouseBehavior behavior) {
    long seed = behavior == MouseBehavior.RANDOM ? 41L : 53L;
    MazeState maze = maze(level(behavior, seed, TIMEOUT), PASSING_WALLS, PASSING_SLOW_FLOORS);
    MouseSimulation whole = MouseSimulationFactory.create(maze);
    MouseSimulation fractional = MouseSimulationFactory.create(maze);
    MouseSimulation chunked = MouseSimulationFactory.create(maze);

    MouseRunResult wholeResult = whole.update(Duration.ofSeconds(20));
    fractional.update(Duration.ofMillis(125));
    fractional.update(Duration.ofMillis(625));
    fractional.update(Duration.ofSeconds(20));
    for (int update = 0; update < 65; update++) {
      chunked.update(Duration.ofMillis(100));
    }

    assertEquals(wholeResult, fractional.result());
    assertEquals(wholeResult, chunked.result());
  }

  @ParameterizedTest
  @MethodSource("behaviors")
  void replayOfCompletedSlowFloorMazeIsDeterministic(MouseBehavior behavior) {
    long seed = behavior == MouseBehavior.RANDOM ? 41L : 53L;
    MazeState maze = maze(level(behavior, seed, TIMEOUT), PASSING_WALLS, PASSING_SLOW_FLOORS);

    assertEquals(
        MouseSimulationFactory.create(maze).update(TIMEOUT),
        MouseSimulationFactory.create(maze).update(TIMEOUT));
  }

  @Test
  void timeoutDuringPendingSlowWaitEndsWithoutAnotherDecision() {
    LevelDefinition level = corridorLevel(Duration.ofMillis(500));
    MazeState maze =
        maze(
            level,
            Set.of(position(2, 0), position(2, 2), position(1, 0), position(1, 2)),
            Set.of(position(1, 1)));

    assertEquals(
        new MouseRunResult(position(1, 1), Duration.ofMillis(500), 1, MouseRunStatus.TIMED_OUT),
        run(maze));
  }

  @Test
  void cheeseArrivalEndsImmediatelyAfterACompletedSlowWait() {
    LevelDefinition level = corridorLevel(Duration.ofSeconds(1));
    MazeState maze =
        maze(
            level,
            Set.of(position(2, 0), position(2, 2), position(1, 0), position(1, 2)),
            Set.of(position(1, 1)));

    assertEquals(
        new MouseRunResult(
            level.cheese(), Duration.ofMillis(750), 2, MouseRunStatus.REACHED_CHEESE),
        run(maze));
  }

  @Test
  void cheeseArrivalStillWinsANormalDecisionAtTheTimeoutBoundary() {
    LevelDefinition level =
        new LevelDefinition(
            "cheese-boundary",
            "Cheese Boundary",
            GridSize.square(3),
            position(1, 1),
            position(0, 1),
            Duration.ofSeconds(1),
            MOVE_INTERVAL,
            MOVE_INTERVAL,
            MOVE_INTERVAL,
            infiniteSupplies(),
            MouseBehavior.LEFT_PRIORITY,
            1L);
    MazeState maze = maze(level, Set.of(position(1, 0), position(1, 2)), Set.of());

    assertEquals(
        new MouseRunResult(level.cheese(), MOVE_INTERVAL, 1, MouseRunStatus.REACHED_CHEESE),
        run(maze));
  }

  private static MouseRunResult run(MazeState maze) {
    return MouseSimulationFactory.create(maze).update(maze.levelDefinition().maximumSolveTime());
  }

  private static Trace trace(MazeState maze) {
    MouseSimulation simulation = MouseSimulationFactory.create(maze);
    List<GridPosition> positions = new ArrayList<>();
    List<Long> decisionTimes = new ArrayList<>();
    positions.add(simulation.result().position());
    int previousMoveCount = 0;
    while (simulation.result().status() == MouseRunStatus.RUNNING) {
      MouseRunResult result = simulation.update(MOVE_INTERVAL);
      if (result.moveCount() != previousMoveCount) {
        positions.add(result.position());
        decisionTimes.add(result.elapsedTime().toMillis());
        previousMoveCount = result.moveCount();
      }
    }
    return new Trace(simulation.result(), List.copyOf(positions), List.copyOf(decisionTimes));
  }

  private static MazeState maze(
      LevelDefinition level, Set<GridPosition> walls, Set<GridPosition> slowFloors) {
    Map<GridPosition, PlaceableCellType> cells = new HashMap<>();
    walls.forEach(position -> cells.put(position, PlaceableCellType.WALL));
    slowFloors.forEach(position -> cells.put(position, PlaceableCellType.SLOW_FLOOR));
    return new MazeState(level, cells);
  }

  private static LevelDefinition level(
      MouseBehavior behavior, long seed, Duration maximumSolveTime) {
    return new LevelDefinition(
        "slow-floor-" + behavior.name().toLowerCase(java.util.Locale.ROOT),
        "Slow Floor " + behavior,
        GridSize.square(7),
        START,
        CHEESE,
        Duration.ofSeconds(25),
        Duration.ofMillis(5500),
        maximumSolveTime,
        MOVE_INTERVAL,
        infiniteSupplies(),
        behavior,
        seed);
  }

  private static LevelDefinition corridorLevel(Duration maximumSolveTime) {
    return new LevelDefinition(
        "slow-corridor",
        "Slow Corridor",
        GridSize.square(3),
        position(2, 1),
        position(0, 1),
        Duration.ofSeconds(1),
        Duration.ofMillis(250),
        maximumSolveTime,
        MOVE_INTERVAL,
        infiniteSupplies(),
        MouseBehavior.LEFT_PRIORITY,
        1L);
  }

  private static List<PlaceableCellSupply> infiniteSupplies() {
    return List.of(
        new PlaceableCellSupply(PlaceableCellType.WALL, CellSupply.infinite()),
        new PlaceableCellSupply(PlaceableCellType.SLOW_FLOOR, CellSupply.infinite()));
  }

  private static Stream<MouseBehavior> behaviors() {
    return Stream.of(MouseBehavior.values());
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }

  private static List<GridPosition> positions(int... coordinatePairs) {
    List<GridPosition> positions = new ArrayList<>();
    for (int index = 0; index < coordinatePairs.length; index += 2) {
      positions.add(position(coordinatePairs[index], coordinatePairs[index + 1]));
    }
    return List.copyOf(positions);
  }

  private record Trace(
      MouseRunResult result, List<GridPosition> positions, List<Long> decisionTimesMillis) {}
}
