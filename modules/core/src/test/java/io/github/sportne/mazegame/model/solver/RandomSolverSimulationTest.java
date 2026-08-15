package io.github.sportne.mazegame.model.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class RandomSolverSimulationTest {
  @Test
  void startsAtSolverStartWithoutMoves() {
    RandomSolverSimulation simulation =
        new RandomSolverSimulation(MazeState.empty(Levels.levelOne()));

    SolverRunResult result = simulation.result();

    assertEquals(Levels.levelOne().solverStart(), result.position());
    assertEquals(Duration.ZERO, result.elapsedTime());
    assertEquals(0, result.moveCount());
    assertEquals(SolverRunStatus.RUNNING, result.status());
  }

  @Test
  void ignoresPartialMoveIntervalsUntilEnoughTimeAccumulates() {
    RandomSolverSimulation simulation =
        new RandomSolverSimulation(MazeState.empty(Levels.levelOne()));

    SolverRunResult result = simulation.update(Duration.ofMillis(249));

    assertEquals(Levels.levelOne().solverStart(), result.position());
    assertEquals(0, result.moveCount());
  }

  @Test
  void movesDeterministicallyForTheSameMazeAndSeed() {
    MazeState maze = MazeState.empty(Levels.levelOne());
    RandomSolverSimulation first = new RandomSolverSimulation(maze);
    RandomSolverSimulation second = new RandomSolverSimulation(maze);

    SolverRunResult firstResult = first.update(Duration.ofSeconds(1));
    SolverRunResult secondResult = second.update(Duration.ofSeconds(1));

    assertEquals(firstResult, secondResult);
  }

  @Test
  void preservesResultsThroughTheSharedSimulationContract() {
    MazeState maze = MazeState.empty(Levels.levelOne());
    SolverSimulation simulation = new RandomSolverSimulation(maze);

    assertEquals(
        new RandomSolverSimulation(maze).update(Duration.ofSeconds(1)),
        simulation.update(Duration.ofSeconds(1)));
    assertEquals(simulation.result(), simulation.update(Duration.ZERO));
  }

  @Test
  void onlyMovesToOpenNeighboringCells() {
    MazeState maze =
        new MazeState(
            Levels.levelOne(),
            Set.of(
                new GridPosition(4, 1),
                new GridPosition(4, 3),
                new GridPosition(3, 1),
                new GridPosition(3, 3)));
    RandomSolverSimulation simulation = new RandomSolverSimulation(maze);

    SolverRunResult result = simulation.update(Duration.ofMillis(250));

    assertEquals(new GridPosition(3, 2), result.position());
    assertEquals(1, result.moveCount());
  }

  @Test
  void reachesGoalWhenRandomWalkArrivesThere() {
    MazeState maze = verticalCorridor(Levels.levelOne());
    RandomSolverSimulation simulation = new RandomSolverSimulation(maze);

    SolverRunResult result = simulation.update(Duration.ofSeconds(1));

    assertEquals(Levels.levelOne().goal(), result.position());
    assertEquals(SolverRunStatus.REACHED_GOAL, result.status());
    assertEquals(4, result.moveCount());
  }

  @Test
  void canImmediatelyMoveBackToPreviousCell() {
    LevelDefinition level = levelWithSeed(3L);
    MazeState maze = verticalCorridor(level);
    RandomSolverSimulation simulation = new RandomSolverSimulation(maze);

    SolverRunResult result = simulation.update(Duration.ofMillis(500));

    assertEquals(level.solverStart(), result.position());
    assertEquals(2, result.moveCount());
  }

  @Test
  void timesOutAtMaximumSolveTime() {
    RandomSolverSimulation simulation =
        new RandomSolverSimulation(MazeState.empty(Levels.levelOne()));

    SolverRunResult result = simulation.update(Duration.ofSeconds(10));

    assertTrue(result.moveCount() > 0);
    assertEquals(Duration.ofSeconds(10), result.elapsedTime());
    assertEquals(SolverRunStatus.TIMED_OUT, result.status());
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
            PlaceableCellSupply.releasedDefaults(),
            SolverBehavior.RANDOM,
            1L);
    RandomSolverSimulation simulation = new RandomSolverSimulation(MazeState.empty(level));

    SolverRunResult result = simulation.update(Duration.ofMillis(500));

    assertEquals(Duration.ofMillis(100), result.elapsedTime());
    assertEquals(0, result.moveCount());
    assertEquals(SolverRunStatus.TIMED_OUT, result.status());
  }

  @Test
  void rejectsNegativeDeltaTime() {
    RandomSolverSimulation simulation =
        new RandomSolverSimulation(MazeState.empty(Levels.levelOne()));

    assertThrows(IllegalArgumentException.class, () -> simulation.update(Duration.ofMillis(-1)));
  }

  private static LevelDefinition levelWithSeed(long seed) {
    LevelDefinition milestoneOne = Levels.levelOne();
    return new LevelDefinition(
        "seed-" + seed,
        "Seed " + seed,
        milestoneOne.gridSize(),
        milestoneOne.solverStart(),
        milestoneOne.goal(),
        milestoneOne.buildTime(),
        milestoneOne.targetSolveTime(),
        milestoneOne.maximumSolveTime(),
        milestoneOne.solverMoveInterval(),
        milestoneOne.placeableCellSupplies(),
        SolverBehavior.RANDOM,
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
