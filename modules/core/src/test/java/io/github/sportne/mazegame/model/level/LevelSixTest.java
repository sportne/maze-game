package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.maze.MazeEditStatus;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import io.github.sportne.mazegame.model.solver.SolverSimulation;
import io.github.sportne.mazegame.model.solver.SolverSimulationFactory;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameResultEvaluator;
import io.github.sportne.mazegame.state.GameSession;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

/** Production authoring and exhaustive balance evidence for Tracker's introductory level. */
final class LevelSixTest {
  private static final LevelDefinition LEVEL = Levels.levelSix();
  private static final GridPosition START = position(0, 0);
  private static final GridPosition GOAL = position(4, 4);
  private static final Map<GridPosition, PlaceableCellType> PASSING_CELLS =
      Map.of(
          position(3, 4), PlaceableCellType.WALL,
          position(2, 3), PlaceableCellType.SLOW_FLOOR,
          position(1, 3), PlaceableCellType.SLOW_FLOOR,
          position(1, 4), PlaceableCellType.SLOW_FLOOR);

  @Test
  void catalogsTheAcceptedTrackerLevelWithFixedLoopGeometry() {
    assertEquals("level-6", LEVEL.id());
    assertEquals("Level 6", LEVEL.name());
    assertEquals(GridSize.square(5), LEVEL.gridSize());
    assertEquals(Duration.ofSeconds(20), LEVEL.buildTime());
    assertEquals(Duration.ofSeconds(6), LEVEL.targetSolveTime());
    assertEquals(Duration.ofSeconds(8), LEVEL.maximumSolveTime());
    assertEquals(Duration.ofMillis(250), LEVEL.solverMoveInterval());
    assertEquals(CellSupply.finite(1), LEVEL.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(3), LEVEL.supplyFor(PlaceableCellType.SLOW_FLOOR));
    assertEquals(
        List.of(
            new FixedCell(position(0, 2), FixedCellType.WALL),
            new FixedCell(position(1, 1), FixedCellType.WALL)),
        LEVEL.fixedCells());
    assertEquals(
        new LevelSolver(
            START,
            GOAL,
            SolverBehavior.LEAST_VISITED,
            OptionalLong.empty(),
            SolverAppearance.TRACKER_RACCOON,
            GoalType.TRASH_CAN),
        LEVEL.primarySolver());
    assertEquals(LEVEL, Levels.catalog().levels().get(5));
  }

  @Test
  void fixedWallsTeachTrackerToChangeItsChoiceAfterRevisitingTheStart() {
    MazeState maze = MazeState.empty(LEVEL);
    SolverSimulation simulation = SolverSimulationFactory.create(maze);

    assertEquals(
        List.of(
            START,
            position(0, 1),
            START,
            position(1, 0),
            position(2, 0),
            position(2, 1),
            position(2, 2),
            position(2, 3),
            position(2, 4),
            position(3, 4),
            GOAL),
        trace(simulation));
    assertEquals(
        new SolverRunResult(GOAL, Duration.ofMillis(2500), 10, SolverRunStatus.REACHED_GOAL),
        simulation.result());
    assertEquals(2, simulation.decisionState().cellVisitCounts().get(START));
    assertFalse(GameResultEvaluator.passed(GamePhase.RESULT, simulation.result(), LEVEL));

    for (FixedCell fixedCell : LEVEL.fixedCells()) {
      assertEquals(
          MazeEditStatus.REJECTED_FIXED_CELL,
          maze.placeOrReplace(PlaceableCellType.WALL, fixedCell.position()).status());
    }
  }

  @Test
  void onlyTheFullCombinedInventoryCanExceedTheTarget() {
    List<GridPosition> editable = editablePositions();
    long slowOnlyMaximum = slowOnlyMaximum(editable);
    long[] combinedMaximumBySlowCount = new long[4];
    int fullInventoryPassingLayouts = 0;

    for (GridPosition wall : editable) {
      fullInventoryPassingLayouts +=
          evaluateWallLayouts(
              editable, wall, combinedMaximumBySlowCount, LEVEL.targetSolveTime().toMillis());
    }

    assertEquals(3250L, slowOnlyMaximum);
    assertEquals(List.of(5000L, 5500L, 6000L, 6500L), boxed(combinedMaximumBySlowCount));
    assertEquals(64, fullInventoryPassingLayouts);
  }

