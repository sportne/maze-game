package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.maze.MazeEditResult;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import io.github.sportne.mazegame.model.solver.SolverSimulationFactory;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameResultEvaluator;
import io.github.sportne.mazegame.state.GameSession;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Release evidence for the tutorial-first ten-level progression. */
final class ReleasedLevelProgressionTest {
  private static final List<Map<GridPosition, PlaceableCellType>> PASSING_EDITS =
      List.of(
          cells(walls(p(1, 2)), Map.of()),
          cells(walls(p(2, 2)), Map.of()),
          cells(walls(p(0, 3), p(3, 1)), Map.of()),
          cells(walls(p(3, 1), p(1, 0)), Map.of()),
          cells(walls(p(1, 5)), slows(p(1, 3), p(2, 3))),
          cells(walls(p(2, 3)), slows(p(1, 2), p(2, 2), p(3, 2))),
          cells(walls(p(1, 2)), slows(p(1, 1), p(2, 0), p(2, 1))),
          cells(walls(p(2, 6)), slows(p(2, 5), p(2, 4), p(3, 4), p(1, 5))),
          cells(cells(walls(p(7, 1)), slows(p(7, 0), p(3, 6), p(7, 2), p(8, 1))), gates(p(1, 8))),
          cells(
              walls(p(9, 3), p(4, 0)),
              slows(p(7, 0), p(8, 0), p(7, 1), p(9, 1), p(6, 1), p(8, 4))));

  @Test
  void catalogFormsTheAcceptedCharacterAndGridTutorial() {
    List<LevelDefinition> levels = Levels.catalog().levels();

    assertEquals(10, levels.size());
    assertEquals(
        List.of(5, 5, 6, 6, 7, 7, 7, 8, 9, 10),
        levels.stream().map(level -> level.gridSize().rows()).toList());
    assertEquals(
        List.of(
            SolverBehavior.RANDOM,
            SolverBehavior.LEFT_PRIORITY,
            SolverBehavior.LEAST_VISITED,
            SolverBehavior.LINE_OF_SIGHT,
            SolverBehavior.RANDOM,
            SolverBehavior.LEFT_PRIORITY,
            SolverBehavior.LEAST_VISITED,
            SolverBehavior.LINE_OF_SIGHT,
            SolverBehavior.RANDOM,
            SolverBehavior.RANDOM),
        levels.stream().map(level -> level.primarySolver().behavior()).toList());
    assertTrue(levels.subList(0, 9).stream().allMatch(level -> level.solvers().size() == 1));
    assertEquals(2, Levels.levelTen().solvers().size());
    assertEquals(
        List.of(SolverBehavior.RANDOM, SolverBehavior.LEFT_PRIORITY),
        Levels.levelTen().solvers().stream().map(LevelSolver::behavior).toList());
  }

  @Test
  void firstFourLevelsArePresetWallTutorialsWithOnlyOneOrTwoEditsRemaining() {
    List<LevelDefinition> tutorials = Levels.catalog().levels().subList(0, 4);

    assertEquals(
        List.of(9, 9, 13, 13),
        tutorials.stream().map(level -> level.presetCells().size()).toList());
    assertTrue(tutorials.stream().allMatch(level -> level.fixedCells().isEmpty()));
    assertTrue(
        tutorials.stream()
            .flatMap(level -> level.presetCells().stream())
            .allMatch(cell -> cell.type() == PlaceableCellType.WALL));
    assertEquals(
        List.of(
            CellSupply.finite(1), CellSupply.finite(1), CellSupply.finite(2), CellSupply.finite(2)),
        tutorials.stream()
            .map(MazeState::initial)
            .map(maze -> maze.remainingSupply(PlaceableCellType.WALL))
            .toList());
    assertTrue(
        tutorials.stream()
            .allMatch(
                level ->
                    level.supplyFor(PlaceableCellType.SLOW_FLOOR).equals(CellSupply.finite(0))));
  }

  @Test
  void firstFourTutorialSolutionsRerouteAcrossAnAlternatePathAndCrossTheirTargets() {
    List<SolverRunResult> expected =
        List.of(
            reached(Levels.levelOne(), 9000, 36),
            reached(Levels.levelTwo(), 4500, 18),
            reached(Levels.levelThree(), 6000, 24),
            reached(Levels.levelFour(), 6000, 24));

    for (int index = 0; index < 4; index++) {
      LevelDefinition level = Levels.catalog().levels().get(index);
      MazeState initial = MazeState.initial(level);
      MazeState passing = apply(initial, PASSING_EDITS.get(index));

      assertFalse(passed(run(initial), level));
      assertTrue(passing.hasPathFromStartToGoal());
      assertEquals(expected.get(index), run(passing));
      assertTrue(passed(run(passing), level));
    }
  }

