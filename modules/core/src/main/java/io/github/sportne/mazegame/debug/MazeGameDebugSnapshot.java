package io.github.sportne.mazegame.debug;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
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
    List<CellPaletteState> paletteState) {
  /** Creates a debug snapshot with immutable palette state. */
  public MazeGameDebugSnapshot {
    paletteState = List.copyOf(paletteState);
  }
}
