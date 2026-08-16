package io.github.sportne.mazegame.model.solver;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.TestMazeStates;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.state.GamePhase;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

// Test-side evidence for the GAMEPLAY-01 cell-type shortlist.
final class GameplayAdditionalCellTypesDesignTest {
  private static final Duration MOVE_INTERVAL = Duration.ofMillis(100);
  private static final int MAXIMUM_DECISIONS = 250;
  private static final List<CardinalDirection> REFERENCE_DIRECTION_ORDER =
      List.of(
          CardinalDirection.NORTH,
          CardinalDirection.EAST,
          CardinalDirection.SOUTH,
          CardinalDirection.WEST);
  private static final List<CardinalDirection> REFERENCE_RANDOM_ORDER =
      List.of(
          CardinalDirection.NORTH,
          CardinalDirection.SOUTH,
          CardinalDirection.WEST,
          CardinalDirection.EAST);

  @Test
  void rightTurnFloorOverridesEveryBehaviorWithoutRemovingOpenExits() {
    GridPosition start = position(3, 1);
    GridPosition turnFloor = position(2, 1);
    GridPosition goal = position(0, 1);
    ReferenceBoard board =
        new ReferenceBoard(4, Set.of(position(3, 0), position(3, 2)), Set.of(turnFloor), Set.of());

    for (SolverBehavior behavior : SolverBehavior.values()) {
      ReferenceSolver solver = new ReferenceSolver(board, start, goal, behavior, 7L);

      solver.move();
      solver.move();

      assertEquals(List.of(start, turnFloor, position(2, 2)), solver.trace());
      assertTrue(board.canMove(turnFloor, CardinalDirection.NORTH));
      assertTrue(board.canMove(turnFloor, CardinalDirection.WEST));
      assertTrue(board.canMove(turnFloor, CardinalDirection.EAST));
      if (behavior == SolverBehavior.RANDOM) {
        assertEquals(1, solver.randomDecisionCount());
      }
      if (behavior == SolverBehavior.LINE_OF_SIGHT) {
        assertEquals(0, solver.randomDecisionCount());
      }
      if (behavior == SolverBehavior.LEFT_PRIORITY) {
        assertEquals(CardinalDirection.EAST, solver.heading());
      }
      if (behavior == SolverBehavior.LEAST_VISITED) {
        assertEquals(1, solver.visits().get(position(2, 2)));
      }
    }
  }

  @Test
  void rightTurnForcedMovePreservesTheNextSeededExplorationDecision() {
    GridPosition start = position(3, 1);
    GridPosition turnFloor = position(2, 1);
    GridPosition goal = position(0, 1);
    ReferenceBoard board =
        new ReferenceBoard(4, Set.of(position(3, 0), position(3, 2)), Set.of(turnFloor), Set.of());
    ReferenceSolver random = new ReferenceSolver(board, start, goal, SolverBehavior.RANDOM, 7L);
    ReferenceSolver seeker =
        new ReferenceSolver(board, start, goal, SolverBehavior.LINE_OF_SIGHT, 7L);

    random.move();
    random.move();
    random.move();
    seeker.move();
    seeker.move();
    seeker.move();

    assertEquals(List.of(start, turnFloor, position(2, 2), position(2, 3)), random.trace());
    assertEquals(2, random.randomDecisionCount());
    assertEquals(List.of(start, turnFloor, position(2, 2), turnFloor), seeker.trace());
    assertEquals(1, seeker.randomDecisionCount());
  }

  @Test
  void rightTurnDirectionTableCoversEveryArrivalIndependently() {
    assertEquals(CardinalDirection.EAST, rightOf(CardinalDirection.NORTH));
    assertEquals(CardinalDirection.SOUTH, rightOf(CardinalDirection.EAST));
    assertEquals(CardinalDirection.WEST, rightOf(CardinalDirection.SOUTH));
    assertEquals(CardinalDirection.NORTH, rightOf(CardinalDirection.WEST));
  }

