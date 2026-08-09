package io.github.sportne.mazegame.render;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.state.CellPaletteState;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.LevelProgress;
import java.util.List;
import java.util.Objects;

/**
 * Immutable data needed to draw one frame.
 *
 * @param phase active phase
 * @param levelDefinition current level definition
 * @param mazeState current immutable maze
 * @param buildTimeRemainingSeconds build timer value
 * @param rejectedPosition cell flashing as rejected, or null
 * @param rejectedFlashRemainingSeconds rejected flash time remaining
 * @param mouseRunResult latest mouse result, or null before a run starts
 * @param bestResult best saved result for the current level, or null when none exists
 * @param levelProgress authored level presentation state in catalog order
 * @param paletteState authored and remaining build-palette state in display order
 * @param audioEnabled whether session audio is enabled
 * @param resultPassed whether the latest result passed
 * @param hasNextLevel whether a next level option exists
 */
public record GameRenderSnapshot(
    GamePhase phase,
    LevelDefinition levelDefinition,
    MazeState mazeState,
    float buildTimeRemainingSeconds,
    GridPosition rejectedPosition,
    float rejectedFlashRemainingSeconds,
    MouseRunResult mouseRunResult,
    BestResult bestResult,
    List<LevelProgress> levelProgress,
    List<CellPaletteState> paletteState,
    boolean audioEnabled,
    boolean resultPassed,
    boolean hasNextLevel) {
  /** Creates a render snapshot with required frame state validated. */
  public GameRenderSnapshot {
    Objects.requireNonNull(phase, "phase");
    Objects.requireNonNull(levelDefinition, "levelDefinition");
    Objects.requireNonNull(mazeState, "mazeState");
    levelProgress = List.copyOf(levelProgress);
    paletteState = List.copyOf(paletteState);
  }

  /** Creates a snapshot without palette data for non-building and compatibility fixtures. */
  public GameRenderSnapshot(
      GamePhase phase,
      LevelDefinition levelDefinition,
      MazeState mazeState,
      float buildTimeRemainingSeconds,
      GridPosition rejectedPosition,
      float rejectedFlashRemainingSeconds,
      MouseRunResult mouseRunResult,
      BestResult bestResult,
      List<LevelProgress> levelProgress,
      boolean audioEnabled,
      boolean resultPassed,
      boolean hasNextLevel) {
    this(
        phase,
        levelDefinition,
        mazeState,
        buildTimeRemainingSeconds,
        rejectedPosition,
        rejectedFlashRemainingSeconds,
        mouseRunResult,
        bestResult,
        levelProgress,
        List.of(),
        audioEnabled,
        resultPassed,
        hasNextLevel);
  }
}
