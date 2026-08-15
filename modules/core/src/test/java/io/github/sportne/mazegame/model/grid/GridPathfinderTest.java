package io.github.sportne.mazegame.model.grid;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

final class GridPathfinderTest {
  private static final GridSize GRID = GridSize.square(3);
  private static final GridPosition START = new GridPosition(2, 1);
  private static final GridPosition GOAL = new GridPosition(0, 1);

  @Test
  void findsAPathAroundBlockedCellsAndRejectsACompleteBarrier() {
    Set<GridPosition> partialBarrier = Set.of(new GridPosition(1, 0), new GridPosition(1, 2));
    Set<GridPosition> completeBarrier =
        Set.of(new GridPosition(1, 0), new GridPosition(1, 1), new GridPosition(1, 2));

    assertTrue(
        GridPathfinder.hasPath(GRID, START, GOAL, position -> !partialBarrier.contains(position)));
    assertFalse(
        GridPathfinder.hasPath(GRID, START, GOAL, position -> !completeBarrier.contains(position)));
  }

  @Test
  void requiresGridEndpointsAndWalkabilityRule() {
    assertThrows(
        NullPointerException.class,
        () -> GridPathfinder.hasPath(null, START, GOAL, ignored -> true));
    assertThrows(
        NullPointerException.class,
        () -> GridPathfinder.hasPath(GRID, null, GOAL, ignored -> true));
    assertThrows(
        NullPointerException.class,
        () -> GridPathfinder.hasPath(GRID, START, null, ignored -> true));
    assertThrows(NullPointerException.class, () -> GridPathfinder.hasPath(GRID, START, GOAL, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> GridPathfinder.hasPath(GRID, new GridPosition(-1, 1), GOAL, ignored -> true));
    assertThrows(
        IllegalArgumentException.class,
        () -> GridPathfinder.hasPath(GRID, START, new GridPosition(3, 1), ignored -> true));
  }
}