  @Test
  void rightTurnFloorOverridesVisibleGoalButFallsBackWhenRightIsBlocked() {
    GridPosition start = position(3, 1);
    GridPosition turnFloor = position(2, 1);
    GridPosition goal = position(0, 1);
    Set<GridPosition> entryWalls = Set.of(position(3, 0), position(3, 2));
    ReferenceBoard open = new ReferenceBoard(4, entryWalls, Set.of(turnFloor), Set.of());
    ReferenceSolver seeker =
        new ReferenceSolver(open, start, goal, SolverBehavior.LINE_OF_SIGHT, 7L);

    seeker.move();
    seeker.move();

    assertEquals(position(2, 2), seeker.position());
    assertEquals(0, seeker.randomDecisionCount());

    Set<GridPosition> blockedWalls = Set.of(position(3, 0), position(3, 2), position(2, 2));
    ReferenceBoard withTurn = new ReferenceBoard(4, blockedWalls, Set.of(turnFloor), Set.of());
    ReferenceBoard withoutTurn = new ReferenceBoard(4, blockedWalls, Set.of(), Set.of());
    for (SolverBehavior behavior : SolverBehavior.values()) {
      ReferenceSolver candidate = new ReferenceSolver(withTurn, start, goal, behavior, 17L);
      ReferenceSolver baseline = new ReferenceSolver(withoutTurn, start, goal, behavior, 17L);

      candidate.run();
      baseline.run();

      assertEquals(baseline.snapshot(), candidate.snapshot());
    }
  }

  @Test
  void railGatePreservesVerticalRouteWhileClosingHorizontalShortcut() {
    GridPosition rail = position(2, 2);
    Set<GridPosition> walls =
        Set.of(position(4, 1), position(4, 3), position(3, 1), position(3, 3));
    ReferenceBoard empty = new ReferenceBoard(5, walls, Set.of(), Set.of());
    ReferenceBoard withRail = new ReferenceBoard(5, walls, Set.of(), Set.of(rail));
    ReferenceBoard withWall = new ReferenceBoard(5, union(walls, Set.of(rail)), Set.of(), Set.of());
    GridPosition horizontalStart = position(2, 0);
    GridPosition horizontalGoal = position(2, 4);
    GridPosition verticalStart = position(4, 2);
    GridPosition verticalGoal = position(0, 2);

    assertEquals(4, shortestPath(empty, horizontalStart, horizontalGoal));
    assertEquals(6, shortestPath(withRail, horizontalStart, horizontalGoal));
    assertEquals(4, shortestPath(withRail, verticalStart, verticalGoal));
    assertEquals(-1, shortestPath(withWall, verticalStart, verticalGoal));
    assertFalse(withRail.canMove(position(2, 1), CardinalDirection.EAST));
    assertFalse(withRail.canMove(rail, CardinalDirection.EAST));
    assertFalse(withRail.canMove(position(2, 3), CardinalDirection.WEST));
    assertFalse(withRail.canMove(rail, CardinalDirection.WEST));
    assertTrue(withRail.canMove(position(3, 2), CardinalDirection.NORTH));
    assertTrue(withRail.canMove(rail, CardinalDirection.NORTH));
    assertTrue(withRail.canMove(position(1, 2), CardinalDirection.SOUTH));
    assertTrue(withRail.canMove(rail, CardinalDirection.SOUTH));
  }

  @Test
  void railGateRulesRemainDeterministicForAllFourSolvers() {
    GridPosition rail = position(2, 2);
    Set<GridPosition> walls =
        Set.of(position(4, 1), position(4, 3), position(3, 1), position(3, 3));
    ReferenceBoard board = new ReferenceBoard(5, walls, Set.of(), Set.of(rail));
    for (SolverBehavior behavior : SolverBehavior.values()) {
      assertDeterministicRoute(board, position(2, 0), position(2, 4), behavior, 31L);
      assertDeterministicRoute(board, position(4, 2), position(0, 2), behavior, 31L);
    }
  }

