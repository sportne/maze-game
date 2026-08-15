package io.github.sportne.mazegame.model.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import org.junit.jupiter.api.Test;

final class SolverSimulationFactoryTest {
  @Test
  void createsRandomSimulationForRandomBehavior() {
    SolverSimulation simulation =
        SolverSimulationFactory.create(MazeState.empty(Levels.milestoneOne()));

    assertInstanceOf(RandomSolverSimulation.class, simulation);
  }

  @Test
  void createsScoutSimulationForLeftPriorityBehavior() {
    LevelDefinition level = withBehavior(Levels.milestoneOne(), SolverBehavior.LEFT_PRIORITY);

    SolverSimulation simulation = SolverSimulationFactory.create(MazeState.empty(level));

    assertInstanceOf(ScoutSolverSimulation.class, simulation);
  }

  @Test
  void requiresAMaze() {
    assertThrows(NullPointerException.class, () -> SolverSimulationFactory.create(null));
  }

  @Test
  void createsAnIndependentSimulationFromAnAuthoredSolver() {
    LevelDefinition level = Levels.milestoneFive();
    LevelSolver scout = level.solvers().get(1);

    SolverSimulation simulation = SolverSimulationFactory.create(MazeState.empty(level), scout);

    assertInstanceOf(ScoutSolverSimulation.class, simulation);
    assertEquals(scout.start(), simulation.result().position());
  }

  @Test
  void rejectsASolverThatIsNotAuthoredByTheMazeLevel() {
    LevelDefinition level = Levels.milestoneFive();
    LevelSolver unknown =
        new LevelSolver(level.goal(), level.solverStart(), SolverBehavior.RANDOM, 1L);

    assertThrows(
        IllegalArgumentException.class,
        () -> SolverSimulationFactory.create(MazeState.empty(level), unknown));
    assertThrows(
        NullPointerException.class,
        () -> SolverSimulationFactory.create(MazeState.empty(level), null));
  }

  private static LevelDefinition withBehavior(
      LevelDefinition source, SolverBehavior solverBehavior) {
    return new LevelDefinition(
        source.id(),
        source.name(),
        source.gridSize(),
        source.solverStart(),
        source.goal(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.solverMoveInterval(),
        source.placeableCellSupplies(),
        solverBehavior,
        source.randomSeed());
  }
}
