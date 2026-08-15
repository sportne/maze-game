package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import io.github.sportne.mazegame.model.solver.SolverSimulation;
import io.github.sportne.mazegame.model.solver.SolverSimulationFactory;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Test-side design evidence for the Milestone 4 cell palette and fourth level. */
final class MilestoneFourCellDesignTest {
  private static final int GRID_SIZE = 7;
  private static final GridPosition START = position(6, 3);
  private static final GridPosition GOAL = position(0, 3);
  private static final Duration MOVE_INTERVAL = Duration.ofMillis(250);
  private static final Duration TARGET = Duration.ofMillis(5500);
  private static final Duration TIMEOUT = Duration.ofMillis(6500);
  private static final int WALL_SUPPLY = 4;
  private static final int SLOW_FLOOR_SUPPLY = 3;

  private static final Set<GridPosition> PASSING_WALLS = positions(0, 0, 1, 1, 2, 2);
  private static final Set<GridPosition> WALL_ONLY_FALLBACK = positions(0, 0, 1, 1, 2, 2, 3, 1);
  private static final Set<GridPosition> PASSING_SLOW_FLOORS = positions(6, 2, 6, 1, 6, 0);
  private static final Set<GridPosition> TIMEOUT_WALLS = positions(0, 1, 1, 2, 2, 1);
  private static final Set<GridPosition> TIMEOUT_SLOW_FLOORS = positions(1, 0, 2, 0, 1, 3);

  private static final List<GridPosition> EMPTY_TRACE =
      path(6, 3, 6, 2, 6, 1, 6, 0, 5, 0, 4, 0, 3, 0, 2, 0, 1, 0, 0, 0, 0, 1, 0, 2, 0, 3);

  private static final List<GridPosition> PASSING_TRACE =
      path(
          6, 3, 6, 2, 6, 1, 6, 0, 5, 0, 4, 0, 3, 0, 2, 0, 1, 0, 2, 0, 2, 1, 3, 1, 3, 2, 3, 3, 2, 3,
          1, 3, 1, 2, 0, 2, 0, 1, 0, 2, 0, 3);

  private static final List<GridPosition> TIMEOUT_TRACE =
      path(
          6, 3, 6, 2, 6, 1, 6, 0, 5, 0, 4, 0, 3, 0, 2, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 2, 0, 3, 0,
          3, 1, 3, 2, 2, 2, 2, 3, 1, 3);

  @Test
  void recordsAcceptedLevelParametersAndCombinedPassingFixture() {
    ReferenceBoard board = acceptedBoard();
    ReferenceRun run = ReferenceSimulation.scout(board).update(TIMEOUT);

    assertEquals(GRID_SIZE, board.gridSize());
    assertEquals(START, board.start());
    assertEquals(GOAL, board.goal());
    assertEquals(
        Map.of(CellType.WALL, Supply.finite(1), CellType.SLOW_FLOOR, Supply.finite(0)),
        board.remaining());
    assertTrue(board.hasPath());
    assertEquals(PASSING_TRACE, run.trace());
    assertEquals(
        List.of(
            250L, 750L, 1250L, 1750L, 2000L, 2250L, 2500L, 2750L, 3000L, 3250L, 3500L, 3750L, 4000L,
            4250L, 4500L, 4750L, 5000L, 5250L, 5500L, 5750L),
        run.decisionTimesMillis());
    assertEquals(
        new RunResult(GOAL, Duration.ofMillis(5750), 20, RunStatus.REACHED_GOAL), run.result());
    assertTrue(passed(run.result()));
  }