  @Test
  void acceptedCombinedFixturePassesAndReplaysWithTrackerSpecificBehavior() {
    MazeState passing = new MazeState(LEVEL, PASSING_CELLS);
    SolverRunResult result = run(passing);

    assertEquals(
        new SolverRunResult(GOAL, Duration.ofMillis(6500), 20, SolverRunStatus.REACHED_GOAL),
        result);
    assertTrue(GameResultEvaluator.passed(GamePhase.RESULT, result, LEVEL));
    assertEquals(CellSupply.finite(0), passing.remainingSupply(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(0), passing.remainingSupply(PlaceableCellType.SLOW_FLOOR));

    GameSession session =
        new GameSession(new LevelCatalog(List.of(LEVEL)), LEVEL.id(), BestResultStore.none());
    assertTrue(session.startLevel(LEVEL.id()));
    PASSING_CELLS.forEach(
        (position, type) -> {
          session.selectCellType(type);
          assertTrue(session.placeOrReplaceCell(position).orElseThrow().accepted());
        });
    session.startRun();
    session.updateSolverRun(8.0F);

    assertTrue(session.resultPassed());
    assertEquals(new BestResult(Duration.ofMillis(6500), 20), session.bestResult());
    SolverRunResult firstRun = session.solverRunResult();
    session.replayRun();
    session.updateSolverRun(8.0F);
    assertEquals(firstRun, session.solverRunResult());
  }

  @Test
  void acceptedFixtureIsNotEquivalentForTheOtherSolverRules() {
    assertEquals(
        new SolverRunResult(START, Duration.ofSeconds(8), 32, SolverRunStatus.TIMED_OUT),
        runWithBehavior(PASSING_CELLS, SolverBehavior.RANDOM, 23L));
    assertEquals(
        new SolverRunResult(GOAL, Duration.ofMillis(4750), 16, SolverRunStatus.REACHED_GOAL),
        runWithBehavior(PASSING_CELLS, SolverBehavior.LEFT_PRIORITY, 0L));
    assertEquals(
        new SolverRunResult(GOAL, Duration.ofSeconds(4), 16, SolverRunStatus.REACHED_GOAL),
        runWithBehavior(PASSING_CELLS, SolverBehavior.LINE_OF_SIGHT, 23L));
  }

  private static int evaluateWallLayouts(
      List<GridPosition> editable,
      GridPosition wall,
      long[] maximumBySlowCount,
      long targetMillis) {
    int passing = 0;
    Map<GridPosition, PlaceableCellType> wallOnly = Map.of(wall, PlaceableCellType.WALL);
    try {
      updateMaximum(maximumBySlowCount, 0, wallOnly);
    } catch (IllegalArgumentException rejectedPath) {
      return 0;
    }
    for (int first = 0; first < editable.size(); first++) {
      if (editable.get(first).equals(wall)) {
        continue;
      }
      Map<GridPosition, PlaceableCellType> oneSlow = withSlowFloors(wallOnly, editable.get(first));
      updateMaximum(maximumBySlowCount, 1, oneSlow);
      for (int second = first + 1; second < editable.size(); second++) {
        if (editable.get(second).equals(wall)) {
          continue;
        }
        Map<GridPosition, PlaceableCellType> twoSlow =
            withSlowFloors(oneSlow, editable.get(second));
        updateMaximum(maximumBySlowCount, 2, twoSlow);
        for (int third = second + 1; third < editable.size(); third++) {
          if (editable.get(third).equals(wall)) {
            continue;
          }
          Map<GridPosition, PlaceableCellType> threeSlow =
              withSlowFloors(twoSlow, editable.get(third));
          SolverRunResult result = run(threeSlow);
          updateMaximum(maximumBySlowCount, 3, result);
          if (result.elapsedTime().toMillis() > targetMillis) {
            passing++;
          }
        }
      }
    }
    return passing;
  }

  private static long slowOnlyMaximum(List<GridPosition> editable) {
    long maximum = reachedMillis(Map.of());
    for (int first = 0; first < editable.size(); first++) {
      Map<GridPosition, PlaceableCellType> oneSlow = withSlowFloors(Map.of(), editable.get(first));
      maximum = Math.max(maximum, reachedMillis(oneSlow));
      for (int second = first + 1; second < editable.size(); second++) {
        Map<GridPosition, PlaceableCellType> twoSlow =
            withSlowFloors(oneSlow, editable.get(second));
        maximum = Math.max(maximum, reachedMillis(twoSlow));
        for (int third = second + 1; third < editable.size(); third++) {
          Map<GridPosition, PlaceableCellType> threeSlow =
              withSlowFloors(twoSlow, editable.get(third));
          maximum = Math.max(maximum, reachedMillis(threeSlow));
        }
      }
    }
    return maximum;
  }

  private static long reachedMillis(Map<GridPosition, PlaceableCellType> cells) {
    SolverRunResult result = run(cells);
    assertEquals(SolverRunStatus.REACHED_GOAL, result.status());
    return result.elapsedTime().toMillis();
  }

  private static void updateMaximum(
      long[] maximumBySlowCount, int slowCount, Map<GridPosition, PlaceableCellType> cells) {
    updateMaximum(maximumBySlowCount, slowCount, run(cells));
  }

  private static void updateMaximum(
      long[] maximumBySlowCount, int slowCount, SolverRunResult result) {
    assertEquals(SolverRunStatus.REACHED_GOAL, result.status());
    maximumBySlowCount[slowCount] =
        Math.max(maximumBySlowCount[slowCount], result.elapsedTime().toMillis());
  }

  private static Map<GridPosition, PlaceableCellType> withSlowFloors(
      Map<GridPosition, PlaceableCellType> source, GridPosition... positions) {
    Map<GridPosition, PlaceableCellType> updated = new HashMap<>(source);
    for (GridPosition position : positions) {
      updated.put(position, PlaceableCellType.SLOW_FLOOR);
    }
    return Map.copyOf(updated);
  }

  private static List<GridPosition> editablePositions() {
    List<GridPosition> positions = new ArrayList<>();
    for (int row = 0; row < LEVEL.gridSize().rows(); row++) {
      for (int column = 0; column < LEVEL.gridSize().columns(); column++) {
        GridPosition position = position(row, column);
        if (!position.equals(START)
            && !position.equals(GOAL)
            && LEVEL.fixedCellAt(position).isEmpty()) {
          positions.add(position);
        }
      }
    }
    return List.copyOf(positions);
  }

  private static List<GridPosition> trace(SolverSimulation simulation) {
    List<GridPosition> trace = new ArrayList<>();
    trace.add(simulation.result().position());
    int previousMoves = 0;
    while (simulation.result().status() == SolverRunStatus.RUNNING) {
      SolverRunResult result = simulation.update(LEVEL.solverMoveInterval());
      if (result.moveCount() > previousMoves) {
        trace.add(result.position());
        previousMoves = result.moveCount();
      }
    }
    return List.copyOf(trace);
  }

  private static SolverRunResult run(Map<GridPosition, PlaceableCellType> cells) {
    return run(new MazeState(LEVEL, cells));
  }

  private static SolverRunResult run(MazeState maze) {
    return SolverSimulationFactory.create(maze).update(LEVEL.maximumSolveTime());
  }

  private static SolverRunResult runWithBehavior(
      Map<GridPosition, PlaceableCellType> cells, SolverBehavior behavior, long seed) {
    OptionalLong randomSeed =
        behavior == SolverBehavior.RANDOM || behavior == SolverBehavior.LINE_OF_SIGHT
            ? OptionalLong.of(seed)
            : OptionalLong.empty();
    LevelDefinition comparison =
        new LevelDefinition(
            "level-6-" + behavior.name().toLowerCase(java.util.Locale.ROOT),
            "Level 6 Comparison",
            LEVEL.gridSize(),
            LEVEL.buildTime(),
            LEVEL.targetSolveTime(),
            LEVEL.maximumSolveTime(),
            LEVEL.solverMoveInterval(),
            LEVEL.placeableCellSupplies(),
            LEVEL.fixedCells(),
            List.of(
                new LevelSolver(
                    START,
                    GOAL,
                    behavior,
                    randomSeed,
                    LEVEL.primarySolver().appearance(),
                    LEVEL.primarySolver().goalType())));
    return SolverSimulationFactory.create(new MazeState(comparison, cells))
        .update(comparison.maximumSolveTime());
  }

  private static List<Long> boxed(long[] values) {
    return java.util.Arrays.stream(values).boxed().toList();
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }
}