  @Test
  void firstFourTutorialMazesOfferMoreThanOneRouteBeforeThePlayerEditsThem() {
    for (LevelDefinition level : Levels.catalog().levels().subList(0, 4)) {
      MazeState initial = MazeState.initial(level);
      LevelSolver solver = level.primarySolver();

      assertEquals(2, countRoutesUpToTwo(initial, solver.start(), solver.goal(), new HashSet<>()));
    }
  }

  @Test
  void levelsThreeAndFourNeedBothRemainingWalls() {
    for (int index : List.of(2, 3)) {
      LevelDefinition level = Levels.catalog().levels().get(index);
      MazeState initial = MazeState.initial(level);
      Map<GridPosition, PlaceableCellType> edits = PASSING_EDITS.get(index);

      for (GridPosition wall : edits.keySet()) {
        MazeState single = apply(initial, Map.of(wall, PlaceableCellType.WALL));
        assertFalse(passed(run(single), level));
      }
      assertTrue(passed(run(apply(initial, edits)), level));
    }
  }

  @Test
  void levelFiveIsCompletelyOpenAndIntroducesSlowFloorsBesideInfiniteWalls() {
    LevelDefinition level = Levels.levelFive();
    MazeState initial = MazeState.initial(level);
    MazeState wallOnly = apply(initial, walls(p(1, 5)));
    MazeState slowOnly = apply(initial, slows(p(1, 3), p(2, 3)));
    MazeState passing = apply(initial, PASSING_EDITS.get(4));

    assertEquals(GridSize.square(7), level.gridSize());
    assertTrue(level.fixedCells().isEmpty());
    assertTrue(level.presetCells().isEmpty());
    assertTrue(initial.placedCells().isEmpty());
    assertEquals(CellSupply.infinite(), level.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(2), level.supplyFor(PlaceableCellType.SLOW_FLOOR));
    assertEquals(reached(level, 3000, 12), run(initial));
    assertEquals(reached(level, 4500, 18), run(wallOnly));
    assertEquals(reached(level, 3000, 12), run(slowOnly));
    assertFalse(passed(run(initial), level));
    assertFalse(passed(run(wallOnly), level));
    assertFalse(passed(run(slowOnly), level));
    assertEquals(reached(level, 6250, 18), run(passing));
    assertTrue(passed(run(passing), level));
  }

  @Test
  void laterSingleSolverLevelsRequireTheAcceptedMixedCellLayouts() {
    List<SolverRunResult> expected =
        List.of(
            reached(Levels.levelSix(), 7000, 22),
            reached(Levels.levelSeven(), 8750, 26),
            reached(Levels.levelEight(), 17000, 46),
            new SolverRunResult(
                p(2, 5), Levels.levelNine().maximumSolveTime(), 61, SolverRunStatus.TIMED_OUT));

    for (int index = 5; index < 9; index++) {
      LevelDefinition level = Levels.catalog().levels().get(index);
      MazeState initial = MazeState.initial(level);
      MazeState passing = apply(initial, PASSING_EDITS.get(index));

      assertFalse(passed(run(initial), level));
      assertEquals(expected.get(index - 5), run(passing));
      assertTrue(passed(run(passing), level));
    }
  }

  @Test
  void levelNineIntroducesOneAlternatingGateThatCompletesTheAcceptedSolution() {
    LevelDefinition level = Levels.levelNine();
    MazeState withoutGate =
        apply(
            MazeState.initial(level),
            cells(walls(p(7, 1)), slows(p(7, 0), p(3, 6), p(7, 2), p(8, 1))));
    MazeState passing = apply(MazeState.initial(level), PASSING_EDITS.get(8));

    assertEquals(CellSupply.finite(1), level.supplyFor(PlaceableCellType.ALTERNATING_GATE));
    assertEquals(
        List.of(
            PlaceableCellType.WALL,
            PlaceableCellType.SLOW_FLOOR,
            PlaceableCellType.ALTERNATING_GATE),
        level.initiallyAvailableCellTypes());
    assertEquals(reached(level, 17250, 54), run(withoutGate));
    assertFalse(passed(run(withoutGate), level));
    assertTrue(passed(run(passing), level));
  }

