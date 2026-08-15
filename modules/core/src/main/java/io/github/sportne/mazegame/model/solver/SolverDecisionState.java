package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Map;
import java.util.Objects;

/** Immutable behavior-independent view of solver decision memory. */
public record SolverDecisionState(Map<GridPosition, Integer> cellVisitCounts) {
  private static final SolverDecisionState EMPTY = new SolverDecisionState(Map.of());

  /** Creates validated immutable decision state. */
  public SolverDecisionState {
    Objects.requireNonNull(cellVisitCounts, "cellVisitCounts");
    cellVisitCounts = Map.copyOf(cellVisitCounts);
    if (cellVisitCounts.entrySet().stream()
        .anyMatch(
            entry -> entry.getKey() == null || entry.getValue() == null || entry.getValue() < 1)) {
      throw new IllegalArgumentException(
          "cell visit counts must have positions and positive counts");
    }
  }

  /** Returns decision state for a solver behavior that keeps no inspectable memory. */
  public static SolverDecisionState empty() {
    return EMPTY;
  }
}
