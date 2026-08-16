package io.github.sportne.mazegame.model.level;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static io.github.sportne.mazegame.TestMazeStates.withWalls;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.solver.RandomSolverSimulation;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import io.github.sportne.mazegame.model.solver.SolverSimulation;
import io.github.sportne.mazegame.model.solver.SolverSimulationFactory;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameResultEvaluator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

// Reproducible design evidence for Scout and the proposed Milestone 3 level.
final class MilestoneThreeLevelDesignTest {
  private static final Duration MOVE_INTERVAL = Duration.ofMillis(250);

  private static final LevelDefinition PROPOSED_LEVEL = Levels.levelThree();

  private static final Set<GridPosition> PASSING_LAYOUT_A =
      Set.of(
          new GridPosition(2, 2),
          new GridPosition(3, 1),
          new GridPosition(4, 0),
          new GridPosition(5, 1));

  private static final Set<GridPosition> PASSING_LAYOUT_B =
      Set.of(
          new GridPosition(2, 1),
          new GridPosition(3, 0),
          new GridPosition(3, 2),
          new GridPosition(3, 4),
          new GridPosition(4, 3));

  private static final Set<GridPosition> TIMEOUT_LAYOUT =
      Set.of(
          new GridPosition(3, 2),
          new GridPosition(3, 4),
          new GridPosition(4, 3),
          new GridPosition(5, 2),
          new GridPosition(5, 4),
          new GridPosition(6, 1));

  private static final Set<GridPosition> MILESTONE_TWO_PASSING_LAYOUT =
      Set.of(
          new GridPosition(1, 1),
          new GridPosition(1, 4),
          new GridPosition(2, 0),
          new GridPosition(2, 6),
          new GridPosition(3, 3),
          new GridPosition(3, 6),
          new GridPosition(4, 0),
          new GridPosition(5, 0),
          new GridPosition(5, 2));

  private static final Set<GridPosition> MILESTONE_TWO_PASSING_LAYOUT_B =
      Set.of(
          new GridPosition(1, 2),
          new GridPosition(1, 4),
          new GridPosition(1, 6),
          new GridPosition(2, 1),
          new GridPosition(3, 3),
          new GridPosition(3, 5),
          new GridPosition(5, 2),
          new GridPosition(5, 6),
          new GridPosition(6, 4));

  private static final Set<GridPosition> MILESTONE_TWO_TIMEOUT_LAYOUT =
      Set.of(
          new GridPosition(0, 5),
          new GridPosition(1, 6),
          new GridPosition(2, 1),
          new GridPosition(2, 4),
          new GridPosition(3, 5),
          new GridPosition(4, 1),
          new GridPosition(4, 2),
          new GridPosition(5, 6),
          new GridPosition(6, 1));

  private static final List<GridPosition> PASSING_TRACE_A =
      positions(
          6, 3, 6, 2, 6, 1, 6, 0, 5, 0, 6, 0, 6, 1, 6, 2, 5, 2, 4, 2, 4, 1, 4, 2, 3, 2, 3, 3, 2, 3,
          1, 3, 1, 2, 1, 1, 2, 1, 2, 0, 3, 0, 2, 0, 1, 0, 0, 0, 0, 1, 0, 2, 0, 3);

  private static final List<GridPosition> PASSING_TRACE_B =
      positions(
          6, 3, 6, 2, 6, 1, 6, 0, 5, 0, 4, 0, 4, 1, 3, 1, 4, 1, 4, 2, 5, 2, 5, 3, 5, 4, 4, 4, 4, 5,
          3, 5, 2, 5, 2, 4, 2, 3, 3, 3, 2, 3, 2, 2, 1, 2, 1, 1, 1, 0, 2, 0, 1, 0, 0, 0, 0, 1, 0, 2,
          0, 3);

  private static final List<GridPosition> TIMEOUT_TRACE =
      positions(
          6, 3, 6, 2, 6, 3, 5, 3, 6, 3, 6, 4, 6, 5, 5, 5, 4, 5, 4, 4, 4, 5, 3, 5, 2, 5, 2, 4, 2, 3,
          3, 3, 2, 3, 2, 2, 2, 1, 3, 1, 4, 1, 4, 2, 4, 1, 5, 1, 5, 0, 6, 0, 5, 0, 4, 0, 3, 0, 2, 0,
          1, 0, 0, 0, 0, 1);