  @Test
  void levelTenIntroducesTwoProtectedSolverGoalPairsAndDelaysBothPastTarget() {
    LevelDefinition level = Levels.levelTen();
    MazeState initial = MazeState.initial(level);
    MazeState passing = apply(initial, PASSING_EDITS.get(9));
    List<SolverRunResult> results =
        level.solvers().stream()
            .map(
                solver ->
                    SolverSimulationFactory.create(passing, solver)
                        .update(level.maximumSolveTime()))
            .toList();

    assertEquals(GridSize.square(10), level.gridSize());
    assertEquals(2, level.solvers().size());
    assertEquals(
        4,
        level.solvers().stream()
            .flatMap(solver -> java.util.stream.Stream.of(solver.start(), solver.goal()))
            .distinct()
            .count());
    assertTrue(
        level.solvers().stream()
            .allMatch(
                solver ->
                    passing.isProtected(solver.start()) && passing.isProtected(solver.goal())));
    assertEquals(
        List.of(
            new SolverRunResult(
                level.solvers().get(0).goal(),
                Duration.ofMillis(12750),
                42,
                SolverRunStatus.REACHED_GOAL),
            new SolverRunResult(
                level.solvers().get(1).goal(),
                Duration.ofMillis(11500),
                34,
                SolverRunStatus.REACHED_GOAL)),
        results);
    assertTrue(
        results.stream()
            .allMatch(result -> result.elapsedTime().compareTo(level.targetSolveTime()) > 0));
  }

  @Test
  void levelTenRecordsTheFirstSolverFinishAndBothSolversMoves() {
    LevelDefinition level = Levels.levelTen();
    GameSession session =
        new GameSession(new LevelCatalog(List.of(level)), level.id(), BestResultStore.none());

    assertTrue(session.startLevel(level.id()));
    PASSING_EDITS
        .get(9)
        .forEach(
            (position, type) -> {
              session.selectCellType(type);
              assertTrue(session.placeOrReplaceCell(position).orElseThrow().accepted());
            });
    session.startRun();
    session.updateSolverRun(13.5F);

    assertTrue(session.resultPassed());
    assertEquals(new BestResult(Duration.ofMillis(11500), 71), session.bestResult());
  }

  private static SolverRunResult run(MazeState maze) {
    return SolverSimulationFactory.create(maze).update(maze.levelDefinition().maximumSolveTime());
  }

  private static boolean passed(SolverRunResult result, LevelDefinition level) {
    return GameResultEvaluator.passed(GamePhase.RESULT, result, level);
  }

  private static SolverRunResult reached(LevelDefinition level, long millis, int moves) {
    return new SolverRunResult(
        level.primarySolver().goal(),
        Duration.ofMillis(millis),
        moves,
        SolverRunStatus.REACHED_GOAL);
  }

  private static MazeState apply(MazeState initial, Map<GridPosition, PlaceableCellType> edits) {
    MazeState maze = initial;
    for (Map.Entry<GridPosition, PlaceableCellType> edit : edits.entrySet()) {
      MazeEditResult result = maze.placeOrReplace(edit.getValue(), edit.getKey());
      assertTrue(result.accepted(), result.status().toString());
      maze = result.mazeState();
    }
    return maze;
  }

  private static Map<GridPosition, PlaceableCellType> cells(
      Map<GridPosition, PlaceableCellType> first, Map<GridPosition, PlaceableCellType> second) {
    Map<GridPosition, PlaceableCellType> cells = new HashMap<>(first);
    cells.putAll(second);
    return Map.copyOf(cells);
  }

  private static Map<GridPosition, PlaceableCellType> walls(GridPosition... positions) {
    return typedCells(PlaceableCellType.WALL, positions);
  }

  private static Map<GridPosition, PlaceableCellType> slows(GridPosition... positions) {
    return typedCells(PlaceableCellType.SLOW_FLOOR, positions);
  }

  private static Map<GridPosition, PlaceableCellType> gates(GridPosition... positions) {
    return typedCells(PlaceableCellType.ALTERNATING_GATE, positions);
  }

  private static Map<GridPosition, PlaceableCellType> typedCells(
      PlaceableCellType type, GridPosition... positions) {
    Map<GridPosition, PlaceableCellType> cells = new HashMap<>();
    for (GridPosition position : positions) {
      cells.put(position, type);
    }
    return Map.copyOf(cells);
  }

  private static GridPosition p(int row, int column) {
    return new GridPosition(row, column);
  }

  private static int countRoutesUpToTwo(
      MazeState maze, GridPosition current, GridPosition goal, Set<GridPosition> visited) {
    if (current.equals(goal)) {
      return 1;
    }
    visited.add(current);
    int routes = 0;
    for (GridPosition neighbor :
        List.of(
            p(current.row() - 1, current.column()),
            p(current.row() + 1, current.column()),
            p(current.row(), current.column() - 1),
            p(current.row(), current.column() + 1))) {
      if (neighbor.isWithin(maze.levelDefinition().gridSize())
          && maze.isTraversable(neighbor)
          && !visited.contains(neighbor)) {
        routes += countRoutesUpToTwo(maze, neighbor, goal, visited);
        if (routes >= 2) {
          break;
        }
      }
    }
    visited.remove(current);
    return Math.min(routes, 2);
  }
}
