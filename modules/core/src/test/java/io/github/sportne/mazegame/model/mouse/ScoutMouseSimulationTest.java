package io.github.sportne.mazegame.model.mouse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.MouseBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class ScoutMouseSimulationTest {
  private static final Duration MOVE_INTERVAL = Duration.ofMillis(250);

  @Test
  void cardinalDirectionsEncodeEveryRelativePriority() {
    assertEquals(
        List.of(
            CardinalDirection.WEST,
            CardinalDirection.NORTH,
            CardinalDirection.EAST,
            CardinalDirection.SOUTH),
        CardinalDirection.NORTH.leftStraightRightBack());
    assertEquals(
        List.of(
            CardinalDirection.NORTH,
            CardinalDirection.EAST,
            CardinalDirection.SOUTH,
            CardinalDirection.WEST),
        CardinalDirection.EAST.leftStraightRightBack());
    assertEquals(
        List.of(
            CardinalDirection.EAST,
            CardinalDirection.SOUTH,
            CardinalDirection.WEST,
            CardinalDirection.NORTH),
        CardinalDirection.SOUTH.leftStraightRightBack());
    assertEquals(
        List.of(
            CardinalDirection.SOUTH,
            CardinalDirection.WEST,
            CardinalDirection.NORTH,
            CardinalDirection.EAST),
        CardinalDirection.WEST.leftStraightRightBack());
  }

  @Test
  void choosesFirstOpenDirectionForEveryHeadingAndObstructionCombination() {
    for (CardinalDirection heading : CardinalDirection.values()) {
      List<CardinalDirection> priority = heading.leftStraightRightBack();
      for (int openMask = 0; openMask < 16; openMask++) {
        Set<CardinalDirection> open = openDirections(priority, openMask);
        CardinalDirection expected =
            priority.stream().filter(open::contains).findFirst().orElse(null);

        assertEquals(expected, ScoutMouseSimulation.chooseDirection(heading, open));
      }
    }
    assertNull(
        ScoutMouseSimulation.chooseDirection(
            CardinalDirection.NORTH, EnumSet.noneOf(CardinalDirection.class)));
  }

  @Test
  void northFacingScoutSelectsLeftThenStraightThenRightThenBack() {
    assertFirstMove(position(1, 0), position(1, 0), Set.of());
    assertFirstMove(position(0, 1), position(0, 1), Set.of(position(1, 0)));
    assertFirstMove(position(1, 2), position(1, 2), Set.of(position(1, 0), position(0, 1)));
    assertFirstMove(
        position(2, 1), position(2, 1), Set.of(position(1, 0), position(0, 1), position(1, 2)));
  }

  @Test
  void reverseMovementChangesTheHeadingForTheNextDecision() {
    LevelDefinition level = level(GridSize.square(3), position(1, 1), position(2, 2), 1L);
    ScoutMouseSimulation scout =
        new ScoutMouseSimulation(
            new MazeState(level, Set.of(position(1, 0), position(0, 1), position(1, 2))));

    assertEquals(position(2, 1), scout.update(MOVE_INTERVAL).position());
    assertEquals(position(2, 2), scout.update(MOVE_INTERVAL).position());
    assertEquals(MouseRunStatus.REACHED_CHEESE, scout.result().status());
  }

  @Test
  void ignoresTheRandomSeed() {
    LevelDefinition first = level(GridSize.square(7), position(6, 3), position(0, 3), 1L);
    LevelDefinition second = level(GridSize.square(7), position(6, 3), position(0, 3), 999L);
    Set<GridPosition> walls =
        Set.of(position(2, 2), position(3, 1), position(4, 0), position(5, 1));

    assertEquals(
        new ScoutMouseSimulation(new MazeState(first, walls)).update(Duration.ofSeconds(8)),
        new ScoutMouseSimulation(new MazeState(second, walls)).update(Duration.ofSeconds(8)));
  }

  @Test
  void wholeAndChunkedUpdatesHaveEqualTimingAndMovement() {
    LevelDefinition level = level(GridSize.square(7), position(6, 3), position(0, 3), 1L);
    MazeState maze =
        new MazeState(
            level,
            Set.of(position(2, 1), position(3, 0), position(3, 2), position(3, 4), position(4, 3)));
    ScoutMouseSimulation whole = new ScoutMouseSimulation(maze);
    ScoutMouseSimulation chunked = new ScoutMouseSimulation(maze);

    MouseRunResult expected = whole.update(Duration.ofSeconds(8));
    for (int index = 0; index < 80; index++) {
      chunked.update(Duration.ofMillis(100));
    }

    assertEquals(
        new MouseRunResult(
            level.cheese(), Duration.ofMillis(7500), 30, MouseRunStatus.REACHED_CHEESE),
        expected);
    assertEquals(expected, chunked.result());
  }

  @Test
  void deterministicPerimeterLoopTimesOutDespiteAViableDirectPath() {
    LevelDefinition level =
        new LevelDefinition(
            "loop",
            "Loop",
            GridSize.square(3),
            position(2, 1),
            position(1, 1),
            Duration.ofSeconds(1),
            Duration.ofSeconds(1),
            Duration.ofSeconds(2),
            MOVE_INTERVAL,
            PlaceableCellSupply.releasedDefaults(),
            MouseBehavior.LEFT_PRIORITY,
            1L);
    MazeState maze = MazeState.empty(level);

    MouseRunResult result = new ScoutMouseSimulation(maze).update(Duration.ofSeconds(10));

    assertEquals(
        new MouseRunResult(level.mouseStart(), Duration.ofSeconds(2), 8, MouseRunStatus.TIMED_OUT),
        result);
  }

  @Test
  void validatesDeltasAndStopsUpdatingAfterTerminalResult() {
    LevelDefinition level =
        new LevelDefinition(
            "short-timeout",
            "Short Timeout",
            GridSize.square(2),
            position(1, 0),
            position(0, 1),
            Duration.ofMillis(50),
            Duration.ofMillis(100),
            Duration.ofMillis(100),
            MOVE_INTERVAL,
            PlaceableCellSupply.releasedDefaults(),
            MouseBehavior.LEFT_PRIORITY,
            1L);
    ScoutMouseSimulation scout = new ScoutMouseSimulation(MazeState.empty(level));

    assertThrows(IllegalArgumentException.class, () -> scout.update(Duration.ofMillis(-1)));
    assertEquals(scout.result(), scout.update(Duration.ZERO));
    MouseRunResult terminal = scout.update(Duration.ofSeconds(1));
    assertEquals(
        new MouseRunResult(level.mouseStart(), Duration.ofMillis(100), 0, MouseRunStatus.TIMED_OUT),
        terminal);
    assertEquals(terminal, scout.update(Duration.ofSeconds(1)));
  }

  @Test
  void mouseBehaviorsRemainAClosedTwoValueSet() {
    assertArrayEquals(
        new MouseBehavior[] {MouseBehavior.RANDOM, MouseBehavior.LEFT_PRIORITY},
        MouseBehavior.values());
  }

  private static void assertFirstMove(
      GridPosition cheese, GridPosition expected, Set<GridPosition> walls) {
    LevelDefinition level = level(GridSize.square(3), position(1, 1), cheese, 1L);

    MouseRunResult result =
        new ScoutMouseSimulation(new MazeState(level, walls)).update(MOVE_INTERVAL);

    assertEquals(expected, result.position());
    assertEquals(1, result.moveCount());
  }

  private static Set<CardinalDirection> openDirections(
      List<CardinalDirection> priority, int openMask) {
    Set<CardinalDirection> open = EnumSet.noneOf(CardinalDirection.class);
    for (int index = 0; index < priority.size(); index++) {
      if ((openMask & (1 << index)) != 0) {
        open.add(priority.get(index));
      }
    }
    return open;
  }

  private static LevelDefinition level(
      GridSize gridSize, GridPosition start, GridPosition cheese, long seed) {
    return new LevelDefinition(
        "scout-" + seed,
        "Scout " + seed,
        gridSize,
        start,
        cheese,
        Duration.ofSeconds(25),
        Duration.ofSeconds(6),
        Duration.ofSeconds(8),
        MOVE_INTERVAL,
        PlaceableCellSupply.releasedDefaults(),
        MouseBehavior.LEFT_PRIORITY,
        seed);
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }
}
