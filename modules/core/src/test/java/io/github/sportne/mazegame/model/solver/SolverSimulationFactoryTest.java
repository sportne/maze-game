package io.github.sportne.mazegame.model.solver;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import org.junit.jupiter.api.Test;

final class SolverSimulationFactoryTest {
  @Test
  void createsRandomSimulationForRandomBehavior() {
    SolverSimulation simulation =
        SolverSimulationFactory.create(MazeState.empty(Levels.levelOne()));

    assertInstanceOf(RandomSolverSimulation.class, simulation);
  }

  @Test
  void createsScoutSimulationForLeftPriorityBehavior() {
    LevelDefinition level = withBehavior(Levels.levelOne(), SolverBehavior.LEFT_PRIORITY);

    SolverSimulation simulation = SolverSimulationFactory.create(MazeState.empty(level));

    assertInstanceOf(ScoutSolverSimulation.class, simulation);
  }

  @Test
  void createsTrackerSimulationForLeastVisitedBehavior() {
    LevelDefinition level = withBehavior(Levels.levelOne(), SolverBehavior.LEAST_VISITED);

    SolverSimulation simulation = SolverSimulationFactory.create(MazeState.empty(level));

    assertInstanceOf(TrackerSolverSimulation.class, simulation);
  }

  @Test
  void createsSeekerSimulationForLineOfSightBehavior() {
    LevelDefinition level = withBehavior(Levels.levelOne(), SolverBehavior.LINE_OF_SIGHT);

    SolverSimulation simulation = SolverSimulationFactory.create(MazeState.empty(level));

    assertInstanceOf(LineOfSightSolverSimulation.class, simulation);
  }

  @Test
  void requiresAMaze() {
    assertThrows(NullPointerException.class, () -> SolverSimulationFactory.create(null));
  }

  @Test
  void createsAnIndependentSimulationFromAnAuthoredSolver() {
    LevelDefinition level = Levels.levelTen();
    LevelSolver scout = level.solvers().get(1);

    SolverSimulation simulation = SolverSimulationFactory.create(MazeState.empty(level), scout);

    assertInstanceOf(ScoutSolverSimulation.class, simulation);
    assertEquals(scout.start(), simulation.result().position());
  }

  @Test
  void rejectsASolverThatIsNotAuthoredByTheMazeLevel() {
    LevelDefinition level = Levels.levelTen();
    LevelSolver unknown =
        new LevelSolver(
            level.primarySolver().goal(),
            level.primarySolver().start(),
            SolverBehavior.RANDOM,
            java.util.OptionalLong.of(1L),
            SolverAppearance.CLASSIC_MOUSE,
            GoalType.CHEESE);

    assertThrows(
        IllegalArgumentException.class,
        () -> SolverSimulationFactory.create(MazeState.empty(level), unknown));
    assertThrows(
        NullPointerException.class,
        () -> SolverSimulationFactory.create(MazeState.empty(level), null));
  }

  private static LevelDefinition withBehavior(
      LevelDefinition source, SolverBehavior solverBehavior) {
    return singleSolverLevel(
        source.id(),
        source.name(),
        source.gridSize(),
        source.primarySolver().start(),
        source.primarySolver().goal(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.solverMoveInterval(),
        source.placeableCellSupplies(),
        solverBehavior,
        source.primarySolver().randomSeed().orElseThrow());
  }
}
