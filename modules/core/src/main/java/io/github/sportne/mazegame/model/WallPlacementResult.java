package io.github.sportne.mazegame.model;

import java.util.Objects;

/** Result of trying to place a wall in a maze. */
public record WallPlacementResult(MazeState mazeState, WallPlacementStatus status) {
  public WallPlacementResult {
    Objects.requireNonNull(mazeState, "mazeState");
    Objects.requireNonNull(status, "status");
  }

  /** Creates an accepted wall placement result. */
  public static WallPlacementResult accepted(MazeState mazeState, WallPlacementStatus status) {
    if (!status.accepted()) {
      throw new IllegalArgumentException("status must be accepted");
    }
    return new WallPlacementResult(mazeState, status);
  }

  /** Creates a rejected wall placement result. */
  public static WallPlacementResult rejected(MazeState mazeState, WallPlacementStatus status) {
    if (status.accepted()) {
      throw new IllegalArgumentException("status must be rejected");
    }
    return new WallPlacementResult(mazeState, status);
  }

  /** Returns whether the wall placement was accepted. */
  public boolean accepted() {
    return status.accepted();
  }
}
