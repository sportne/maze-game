package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.layout.LayoutElementKind;
import io.github.sportne.mazegame.layout.LayoutValidator;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.mouse.MouseRunStatus;
import io.github.sportne.mazegame.model.mouse.RandomMouseSimulation;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameResultEvaluator;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Reproducible balancing evidence for the authored Milestone 2 level specification. */
final class MilestoneTwoLevelDesignTest {
  private static final LevelDefinition LEVEL = Levels.milestoneTwo();

  private static final Set<GridPosition> PASSING_LAYOUT_A =
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

  private static final Set<GridPosition> PASSING_LAYOUT_B =
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

  private static final Set<GridPosition> TIMEOUT_LAYOUT =
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

  @Test
  void recordsTheAcceptedAuthoredParameters() {
    assertEquals("milestone-2", LEVEL.id());
    assertEquals("Milestone 2", LEVEL.name());
    assertEquals(GridSize.square(7), LEVEL.gridSize());
    assertEquals(new GridPosition(6, 3), LEVEL.mouseStart());
    assertEquals(new GridPosition(0, 3), LEVEL.cheese());
    assertEquals(Duration.ofSeconds(25), LEVEL.buildTime());
    assertEquals(Duration.ofSeconds(6), LEVEL.targetSolveTime());
    assertEquals(Duration.ofSeconds(15), LEVEL.maximumSolveTime());
    assertEquals(Duration.ofMillis(250), LEVEL.mouseMoveInterval());
    assertEquals(38L, LEVEL.randomSeed());
    assertEquals(List.of(Levels.milestoneOne(), LEVEL), Levels.catalog().levels());
  }

  @Test
  void emptyMazeIsSolvableButFailsTheTarget() {
    MazeState maze = MazeState.empty(LEVEL);

    MouseRunResult result = run(maze);

    assertTrue(maze.hasPathFromStartToCheese());
    assertEquals(
        new MouseRunResult(
            LEVEL.cheese(), Duration.ofSeconds(3), 12, MouseRunStatus.REACHED_CHEESE),
        result);
    assertFalse(GameResultEvaluator.passed(GamePhase.RESULT, result, LEVEL));
  }

  @Test
  void acceptedLayoutsPreserveAPathAndPassDeterministically() {
    assertPassingLayout(PASSING_LAYOUT_A, Duration.ofMillis(9500), 38);
    assertPassingLayout(PASSING_LAYOUT_B, Duration.ofMillis(8500), 34);
  }

  private static void assertPassingLayout(
      Set<GridPosition> walls, Duration elapsedTime, int moveCount) {
    MazeState maze = new MazeState(LEVEL, walls);
    MouseRunResult expected =
        new MouseRunResult(LEVEL.cheese(), elapsedTime, moveCount, MouseRunStatus.REACHED_CHEESE);

    assertTrue(maze.hasPathFromStartToCheese());
    assertEquals(expected, run(maze));
    assertEquals(expected, run(maze));
    assertTrue(GameResultEvaluator.passed(GamePhase.RESULT, expected, LEVEL));
  }

  @Test
  void representativeValidLayoutCanReachTheTimeout() {
    MazeState maze = new MazeState(LEVEL, TIMEOUT_LAYOUT);

    MouseRunResult result = run(maze);

    assertTrue(maze.hasPathFromStartToCheese());
    assertEquals(
        new MouseRunResult(
            new GridPosition(1, 2), Duration.ofSeconds(15), 60, MouseRunStatus.TIMED_OUT),
        result);
    assertTrue(GameResultEvaluator.passed(GamePhase.RESULT, result, LEVEL));
  }

  @Test
  void sevenBySevenGridRemainsUsableOnSupportedMobileLayouts() {
    int[][] viewports = {{390, 844}, {844, 286}, {756, 286}};
    for (int[] viewport : viewports) {
      ScreenLayout layout =
          MazeGameLayout.forPhase(
              GamePhase.BUILDING, viewport[0], viewport[1], LEVEL.gridSize(), false);
      float cellWidth =
          layout.bounds(MazeGameLayout.GAME_GRID).width() / LEVEL.gridSize().columns();

      assertTrue(LayoutValidator.validate(layout).isEmpty());
      assertTrue(cellWidth >= 32.0F);
      assertTrue(
          layout.elements().stream()
              .filter(element -> element.kind() == LayoutElementKind.BUTTON)
              .allMatch(
                  element ->
                      element.bounds().width() >= 44.0F && element.bounds().height() >= 44.0F));
    }
  }

  private static MouseRunResult run(MazeState maze) {
    return new RandomMouseSimulation(maze).update(LEVEL.maximumSolveTime());
  }
}
