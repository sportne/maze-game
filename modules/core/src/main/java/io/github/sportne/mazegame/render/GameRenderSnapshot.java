package io.github.sportne.mazegame.render;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.CardinalDirection;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.state.CellPaletteState;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.LevelProgress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
 * @param solverDirections most recent movement direction for each solver result
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
    List<SolverRunResult> solverRunResults,
    List<Optional<CardinalDirection>> solverDirections) {
  /** Creates a snapshot without directional presentation data for compatibility callers. */
  public GameRenderSnapshot(
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
        solverRunResults,
        emptyDirections(solverRunResults));
  }

  /** Creates a render snapshot with required frame state validated. */
  public GameRenderSnapshot {
    Objects.requireNonNull(phase, "phase");
    Objects.requireNonNull(levelDefinition, "levelDefinition");
    Objects.requireNonNull(mazeState, "mazeState");
    levelProgress = List.copyOf(levelProgress);
    paletteState = List.copyOf(paletteState);
    solverRunResults = List.copyOf(solverRunResults);
    solverDirections = List.copyOf(solverDirections);
    if (solverDirections.size() != solverRunResults.size()) {
      throw new IllegalArgumentException("solver directions must align with solver results");
    }
  }

  /** Returns the primary solver result, or null before a run starts. */
  public SolverRunResult solverRunResult() {
    return solverRunResults.isEmpty() ? null : solverRunResults.get(0);
  }

  private static List<Optional<CardinalDirection>> emptyDirections(
      List<SolverRunResult> solverRunResults) {
    Objects.requireNonNull(solverRunResults, "solverRunResults");
    return solverRunResults.stream().map(ignored -> Optional.<CardinalDirection>empty()).toList();
  }
}
