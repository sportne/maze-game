package io.github.sportne.mazegame.render;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
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
 * @param bestResult best saved result for the current level, or null when none exists
 * @param levelProgress authored level presentation state in catalog order
 * @param paletteState authored and remaining build-palette state in display order
 * @param paletteDragPreview active palette drag preview, or null
 * @param paletteTooltipType palette item whose delayed hover tooltip is visible, or null
 * @param audioEnabled whether session audio is enabled
 * @param resultPassed whether the latest result passed
 * @param hasNextLevel whether a next level option exists
 * @param solverRunResults latest results in authored solver order
 */
public record GameRenderSnapshot(
    GamePhase phase,
    LevelDefinition levelDefinition,
    MazeState mazeState,
    float buildTimeRemainingSeconds,
    GridPosition rejectedPosition,
    float rejectedFlashRemainingSeconds,
    BestResult bestResult,
    List<LevelProgress> levelProgress,
    List<CellPaletteState> paletteState,
    PaletteDragPreview paletteDragPreview,
    PlaceableCellType paletteTooltipType,
    boolean audioEnabled,
    boolean resultPassed,
    boolean hasNextLevel,
    List<SolverRunResult> solverRunResults) {
  /** Creates a render snapshot with required frame state validated. */
  public GameRenderSnapshot {
    Objects.requireNonNull(phase, "phase");
    Objects.requireNonNull(levelDefinition, "levelDefinition");
    Objects.requireNonNull(mazeState, "mazeState");
    levelProgress = List.copyOf(levelProgress);
    paletteState = List.copyOf(paletteState);
    solverRunResults = List.copyOf(solverRunResults);
  }

  /** Creates a snapshot using the released single-result representation. */
  public GameRenderSnapshot(
      GamePhase phase,
      LevelDefinition levelDefinition,
      MazeState mazeState,
      float buildTimeRemainingSeconds,
      GridPosition rejectedPosition,
      float rejectedFlashRemainingSeconds,
      SolverRunResult solverRunResult,
      BestResult bestResult,
      List<LevelProgress> levelProgress,
      List<CellPaletteState> paletteState,
      PaletteDragPreview paletteDragPreview,
      PlaceableCellType paletteTooltipType,
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
        bestResult,
        levelProgress,
        paletteState,
        paletteDragPreview,
        paletteTooltipType,
        audioEnabled,
        resultPassed,
        hasNextLevel,
        solverRunResult == null ? List.of() : List.of(solverRunResult));
  }

  /** Creates a snapshot with an active drag but no delayed hover tooltip. */
  public GameRenderSnapshot(
      GamePhase phase,
      LevelDefinition levelDefinition,
      MazeState mazeState,
      float buildTimeRemainingSeconds,
      GridPosition rejectedPosition,
      float rejectedFlashRemainingSeconds,
      SolverRunResult solverRunResult,
      BestResult bestResult,
      List<LevelProgress> levelProgress,
      List<CellPaletteState> paletteState,
      PaletteDragPreview paletteDragPreview,
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
        bestResult,
        levelProgress,
        paletteState,
        paletteDragPreview,
        null,
        audioEnabled,
        resultPassed,
        hasNextLevel,
        solverRunResult == null ? List.of() : List.of(solverRunResult));
  }

  /** Creates a snapshot with palette data and no active drag for compatibility fixtures. */
  public GameRenderSnapshot(
      GamePhase phase,
      LevelDefinition levelDefinition,
      MazeState mazeState,
      float buildTimeRemainingSeconds,
      GridPosition rejectedPosition,
      float rejectedFlashRemainingSeconds,
      SolverRunResult solverRunResult,
      BestResult bestResult,
      List<LevelProgress> levelProgress,
      List<CellPaletteState> paletteState,
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
        bestResult,
        levelProgress,
        paletteState,
        null,
        null,
        audioEnabled,
        resultPassed,
        hasNextLevel,
        solverRunResult == null ? List.of() : List.of(solverRunResult));
  }

  /** Creates a snapshot without palette data for non-building and compatibility fixtures. */
  public GameRenderSnapshot(
      GamePhase phase,
      LevelDefinition levelDefinition,
      MazeState mazeState,
      float buildTimeRemainingSeconds,
      GridPosition rejectedPosition,
      float rejectedFlashRemainingSeconds,
      SolverRunResult solverRunResult,
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
        bestResult,
        levelProgress,
        List.of(),
        null,
        null,
        audioEnabled,
        resultPassed,
        hasNextLevel,
        solverRunResult == null ? List.of() : List.of(solverRunResult));
  }

  /** Returns the primary solver result, or null before a run starts. */
  public SolverRunResult solverRunResult() {
    return solverRunResults.isEmpty() ? null : solverRunResults.get(0);
  }
}