  @Test
  void emptyWallOnlyAndSlowOnlyLayoutsFailTheTarget() {
    assertFixture(
        board(Set.of(), Set.of()),
        EMPTY_TRACE,
        new RunResult(GOAL, Duration.ofSeconds(3), 12, RunStatus.REACHED_GOAL),
        false);
    assertFixture(
        board(PASSING_WALLS, Set.of()),
        PASSING_TRACE,
        new RunResult(GOAL, Duration.ofSeconds(5), 20, RunStatus.REACHED_GOAL),
        false);
    assertFixture(
        board(Set.of(), PASSING_SLOW_FLOORS),
        EMPTY_TRACE,
        new RunResult(GOAL, Duration.ofMillis(3750), 12, RunStatus.REACHED_GOAL),
        false);
  }

  @Test
  void fourthWallProvidesAPlayableWallOnlyFallback() {
    List<GridPosition> editable = editablePositions();
    List<RunResult> results = new ArrayList<>();
    for (int wallCount = 0; wallCount <= WALL_SUPPLY; wallCount++) {
      collectWallOnlyResults(editable, wallCount, 0, new HashSet<>(), results);
    }

    assertFalse(results.isEmpty());
    assertTrue(results.stream().allMatch(result -> result.status() == RunStatus.REACHED_GOAL));
    assertEquals(
        TIMEOUT, results.stream().map(RunResult::elapsed).max(Duration::compareTo).orElseThrow());
    assertTrue(results.stream().anyMatch(MilestoneFourCellDesignTest::passed));
    RunResult fallback =
        ReferenceSimulation.scout(board(WALL_ONLY_FALLBACK, Set.of())).update(TIMEOUT).result();
    assertEquals(new RunResult(GOAL, Duration.ofSeconds(6), 24, RunStatus.REACHED_GOAL), fallback);
    assertTrue(passed(fallback));
  }

  @Test
  void inventoryRejectsInsufficientSupplyAndRecordsReplacementTransitions() {
    EditResult fourthWall = acceptedBoard().placeOrReplace(CellType.WALL, position(4, 4));
    assertTrue(fourthWall.accepted());
    ReferenceBoard full = fourthWall.board();
    EditResult exhausted = full.placeOrReplace(CellType.WALL, position(4, 5));

    assertFalse(exhausted.accepted());
    assertEquals(EditStatus.EXHAUSTED, exhausted.status());
    assertSame(full, exhausted.board());
    assertEquals(
        Map.of(CellType.WALL, Supply.finite(0), CellType.SLOW_FLOOR, Supply.finite(0)),
        exhausted.board().remaining());

    ReferenceBoard initial = ReferenceBoard.empty();
    EditResult placedWall = initial.placeOrReplace(CellType.WALL, position(4, 4));
    EditResult replaced = placedWall.board().placeOrReplace(CellType.SLOW_FLOOR, position(4, 4));
    EditResult recovered = replaced.board().placeOrReplace(CellType.SLOW_FLOOR, position(4, 4));

    assertEquals(EditStatus.PLACED, placedWall.status());
    assertEquals(
        Map.of(CellType.WALL, Supply.finite(3), CellType.SLOW_FLOOR, Supply.finite(3)),
        placedWall.board().remaining());
    assertEquals(EditStatus.REPLACED, replaced.status());
    assertEquals(CellType.SLOW_FLOOR, replaced.board().cells().get(position(4, 4)));
    assertEquals(
        Map.of(CellType.WALL, Supply.finite(4), CellType.SLOW_FLOOR, Supply.finite(2)),
        replaced.board().remaining());
    assertEquals(EditStatus.REMOVED, recovered.status());
    assertEquals(
        Map.of(CellType.WALL, Supply.finite(4), CellType.SLOW_FLOOR, Supply.finite(3)),
        recovered.board().remaining());
  }

