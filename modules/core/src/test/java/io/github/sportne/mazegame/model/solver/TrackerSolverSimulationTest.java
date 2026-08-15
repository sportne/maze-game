package io.github.sportne.mazegame.model.solver;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class TrackerSolverSimulationTest {
  private static final Duration MOVE_INTERVAL = Duration.ofMillis(250);
  private static final GridPosition LOOP_START = position(0, 0);
  private static final GridPosition LOOP_GOAL = position(1, 2);
  private static final Set<GridPosition> LOOP_WALLS = Set.of(position(0, 2), position(1, 1));
  private static final List<GridPosition> LOOP_TRACE =
      List.of(
          LOOP_START,
          position(0, 1),
          LOOP_START,
          position(1, 0),
          position(2, 0),
          position(2, 1),
          position(2, 2),
          LOOP_GOAL);

  @Test
  void decisionTablePrefersVisitsThenGoalDistanceThenAbsoluteDirection() {
    GridPosition center = position(1, 1);
    GridPosition goal = position(0, 2);
    Set<CardinalDirection> all = EnumSet.allOf(CardinalDirection.class);

    assertEquals(
        CardinalDirection.NORTH,
        TrackerSolverSimulation.chooseDirection(center, goal, all, Map.of()));
    assertEquals(
        CardinalDirection.EAST,
        TrackerSolverSimulation.chooseDirection(center, goal, all, Map.of(position(0, 1), 1)));
    assertEquals(
        CardinalDirection.SOUTH,
        TrackerSolverSimulation.chooseDirection(
            center, goal, all, Map.of(position(0, 1), 1, position(1, 2), 1, position(1, 0), 1)));
    assertEquals(
        CardinalDirection.WEST,
        TrackerSolverSimulation.chooseDirection(
            center, goal, all, Map.of(position(0, 1), 2, position(1, 2), 2, position(2, 1), 2)));
    assertNull(
        TrackerSolverSimulation.chooseDirection(
            center, goal, EnumSet.noneOf(CardinalDirection.class), Map.of(center, 1)));
  }

  @Test
  void absoluteTieOrderCoversEveryObstructionCombination() {
    GridPosition center = position(1, 1);
    GridPosition goal = position(1, 1);
    for (int openMask = 0; openMask < 16; openMask++) {
      Set<CardinalDirection> open = EnumSet.noneOf(CardinalDirection.class);
      CardinalDirection[] directions = CardinalDirection.values();
      for (int index = 0; index < directions.length; index++) {
        if ((openMask & (1 << index)) != 0) {
          open.add(directions[index]);
        }
      }
      CardinalDirection expected = open.stream().findFirst().orElse(null);

      assertEquals(expected, TrackerSolverSimulation.chooseDirection(center, goal, open, Map.of()));
    }
  }

  @Test
  void absoluteTieOrderDoesNotDependOnInputSetIteration() {
    Set<CardinalDirection> reverseIteration =
        new LinkedHashSet<>(
            List.of(
                CardinalDirection.WEST,
                CardinalDirection.SOUTH,
                CardinalDirection.EAST,
                CardinalDirection.NORTH));

    assertEquals(
        CardinalDirection.NORTH,
        TrackerSolverSimulation.chooseDirection(
            position(1, 1), position(1, 1), reverseIteration, Map.of()));
  }

  @Test
  void exactLoopTraceBacktracksThenChangesTheRepeatedCellDecision() {
    MazeState maze = loopMaze(Set.of());
    TrackerSolverSimulation tracker = new TrackerSolverSimulation(maze);

    assertEquals(LOOP_TRACE, trace(tracker));
    assertEquals(
        new SolverRunResult(LOOP_GOAL, Duration.ofMillis(1750), 7, SolverRunStatus.REACHED_GOAL),
        tracker.result());
    assertEquals(
        new SolverDecisionState(
            Map.of(
                LOOP_START,
                2,
                position(0, 1),
                1,
                position(1, 0),
                1,
                position(2, 0),
                1,
                position(2, 1),
                1,
                position(2, 2),
                1,
                LOOP_GOAL,
                1)),
        tracker.decisionState());
  }

  @Test
  void referenceModelAndProductionAgreeAtEveryDecision() {
    MazeState maze = loopMaze(Set.of());
    TrackerSolverSimulation production = new TrackerSolverSimulation(maze);
    ReferenceTracker reference = new ReferenceTracker(maze);

    while (production.result().status() == SolverRunStatus.RUNNING) {
      production.update(MOVE_INTERVAL);
      reference.move();
      assertEquals(reference.position(), production.result().position());
      assertEquals(reference.state(), production.decisionState());
    }
  }

  @Test
  void slowFloorPreservesTraceAndMemoryButDelaysTheNextDecision() {
    TrackerSolverSimulation normal = new TrackerSolverSimulation(loopMaze(Set.of()));
    TrackerSolverSimulation slowed = new TrackerSolverSimulation(loopMaze(Set.of(position(0, 1))));

    List<GridPosition> normalTrace = trace(normal);
    List<GridPosition> slowedTrace = trace(slowed);

    assertEquals(normalTrace, slowedTrace);
    assertEquals(normal.decisionState(), slowed.decisionState());
    assertEquals(normal.result().moveCount(), slowed.result().moveCount());
    assertEquals(MOVE_INTERVAL, slowed.result().elapsedTime().minus(normal.result().elapsedTime()));
  }

  @Test
  void timeoutDuringSlowFloorWaitDoesNotAddAMoveOrVisit() {
    LevelDefinition level = loopLevel(Duration.ofMillis(500));
    MazeState maze = maze(level, LOOP_WALLS, Set.of(position(0, 1)));
    TrackerSolverSimulation tracker = new TrackerSolverSimulation(maze);

    SolverRunResult result = tracker.update(Duration.ofSeconds(5));

    assertEquals(
        new SolverRunResult(position(0, 1), Duration.ofMillis(500), 1, SolverRunStatus.TIMED_OUT),
        result);
    assertEquals(
        new SolverDecisionState(Map.of(LOOP_START, 1, position(0, 1), 1)), tracker.decisionState());
  }

  @Test
  void goalMoveWinsAtTheExactTimeoutBoundary() {
    LevelDefinition level =
        singleSolverLevel(
            "tracker-goal-boundary",
            "Tracker Goal Boundary",
            GridSize.square(2),
            position(1, 0),
            position(0, 0),
            Duration.ofSeconds(1),
            MOVE_INTERVAL,
            MOVE_INTERVAL,
            MOVE_INTERVAL,
            infiniteSupplies(),
            SolverBehavior.LEAST_VISITED,
            1L);
    TrackerSolverSimulation tracker = new TrackerSolverSimulation(MazeState.empty(level));

    assertEquals(
        new SolverRunResult(position(0, 0), MOVE_INTERVAL, 1, SolverRunStatus.REACHED_GOAL),
        tracker.update(Duration.ofSeconds(1)));
  }

  @Test
  void wholeChunkedAndReplayRunsReproduceResultDirectionAndDecisionState() {
    MazeState maze = loopMaze(Set.of(position(0, 1), position(2, 1)));
    SolverSimulation whole = SolverSimulationFactory.create(maze);
    SolverSimulation chunked = SolverSimulationFactory.create(maze);
    SolverSimulation replay = SolverSimulationFactory.create(maze);

    SolverRunResult expected = whole.update(Duration.ofSeconds(20));
    for (int index = 0; index < 40; index++) {
      chunked.update(Duration.ofMillis(100));
    }
    replay.update(Duration.ofSeconds(20));

    assertEquals(expected, chunked.result());
    assertEquals(expected, replay.result());
    assertEquals(whole.lastDirection(), chunked.lastDirection());
    assertEquals(whole.lastDirection(), replay.lastDirection());
    assertEquals(whole.decisionState(), chunked.decisionState());
    assertEquals(whole.decisionState(), replay.decisionState());
  }

  @Test
  void validatesDeltasAndIgnoresUpdatesAfterTerminalResult() {
    TrackerSolverSimulation tracker = new TrackerSolverSimulation(loopMaze(Set.of()));

    assertThrows(IllegalArgumentException.class, () -> tracker.update(Duration.ofMillis(-1)));
    assertEquals(tracker.result(), tracker.update(Duration.ZERO));
    SolverRunResult terminal = tracker.update(Duration.ofSeconds(20));
    SolverDecisionState terminalState = tracker.decisionState();

    assertEquals(terminal, tracker.update(Duration.ofSeconds(1)));
    assertEquals(terminalState, tracker.decisionState());
  }

  @Test
  void allThreeBehaviorsHaveDifferentTracesOnTheSameMaze() {
    List<GridPosition> tracker = trace(SolverBehavior.LEAST_VISITED, 1L);
    List<GridPosition> scout = trace(SolverBehavior.LEFT_PRIORITY, 1L);
    List<GridPosition> random = trace(SolverBehavior.RANDOM, 2L);

    assertEquals(List.of(position(2, 1), position(1, 1), position(0, 1)), tracker);
    assertEquals(
        List.of(position(2, 1), position(2, 0), position(1, 0), position(0, 0), position(0, 1)),
        scout);
    assertEquals(
        List.of(position(2, 1), position(2, 0), position(1, 0), position(1, 1), position(0, 1)),
        random);
    assertNotEquals(tracker, scout);
    assertNotEquals(tracker, random);
    assertNotEquals(scout, random);
  }

  @Test
  void decisionStateRejectsInvalidOrMutableCounts() {
    Map<GridPosition, Integer> mutable = new HashMap<>();
    mutable.put(LOOP_START, 1);
    SolverDecisionState state = new SolverDecisionState(mutable);
    mutable.put(position(0, 1), 1);

    assertEquals(Map.of(LOOP_START, 1), state.cellVisitCounts());
    assertThrows(
        UnsupportedOperationException.class, () -> state.cellVisitCounts().put(position(0, 1), 1));
    assertThrows(
        IllegalArgumentException.class, () -> new SolverDecisionState(Map.of(LOOP_START, 0)));
    assertThrows(NullPointerException.class, () -> new SolverDecisionState(null));
  }

  private static List<GridPosition> trace(SolverBehavior behavior, long seed) {
    LevelDefinition level =
        singleSolverLevel(
            "comparison-" + behavior,
            "Comparison " + behavior,
            GridSize.square(3),
            position(2, 1),
            position(0, 1),
            Duration.ofSeconds(1),
            Duration.ofSeconds(1),
            Duration.ofSeconds(5),
            MOVE_INTERVAL,
            infiniteSupplies(),
            behavior,
            seed);
    return trace(SolverSimulationFactory.create(maze(level, Set.of(), Set.of())));
  }

  private static List<GridPosition> trace(SolverSimulation simulation) {
    List<GridPosition> positions = new ArrayList<>();
    positions.add(simulation.result().position());
    while (simulation.result().status() == SolverRunStatus.RUNNING) {
      int previousMoves = simulation.result().moveCount();
      SolverRunResult result = simulation.update(MOVE_INTERVAL);
      if (result.moveCount() != previousMoves) {
        positions.add(result.position());
      }
    }
    return List.copyOf(positions);
  }

  private static MazeState loopMaze(Set<GridPosition> slowFloors) {
    return maze(loopLevel(Duration.ofSeconds(5)), LOOP_WALLS, slowFloors);
  }

  private static LevelDefinition loopLevel(Duration timeout) {
    return singleSolverLevel(
        "tracker-loop",
        "Tracker Loop",
        GridSize.square(3),
        LOOP_START,
        LOOP_GOAL,
        Duration.ofSeconds(1),
        Duration.ofMillis(250),
        timeout,
        MOVE_INTERVAL,
        infiniteSupplies(),
        SolverBehavior.LEAST_VISITED,
        1L);
  }

  private static MazeState maze(
      LevelDefinition level, Set<GridPosition> walls, Set<GridPosition> slowFloors) {
    Map<GridPosition, PlaceableCellType> cells = new HashMap<>();
    walls.forEach(position -> cells.put(position, PlaceableCellType.WALL));
    slowFloors.forEach(position -> cells.put(position, PlaceableCellType.SLOW_FLOOR));
    return new MazeState(level, cells);
  }

  private static List<PlaceableCellSupply> infiniteSupplies() {
    return List.of(
        new PlaceableCellSupply(PlaceableCellType.WALL, CellSupply.infinite()),
        new PlaceableCellSupply(PlaceableCellType.SLOW_FLOOR, CellSupply.infinite()));
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }

  private static final class ReferenceTracker {
    private final MazeState maze;
    private final GridPosition goal;
    private final Map<GridPosition, Integer> visits = new HashMap<>();
    private GridPosition position;

    private ReferenceTracker(MazeState maze) {
      this.maze = maze;
      position = maze.levelDefinition().primarySolver().start();
      goal = maze.levelDefinition().primarySolver().goal();
      visits.put(position, 1);
    }

    private void move() {
      GridPosition best = null;
      int bestVisits = Integer.MAX_VALUE;
      int bestDistance = Integer.MAX_VALUE;
      for (CardinalDirection direction : CardinalDirection.values()) {
        GridPosition candidate = direction.move(position);
        if (maze.isTraversable(candidate)) {
          int candidateVisits = visits.getOrDefault(candidate, 0);
          int candidateDistance =
              Math.abs(candidate.row() - goal.row()) + Math.abs(candidate.column() - goal.column());
          if (candidateVisits < bestVisits
              || (candidateVisits == bestVisits && candidateDistance < bestDistance)) {
            best = candidate;
            bestVisits = candidateVisits;
            bestDistance = candidateDistance;
          }
        }
      }
      position = best;
      visits.merge(position, 1, Integer::sum);
    }

    private GridPosition position() {
      return position;
    }

    private SolverDecisionState state() {
      return new SolverDecisionState(visits);
    }
  }
}
