package io.github.sportne.mazegame;

import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.MazeState;
import io.github.sportne.mazegame.model.MouseRunResult;

/**
 * Immutable view of the game state exposed by the debug harness.
 *
 * @param gamePhase current high-level game phase
 * @param mazeState current maze walls and level definition
 * @param buildTimeRemainingSeconds seconds left in the build phase
 * @param rejectedPosition most recent rejected placement cell, or null when no flash is active
 * @param mouseRunResult current mouse simulation result, or null before a run starts
 * @param resultPassed true when the last completed run passed the level target
 * @param hasNextLevel true when the result screen can offer another level
 */
public record MazeGameDebugSnapshot(
    GamePhase gamePhase,
    MazeState mazeState,
    float buildTimeRemainingSeconds,
    GridPosition rejectedPosition,
    MouseRunResult mouseRunResult,
    boolean resultPassed,
    boolean hasNextLevel) {}
