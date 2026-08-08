package io.github.sportne.mazegame.model.mouse;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** Deterministic north-facing mouse that prefers left, straight, right, then reverse. */
final class ScoutMouseSimulation extends TimedMouseSimulation {
  private CardinalDirection heading = CardinalDirection.NORTH;

  ScoutMouseSimulation(MazeState mazeState) {
    super(mazeState);
  }

  @Override
  void moveOnce() {
    Set<CardinalDirection> openDirections = EnumSet.noneOf(CardinalDirection.class);
    for (CardinalDirection direction : CardinalDirection.values()) {
      if (isOpen(direction.move(position()))) {
        openDirections.add(direction);
      }
    }
    CardinalDirection selected = chooseDirection(heading, openDirections);
    if (selected != null) {
      GridPosition destination = selected.move(position());
      moveTo(destination);
      heading = selected;
    }
  }

  static CardinalDirection chooseDirection(
      CardinalDirection heading, Set<CardinalDirection> openDirections) {
    Objects.requireNonNull(heading, "heading");
    Objects.requireNonNull(openDirections, "openDirections");
    return heading.leftStraightRightBack().stream()
        .filter(openDirections::contains)
        .findFirst()
        .orElse(null);
  }
}
