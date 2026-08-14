package io.github.sportne.mazegame.model.mouse;

import io.github.sportne.mazegame.model.level.LevelMouse;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.util.Objects;

/** Creates the concrete mouse simulation selected by immutable level authoring data. */
public final class MouseSimulationFactory {
  private MouseSimulationFactory() {}

  /**
   * Creates a fresh simulation for a maze.
   *
   * @param mazeState immutable maze to solve
   * @return simulation selected by the maze's level definition
   */
  public static MouseSimulation create(MazeState mazeState) {
    Objects.requireNonNull(mazeState, "mazeState");
    return switch (mazeState.levelDefinition().mouseBehavior()) {
      case RANDOM -> new RandomMouseSimulation(mazeState);
      case LEFT_PRIORITY -> new ScoutMouseSimulation(mazeState);
    };
  }

  /** Creates an independent simulation for one mouse authored by a multi-mouse level. */
  public static MouseSimulation create(MazeState mazeState, LevelMouse mouse) {
    Objects.requireNonNull(mazeState, "mazeState");
    Objects.requireNonNull(mouse, "mouse");
    MazeState mouseMaze =
        new MazeState(mazeState.levelDefinition().forMouse(mouse), mazeState.placedCells());
    return create(mouseMaze);
  }
}