  @Test
  void representativeMultiSolverBoardKeepsIndependentRoutes() {
    GridPosition rail = position(2, 2);
    Set<GridPosition> walls =
        Set.of(position(4, 1), position(4, 3), position(3, 1), position(3, 3));
    ReferenceBoard board = new ReferenceBoard(5, walls, Set.of(), Set.of(rail));
    ReferenceSolver random =
        new ReferenceSolver(board, position(2, 0), position(2, 4), SolverBehavior.RANDOM, 31L);
    ReferenceSolver scout =
        new ReferenceSolver(
            board, position(4, 2), position(0, 2), SolverBehavior.LEFT_PRIORITY, 1L);

    random.run();
    scout.run();

    assertEquals(position(2, 4), random.position());
    assertEquals(position(0, 2), scout.position());
    assertTrue(random.trace().size() > scout.trace().size());
    assertFalse(crossesRailHorizontally(random.trace(), rail));
    assertFalse(crossesRailHorizontally(scout.trace(), rail));
  }

  @Test
  void rightTurnMultiSolverBoardUsesEachSolversOwnArrivalDirection() {
    GridPosition turnFloor = position(2, 2);
    Set<GridPosition> walls =
        Set.of(position(4, 2), position(3, 1), position(3, 3), position(1, 1), position(2, 0));
    ReferenceBoard board = new ReferenceBoard(5, walls, Set.of(turnFloor), Set.of());
    GridPosition southStart = position(3, 2);
    GridPosition westStart = position(2, 1);
    ReferenceSolver random =
        new ReferenceSolver(board, southStart, position(0, 4), SolverBehavior.RANDOM, 31L);
    ReferenceSolver scout =
        new ReferenceSolver(board, westStart, position(4, 4), SolverBehavior.LEFT_PRIORITY, 1L);

    random.move();
    scout.move();
    random.move();
    scout.move();

    assertEquals(List.of(southStart, turnFloor, position(2, 3)), random.trace());
    assertEquals(List.of(westStart, turnFloor, position(3, 2)), scout.trace());
    assertEquals(CardinalDirection.EAST, random.heading());
    assertEquals(CardinalDirection.SOUTH, scout.heading());
  }

  @Test
  void referenceBehaviorsMatchProductionWhenCandidateCellsAreAbsent() {
    GridPosition start = position(3, 1);
    GridPosition goal = position(0, 1);
    Set<GridPosition> walls = Set.of(position(3, 0), position(3, 2));
    ReferenceBoard board = new ReferenceBoard(4, walls, Set.of(), Set.of());

    for (SolverBehavior behavior : SolverBehavior.values()) {
      ReferenceSolver reference = new ReferenceSolver(board, start, goal, behavior, 23L);
      reference.run();

      assertEquals(
          productionTrace(board, start, goal, behavior, 23L),
          reference.trace(),
          () -> "reference drift for " + behavior);
    }

    GridPosition exploringStart = position(3, 1);
    GridPosition exploringGoal = position(0, 2);
    ReferenceBoard exploringBoard = new ReferenceBoard(4, Set.of(), Set.of(), Set.of());
    ReferenceSolver exploringSeeker =
        new ReferenceSolver(
            exploringBoard, exploringStart, exploringGoal, SolverBehavior.LINE_OF_SIGHT, 37L);
    exploringSeeker.run();

    assertTrue(exploringSeeker.randomDecisionCount() > 0);
    assertEquals(
        productionTrace(
            exploringBoard, exploringStart, exploringGoal, SolverBehavior.LINE_OF_SIGHT, 37L),
        exploringSeeker.trace());
  }