  @Test
  void productionLevelMatchesEveryAcceptedAuthoredParameter() {
    assertEquals("milestone-3", PROPOSED_LEVEL.id());
    assertEquals("Level 3", PROPOSED_LEVEL.name());
    assertEquals(GridSize.square(7), PROPOSED_LEVEL.gridSize());
    assertEquals(position(6, 3), PROPOSED_LEVEL.primarySolver().start());
    assertEquals(position(0, 3), PROPOSED_LEVEL.primarySolver().goal());
    assertEquals(Duration.ofSeconds(25), PROPOSED_LEVEL.buildTime());
    assertEquals(Duration.ofSeconds(6), PROPOSED_LEVEL.targetSolveTime());
    assertEquals(Duration.ofSeconds(8), PROPOSED_LEVEL.maximumSolveTime());
    assertEquals(MOVE_INTERVAL, PROPOSED_LEVEL.solverMoveInterval());
    assertEquals(SolverBehavior.LEFT_PRIORITY, PROPOSED_LEVEL.primarySolver().behavior());
    assertTrue(PROPOSED_LEVEL.primarySolver().randomSeed().isEmpty());
    assertEquals(SolverAppearance.SCOUT_SQUIRREL, PROPOSED_LEVEL.primarySolver().appearance());
    assertEquals(GoalType.ACORN, PROPOSED_LEVEL.primarySolver().goalType());
    assertEquals(
        List.of(
            Levels.levelOne(),
            Levels.levelTwo(),
            PROPOSED_LEVEL,
            Levels.levelFour(),
            Levels.levelFive(),
            Levels.levelSix(),
            Levels.levelSeven(),
            Levels.levelEight(),
            Levels.levelNine()),
        Levels.catalog().levels());
  }

