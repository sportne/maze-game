package io.github.sportne.mazegame.model;

import java.util.Objects;

/**
 * Result of trying to place a wall in a maze.
 *
 * @param mazeState resulting maze; rejected placements keep the original maze
 * @param status reason the placement was accepted or rejected
 */
public record WallPlacementResult(MazeState mazeState, WallPlacementStatus status) {
  /**
   * Creates a wall placement result.
   *
   * @throws NullPointerException when the maze or status is missing
   */
  public WallPlacementResult {
    Objects.requireNonNull(mazeState, "mazeState");
    Objects.requireNonNull(status, "status");
  }

  /**
   * Creates an accepted wall placement result.
   *
   * @param mazeState resulting maze state
   * @param status accepted status to report
   * @return placement result containing the supplied maze and status
   */
  public static WallPlacementResult accepted(MazeState mazeState, WallPlacementStatus status) {
    if (!status.accepted()) {
      throw new IllegalArgumentException("status must be accepted");
    }
    return new WallPlacementResult(mazeState, status);
  }

  /**
   * Creates a rejected wall placement result.
   *
   * @param mazeState unchanged maze state
   * @param status rejection status to report
   * @return placement result containing the original maze and rejection reason
   */
  public static WallPlacementResult rejected(MazeState mazeState, WallPlacementStatus status) {
    if (status.accepted()) {
      throw new IllegalArgumentException("status must be rejected");
    }
    return new WallPlacementResult(mazeState, status);
  }

  /**
   * Returns whether the wall placement was accepted.
   *
   * @return true when the status is accepted
   */
  public boolean accepted() {
    return status.accepted();
  }
}
