package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import io.github.sportne.mazegame.model.solver.SolverSimulation;
import io.github.sportne.mazegame.model.solver.SolverSimulationFactory;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameSession;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Authored fixtures and exhaustive timing evidence for Levels 7, 8, and 9. */
final class LevelsSevenToNineTest {
  private static final Duration STEP = Duration.ofMillis(250);
  private static final Map<GridPosition, PlaceableCellType> LEVEL_SEVEN_PASSING =
      cells(p(4, 2), List.of(p(3, 0), p(3, 1), p(3, 2)));
  private static final Map<GridPosition, PlaceableCellType> LEVEL_EIGHT_PASSING =
      cells(p(3, 1), List.of(p(0, 0), p(0, 1), p(0, 2), p(2, 1)));
  private static final Map<GridPosition, PlaceableCellType> LEVEL_NINE_PASSING =
      cells(p(0, 2), List.of(p(0, 0), p(0, 1), p(1, 0), p(2, 0), p(0, 3)));

  @Test
  void catalogsTheThreeAcceptedDefinitionsInProgressionOrder() {
    LevelDefinition seven = Levels.levelSeven();
    assertEquals("level-7", seven.id());
    assertEquals("Level 7", seven.name());
    assertEquals(GridSize.square(5), seven.gridSize());
    assertEquals(Duration.ofSeconds(20), seven.buildTime());
    assertEquals(Duration.ofSeconds(6), seven.targetSolveTime());
    assertEquals(Duration.ofSeconds(10), seven.maximumSolveTime());
    assertEquals(CellSupply.finite(1), seven.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(3), seven.supplyFor(PlaceableCellType.SLOW_FLOOR));
    assertEquals(
        new LevelSolver(
            p(4, 0),
            p(0, 4),
            SolverBehavior.LINE_OF_SIGHT,
            OptionalLong.of(107L),
            SolverAppearance.SEEKER_RABBIT,
            GoalType.CARROT),
        seven.primarySolver());

    LevelDefinition eight = Levels.levelEight();
    assertEquals("level-8", eight.id());
    assertEquals("Level 8", eight.name());
    assertEquals(GridSize.square(6), eight.gridSize());
    assertEquals(Duration.ofSeconds(25), eight.buildTime());
    assertEquals(Duration.ofMillis(7300), eight.targetSolveTime());
    assertEquals(Duration.ofSeconds(8), eight.maximumSolveTime());
    assertEquals(CellSupply.finite(1), eight.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(4), eight.supplyFor(PlaceableCellType.SLOW_FLOOR));
    assertEquals(
        new LevelSolver(
            p(5, 0),
            p(0, 5),
            SolverBehavior.LEFT_PRIORITY,
            OptionalLong.empty(),
            SolverAppearance.SCOUT_SQUIRREL,
            GoalType.ACORN),
        eight.primarySolver());

    LevelDefinition nine = Levels.levelNine();
    assertEquals("level-9", nine.id());
    assertEquals("Level 9", nine.name());
    assertEquals(GridSize.square(7), nine.gridSize());
    assertEquals(Duration.ofSeconds(30), nine.buildTime());
    assertEquals(Duration.ofMillis(7500), nine.targetSolveTime());
    assertEquals(Duration.ofSeconds(9), nine.maximumSolveTime());
    assertEquals(CellSupply.finite(1), nine.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(5), nine.supplyFor(PlaceableCellType.SLOW_FLOOR));
    assertEquals(
        new LevelSolver(
            p(6, 0),
            p(0, 6),
            SolverBehavior.LEAST_VISITED,
            OptionalLong.empty(),
            SolverAppearance.TRACKER_RACCOON,
            GoalType.TRASH_CAN),
        nine.primarySolver());

    assertEquals(List.of(seven, eight, nine), Levels.catalog().levels().subList(6, 9));
    assertFixedCells(seven, Set.of(p(0, 0), p(2, 0), p(2, 1), p(2, 2), p(2, 3), p(4, 1)), Set.of());
    assertFixedCells(
        eight,
        Set.copyOf(List.of(p(0, 4), p(1, 0), p(2, 5), p(3, 0), p(3, 5), p(4, 2), p(5, 5))),
        Set.of(p(1, 4), p(4, 3)));
    assertFixedCells(
        nine,
        Set.of(p(1, 1), p(1, 4), p(1, 5), p(2, 5), p(3, 1), p(4, 1), p(5, 5), p(5, 6), p(6, 4)),
        Set.of(p(1, 3), p(2, 4), p(4, 5)));
  }

