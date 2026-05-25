package io.github.sportne.mazegame;

import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.MazeState;
import io.github.sportne.mazegame.model.MouseRunResult;

/** Immutable view of the game state exposed by the debug harness. */
public record MazeGameDebugSnapshot(
    GamePhase gamePhase,
    MazeState mazeState,
    float buildTimeRemainingSeconds,
    GridPosition rejectedPosition,
    MouseRunResult mouseRunResult,
    boolean resultPassed,
    boolean hasNextLevel) {}
