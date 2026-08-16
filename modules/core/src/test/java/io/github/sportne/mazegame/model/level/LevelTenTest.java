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
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Release evidence for the authored 10x10 Level 10. */
final class LevelTenTest {
  private static final Map<GridPosition, PlaceableCellType> PRESET_CELLS =
      slowFloors(p(7, 4), p(2, 8));
  private static final Map<GridPosition, PlaceableCellType> REMAINING_PASSING_CELLS =
      slowFloors(p(5, 6), p(4, 6), p(3, 6), p(2, 9));
  private static final Map<GridPosition, PlaceableCellType> PASSING_CELLS =
      slowFloors(p(7, 4), p(2, 8), p(5, 6), p(4, 6), p(3, 6), p(2, 9));

  @Test
  void catalogsTheTenthLevelWithOnlySlowFloorsInitiallyAvailable() {
    LevelDefinition level = Levels.levelTen();

    assertEquals("level-10", level.id());
    assertEquals("Level 10", level.name());
    assertEquals(GridSize.square(10), level.gridSize());
    assertEquals(Duration.ofSeconds(35), level.buildTime());
    assertEquals(Duration.ofMillis(12500), level.targetSolveTime());
    assertEquals(Duration.ofMillis(13500), level.maximumSolveTime());
    assertEquals(CellSupply.finite(0), level.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(6), level.supplyFor(PlaceableCellType.SLOW_FLOOR));
    assertEquals(List.of(PlaceableCellType.SLOW_FLOOR), level.initiallyAvailableCellTypes());
    assertEquals(
        new LevelSolver(
            p(9, 0),
            p(0, 9),
            SolverBehavior.RANDOM,
            OptionalLong.of(1484L),
            SolverAppearance.CLASSIC_MOUSE,
            GoalType.CHEESE),
        level.primarySolver());
    assertEquals(level, Levels.catalog().levels().get(9));
    assertEquals(
        Set.of(
            p(0, 3), p(1, 3), p(2, 3), p(2, 1), p(2, 2), p(3, 5), p(4, 5), p(5, 5), p(5, 3),
            p(5, 4), p(6, 7), p(7, 7), p(8, 7), p(7, 1), p(7, 2), p(1, 7), p(2, 7)),
        fixedPositions(level, FixedCellType.WALL));
    assertEquals(Set.of(p(8, 2), p(3, 8)), fixedPositions(level, FixedCellType.SLOW_FLOOR));
    assertEquals(
        Set.of(
            new PresetCell(p(7, 4), PlaceableCellType.SLOW_FLOOR),
            new PresetCell(p(2, 8), PlaceableCellType.SLOW_FLOOR)),
        Set.copyOf(level.presetCells()));
  }

  @Test
  void followsTheAcceptedSeededRouteAcrossTheAuthoredGeometry() {
    Trace initial = trace(MazeState.initial(Levels.levelTen()));
    Trace passing = trace(PASSING_CELLS);

    assertEquals(
        List.of(
            p(9, 0), p(9, 1), p(8, 1), p(8, 2), p(8, 3), p(8, 4), p(8, 5), p(8, 4), p(7, 4),
            p(7, 3), p(7, 4), p(6, 4), p(7, 4), p(7, 3), p(7, 4), p(7, 5), p(7, 6), p(6, 6),
            p(5, 6), p(5, 7), p(5, 6), p(4, 6), p(3, 6), p(4, 6), p(3, 6), p(3, 7), p(3, 8),
            p(2, 8), p(1, 8), p(2, 8), p(2, 9), p(2, 8), p(2, 9), p(1, 9), p(0, 9)),
        initial.positions());
    assertEquals(
        new SolverRunResult(p(0, 9), Duration.ofMillis(10750), 34, SolverRunStatus.REACHED_GOAL),
        initial.result());
    assertEquals(
        new SolverRunResult(p(0, 9), Duration.ofMillis(12750), 34, SolverRunStatus.REACHED_GOAL),
        passing.result());
  }