  @Test
  void levelSevenTurnsAVisibleGoalIntoSeededExploration() {
    LevelDefinition level = Levels.levelSeven();
    Trace empty = trace(level, level.primarySolver(), Map.of());
    Trace passing = trace(level, level.primarySolver(), LEVEL_SEVEN_PASSING);

    assertEquals(
        List.of(
            p(4, 0), p(3, 0), p(3, 1), p(3, 2), p(4, 2), p(3, 2), p(3, 3), p(3, 4), p(2, 4),
            p(1, 4), p(0, 4)),
        empty.positions());
    assertEquals(
        new SolverRunResult(p(0, 4), Duration.ofMillis(2500), 10, SolverRunStatus.REACHED_GOAL),
        empty.result());
    assertEquals(
        new SolverRunResult(p(0, 4), Duration.ofMillis(6500), 16, SolverRunStatus.REACHED_GOAL),
        passing.result());
    assertTrue(runSession(level, LEVEL_SEVEN_PASSING).resultPassed());
  }

  @Test
  void levelEightGrowsTheGridAndRoutesScoutAcrossFixedSlowFloors() {
    LevelDefinition level = Levels.levelEight();
    Trace trace = trace(level, level.primarySolver(), LEVEL_EIGHT_PASSING);

    assertEquals(
        new SolverRunResult(p(0, 5), Duration.ofMillis(7500), 22, SolverRunStatus.REACHED_GOAL),
        trace.result());
    assertEquals(
        List.of(
            p(5, 0), p(4, 0), p(4, 1), p(5, 1), p(5, 2), p(5, 3), p(4, 3), p(3, 3), p(3, 2),
            p(2, 2), p(2, 1), p(2, 0), p(2, 1), p(1, 1), p(0, 1), p(0, 0), p(0, 1), p(0, 2),
            p(0, 3), p(1, 3), p(1, 4), p(1, 5), p(0, 5)),
        trace.positions());

    GameSession session = runSession(level, LEVEL_EIGHT_PASSING);
    assertTrue(session.resultPassed());
    assertEquals(new BestResult(Duration.ofMillis(7500), 22), session.bestResult());
    assertEquals(GamePhase.RESULT, session.gamePhase());
    assertEquals(SolverRunStatus.REACHED_GOAL, session.solverRunResults().get(0).status());
  }

  @Test
  void levelNineGrowsAgainAndMakesTrackerRevisitTheDelayedRoute() {
    LevelDefinition level = Levels.levelNine();
    Trace trace = trace(level, level.primarySolver(), LEVEL_NINE_PASSING);

    assertEquals(
        new SolverRunResult(p(0, 6), Duration.ofMillis(7750), 20, SolverRunStatus.REACHED_GOAL),
        trace.result());
    assertEquals(
        List.of(
            p(6, 0), p(5, 0), p(4, 0), p(3, 0), p(2, 0), p(1, 0), p(0, 0), p(0, 1), p(0, 0),
            p(0, 1), p(0, 0), p(1, 0), p(2, 0), p(2, 1), p(2, 2), p(1, 2), p(1, 3), p(0, 3),
            p(0, 4), p(0, 5), p(0, 6)),
        trace.positions());

    GameSession session = runSession(level, LEVEL_NINE_PASSING);
    assertTrue(session.resultPassed());
    assertEquals(new BestResult(Duration.ofMillis(7750), 20), session.bestResult());
    assertFalse(session.hasNextLevel());
    assertEquals(SolverRunStatus.REACHED_GOAL, session.solverRunResults().get(0).status());
  }

  @Test
  void onlyFullCombinedInventoryCanPassEachNewLevel() {
    for (LevelCase levelCase :
        List.of(
            new LevelCase(Levels.levelSeven(), 3),
            new LevelCase(Levels.levelEight(), 4),
            new LevelCase(Levels.levelNine(), 5))) {
      Balance balance = analyze(levelCase.level(), levelCase.slowSupply());
      long target = levelCase.level().targetSolveTime().toMillis();

      String evidence = levelCase.level().id() + ": " + balance;
      assertTrue(
          balance.slowOnlyMaximumByCount().stream().allMatch(value -> value <= target), evidence);
      assertTrue(balance.wallMaximumByCount().get(levelCase.slowSupply() - 1) <= target, evidence);
      assertTrue(balance.wallMaximumByCount().get(levelCase.slowSupply()) > target, evidence);
      assertTrue(balance.fullInventoryPassingLayouts() > 0, evidence);
      assertEquals(expectedBalance(levelCase.level()), balance);
    }
  }

