package io.github.sportne.mazegame.model;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable authoring data for one playable level.
 *
 * <p>A level definition contains the static data shared by maze editing, mouse simulation, and
 * result evaluation. It does not contain player-placed walls; those live in {@link MazeState}.
 *
 * @param id stable machine-readable level identifier
 * @param name display name for the level
 * @param gridSize dimensions of the level grid
 * @param mouseStart fixed starting position for the mouse
 * @param cheese fixed endpoint position for the cheese
 * @param buildTime amount of time the player gets to place walls before auto-start
 * @param targetSolveTime solve time the mouse must exceed for the player to pass
 * @param maximumSolveTime timeout that ends the run if the cheese is not reached
 * @param mouseMoveInterval time between mouse movement decisions
 * @param randomSeed seed used by deterministic mouse AI
 */
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
  /**
   * Creates validated level authoring data.
   *
   * @throws IllegalArgumentException when metadata is blank, positions are invalid, durations are
   *     non-positive, start and cheese overlap, or the target exceeds the timeout
   */
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

  /**
   * Returns a nonblank string or throws a validation error.
   *
   * @param value value to validate
   * @param name parameter name used in error messages
   * @return the same value when it is nonblank
   */
  private static String requireNonBlank(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }

  /**
   * Validates that a duration is strictly positive.
   *
   * @param value duration to validate
   * @param name parameter name used in error messages
   */
  private static void requirePositive(Duration value, String name) {
    Objects.requireNonNull(value, name);
    if (value.compareTo(Duration.ZERO) <= 0) {
      throw new IllegalArgumentException(name + " must be positive");
    }
  }

  /**
   * Validates that a level position lies within the level grid.
   *
   * @param position position to validate
   * @param gridSize grid bounds for the level
   * @param name parameter name used in error messages
   */
  private static void requireWithinGrid(GridPosition position, GridSize gridSize, String name) {
    if (!position.isWithin(gridSize)) {
      throw new IllegalArgumentException(name + " must be inside the grid");
    }
  }
}