  @Test
  void releasedInfiniteWallAndZeroSlowFloorSuppliesRemainCompatible() {
    ReferenceBoard released = ReferenceBoard.releasedLevel();
    ReferenceBoard withWalls = released;
    for (GridPosition destination : editablePositions().subList(0, 8)) {
      EditResult result = withWalls.placeOrReplace(CellType.WALL, destination);
      assertTrue(result.accepted());
      withWalls = result.board();
    }

    assertEquals(Supply.unlimited(), withWalls.remaining().get(CellType.WALL));
    assertEquals(Supply.finite(0), withWalls.remaining().get(CellType.SLOW_FLOOR));
    assertEquals(
        EditStatus.EXHAUSTED,
        withWalls.placeOrReplace(CellType.SLOW_FLOOR, position(4, 4)).status());

    EditResult removed = withWalls.placeOrReplace(CellType.WALL, editablePositions().get(0));
    assertEquals(EditStatus.REMOVED, removed.status());
    assertEquals(Supply.unlimited(), removed.board().remaining().get(CellType.WALL));

    MazeState production = MazeState.empty(Levels.milestoneOne());
    assertTrue(production.withWall(position(1, 1)).withoutWall(position(1, 1)).walls().isEmpty());
  }

  @Test
  void editorRejectsProtectedAndOutsideDestinationsWithoutChangingInventory() {
    ReferenceBoard board = ReferenceBoard.empty();
    for (GridPosition protectedCell : List.of(START, GOAL)) {
      EditResult result = board.placeOrReplace(CellType.SLOW_FLOOR, protectedCell);
      assertEquals(EditStatus.PROTECTED, result.status());
      assertFalse(result.accepted());
      assertSame(board, result.board());
    }
    for (GridPosition outside : List.of(position(-1, 0), position(0, GRID_SIZE))) {
      EditResult result = board.placeOrReplace(CellType.WALL, outside);
      assertEquals(EditStatus.OUTSIDE_GRID, result.status());
      assertFalse(result.accepted());
      assertSame(board, result.board());
    }
    assertEquals(
        Map.of(CellType.WALL, Supply.finite(4), CellType.SLOW_FLOOR, Supply.finite(3)),
        board.remaining());
  }

  @Test
  void wholeAndChunkedUpdatesHaveIdenticalTraceTimingAndResult() {
    ReferenceSimulation whole = ReferenceSimulation.scout(acceptedBoard());
    ReferenceSimulation chunked = ReferenceSimulation.scout(acceptedBoard());

    whole.update(TIMEOUT);
    for (int update = 0; update < 65; update++) {
      chunked.update(Duration.ofMillis(100));
    }

    assertEquals(whole.snapshot(), chunked.snapshot());
  }

  @Test
  void slowFloorChangesWaitsButNotScoutRouteOrMoveCount() {
    ReferenceRun normal = ReferenceSimulation.scout(board(PASSING_WALLS, Set.of())).update(TIMEOUT);
    ReferenceRun slowed = ReferenceSimulation.scout(acceptedBoard()).update(TIMEOUT);

    assertEquals(normal.trace(), slowed.trace());
    assertEquals(normal.result().moveCount(), slowed.result().moveCount());
    assertEquals(
        Duration.ofMillis(750), slowed.result().elapsed().minus(normal.result().elapsed()));
  }

  @Test
  void slowFloorChangesWaitsButNotSeededRandomRouteOrMoveCount() {
    ReferenceRun normal =
        ReferenceSimulation.random(board(PASSING_WALLS, Set.of()), 41L).update(TIMEOUT);
    ReferenceRun slowed = ReferenceSimulation.random(acceptedBoard(), 41L).update(TIMEOUT);

    assertEquals(normal.trace(), slowed.trace());
    assertEquals(normal.result().moveCount(), slowed.result().moveCount());
    assertEquals(
        path(
            6, 3, 6, 2, 5, 2, 4, 2, 5, 2, 4, 2, 3, 2, 3, 1, 4, 1, 4, 2, 4, 3, 4, 4, 3, 4, 2, 4, 1,
            4, 0, 4, 0, 3),
        slowed.trace());
    assertEquals(
        new RunResult(GOAL, Duration.ofMillis(4250), 16, RunStatus.REACHED_GOAL), slowed.result());
    assertEquals(
        Duration.ofMillis(250), slowed.result().elapsed().minus(normal.result().elapsed()));
  }

