package io.github.sportne.mazegame.model;

import java.time.Duration;
import java.util.Objects;

/** Immutable authoring data for one playable level. */
public record LevelDefinition(
    String id,
    String name,
    GridSize gridSize,
    GridPosition mouseStart,
    GridPosition cheese,
    Duration buildTime,
    Duration targetSolveTime,
    Duration maximumSolveTime,
    Duration mouseMoveInterval,
    long randomSeed) {
  public LevelDefinition {
    id = requireNonBlank(id, "id");
    name = requireNonBlank(name, "name");
    Objects.requireNonNull(gridSize, "gridSize");
    Objects.requireNonNull(mouseStart, "mouseStart");
    Objects.requireNonNull(cheese, "cheese");
    requirePositive(buildTime, "buildTime");
    requirePositive(targetSolveTime, "targetSolveTime");
    requirePositive(maximumSolveTime, "maximumSolveTime");
    requirePositive(mouseMoveInterval, "mouseMoveInterval");
    requireWithinGrid(mouseStart, gridSize, "mouseStart");
    requireWithinGrid(cheese, gridSize, "cheese");
    if (mouseStart.equals(cheese)) {
      throw new IllegalArgumentException("mouseStart and cheese must be different");
    }
    if (targetSolveTime.compareTo(maximumSolveTime) > 0) {
      throw new IllegalArgumentException("targetSolveTime must not exceed maximumSolveTime");
    }
  }

  private static String requireNonBlank(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }

  private static void requirePositive(Duration value, String name) {
    Objects.requireNonNull(value, name);
    if (value.compareTo(Duration.ZERO) <= 0) {
      throw new IllegalArgumentException(name + " must be positive");
    }
  }

  private static void requireWithinGrid(GridPosition position, GridSize gridSize, String name) {
    if (!position.isWithin(gridSize)) {
      throw new IllegalArgumentException(name + " must be inside the grid");
    }
  }
}