  private static Balance expectedBalance(LevelDefinition level) {
    if (level.id().equals("level-7")) {
      return new Balance(
          List.of(2500L, 3000L, 3250L, 3500L), List.of(4000L, 5250L, 6000L, 6500L), 8);
    }
    if (level.id().equals("level-8")) {
      return new Balance(
          List.of(4250L, 4750L, 5250L, 5500L, 5750L),
          List.of(6000L, 6500L, 7000L, 7250L, 7500L),
          196);
    }
    if (level.id().equals("level-9")) {
      return new Balance(
          List.of(3000L, 3250L, 3500L, 3750L, 4000L, 4250L),
          List.of(5500L, 6000L, 6500L, 7000L, 7500L, 7750L),
          9);
    }
    throw new IllegalArgumentException("unexpected level " + level.id());
  }

  private static Balance analyze(LevelDefinition level, int slowSupply) {
    List<GridPosition> editable = editable(level);
    List<Long> slowOnlyMaximum = new ArrayList<>();
    List<Long> wallMaximum = new ArrayList<>();
    for (int count = 0; count <= slowSupply; count++) {
      slowOnlyMaximum.add(0L);
      wallMaximum.add(0L);
    }
    enumerateSlows(
        level,
        Map.of(),
        editable,
        slowSupply,
        (count, score) -> slowOnlyMaximum.set(count, Math.max(slowOnlyMaximum.get(count), score)));

    int[] passing = {0};
    for (GridPosition wall : editable) {
      Map<GridPosition, PlaceableCellType> wallOnly = Map.of(wall, PlaceableCellType.WALL);
      List<Trace> baseTraces;
      try {
        baseTraces = traces(level, wallOnly);
      } catch (IllegalArgumentException rejectedPath) {
        continue;
      }
      List<GridPosition> available =
          editable.stream().filter(position -> !position.equals(wall)).toList();
      enumerateSlows(
          level,
          available,
          slowSupply,
          baseTraces,
          (count, score) -> {
            wallMaximum.set(count, Math.max(wallMaximum.get(count), score));
            if (count == slowSupply && score > level.targetSolveTime().toMillis()) {
              passing[0]++;
            }
          });
    }
    return new Balance(List.copyOf(slowOnlyMaximum), List.copyOf(wallMaximum), passing[0]);
  }

  private static void enumerateSlows(
      LevelDefinition level,
      Map<GridPosition, PlaceableCellType> base,
      List<GridPosition> available,
      int maximumCount,
      ScoreConsumer consumer) {
    enumerateSlows(level, available, maximumCount, traces(level, base), consumer);
  }

  private static void enumerateSlows(
      LevelDefinition level,
      List<GridPosition> available,
      int maximumCount,
      List<Trace> baseTraces,
      ScoreConsumer consumer) {
    consumer.accept(0, completionMillis(level, baseTraces, Set.of()));
    enumerateSlows(level, baseTraces, available, maximumCount, 0, new ArrayList<>(), consumer);
  }

  private static void enumerateSlows(
      LevelDefinition level,
      List<Trace> baseTraces,
      List<GridPosition> available,
      int maximumCount,
      int start,
      List<GridPosition> selected,
      ScoreConsumer consumer) {
    if (selected.size() == maximumCount) {
      return;
    }
    for (int index = start; index < available.size(); index++) {
      selected.add(available.get(index));
      consumer.accept(selected.size(), completionMillis(level, baseTraces, Set.copyOf(selected)));
      enumerateSlows(level, baseTraces, available, maximumCount, index + 1, selected, consumer);
      selected.remove(selected.size() - 1);
    }
  }