  @Test
  void referenceTracesMatchBothExistingProductionSolverImplementations() {
    assertMatchesProduction(
        SolverBehavior.LEFT_PRIORITY,
        53L,
        ReferenceSimulation.scout(board(PASSING_WALLS, Set.of())));
    assertMatchesProduction(
        SolverBehavior.RANDOM,
        41L,
        ReferenceSimulation.random(board(PASSING_WALLS, Set.of()), 41L));
  }

  @Test
  void timeoutDuringSlowWaitEndsWithoutAnExtraMove() {
    ReferenceRun run =
        ReferenceSimulation.scout(board(TIMEOUT_WALLS, TIMEOUT_SLOW_FLOORS)).update(TIMEOUT);

    assertEquals(TIMEOUT_TRACE, run.trace());
    assertEquals(new RunResult(position(1, 3), TIMEOUT, 19, RunStatus.TIMED_OUT), run.result());
    assertEquals(19, run.decisionTimesMillis().size());
  }

  @Test
  void goalArrivalWinsImmediatelyWithoutSchedulingItsSlowDelay() {
    ReferenceBoard board = board(Set.of(), Set.of(position(0, 2)));
    ReferenceRun run = ReferenceSimulation.scout(board).update(TIMEOUT);

    assertEquals(EMPTY_TRACE, run.trace());
    assertEquals(
        new RunResult(GOAL, Duration.ofMillis(3250), 12, RunStatus.REACHED_GOAL), run.result());
  }

  @Test
  void scoutIsSelectedBecauseRandomDoesNotTeachTheCombinedTarget() {
    ReferenceRun scout = ReferenceSimulation.scout(acceptedBoard()).update(TIMEOUT);
    ReferenceRun randomEmpty =
        ReferenceSimulation.random(board(Set.of(), Set.of()), 53L).update(TIMEOUT);
    ReferenceRun randomCombined = ReferenceSimulation.random(acceptedBoard(), 53L).update(TIMEOUT);

    assertEquals(RunStatus.REACHED_GOAL, scout.result().status());
    assertEquals(Duration.ofMillis(5750), scout.result().elapsed());
    assertEquals(
        new RunResult(position(4, 3), TIMEOUT, 26, RunStatus.TIMED_OUT), randomEmpty.result());
    assertEquals(
        new RunResult(position(4, 4), TIMEOUT, 25, RunStatus.TIMED_OUT), randomCombined.result());
    assertTrue(passed(randomEmpty.result()));
    assertTrue(passed(randomCombined.result()));
  }

  private static void assertMatchesProduction(
      SolverBehavior behavior, long seed, ReferenceSimulation referenceSimulation) {
    LevelDefinition level =
        new LevelDefinition(
            "milestone-4-reference-" + behavior.name().toLowerCase(java.util.Locale.ROOT),
            "Milestone 4 Reference",
            GridSize.square(GRID_SIZE),
            START,
            GOAL,
            Duration.ofSeconds(25),
            TARGET,
            TIMEOUT,
            MOVE_INTERVAL,
            PlaceableCellSupply.releasedDefaults(),
            behavior,
            seed);
    MazeState productionMaze = new MazeState(level, PASSING_WALLS);
    SolverSimulation production = SolverSimulationFactory.create(productionMaze);
    List<GridPosition> productionTrace = new ArrayList<>();
    productionTrace.add(START);
    while (production.result().status() == SolverRunStatus.RUNNING) {
      productionTrace.add(production.update(MOVE_INTERVAL).position());
    }
    ReferenceRun reference = referenceSimulation.update(TIMEOUT);

    assertEquals(reference.trace(), productionTrace);
    assertEquals(reference.result().position(), production.result().position());
    assertEquals(reference.result().elapsed(), production.result().elapsedTime());
    assertEquals(reference.result().moveCount(), production.result().moveCount());
    assertEquals(reference.result().status().name(), production.result().status().name());
  }

