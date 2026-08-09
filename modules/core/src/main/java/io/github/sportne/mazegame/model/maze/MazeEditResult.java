package io.github.sportne.mazegame.model.maze;

import java.util.Objects;

/** Result of one atomic immutable maze edit. */
public record MazeEditResult(MazeState mazeState, MazeEditStatus status) {
  /** Creates a result with non-null state and status. */
  public MazeEditResult {
    Objects.requireNonNull(mazeState, "mazeState");
    Objects.requireNonNull(status, "status");
  }

  /** Returns whether the edit was accepted. */
  public boolean accepted() {
    return status.accepted();
  }

  static MazeEditResult accepted(MazeState mazeState, MazeEditStatus status) {
    if (!status.accepted()) {
      throw new IllegalArgumentException("status must be accepted");
    }
    return new MazeEditResult(mazeState, status);
  }

  static MazeEditResult rejected(MazeState mazeState, MazeEditStatus status) {
    if (status.accepted()) {
      throw new IllegalArgumentException("status must be rejected");
    }
    return new MazeEditResult(mazeState, status);
  }
}