  @Test
  void twoAdditionalPaletteItemsFitEverySupportedReferenceViewport() {
    for (int[] viewport :
        List.of(
            new int[] {1280, 720},
            new int[] {390, 844},
            new int[] {844, 286},
            new int[] {756, 286},
            new int[] {600, 421},
            new int[] {568, 270})) {
      List<ScreenRectangle> items = paletteItems(viewport[0], viewport[1], 7, 4);

      assertEquals(4, items.size());
      assertTrue(items.stream().allMatch(item -> item.width() >= 44.0F));
      assertTrue(items.stream().allMatch(item -> item.height() >= 44.0F));
      ScreenRectangle viewportBounds = new ScreenRectangle(0.0F, 0.0F, viewport[0], viewport[1]);
      assertTrue(items.stream().allMatch(item -> item.fitsWithin(viewportBounds)));
      ScreenRectangle grid = referenceGrid(viewport[0], viewport[1], 7);
      assertTrue(items.stream().noneMatch(item -> item.overlaps(grid)));
      for (ScreenRectangle action : referenceBuildActionButtons(viewport[0], viewport[1], 7)) {
        assertTrue(items.stream().noneMatch(item -> item.overlaps(action)));
      }
      for (int index = 1; index < items.size(); index++) {
        assertFalse(items.get(index - 1).overlaps(items.get(index)));
      }
    }
  }