  private static void collectWallOnlyResults(
      List<GridPosition> editable,
      int wallsRemaining,
      int nextIndex,
      Set<GridPosition> selected,
      List<RunResult> results) {
    if (wallsRemaining == 0) {
      ReferenceBoard candidate = board(selected, Set.of());
      if (candidate.hasPath()) {
        results.add(ReferenceSimulation.scout(candidate).update(TIMEOUT).result());
      }
      return;
    }
    for (int index = nextIndex; index <= editable.size() - wallsRemaining; index++) {
      selected.add(editable.get(index));
      collectWallOnlyResults(editable, wallsRemaining - 1, index + 1, selected, results);
      selected.remove(editable.get(index));
    }
  }

  private static void assertFixture(
      ReferenceBoard board,
      List<GridPosition> expectedTrace,
      RunResult expectedResult,
      boolean expectedPass) {
    ReferenceRun run = ReferenceSimulation.scout(board).update(TIMEOUT);
    assertTrue(board.hasPath());
    assertEquals(expectedTrace, run.trace());
    assertEquals(expectedResult, run.result());
    assertEquals(expectedPass, passed(run.result()));
  }

  private static boolean passed(RunResult result) {
    return result.elapsed().compareTo(TARGET) > 0;
  }

  private static ReferenceBoard acceptedBoard() {
    return board(PASSING_WALLS, PASSING_SLOW_FLOORS);
  }

  private static ReferenceBoard board(Set<GridPosition> walls, Set<GridPosition> slowFloors) {
    Map<GridPosition, CellType> cells = new HashMap<>();
    walls.forEach(position -> cells.put(position, CellType.WALL));
    slowFloors.forEach(position -> cells.put(position, CellType.SLOW_FLOOR));
    return new ReferenceBoard(cells);
  }

  private static List<GridPosition> editablePositions() {
    List<GridPosition> positions = new ArrayList<>();
    for (int row = 0; row < GRID_SIZE; row++) {
      for (int column = 0; column < GRID_SIZE; column++) {
        GridPosition position = position(row, column);
        if (!position.equals(START) && !position.equals(GOAL)) {
          positions.add(position);
        }
      }
    }
    return List.copyOf(positions);
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }

  private static Set<GridPosition> positions(int... coordinatePairs) {
    return Set.copyOf(path(coordinatePairs));
  }

  private static List<GridPosition> path(int... coordinatePairs) {
    List<GridPosition> positions = new ArrayList<>();
    for (int index = 0; index < coordinatePairs.length; index += 2) {
      positions.add(position(coordinatePairs[index], coordinatePairs[index + 1]));
    }
    return List.copyOf(positions);
  }

  private enum CellType {
    WALL,
    SLOW_FLOOR
  }

  private enum EditStatus {
    PLACED,
    REPLACED,
    REMOVED,
    EXHAUSTED,
    OUTSIDE_GRID,
    PROTECTED,
    BLOCKS_PATH
  }

  private enum RunStatus {
    RUNNING,
    REACHED_GOAL,
    TIMED_OUT
  }

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

    private GridPosition move(GridPosition origin) {
      return position(origin.row() + rowChange, origin.column() + columnChange);
    }