  @Test
  void requiresBothPresetsAndAllFourRemainingSlowFloorsToCompleteBeforeTimeout() {
    MazeState initialMaze = MazeState.initial(Levels.levelTen());
    Trace initial = trace(initialMaze);
    Map<GridPosition, Long> editableVisitCounts = new HashMap<>();
    initial.positions().stream()
        .skip(1)
        .filter(position -> Levels.levelTen().fixedCellAt(position).isEmpty())
        .filter(position -> !initialMaze.hasPlacedCellAt(position))
        .filter(position -> !position.equals(Levels.levelTen().primarySolver().goal()))
        .forEach(position -> editableVisitCounts.merge(position, 1L, Long::sum));
    List<Long> descendingVisits =
        editableVisitCounts.values().stream().sorted(java.util.Comparator.reverseOrder()).toList();

    List<Long> maximumBySlowFloorCount = new ArrayList<>();
    long maximum = initial.result().elapsedTime().toMillis();
    maximumBySlowFloorCount.add(maximum);
    for (int count = 0; count < 4; count++) {
      maximum += descendingVisits.get(count) * 250L;
      maximumBySlowFloorCount.add(maximum);
    }
    assertEquals(List.of(10750L, 11250L, 11750L, 12250L, 12750L), maximumBySlowFloorCount);
    assertTrue(maximumBySlowFloorCount.subList(0, 4).stream().allMatch(value -> value <= 12500L));
    assertTrue(maximumBySlowFloorCount.get(4) > 12500L);

    GameSession session = runSession(REMAINING_PASSING_CELLS);
    assertTrue(session.resultPassed());
    assertEquals(new BestResult(Duration.ofMillis(12750), 34), session.bestResult());
    assertEquals(GamePhase.RESULT, session.gamePhase());
    assertFalse(session.hasNextLevel());
  }

  private static Trace trace(Map<GridPosition, PlaceableCellType> cells) {
    LevelDefinition level = Levels.levelTen();
    return trace(new MazeState(level, cells));
  }

  private static Trace trace(MazeState mazeState) {
    LevelDefinition level = mazeState.levelDefinition();
    SolverSimulation simulation = SolverSimulationFactory.create(mazeState, level.primarySolver());
    List<GridPosition> positions = new ArrayList<>();
    positions.add(simulation.result().position());
    do {
      int previousMoves = simulation.result().moveCount();
      SolverRunResult result = simulation.update(level.solverMoveInterval());
      if (result.moveCount() > previousMoves) {
        positions.add(result.position());
      }
    } while (simulation.result().status() == SolverRunStatus.RUNNING);
    return new Trace(simulation.result(), List.copyOf(positions));
  }

  private static GameSession runSession(Map<GridPosition, PlaceableCellType> cells) {
    LevelDefinition level = Levels.levelTen();
    LevelCatalog catalog = new LevelCatalog(List.of(level));
    GameSession session =
        new GameSession(catalog, catalog.levels().getFirst().id(), BestResultStore.none());
    assertTrue(session.startLevel(level.id()));
    assertEquals(PRESET_CELLS, session.mazeState().placedCells());
    assertEquals(
        CellSupply.finite(4), session.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
    for (Map.Entry<GridPosition, PlaceableCellType> entry : cells.entrySet()) {
      session.selectCellType(entry.getValue());
      assertTrue(session.placeOrReplaceCell(entry.getKey()).orElseThrow().accepted());
    }
    session.startRun();
    session.updateSolverRun(level.maximumSolveTime().toMillis() / 1000.0F);
    return session;
  }

  private static Set<GridPosition> fixedPositions(LevelDefinition level, FixedCellType type) {
    return level.fixedCells().stream()
        .filter(cell -> cell.type() == type)
        .map(FixedCell::position)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }

  private static Map<GridPosition, PlaceableCellType> slowFloors(GridPosition... positions) {
    Map<GridPosition, PlaceableCellType> cells = new HashMap<>();
    for (GridPosition position : positions) {
      cells.put(position, PlaceableCellType.SLOW_FLOOR);
    }
    return Map.copyOf(cells);
  }

  private static GridPosition p(int row, int column) {
    return new GridPosition(row, column);
  }

  private record Trace(SolverRunResult result, List<GridPosition> positions) {}
}