  private static long completionMillis(
      LevelDefinition level, List<Trace> baseTraces, Set<GridPosition> slowFloors) {
    long completion = Long.MAX_VALUE;
    for (Trace trace : baseTraces) {
      if (trace.result().status() != SolverRunStatus.REACHED_GOAL) {
        continue;
      }
      long delays = trace.positions().stream().skip(1).filter(slowFloors::contains).count();
      long arrival = trace.result().elapsedTime().toMillis() + STEP.toMillis() * delays;
      if (arrival <= level.maximumSolveTime().toMillis()) {
        completion = Math.min(completion, arrival);
      }
    }
    return completion == Long.MAX_VALUE ? level.maximumSolveTime().toMillis() : completion;
  }

  private static GameSession runSession(
      LevelDefinition level, Map<GridPosition, PlaceableCellType> cells) {
    GameSession session =
        new GameSession(new LevelCatalog(List.of(level)), level.id(), BestResultStore.none());
    assertTrue(session.startLevel(level.id()));
    cells.forEach(
        (position, type) -> {
          session.selectCellType(type);
          assertTrue(session.placeOrReplaceCell(position).orElseThrow().accepted());
        });
    session.startRun();
    session.updateSolverRun(level.maximumSolveTime().toMillis() / 1000.0F);
    return session;
  }

  private static List<Trace> traces(
      LevelDefinition level, Map<GridPosition, PlaceableCellType> cells) {
    MazeState maze = new MazeState(level, cells);
    return level.solvers().stream().map(solver -> trace(level, solver, maze)).toList();
  }

  private static Trace trace(
      LevelDefinition level, LevelSolver solver, Map<GridPosition, PlaceableCellType> cells) {
    return trace(level, solver, new MazeState(level, cells));
  }

  private static Trace trace(LevelDefinition level, LevelSolver solver, MazeState maze) {
    SolverSimulation simulation = SolverSimulationFactory.create(maze, solver);
    List<GridPosition> positions = new ArrayList<>();
    positions.add(simulation.result().position());
    int previousMoves = 0;
    while (simulation.result().status() == SolverRunStatus.RUNNING) {
      SolverRunResult result = simulation.update(level.solverMoveInterval());
      if (result.moveCount() > previousMoves) {
        positions.add(result.position());
        previousMoves = result.moveCount();
      }
    }
    return new Trace(simulation.result(), List.copyOf(positions));
  }

  private static List<GridPosition> editable(LevelDefinition level) {
    Set<GridPosition> protectedPositions = new HashSet<>();
    level
        .solvers()
        .forEach(
            solver -> {
              protectedPositions.add(solver.start());
              protectedPositions.add(solver.goal());
            });
    List<GridPosition> positions = new ArrayList<>();
    for (int row = 0; row < level.gridSize().rows(); row++) {
      for (int column = 0; column < level.gridSize().columns(); column++) {
        GridPosition position = p(row, column);
        if (!protectedPositions.contains(position) && level.fixedCellAt(position).isEmpty()) {
          positions.add(position);
        }
      }
    }
    return List.copyOf(positions);
  }

  private static void assertFixedCells(
      LevelDefinition level,
      Set<GridPosition> expectedWalls,
      Set<GridPosition> expectedSlowFloors) {
    assertEquals(
        expectedWalls,
        level.fixedCells().stream()
            .filter(cell -> cell.type() == FixedCellType.WALL)
            .map(FixedCell::position)
            .collect(java.util.stream.Collectors.toSet()));
    assertEquals(
        expectedSlowFloors,
        level.fixedCells().stream()
            .filter(cell -> cell.type() == FixedCellType.SLOW_FLOOR)
            .map(FixedCell::position)
            .collect(java.util.stream.Collectors.toSet()));
  }

  private static Map<GridPosition, PlaceableCellType> cells(
      GridPosition wall, List<GridPosition> slowFloors) {
    Map<GridPosition, PlaceableCellType> cells = new HashMap<>();
    cells.put(wall, PlaceableCellType.WALL);
    slowFloors.forEach(position -> cells.put(position, PlaceableCellType.SLOW_FLOOR));
    return Map.copyOf(cells);
  }

  private static GridPosition p(int row, int column) {
    return new GridPosition(row, column);
  }

  private record LevelCase(LevelDefinition level, int slowSupply) {}

  private record Trace(SolverRunResult result, List<GridPosition> positions) {}

  private record Balance(
      List<Long> slowOnlyMaximumByCount,
      List<Long> wallMaximumByCount,
      int fullInventoryPassingLayouts) {}

  @FunctionalInterface
  private interface ScoreConsumer {
    void accept(int slowCount, long completionMillis);
  }
}