  @Test
  void paletteCountReferenceMatchesDeclaredLayoutForExistingTypes() {
    for (int[] viewport :
        List.of(
            new int[] {1280, 720},
            new int[] {390, 844},
            new int[] {844, 286},
            new int[] {756, 286},
            new int[] {600, 421},
            new int[] {568, 270})) {
      List<ScreenRectangle> reference = paletteItems(viewport[0], viewport[1], 7, 2);
      ScreenLayout production =
          MazeGameLayout.forPhase(
              GamePhase.BUILDING,
              viewport[0],
              viewport[1],
              GridSize.square(7),
              false,
              5,
              false,
              List.of(PlaceableCellType.WALL, PlaceableCellType.SLOW_FLOOR));

      assertEquals(
          reference.get(0),
          production.bounds(MazeGameLayout.paletteItemId(PlaceableCellType.WALL)));
      assertEquals(
          reference.get(1),
          production.bounds(MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR)));
      List<ScreenRectangle> actions = referenceBuildActionButtons(viewport[0], viewport[1], 7);
      assertEquals(actions.get(0), production.bounds(MazeGameLayout.BUILD_BACK));
      assertEquals(actions.get(1), production.bounds(MazeGameLayout.BUILD_START));
      assertEquals(
          referenceGrid(viewport[0], viewport[1], 7), production.bounds(MazeGameLayout.GAME_GRID));
    }
  }

  private static void assertDeterministicRoute(
      ReferenceBoard board,
      GridPosition start,
      GridPosition goal,
      SolverBehavior behavior,
      long seed) {
    ReferenceSolver first = new ReferenceSolver(board, start, goal, behavior, seed);
    ReferenceSolver replay = new ReferenceSolver(board, start, goal, behavior, seed);

    first.run();
    replay.run();

    assertEquals(goal, first.position(), () -> behavior + " did not retain its matching route");
    assertEquals(first.snapshot(), replay.snapshot());
    assertFalse(crossesRailHorizontally(first.trace(), position(2, 2)));
  }

  private static List<GridPosition> productionTrace(
      ReferenceBoard board,
      GridPosition start,
      GridPosition goal,
      SolverBehavior behavior,
      long seed) {
    LevelDefinition level =
        singleSolverLevel(
            "gameplay-01-" + behavior.name().toLowerCase(java.util.Locale.ROOT),
            "Gameplay 01 " + behavior,
            GridSize.square(board.size()),
            start,
            goal,
            Duration.ofSeconds(1),
            Duration.ofSeconds(1),
            Duration.ofSeconds(25),
            MOVE_INTERVAL,
            PlaceableCellSupply.unlimitedWallsOnly(),
            behavior,
            seed);
    MazeState maze = TestMazeStates.withWalls(level, board.walls());
    SolverSimulation simulation = SolverSimulationFactory.create(maze);
    List<GridPosition> trace = new ArrayList<>();
    trace.add(start);
    while (simulation.result().status() == SolverRunStatus.RUNNING) {
      int previousMoves = simulation.result().moveCount();
      SolverRunResult result = simulation.update(MOVE_INTERVAL);
      if (result.moveCount() > previousMoves) {
        trace.add(result.position());
      }
    }
    return List.copyOf(trace);
  }

  private static int shortestPath(ReferenceBoard board, GridPosition start, GridPosition goal) {
    Queue<PathStep> frontier = new ArrayDeque<>();
    Set<GridPosition> visited = new HashSet<>();
    frontier.add(new PathStep(start, 0));
    visited.add(start);
    while (!frontier.isEmpty()) {
      PathStep current = frontier.remove();
      if (current.position().equals(goal)) {
        return current.distance();
      }
      for (CardinalDirection direction : CardinalDirection.values()) {
        if (board.canMove(current.position(), direction)) {
          GridPosition destination = direction.move(current.position());
          if (visited.add(destination)) {
            frontier.add(new PathStep(destination, current.distance() + 1));
          }
        }
      }
    }
    return -1;
  }

  private static boolean crossesRailHorizontally(List<GridPosition> trace, GridPosition rail) {
    for (int index = 1; index < trace.size(); index++) {
      GridPosition previous = trace.get(index - 1);
      GridPosition current = trace.get(index);
      if ((previous.equals(rail) || current.equals(rail)) && previous.row() == current.row()) {
        return true;
      }
    }
    return false;
  }

  private static List<ScreenRectangle> paletteItems(
      int screenWidth, int screenHeight, int gridSide, int itemCount) {
    boolean landscape = screenWidth > screenHeight && (screenWidth < 800 || screenHeight < 600);
    boolean portrait = screenWidth <= screenHeight && screenWidth < 800;
    ScreenRectangle grid = referenceGrid(screenWidth, screenHeight, gridSide);
    float areaX = 16.0F;
    if (landscape) {
      areaX = grid.right() + 24.0F;
    }
    float areaWidth = screenWidth - areaX - 16.0F;
    float gaps = (itemCount - 1) * 12.0F;
    float itemWidth = Math.min(56.0F, (areaWidth - gaps) / itemCount);
    float totalWidth = itemCount * itemWidth + gaps;
    float left = areaX + (areaWidth - totalWidth) / 2.0F;
    float height = portrait ? 56.0F : 44.0F;
    float y;
    if (landscape) {
      y = 16.0F;
    } else if (portrait) {
      y = 72.0F;
    } else {
      float actionY = Math.max(24.0F, grid.y() - 52.0F - 44.0F);
      y = actionY + 48.0F;
    }
    List<ScreenRectangle> items = new ArrayList<>();
    for (int index = 0; index < itemCount; index++) {
      items.add(new ScreenRectangle(left + index * (itemWidth + 12.0F), y, itemWidth, height));
    }
    return List.copyOf(items);
  }

  private static ScreenRectangle referenceGrid(int screenWidth, int screenHeight, int gridSide) {
    boolean landscape = screenWidth > screenHeight && (screenWidth < 800 || screenHeight < 600);
    boolean portrait = screenWidth <= screenHeight && screenWidth < 800;
    float available;
    if (landscape) {
      available = Math.min(screenHeight - 32.0F, Math.min(240.0F, screenWidth - 320.0F));
    } else if (portrait) {
      available = Math.min(screenWidth - 32.0F, screenHeight * 0.48F);
    } else {
      available = Math.min(Math.min(screenWidth, screenHeight) * 0.62F, screenHeight - 240.0F);
    }
    float pixels = (float) Math.floor(available / gridSide) * gridSide;
    float x = landscape ? 16.0F : (screenWidth - pixels) / 2.0F;
    float y = (screenHeight - pixels) / 2.0F;
    return new ScreenRectangle(x, y, pixels, pixels);
  }

  private static List<ScreenRectangle> referenceBuildActionButtons(
      int screenWidth, int screenHeight, int gridSide) {
    boolean landscape = screenWidth > screenHeight && (screenWidth < 800 || screenHeight < 600);
    boolean portrait = screenWidth <= screenHeight && screenWidth < 800;
    ScreenRectangle grid = referenceGrid(screenWidth, screenHeight, gridSide);
    float areaX = landscape ? grid.right() + 24.0F : 16.0F;
    float areaWidth = screenWidth - areaX - 16.0F;
    float gap = 12.0F;
    float buttonWidth = Math.min(180.0F, (areaWidth - gap) / 2.0F);
    float totalWidth = 2.0F * buttonWidth + gap;
    float left = areaX + (areaWidth - totalWidth) / 2.0F;
    float y;
    if (landscape) {
      y = Math.max(16.0F, screenHeight / 2.0F - 22.0F);
    } else if (portrait) {
      y = 16.0F;
    } else {
      y = Math.max(24.0F, grid.y() - 52.0F - 44.0F);
    }
    return List.of(
        new ScreenRectangle(left, y, buttonWidth, 44.0F),
        new ScreenRectangle(left + buttonWidth + gap, y, buttonWidth, 44.0F));
  }

  private static Set<GridPosition> union(Set<GridPosition> first, Set<GridPosition> second) {
    Set<GridPosition> combined = new HashSet<>(first);
    combined.addAll(second);
    return Set.copyOf(combined);
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }

  private static CardinalDirection rightOf(CardinalDirection incoming) {
    return switch (incoming) {
      case NORTH -> CardinalDirection.EAST;
      case EAST -> CardinalDirection.SOUTH;
      case SOUTH -> CardinalDirection.WEST;
      case WEST -> CardinalDirection.NORTH;
    };
  }

  private record PathStep(GridPosition position, int distance) {}

  private record ReferenceBoard(
      int size,
      Set<GridPosition> walls,
      Set<GridPosition> rightTurnFloors,
      Set<GridPosition> northSouthRails) {
    private ReferenceBoard {
      walls = Set.copyOf(walls);
      rightTurnFloors = Set.copyOf(rightTurnFloors);
      northSouthRails = Set.copyOf(northSouthRails);
    }

    private boolean canMove(GridPosition origin, CardinalDirection direction) {
      GridPosition destination = direction.move(origin);
      if (!destination.isWithin(GridSize.square(size)) || walls.contains(destination)) {
        return false;
      }
      boolean horizontal =
          direction == CardinalDirection.EAST || direction == CardinalDirection.WEST;
      return !horizontal
          || (!northSouthRails.contains(origin) && !northSouthRails.contains(destination));
    }
  }

  private static final class ReferenceSolver {
    private final ReferenceBoard board;
    private final GridPosition goal;
    private final SolverBehavior behavior;
    private final Random random;
    private final Map<GridPosition, Integer> visits = new HashMap<>();
    private final List<GridPosition> trace = new ArrayList<>();
    private GridPosition position;
    private CardinalDirection heading = CardinalDirection.NORTH;
    private CardinalDirection lastDirection;
    private int randomDecisionCount;

    private ReferenceSolver(
        ReferenceBoard board,
        GridPosition start,
        GridPosition goal,
        SolverBehavior behavior,
        long seed) {
      this.board = board;
      this.position = start;
      this.goal = goal;
      this.behavior = behavior;
      random = new Random(seed);
      visits.put(start, 1);
      trace.add(start);
    }

    private void run() {
      int decisions = 0;
      while (!position.equals(goal) && decisions++ < MAXIMUM_DECISIONS) {
        move();
      }
    }

    private void move() {
      CardinalDirection selected = forcedRightTurn();
      if (selected == null) {
        selected = normalDecision();
      }
      if (selected == null) {
        trace.add(position);
        return;
      }
      position = selected.move(position);
      lastDirection = selected;
      heading = selected;
      if (behavior == SolverBehavior.LEAST_VISITED) {
        visits.merge(position, 1, Integer::sum);
      }
      trace.add(position);
    }

    private CardinalDirection forcedRightTurn() {
      if (!board.rightTurnFloors().contains(position) || lastDirection == null) {
        return null;
      }
      CardinalDirection right = rightOf(lastDirection);
      return board.canMove(position, right) ? right : null;
    }

    private CardinalDirection normalDecision() {
      return switch (behavior) {
        case RANDOM -> randomDecision();
        case LEFT_PRIORITY ->
            referenceScoutOrder(heading).stream()
                .filter(direction -> board.canMove(position, direction))
                .findFirst()
                .orElse(null);
        case LEAST_VISITED -> referenceTrackerDecision();
        case LINE_OF_SIGHT -> {
          CardinalDirection visible = visibleGoalDirection();
          yield visible != null ? visible : randomDecision();
        }
      };
    }

    private CardinalDirection randomDecision() {
      List<CardinalDirection> open =
          REFERENCE_RANDOM_ORDER.stream()
              .filter(direction -> board.canMove(position, direction))
              .toList();
      if (open.isEmpty()) {
        return null;
      }
      randomDecisionCount++;
      return open.get(random.nextInt(open.size()));
    }

    private CardinalDirection visibleGoalDirection() {
      CardinalDirection direction;
      if (position.row() == goal.row()) {
        direction =
            position.column() < goal.column() ? CardinalDirection.EAST : CardinalDirection.WEST;
      } else if (position.column() == goal.column()) {
        direction = position.row() < goal.row() ? CardinalDirection.SOUTH : CardinalDirection.NORTH;
      } else {
        return null;
      }
      GridPosition candidate = position;
      while (!candidate.equals(goal)) {
        if (!board.canMove(candidate, direction)) {
          return null;
        }
        candidate = direction.move(candidate);
      }
      return direction;
    }

    private CardinalDirection referenceTrackerDecision() {
      Set<CardinalDirection> open = openDirections();
      return REFERENCE_DIRECTION_ORDER.stream()
          .filter(open::contains)
          .min(
              Comparator.<CardinalDirection>comparingInt(
                      direction -> visits.getOrDefault(direction.move(position), 0))
                  .thenComparingInt(direction -> manhattanDistance(direction.move(position), goal)))
          .orElse(null);
    }

    private Set<CardinalDirection> openDirections() {
      Set<CardinalDirection> directions = EnumSet.noneOf(CardinalDirection.class);
      for (CardinalDirection direction : CardinalDirection.values()) {
        if (board.canMove(position, direction)) {
          directions.add(direction);
        }
      }
      return directions;
    }

    private GridPosition position() {
      return position;
    }

    private List<GridPosition> trace() {
      return List.copyOf(trace);
    }

    private int randomDecisionCount() {
      return randomDecisionCount;
    }

    private CardinalDirection heading() {
      return heading;
    }

    private Map<GridPosition, Integer> visits() {
      return Map.copyOf(visits);
    }

    private ReferenceSnapshot snapshot() {
      return new ReferenceSnapshot(
          position, trace(), heading, lastDirection, visits(), randomDecisionCount);
    }
  }

  private record ReferenceSnapshot(
      GridPosition position,
      List<GridPosition> trace,
      CardinalDirection heading,
      CardinalDirection lastDirection,
      Map<GridPosition, Integer> visits,
      int randomDecisionCount) {}

  private static List<CardinalDirection> referenceScoutOrder(CardinalDirection heading) {
    return switch (heading) {
      case NORTH ->
          List.of(
              CardinalDirection.WEST,
              CardinalDirection.NORTH,
              CardinalDirection.EAST,
              CardinalDirection.SOUTH);
      case EAST ->
          List.of(
              CardinalDirection.NORTH,
              CardinalDirection.EAST,
              CardinalDirection.SOUTH,
              CardinalDirection.WEST);
      case SOUTH ->
          List.of(
              CardinalDirection.EAST,
              CardinalDirection.SOUTH,
              CardinalDirection.WEST,
              CardinalDirection.NORTH);
      case WEST ->
          List.of(
              CardinalDirection.SOUTH,
              CardinalDirection.WEST,
              CardinalDirection.NORTH,
              CardinalDirection.EAST);
    };
  }

  private static int manhattanDistance(GridPosition first, GridPosition second) {
    return Math.abs(first.row() - second.row()) + Math.abs(first.column() - second.column());
  }
}
