package io.github.sportne.mazegame.model.mouse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class RandomMouseSimulationTest {
  @Test
  void startsAtMouseStartWithoutMoves() {
    RandomMouseSimulation simulation =
        new RandomMouseSimulation(MazeState.empty(Levels.milestoneOne()));

    MouseRunResult result = simulation.result();

    assertEquals(Levels.milestoneOne().mouseStart(), result.position());
    assertEquals(Duration.ZERO, result.elapsedTime());
    assertEquals(0, result.moveCount());
    assertEquals(MouseRunStatus.RUNNING, result.status());
  }

  @Test
  void ignoresPartialMoveIntervalsUntilEnoughTimeAccumulates() {
    RandomMouseSimulation simulation =
        new RandomMouseSimulation(MazeState.empty(Levels.milestoneOne()));

    MouseRunResult result = simulation.update(Duration.ofMillis(249));

    assertEquals(Levels.milestoneOne().mouseStart(), result.position());
    assertEquals(0, result.moveCount());
  }

  @Test
  void movesDeterministicallyForTheSameMazeAndSeed() {
    MazeState maze = MazeState.empty(Levels.milestoneOne());
    RandomMouseSimulation first = new RandomMouseSimulation(maze);
    RandomMouseSimulation second = new RandomMouseSimulation(maze);

    MouseRunResult firstResult = first.update(Duration.ofSeconds(1));
    MouseRunResult secondResult = second.update(Duration.ofSeconds(1));

    assertEquals(firstResult, secondResult);
  }

  @Test
  void onlyMovesToOpenNeighboringCells() {
    MazeState maze =
        new MazeState(
            Levels.milestoneOne(),
            Set.of(
                new GridPosition(4, 1),
                new GridPosition(4, 3),
                new GridPosition(3, 1),
                new GridPosition(3, 3)));
    RandomMouseSimulation simulation = new RandomMouseSimulation(maze);

    MouseRunResult result = simulation.update(Duration.ofMillis(250));

    assertEquals(new GridPosition(3, 2), result.position());
    assertEquals(1, result.moveCount());
  }

  @Test
  void reachesCheeseWhenRandomWalkArrivesThere() {
    MazeState maze = verticalCorridor(Levels.milestoneOne());
    RandomMouseSimulation simulation = new RandomMouseSimulation(maze);

    MouseRunResult result = simulation.update(Duration.ofSeconds(1));

    assertEquals(Levels.milestoneOne().cheese(), result.position());
    assertEquals(MouseRunStatus.REACHED_CHEESE, result.status());
    assertEquals(4, result.moveCount());
  }

  @Test
  void canImmediatelyMoveBackToPreviousCell() {
    LevelDefinition level = levelWithSeed(3L);
    MazeState maze = verticalCorridor(level);
    RandomMouseSimulation simulation = new RandomMouseSimulation(maze);

    MouseRunResult result = simulation.update(Duration.ofMillis(500));

    assertEquals(level.mouseStart(), result.position());
    assertEquals(2, result.moveCount());
  }

  @Test
  void timesOutAtMaximumSolveTime() {
    RandomMouseSimulation simulation =
        new RandomMouseSimulation(MazeState.empty(Levels.milestoneOne()));

    MouseRunResult result = simulation.update(Duration.ofSeconds(10));

    assertTrue(result.moveCount() > 0);
    assertEquals(Duration.ofSeconds(10), result.elapsedTime());
    assertEquals(MouseRunStatus.TIMED_OUT, result.status());
  }

  @Test
  void timesOutAtExactMaximumSolveTimeBeforeNextMoveTick() {
    LevelDefinition level =
        new LevelDefinition(
            "short-timeout",
            "Short Timeout",
            GridSize.square(2),
            new GridPosition(1, 0),
            new GridPosition(0, 1),
            Duration.ofMillis(50),
            Duration.ofMillis(100),
            Duration.ofMillis(100),
            Duration.ofMillis(250),
            1L);
    RandomMouseSimulation simulation = new RandomMouseSimulation(MazeState.empty(level));

    MouseRunResult result = simulation.update(Duration.ofMillis(500));

    assertEquals(Duration.ofMillis(100), result.elapsedTime());
    assertEquals(0, result.moveCount());
    assertEquals(MouseRunStatus.TIMED_OUT, result.status());
  }

  @Test
  void rejectsNegativeDeltaTime() {
    RandomMouseSimulation simulation =
        new RandomMouseSimulation(MazeState.empty(Levels.milestoneOne()));

    assertThrows(IllegalArgumentException.class, () -> simulation.update(Duration.ofMillis(-1)));
  }

  private static LevelDefinition levelWithSeed(long seed) {
    LevelDefinition milestoneOne = Levels.milestoneOne();
    return new LevelDefinition(
        "seed-" + seed,
        "Seed " + seed,
        milestoneOne.gridSize(),
        milestoneOne.mouseStart(),
        milestoneOne.cheese(),
        milestoneOne.buildTime(),
        milestoneOne.targetSolveTime(),
        milestoneOne.maximumSolveTime(),
        milestoneOne.mouseMoveInterval(),
        seed);
  }

  private static MazeState verticalCorridor(LevelDefinition level) {
    return new MazeState(
        level,
        Set.of(
            new GridPosition(4, 1),
            new GridPosition(4, 3),
            new GridPosition(3, 1),
            new GridPosition(3, 3),
            new GridPosition(2, 1),
            new GridPosition(2, 3),
            new GridPosition(1, 1),
            new GridPosition(1, 3),
            new GridPosition(0, 1),
            new GridPosition(0, 3)));
  }
}