    private List<Direction> priority() {
      return switch (this) {
        case NORTH -> List.of(WEST, NORTH, EAST, SOUTH);
        case EAST -> List.of(NORTH, EAST, SOUTH, WEST);
        case SOUTH -> List.of(EAST, SOUTH, WEST, NORTH);
        case WEST -> List.of(SOUTH, WEST, NORTH, EAST);
      };
    }
  }

  private record EditResult(ReferenceBoard board, EditStatus status, boolean accepted) {}

  private record Supply(boolean infinite, int finiteCount) {
    private Supply {
      if (finiteCount < 0) {
        throw new IllegalArgumentException("finiteCount must not be negative");
      }
    }

    private static Supply finite(int count) {
      return new Supply(false, count);
    }

    private static Supply unlimited() {
      return new Supply(true, 0);
    }

    private boolean available() {
      return infinite || finiteCount > 0;
    }

    private Supply consume() {
      return infinite ? this : finite(finiteCount - 1);
    }
  }

  private record RunResult(
      GridPosition position, Duration elapsed, int moveCount, RunStatus status) {}

  private record ReferenceRun(
      RunResult result, List<GridPosition> trace, List<Long> decisionTimesMillis) {}

  private static final class ReferenceBoard {
    private final Map<GridPosition, CellType> cells;
    private final Map<CellType, Supply> authoredSupply;
    private final Map<CellType, Supply> remaining;

    private ReferenceBoard(Map<GridPosition, CellType> cells) {
      this(
          cells,
          Map.of(
              CellType.WALL,
              Supply.finite(WALL_SUPPLY),
              CellType.SLOW_FLOOR,
              Supply.finite(SLOW_FLOOR_SUPPLY)));
    }

    private ReferenceBoard(
        Map<GridPosition, CellType> cells, Map<CellType, Supply> authoredSupply) {
      this.cells = Map.copyOf(cells);
      this.authoredSupply = Map.copyOf(authoredSupply);
      if (!this.authoredSupply.keySet().equals(Set.of(CellType.values()))) {
        throw new IllegalArgumentException("every cell type must have one authored supply");
      }
      EnumMap<CellType, Supply> counts = new EnumMap<>(this.authoredSupply);
      cells
          .values()
          .forEach(
              type ->
                  counts.compute(type, (unused, count) -> Objects.requireNonNull(count).consume()));
      remaining = Map.copyOf(counts);
    }

    private static ReferenceBoard empty() {
      return new ReferenceBoard(Map.of());
    }

    private static ReferenceBoard releasedLevel() {
      return new ReferenceBoard(
          Map.of(),
          Map.of(CellType.WALL, Supply.unlimited(), CellType.SLOW_FLOOR, Supply.finite(0)));
    }

    private int gridSize() {
      return GRID_SIZE;
    }

    private GridPosition start() {
      return START;
    }

    private GridPosition goal() {
      return GOAL;
    }

    private Map<GridPosition, CellType> cells() {
      return cells;
    }

    private Map<CellType, Supply> remaining() {
      return remaining;
    }

    private EditResult placeOrReplace(CellType type, GridPosition destination) {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(destination, "destination");
      if (!isInside(destination)) {
        return new EditResult(this, EditStatus.OUTSIDE_GRID, false);
      }
      if (destination.equals(START) || destination.equals(GOAL)) {
        return new EditResult(this, EditStatus.PROTECTED, false);
      }
      CellType existing = cells.get(destination);
      if (existing == type) {
        Map<GridPosition, CellType> updated = new HashMap<>(cells);
        updated.remove(destination);
        return new EditResult(
            new ReferenceBoard(updated, authoredSupply), EditStatus.REMOVED, true);
      }
      if (!remaining.get(type).available()) {
        return new EditResult(this, EditStatus.EXHAUSTED, false);
      }
      Map<GridPosition, CellType> updated = new HashMap<>(cells);
      updated.put(destination, type);
      ReferenceBoard candidate = new ReferenceBoard(updated, authoredSupply);
      if (!candidate.hasPath()) {
        return new EditResult(this, EditStatus.BLOCKS_PATH, false);
      }
      return new EditResult(
          candidate, existing == null ? EditStatus.PLACED : EditStatus.REPLACED, true);
    }

    private boolean hasPath() {
      Queue<GridPosition> frontier = new ArrayDeque<>();
      Set<GridPosition> visited = new HashSet<>();
      frontier.add(START);
      visited.add(START);
      while (!frontier.isEmpty()) {
        GridPosition current = frontier.remove();
        if (current.equals(GOAL)) {
          return true;
        }
        for (Direction direction : Direction.values()) {
          GridPosition candidate = direction.move(current);
          if (isOpen(candidate) && visited.add(candidate)) {
            frontier.add(candidate);
          }
        }
      }
      return false;
    }

    private boolean isOpen(GridPosition position) {
      return isInside(position) && cells.get(position) != CellType.WALL;
    }

    private boolean isSlow(GridPosition position) {
      return cells.get(position) == CellType.SLOW_FLOOR;
    }

    private static boolean isInside(GridPosition position) {
      return position.row() >= 0
          && position.row() < GRID_SIZE
          && position.column() >= 0
          && position.column() < GRID_SIZE;
    }
  }

  private static final class ReferenceSimulation {
    private final ReferenceBoard board;
    private final Random random;
    private final List<GridPosition> trace = new ArrayList<>();
    private final List<Long> decisionTimesMillis = new ArrayList<>();
    private GridPosition position = START;
    private Direction heading = Direction.NORTH;
    private Duration elapsed = Duration.ZERO;
    private Duration untilDecision = MOVE_INTERVAL;
    private boolean delayedDecision;
    private int moveCount;
    private RunStatus status = RunStatus.RUNNING;

    private ReferenceSimulation(ReferenceBoard board, Random random) {
      this.board = Objects.requireNonNull(board, "board");
      this.random = random;
      trace.add(position);
    }

    private static ReferenceSimulation scout(ReferenceBoard board) {
      return new ReferenceSimulation(board, null);
    }

    private static ReferenceSimulation random(ReferenceBoard board, long seed) {
      return new ReferenceSimulation(board, new Random(seed));
    }

    private ReferenceRun update(Duration delta) {
      Objects.requireNonNull(delta, "delta");
      Duration remaining = delta;
      while (status == RunStatus.RUNNING && !remaining.isZero()) {
        Duration untilTimeout = TIMEOUT.minus(elapsed);
        Duration step = min(remaining, min(untilDecision, untilTimeout));
        elapsed = elapsed.plus(step);
        remaining = remaining.minus(step);
        untilDecision = untilDecision.minus(step);
        boolean reachedTimeout = elapsed.compareTo(TIMEOUT) >= 0;
        if (untilDecision.isZero() && !(delayedDecision && reachedTimeout)) {
          moveOnce();
          if (status == RunStatus.RUNNING && reachedTimeout) {
            status = RunStatus.TIMED_OUT;
          }
        } else if (reachedTimeout) {
          status = RunStatus.TIMED_OUT;
        }
      }
      return snapshot();
    }

    private void moveOnce() {
      position = random == null ? scoutDestination() : randomDestination();
      moveCount++;
      trace.add(position);
      decisionTimesMillis.add(elapsed.toMillis());
      if (position.equals(GOAL)) {
        status = RunStatus.REACHED_GOAL;
      } else {
        delayedDecision = board.isSlow(position);
        untilDecision = delayedDecision ? MOVE_INTERVAL.multipliedBy(2) : MOVE_INTERVAL;
      }
    }

    private GridPosition scoutDestination() {
      for (Direction direction : heading.priority()) {
        GridPosition candidate = direction.move(position);
        if (board.isOpen(candidate)) {
          heading = direction;
          return candidate;
        }
      }
      return position;
    }

    private GridPosition randomDestination() {
      List<GridPosition> candidates = new ArrayList<>();
      for (Direction direction :
          List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
        GridPosition candidate = direction.move(position);
        if (board.isOpen(candidate)) {
          candidates.add(candidate);
        }
      }
      return candidates.isEmpty() ? position : candidates.get(random.nextInt(candidates.size()));
    }

    private ReferenceRun snapshot() {
      return new ReferenceRun(
          new RunResult(position, elapsed, moveCount, status),
          List.copyOf(trace),
          List.copyOf(decisionTimesMillis));
    }

    private static Duration min(Duration first, Duration second) {
      return first.compareTo(second) <= 0 ? first : second;
    }
  }
}
