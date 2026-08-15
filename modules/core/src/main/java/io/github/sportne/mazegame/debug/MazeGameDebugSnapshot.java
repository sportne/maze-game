package io.github.sportne.mazegame.debug;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverDecisionState;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.state.CellPaletteState;
import io.github.sportne.mazegame.state.GamePhase;
import java.util.List;

/**
 * Immutable view of the game state exposed by the debug harness.
 *
 * @param gamePhase current high-level game phase
 * @param mazeState current fixed/player-authored maze and level definition
 * @param buildTimeRemainingSeconds seconds left in the build phase
 * @param rejectedPosition most recent rejected placement cell, or null when no flash is active
 * @param solverRunResult current solver simulation result, or null before a run starts
 * @param bestResult best saved result for the current level, or null when none exists
 * @param resultPassed true when the last completed run passed the level target
 * @param hasNextLevel true when the result screen can offer another level
 * @param paletteState immutable build-palette inventory and selection state
 * @param solverDecisionStates immutable decision memory in authored solver order
 */
public record MazeGameDebugSnapshot(
    GamePhase gamePhase,
    MazeState mazeState,
    float buildTimeRemainingSeconds,
    GridPosition rejectedPosition,
    SolverRunResult solverRunResult,
    BestResult bestResult,
    boolean resultPassed,
    boolean hasNextLevel,
    List<CellPaletteState> paletteState,
    List<SolverDecisionState> solverDecisionStates) {
  /** Creates a compatibility snapshot without active solver decision memory. */
  public MazeGameDebugSnapshot(
      GamePhase gamePhase,
      MazeState mazeState,
      float buildTimeRemainingSeconds,
      GridPosition rejectedPosition,
      SolverRunResult solverRunResult,
      BestResult bestResult,
      boolean resultPassed,
      boolean hasNextLevel,
      List<CellPaletteState> paletteState) {
    this(
        gamePhase,
        mazeState,
        buildTimeRemainingSeconds,
        rejectedPosition,
        solverRunResult,
        bestResult,
        resultPassed,
        hasNextLevel,
        paletteState,
        List.of());
  }

  /** Creates a debug snapshot with immutable palette and decision state. */
  public MazeGameDebugSnapshot {
    paletteState = List.copyOf(paletteState);
    solverDecisionStates = List.copyOf(solverDecisionStates);
  }
}
