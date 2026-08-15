package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPathfinder;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import java.time.Duration;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable authoring data for one playable level.
 *
 * <p>A level definition contains the static data shared by maze editing, solver simulation, and
 * result evaluation. It does not contain player-placed cells; those live in {@link
 * io.github.sportne.mazegame.model.maze.MazeState}.
 *
 * @param id stable machine-readable level identifier
 * @param name display name for the level
 * @param gridSize dimensions of the level grid
 * @param buildTime amount of time the player gets to place walls before auto-start
 * @param targetSolveTime solve time the solver must exceed for the player to pass
 * @param maximumSolveTime timeout that ends the run if the goal is not reached
 * @param solverMoveInterval time between solver movement decisions
 * @param placeableCellSupplies finite or infinite authored supply for every placeable type
 * @param fixedCells level-owned cells that player editing cannot change
 * @param solvers authored solvers in stable presentation order
 */
public record LevelDefinition(
    String id,
    String name,
    GridSize gridSize,
    Duration buildTime,
    Duration targetSolveTime,
    Duration maximumSolveTime,
    Duration solverMoveInterval,
    List<PlaceableCellSupply> placeableCellSupplies,
    List<FixedCell> fixedCells,
    List<LevelSolver> solvers) {
  /** Creates a level without fixed authored cells. */
  public LevelDefinition(
      String id,
      String name,
      GridSize gridSize,
      Duration buildTime,
      Duration targetSolveTime,
      Duration maximumSolveTime,
      Duration solverMoveInterval,
      List<PlaceableCellSupply> placeableCellSupplies,
      List<LevelSolver> solvers) {
    this(
        id,
        name,
        gridSize,
        buildTime,
        targetSolveTime,
        maximumSolveTime,
        solverMoveInterval,
        placeableCellSupplies,
        List.of(),
        solvers);
  }

  /**
   * Creates validated level authoring data.
   *
   * @throws IllegalArgumentException when metadata is blank, positions are invalid, durations are
   *     non-positive, start and goal overlap, or the target exceeds the timeout
   */
  public LevelDefinition {
    id = requireNonBlank(id, "id");
    name = requireNonBlank(name, "name");
    Objects.requireNonNull(gridSize, "gridSize");
    requirePositive(buildTime, "buildTime");
    requirePositive(targetSolveTime, "targetSolveTime");
    requirePositive(maximumSolveTime, "maximumSolveTime");
    requirePositive(solverMoveInterval, "solverMoveInterval");
    placeableCellSupplies = validateSupplies(placeableCellSupplies);
    solvers = validateSolvers(solvers, gridSize);
    fixedCells = validateFixedCells(fixedCells, gridSize, solvers);
    if (targetSolveTime.compareTo(maximumSolveTime) > 0) {
      throw new IllegalArgumentException("targetSolveTime must not exceed maximumSolveTime");
    }
  }

  /** Returns the authored supply for one supported placeable type. */
  public CellSupply supplyFor(PlaceableCellType type) {
    Objects.requireNonNull(type, "type");
    return placeableCellSupplies.stream()
        .filter(entry -> entry.type() == type)
        .findFirst()
        .orElseThrow()
        .supply();
  }

  /** Returns placeable types whose authored supply is usable when the level starts. */
  public List<PlaceableCellType> initiallyAvailableCellTypes() {
    return placeableCellSupplies.stream()
        .filter(entry -> entry.supply().available())
        .map(PlaceableCellSupply::type)
        .toList();
  }

  /** Returns an immutable defensive copy of the authored supplies in palette order. */
  @Override
  public List<PlaceableCellSupply> placeableCellSupplies() {
    return List.copyOf(placeableCellSupplies);
  }

  /** Returns immutable fixed cells in authored order. */
  @Override
  public List<FixedCell> fixedCells() {
    return List.copyOf(fixedCells);
  }

  /** Returns the fixed effect at a position, or empty for an editable cell. */
  public Optional<FixedCellType> fixedCellAt(GridPosition position) {
    Objects.requireNonNull(position, "position");
    return fixedCells.stream()
        .filter(cell -> cell.position().equals(position))
        .map(FixedCell::type)
        .findFirst();
  }

  /** Returns an immutable defensive copy of the authored solvers in presentation order. */
  @Override
  public List<LevelSolver> solvers() {
    return List.copyOf(solvers);
  }

  /** Returns the first authored solver used by single-solver views. */
  public LevelSolver primarySolver() {
    return solvers.get(0);
  }

  private static List<PlaceableCellSupply> validateSupplies(List<PlaceableCellSupply> supplies) {
    Objects.requireNonNull(supplies, "placeableCellSupplies");
    List<PlaceableCellSupply> copied = List.copyOf(supplies);
    EnumSet<PlaceableCellType> seen = EnumSet.noneOf(PlaceableCellType.class);
    for (PlaceableCellSupply entry : copied) {
      Objects.requireNonNull(entry, "placeableCellSupplies entry");
      if (!seen.add(entry.type())) {
        throw new IllegalArgumentException("duplicate supply for " + entry.type());
      }
    }
    if (!seen.equals(EnumSet.allOf(PlaceableCellType.class))) {
      throw new IllegalArgumentException("every placeable cell type must have exactly one supply");
    }
    return copied;
  }

  private static List<LevelSolver> validateSolvers(
      List<LevelSolver> authoredSolvers, GridSize gridSize) {
    Objects.requireNonNull(authoredSolvers, "solvers");
    List<LevelSolver> copied = List.copyOf(authoredSolvers);
    if (copied.isEmpty()) {
      throw new IllegalArgumentException("level must contain at least one solver");
    }
    java.util.HashSet<GridPosition> protectedPositions = new java.util.HashSet<>();
    for (LevelSolver solver : copied) {
      Objects.requireNonNull(solver, "solvers entry");
      requireWithinGrid(solver.start(), gridSize, "solver start");
      requireWithinGrid(solver.goal(), gridSize, "solver goal");
      if (!protectedPositions.add(solver.start())) {
        throw new IllegalArgumentException("solver starts must be unique");
      }
      if (!protectedPositions.add(solver.goal())) {
        throw new IllegalArgumentException("solver starts and goals must not overlap");
      }
    }
    return copied;
  }

  private static List<FixedCell> validateFixedCells(
      List<FixedCell> authoredCells, GridSize gridSize, List<LevelSolver> solvers) {
    Objects.requireNonNull(authoredCells, "fixedCells");
    List<FixedCell> copied = List.copyOf(authoredCells);
    Set<GridPosition> occupied = new HashSet<>();
    for (FixedCell cell : copied) {
      Objects.requireNonNull(cell, "fixedCells entry");
      requireWithinGrid(cell.position(), gridSize, "fixed cell");
      if (!occupied.add(cell.position())) {
        throw new IllegalArgumentException("fixed cells must occupy unique positions");
      }
      if (solvers.stream()
          .anyMatch(
              solver ->
                  cell.position().equals(solver.start())
                      || cell.position().equals(solver.goal()))) {
        throw new IllegalArgumentException("fixed cell must not overlap a solver start or goal");
      }
    }
    if (solvers.stream()
        .anyMatch(
            solver ->
                !GridPathfinder.hasPath(
                    gridSize,
                    solver.start(),
                    solver.goal(),
                    position ->
                        copied.stream()
                            .noneMatch(
                                cell ->
                                    cell.position().equals(position)
                                        && cell.type().blocksMovement())))) {
      throw new IllegalArgumentException("fixed cells must keep a path for every solver");
    }
    return copied;
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