  @Test
  void mapsEveryHeadingToTheAcceptedAbsolutePriority() {
    assertEquals(
        List.of(Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH),
        Direction.NORTH.priority());
    assertEquals(
        List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST),
        Direction.EAST.priority());
    assertEquals(
        List.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH),
        Direction.SOUTH.priority());
    assertEquals(
        List.of(Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST),
        Direction.WEST.priority());
  }

  @Test
  void selectsTheFirstOpenCandidateForEveryObstructionCombination() {
    for (Direction heading : Direction.values()) {
      List<Direction> priority = heading.priority();
      for (int blockedMask = 0; blockedMask < 16; blockedMask++) {
        Set<Direction> blocked = blockedDirections(priority, blockedMask);
        Direction expected = firstOpenDirection(priority, blocked);

        assertEquals(expected, ReferenceScout.chooseDirection(heading, blocked));
      }
    }
  }

  @Test
  void reverseBecomesTheHeadingAndFullyBlockedDecisionHasNoDirection() {
    ReferenceScout scout =
        new ReferenceScout(
            withWalls(
                smallLevel(),
                Set.of(
                    new GridPosition(0, 0),
                    new GridPosition(0, 1),
                    new GridPosition(0, 2),
                    new GridPosition(1, 0),
                    new GridPosition(1, 2))));

    scout.update(MOVE_INTERVAL);
    assertEquals(new GridPosition(2, 1), scout.result().position());
    assertEquals(Direction.SOUTH, scout.heading());
    assertNull(ReferenceScout.chooseDirection(Direction.NORTH, Set.of(Direction.values())));
  }

  @Test
  void boundariesAndRepeatedCorridorsUseTheSameRelativeRule() {
    ReferenceScout scout = new ReferenceScout(MazeState.empty(PROPOSED_LEVEL));

    scout.update(Duration.ofSeconds(3));

    assertEquals(
        List.of(
            position(6, 3),
            position(6, 2),
            position(6, 1),
            position(6, 0),
            position(5, 0),
            position(4, 0),
            position(3, 0),
            position(2, 0),
            position(1, 0),
            position(0, 0),
            position(0, 1),
            position(0, 2),
            position(0, 3)),
        scout.trace());
    assertEquals(SolverRunStatus.REACHED_GOAL, scout.result().status());
  }

  @Test
  void wholeAndChunkedUpdatesProduceTheSameDeterministicResultAndTrace() {
    MazeState maze = withWalls(PROPOSED_LEVEL, PASSING_LAYOUT_B);
    ReferenceScout whole = new ReferenceScout(maze);
    ReferenceScout chunked = new ReferenceScout(maze);

    SolverRunResult wholeResult = whole.update(PROPOSED_LEVEL.maximumSolveTime());
    for (int index = 0; index < 80; index++) {
      chunked.update(Duration.ofMillis(100));
    }

    assertEquals(wholeResult, chunked.result());
    assertEquals(whole.trace(), chunked.trace());
    assertEquals(wholeResult, new ReferenceScout(maze).update(Duration.ofSeconds(8)));
  }

  @Test
  void proposedLevelHasTwoSmallDistinctPassingLayouts() {
    assertPassingLayout(
        PASSING_LAYOUT_A,
        new SolverRunResult(
            PROPOSED_LEVEL.primarySolver().goal(),
            Duration.ofMillis(6500),
            26,
            SolverRunStatus.REACHED_GOAL),
        PASSING_TRACE_A);
    assertPassingLayout(
        PASSING_LAYOUT_B,
        new SolverRunResult(
            PROPOSED_LEVEL.primarySolver().goal(),
            Duration.ofMillis(7500),
            30,
            SolverRunStatus.REACHED_GOAL),
        PASSING_TRACE_B);
  }

  @Test
  void productionScoutMatchesEveryAcceptedThirdLevelFixture() {
    assertProductionTrace(
        PASSING_LAYOUT_A,
        PASSING_TRACE_A,
        new SolverRunResult(
            PROPOSED_LEVEL.primarySolver().goal(),
            Duration.ofMillis(6500),
            26,
            SolverRunStatus.REACHED_GOAL));
    assertProductionTrace(
        PASSING_LAYOUT_B,
        PASSING_TRACE_B,
        new SolverRunResult(
            PROPOSED_LEVEL.primarySolver().goal(),
            Duration.ofMillis(7500),
            30,
            SolverRunStatus.REACHED_GOAL));
    assertProductionTrace(
        TIMEOUT_LAYOUT,
        TIMEOUT_TRACE,
        new SolverRunResult(
            new GridPosition(0, 1), Duration.ofSeconds(8), 32, SolverRunStatus.TIMED_OUT));
  }

  @Test
  void emptyAndNaiveLayoutsFailWhileALongDetourTimesOut() {
    MazeState empty = MazeState.empty(PROPOSED_LEVEL);
    SolverRunResult emptyResult = new ReferenceScout(empty).update(Duration.ofSeconds(8));

    assertTrue(empty.hasPathFromStartToGoal());
    assertEquals(
        new SolverRunResult(
            PROPOSED_LEVEL.primarySolver().goal(),
            Duration.ofSeconds(3),
            12,
            SolverRunStatus.REACHED_GOAL),
        emptyResult);
    assertFalse(GameResultEvaluator.passed(GamePhase.RESULT, emptyResult, PROPOSED_LEVEL));

    MazeState timeoutMaze = withWalls(PROPOSED_LEVEL, TIMEOUT_LAYOUT);
    ReferenceScout timeout = new ReferenceScout(timeoutMaze);
    SolverRunResult timeoutResult = timeout.update(PROPOSED_LEVEL.maximumSolveTime());

    assertTrue(timeoutMaze.hasPathFromStartToGoal());
    assertEquals(
        new SolverRunResult(
            new GridPosition(0, 1), Duration.ofSeconds(8), 32, SolverRunStatus.TIMED_OUT),
        timeoutResult);
    assertEquals(TIMEOUT_TRACE, timeout.trace());
    assertTrue(GameResultEvaluator.passed(GamePhase.RESULT, timeoutResult, PROPOSED_LEVEL));

    LevelDefinition extendedLevel = levelWithMaximumSolveTime(Duration.ofSeconds(9));
    ReferenceScout extended = new ReferenceScout(withWalls(extendedLevel, TIMEOUT_LAYOUT));
    assertEquals(
        new SolverRunResult(
            extendedLevel.primarySolver().goal(),
            Duration.ofMillis(8500),
            34,
            SolverRunStatus.REACHED_GOAL),
        extended.update(extendedLevel.maximumSolveTime()));
    assertEquals(
        positions(0, 2, 0, 3),
        extended.trace().subList(TIMEOUT_TRACE.size(), extended.trace().size()));
  }

  @Test
  void scoutAndRandomProduceDifferentChallengesOnMilestoneTwoMazes() {
    LevelDefinition level = Levels.levelTwo();
    assertComparison(
        level,
        MILESTONE_TWO_PASSING_LAYOUT,
        new SolverRunResult(
            level.primarySolver().goal(),
            Duration.ofMillis(9500),
            38,
            SolverRunStatus.REACHED_GOAL),
        new SolverRunResult(
            level.primarySolver().goal(), Duration.ofSeconds(5), 20, SolverRunStatus.REACHED_GOAL));
    assertComparison(
        level,
        MILESTONE_TWO_PASSING_LAYOUT_B,
        new SolverRunResult(
            level.primarySolver().goal(),
            Duration.ofMillis(8500),
            34,
            SolverRunStatus.REACHED_GOAL),
        new SolverRunResult(
            level.primarySolver().goal(), Duration.ofSeconds(3), 12, SolverRunStatus.REACHED_GOAL));
    assertComparison(
        level,
        MILESTONE_TWO_TIMEOUT_LAYOUT,
        new SolverRunResult(
            new GridPosition(1, 2), Duration.ofSeconds(15), 60, SolverRunStatus.TIMED_OUT),
        new SolverRunResult(
            level.primarySolver().goal(),
            Duration.ofMillis(3500),
            14,
            SolverRunStatus.REACHED_GOAL));
  }

  private static void assertPassingLayout(
      Set<GridPosition> walls, SolverRunResult expectedResult, List<GridPosition> expectedTrace) {
    MazeState maze = withWalls(PROPOSED_LEVEL, walls);
    ReferenceScout first = new ReferenceScout(maze);
    ReferenceScout replay = new ReferenceScout(maze);

    assertTrue(maze.hasPathFromStartToGoal());
    assertEquals(expectedResult, first.update(PROPOSED_LEVEL.maximumSolveTime()));
    assertEquals(expectedResult, replay.update(PROPOSED_LEVEL.maximumSolveTime()));
    assertEquals(expectedTrace, first.trace());
    assertEquals(first.trace(), replay.trace());
    assertTrue(GameResultEvaluator.passed(GamePhase.RESULT, first.result(), PROPOSED_LEVEL));
  }

  private static void assertComparison(
      LevelDefinition level,
      Set<GridPosition> walls,
      SolverRunResult expectedRandom,
      SolverRunResult expectedScout) {
    MazeState maze = withWalls(level, walls);

    assertEquals(expectedRandom, new RandomSolverSimulation(maze).update(level.maximumSolveTime()));
    assertEquals(expectedScout, new ReferenceScout(maze).update(level.maximumSolveTime()));
  }

  private static void assertProductionTrace(
      Set<GridPosition> walls, List<GridPosition> expectedTrace, SolverRunResult expectedResult) {
    MazeState maze = withWalls(PROPOSED_LEVEL, walls);
    SolverSimulation simulation = SolverSimulationFactory.create(maze);
    List<GridPosition> trace = new ArrayList<>();
    trace.add(simulation.result().position());

    while (simulation.result().status() == SolverRunStatus.RUNNING) {
      trace.add(simulation.update(MOVE_INTERVAL).position());
    }

    assertEquals(expectedResult, simulation.result());
    assertEquals(expectedTrace, trace);
  }

  private static LevelDefinition levelWithMaximumSolveTime(Duration maximumSolveTime) {
    return new LevelDefinition(
        PROPOSED_LEVEL.id(),
        PROPOSED_LEVEL.name(),
        PROPOSED_LEVEL.gridSize(),
        PROPOSED_LEVEL.buildTime(),
        PROPOSED_LEVEL.targetSolveTime(),
        maximumSolveTime,
        PROPOSED_LEVEL.solverMoveInterval(),
        PROPOSED_LEVEL.placeableCellSupplies(),
        List.of(PROPOSED_LEVEL.primarySolver()));
  }

  private static LevelDefinition smallLevel() {
    return singleSolverLevel(
        "scout-reference",
        "Scout Reference",
        GridSize.square(3),
        new GridPosition(1, 1),
        new GridPosition(2, 2),
        Duration.ofSeconds(1),
        Duration.ofSeconds(1),
        Duration.ofSeconds(2),
        MOVE_INTERVAL,
        PlaceableCellSupply.unlimitedWallsOnly(),
        SolverBehavior.LEFT_PRIORITY,
        1L);
  }

  private static Set<Direction> blockedDirections(List<Direction> priority, int blockedMask) {
    java.util.EnumSet<Direction> blocked = java.util.EnumSet.noneOf(Direction.class);
    for (int index = 0; index < priority.size(); index++) {
      if ((blockedMask & (1 << index)) != 0) {
        blocked.add(priority.get(index));
      }
    }
    return blocked;
  }

  private static Direction firstOpenDirection(List<Direction> priority, Set<Direction> blocked) {
    return priority.stream()
        .filter(direction -> !blocked.contains(direction))
        .findFirst()
        .orElse(null);
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

  // Cardinal movement used only by this test-side reference model.
  private enum Direction {
    NORTH(-1, 0),
    EAST(0, 1),
    SOUTH(1, 0),
    WEST(0, -1);

    private final int rowChange;
    private final int columnChange;

    Direction(int rowChange, int columnChange) {
      this.rowChange = rowChange;
      this.columnChange = columnChange;
    }

    private List<Direction> priority() {
      return switch (this) {
        case NORTH -> List.of(WEST, NORTH, EAST, SOUTH);
        case EAST -> List.of(NORTH, EAST, SOUTH, WEST);
        case SOUTH -> List.of(EAST, SOUTH, WEST, NORTH);
        case WEST -> List.of(SOUTH, WEST, NORTH, EAST);
      };
    }

    private GridPosition move(GridPosition origin) {
      return new GridPosition(origin.row() + rowChange, origin.column() + columnChange);
    }
  }

  // Small deterministic oracle intentionally kept out of production until M3-03.
  private static final class ReferenceScout {
    private final MazeState mazeState;
    private final List<GridPosition> trace = new ArrayList<>();
    private GridPosition position;
    private Direction heading = Direction.NORTH;
    private Duration elapsedTime = Duration.ZERO;
    private Duration accumulatedTime = Duration.ZERO;
    private int moveCount;
    private SolverRunStatus status = SolverRunStatus.RUNNING;

    private ReferenceScout(MazeState mazeState) {
      this.mazeState = Objects.requireNonNull(mazeState, "mazeState");
      position = mazeState.levelDefinition().primarySolver().start();
      trace.add(position);
    }

    private SolverRunResult update(Duration deltaTime) {
      Objects.requireNonNull(deltaTime, "deltaTime");
      Duration remaining = deltaTime;
      while (status == SolverRunStatus.RUNNING && !remaining.isZero()) {
        Duration step = min(remaining, min(timeUntilMove(), timeUntilTimeout()));
        elapsedTime = elapsedTime.plus(step);
        accumulatedTime = accumulatedTime.plus(step);
        remaining = remaining.minus(step);
        if (accumulatedTime.compareTo(mazeState.levelDefinition().solverMoveInterval()) >= 0) {
          accumulatedTime = Duration.ZERO;
          moveOnce();
        }
        updateStatus();
      }
      return result();
    }

    private void moveOnce() {
      Direction direction = chooseDirection(heading, blockedDirections());
      if (direction != null) {
        position = direction.move(position);
        heading = direction;
      }
      moveCount++;
      trace.add(position);
    }

    private Set<Direction> blockedDirections() {
      Map<Direction, GridPosition> candidates = new EnumMap<>(Direction.class);
      for (Direction direction : Direction.values()) {
        candidates.put(direction, direction.move(position));
      }
      java.util.EnumSet<Direction> blocked = java.util.EnumSet.noneOf(Direction.class);
      candidates.forEach(
          (direction, candidate) -> {
            if (!candidate.isWithin(mazeState.levelDefinition().gridSize())
                || mazeState.placedCells().containsKey(candidate)) {
              blocked.add(direction);
            }
          });
      return blocked;
    }

    private static Direction chooseDirection(Direction heading, Set<Direction> blocked) {
      return heading.priority().stream()
          .filter(direction -> !blocked.contains(direction))
          .findFirst()
          .orElse(null);
    }

    private void updateStatus() {
      if (position.equals(mazeState.levelDefinition().primarySolver().goal())) {
        status = SolverRunStatus.REACHED_GOAL;
      } else if (elapsedTime.compareTo(mazeState.levelDefinition().maximumSolveTime()) >= 0) {
        status = SolverRunStatus.TIMED_OUT;
      }
    }

    private Duration timeUntilMove() {
      return mazeState.levelDefinition().solverMoveInterval().minus(accumulatedTime);
    }

    private Duration timeUntilTimeout() {
      return mazeState.levelDefinition().maximumSolveTime().minus(elapsedTime);
    }

    private static Duration min(Duration first, Duration second) {
      return first.compareTo(second) <= 0 ? first : second;
    }

    private SolverRunResult result() {
      return new SolverRunResult(position, elapsedTime, moveCount, status);
    }

    private Direction heading() {
      return heading;
    }

    private List<GridPosition> trace() {
      return List.copyOf(trace);
    }
  }
}
