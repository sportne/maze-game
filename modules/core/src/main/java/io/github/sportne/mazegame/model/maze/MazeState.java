package io.github.sportne.mazegame.model.maze;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/** Immutable player-placed cells and derived inventory for one authored level. */
public record MazeState(
    LevelDefinition levelDefinition, Map<GridPosition, PlaceableCellType> placedCells) {
  /** Creates a validated, defensively copied maze. */
  public MazeState {
    Objects.requireNonNull(levelDefinition, "levelDefinition");
    Objects.requireNonNull(placedCells, "placedCells");
    placedCells = Map.copyOf(placedCells);
    for (Map.Entry<GridPosition, PlaceableCellType> entry : placedCells.entrySet()) {
      validatePlacedCell(levelDefinition, entry.getKey(), entry.getValue());
    }
    deriveRemainingSupplies(levelDefinition, placedCells);
    if (!hasPathsForEverySolver(levelDefinition, placedCells)) {
      throw new IllegalArgumentException("maze must keep a path for every solver");
    }
  }

  /** Compatibility constructor for released wall-only fixtures during the ordered migration. */
  public MazeState(LevelDefinition levelDefinition, Set<GridPosition> walls) {
    this(levelDefinition, wallCells(walls));
  }

  /** Creates an empty maze with inventory derived from the level definition. */
  public static MazeState empty(LevelDefinition levelDefinition) {
    return new MazeState(levelDefinition, Map.of());
  }

  /** Returns remaining finite or infinite supply for every placeable type. */
  public Map<PlaceableCellType, CellSupply> remainingSupplies() {
    return deriveRemainingSupplies(levelDefinition, placedCells);
  }

  /** Returns remaining finite or infinite supply for one placeable type. */
  public CellSupply remainingSupply(PlaceableCellType type) {
    Objects.requireNonNull(type, "type");
    return remainingSupplies().get(type);
  }

  /** Atomically places, replaces, or removes the selected type at a destination. */
  public MazeEditResult placeOrReplace(PlaceableCellType type, GridPosition destination) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(destination, "destination");
    MazeEditStatus invalidDestination = validateDestination(destination);
    if (invalidDestination != null) {
      return MazeEditResult.rejected(this, invalidDestination);
    }
    PlaceableCellType existing = placedCells.get(destination);
    if (existing == type) {
      Map<GridPosition, PlaceableCellType> updated = new HashMap<>(placedCells);
      updated.remove(destination);
      return MazeEditResult.accepted(
          new MazeState(levelDefinition, updated), MazeEditStatus.REMOVED);
    }
    if (!remainingSupply(type).available()) {
      return MazeEditResult.rejected(this, MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
    }
    Map<GridPosition, PlaceableCellType> updated = new HashMap<>(placedCells);
    updated.put(destination, type);
    if (!hasPathsForEverySolver(levelDefinition, updated)) {
      return MazeEditResult.rejected(this, MazeEditStatus.REJECTED_BLOCKS_PATH);
    }
    return MazeEditResult.accepted(
        new MazeState(levelDefinition, updated),
        existing == null ? MazeEditStatus.PLACED : MazeEditStatus.REPLACED);
  }

  /** Atomically removes a placed cell. Empty destinations are accepted no-ops. */
  public MazeEditResult remove(GridPosition position) {
    Objects.requireNonNull(position, "position");
    MazeEditStatus invalidDestination = validateDestination(position);
    if (invalidDestination != null) {
      return MazeEditResult.rejected(this, invalidDestination);
    }
    if (!placedCells.containsKey(position)) {
      return MazeEditResult.accepted(this, MazeEditStatus.NO_OP);
    }
    Map<GridPosition, PlaceableCellType> updated = new HashMap<>(placedCells);
    updated.remove(position);
    return MazeEditResult.accepted(new MazeState(levelDefinition, updated), MazeEditStatus.REMOVED);
  }

  /** Atomically moves one placed cell to an empty destination without changing inventory. */
  public MazeEditResult move(GridPosition source, GridPosition destination) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(destination, "destination");
    if (!source.isWithin(levelDefinition.gridSize())) {
      return MazeEditResult.rejected(this, MazeEditStatus.REJECTED_OUTSIDE_GRID);
    }
    PlaceableCellType type = placedCells.get(source);
    if (type == null) {
      return MazeEditResult.rejected(this, MazeEditStatus.REJECTED_MISSING_SOURCE);
    }
    MazeEditStatus invalidDestination = validateDestination(destination);
    if (invalidDestination != null) {
      return MazeEditResult.rejected(this, invalidDestination);
    }
    if (source.equals(destination)) {
      return MazeEditResult.accepted(this, MazeEditStatus.NO_OP);
    }
    if (placedCells.containsKey(destination)) {
      return MazeEditResult.rejected(this, MazeEditStatus.REJECTED_OCCUPIED_DESTINATION);
    }
    Map<GridPosition, PlaceableCellType> updated = new HashMap<>(placedCells);
    updated.remove(source);
    updated.put(destination, type);
    if (!hasPathsForEverySolver(levelDefinition, updated)) {
      return MazeEditResult.rejected(this, MazeEditStatus.REJECTED_BLOCKS_PATH);
    }
    return MazeEditResult.accepted(new MazeState(levelDefinition, updated), MazeEditStatus.MOVED);
  }

  /** Returns whether a placeable cell occupies a grid position. */
  public boolean hasPlacedCellAt(GridPosition position) {
    requireInsideGrid(position);
    return placedCells.containsKey(position);
  }

  /** Returns whether a candidate position is inside the grid and walkable by either solver. */
  public boolean isTraversable(GridPosition position) {
    Objects.requireNonNull(position, "position");
    return position.isWithin(levelDefinition.gridSize())
        && placedCells.get(position) != PlaceableCellType.WALL;
  }

  /** Returns whether entering a cell delays the next solver decision by one movement interval. */
  public boolean delaysNextDecisionAt(GridPosition position) {
    requireInsideGrid(position);
    return placedCells.get(position) == PlaceableCellType.SLOW_FLOOR;
  }

  /** Returns the placeable type at a position, or null when the cell is empty or protected. */
  public PlaceableCellType placedCellAt(GridPosition position) {
    requireInsideGrid(position);
    return placedCells.get(position);
  }

  /** Returns the content rendered for one grid position. */
  public CellContent cellContentAt(GridPosition position) {
    requireInsideGrid(position);
    if (levelDefinition.solvers().stream().anyMatch(solver -> position.equals(solver.start()))) {
      return CellContent.SOLVER_START;
    }
    if (levelDefinition.solvers().stream().anyMatch(solver -> position.equals(solver.goal()))) {
      return CellContent.GOAL;
    }
    return switch (placedCells.get(position)) {
      case WALL -> CellContent.NORMAL_WALL;
      case SLOW_FLOOR -> CellContent.SLOW_FLOOR;
      case null -> CellContent.EMPTY;
    };
  }

  /** Returns whether every solver start remains connected to its goal through walkable cells. */
  public boolean hasPathFromStartToGoal() {
    return hasPathsForEverySolver(levelDefinition, placedCells);
  }

  /** Wall-only compatibility view retained until session and renderer migration is complete. */
  public Set<GridPosition> walls() {
    Set<GridPosition> walls = new HashSet<>();
    placedCells.forEach(
        (position, type) -> {
          if (type == PlaceableCellType.WALL) {
            walls.add(position);
          }
        });
    return Set.copyOf(walls);
  }

  /** Wall-only compatibility query retained for existing simulations and UI code. */
  public boolean hasWallAt(GridPosition position) {
    requireInsideGrid(position);
    return placedCells.get(position) == PlaceableCellType.WALL;
  }

  /** Wall-only compatibility placement retained during the ordered migration. */
  public WallPlacementResult placeWall(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (position.isWithin(levelDefinition.gridSize()) && hasWallAt(position)) {
      return WallPlacementResult.accepted(this, WallPlacementStatus.ALREADY_PRESENT);
    }
    MazeEditResult edit = placeOrReplace(PlaceableCellType.WALL, position);
    return edit.accepted()
        ? WallPlacementResult.accepted(edit.mazeState(), WallPlacementStatus.PLACED)
        : WallPlacementResult.rejected(this, wallRejectionStatus(edit.status()));
  }

  /** Wall-only compatibility convenience that throws when placement is rejected. */
  public MazeState withWall(GridPosition position) {
    WallPlacementResult result = placeWall(position);
    if (!result.accepted()) {
      throw new IllegalArgumentException("wall placement rejected: " + result.status());
    }
    return result.mazeState();
  }

  /** Wall-only compatibility removal retained during the ordered migration. */
  public MazeState withoutWall(GridPosition position) {
    requireInsideGrid(position);
    if (placedCells.get(position) != PlaceableCellType.WALL) {
      return this;
    }
    return remove(position).mazeState();
  }

  /** Returns whether a position is reserved for a solver start or goal. */
  public boolean isProtected(GridPosition position) {
    requireInsideGrid(position);
    return levelDefinition.solvers().stream()
        .anyMatch(solver -> position.equals(solver.start()) || position.equals(solver.goal()));
  }

  private MazeEditStatus validateDestination(GridPosition position) {
    if (!position.isWithin(levelDefinition.gridSize())) {
      return MazeEditStatus.REJECTED_OUTSIDE_GRID;
    }
    if (levelDefinition.solvers().stream()
        .anyMatch(solver -> position.equals(solver.start()) || position.equals(solver.goal()))) {
      return MazeEditStatus.REJECTED_PROTECTED_CELL;
    }
    return null;
  }

  private void requireInsideGrid(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (!position.isWithin(levelDefinition.gridSize())) {
      throw new IllegalArgumentException("position must be inside the grid");
    }
  }

  private static Map<GridPosition, PlaceableCellType> wallCells(Set<GridPosition> walls) {
    Objects.requireNonNull(walls, "walls");
    Map<GridPosition, PlaceableCellType> cells = new HashMap<>();
    for (GridPosition wall : walls) {
      cells.put(Objects.requireNonNull(wall, "wall"), PlaceableCellType.WALL);
    }
    return cells;
  }

  private static void validatePlacedCell(
      LevelDefinition levelDefinition, GridPosition position, PlaceableCellType type) {
    Objects.requireNonNull(position, "placed cell position");
    Objects.requireNonNull(type, "placed cell type");
    if (!position.isWithin(levelDefinition.gridSize())) {
      throw new IllegalArgumentException("placed cell must be inside the grid");
    }
    if (levelDefinition.solvers().stream()
        .anyMatch(solver -> position.equals(solver.start()) || position.equals(solver.goal()))) {
      throw new IllegalArgumentException("placed cell must not be protected");
    }
  }

  private static Map<PlaceableCellType, CellSupply> deriveRemainingSupplies(
      LevelDefinition levelDefinition, Map<GridPosition, PlaceableCellType> placedCells) {
    EnumMap<PlaceableCellType, CellSupply> remaining = new EnumMap<>(PlaceableCellType.class);
    for (PlaceableCellType type : PlaceableCellType.values()) {
      remaining.put(type, levelDefinition.supplyFor(type));
    }
    for (PlaceableCellType type : placedCells.values()) {
      CellSupply supply = remaining.get(type);
      if (!supply.available()) {
        throw new IllegalArgumentException("placed cells exceed authored supply for " + type);
      }
      remaining.put(type, supply.consume());
    }
    return Map.copyOf(remaining);
  }

  private static boolean hasPathsForEverySolver(
      LevelDefinition levelDefinition, Map<GridPosition, PlaceableCellType> placedCells) {
    return levelDefinition.solvers().stream()
        .allMatch(solver -> hasPath(levelDefinition, placedCells, solver.start(), solver.goal()));
  }

  private static boolean hasPath(
      LevelDefinition levelDefinition,
      Map<GridPosition, PlaceableCellType> placedCells,
      GridPosition start,
      GridPosition goal) {
    Queue<GridPosition> frontier = new ArrayDeque<>();
    Set<GridPosition> visited = new HashSet<>();
    frontier.add(start);
    visited.add(start);
    while (!frontier.isEmpty()) {
      GridPosition current = frontier.remove();
      if (current.equals(goal)) {
        return true;
      }
      for (GridPosition neighbor : neighbors(current)) {
        if (isOpen(levelDefinition, placedCells, neighbor) && visited.add(neighbor)) {
          frontier.add(neighbor);
        }
      }
    }
    return false;
  }

  private static Set<GridPosition> neighbors(GridPosition position) {
    return Set.of(
        new GridPosition(position.row() - 1, position.column()),
        new GridPosition(position.row() + 1, position.column()),
        new GridPosition(position.row(), position.column() - 1),
        new GridPosition(position.row(), position.column() + 1));
  }

  private static boolean isOpen(
      LevelDefinition levelDefinition,
      Map<GridPosition, PlaceableCellType> placedCells,
      GridPosition position) {
    return position.isWithin(levelDefinition.gridSize())
        && placedCells.get(position) != PlaceableCellType.WALL;
  }

  private static WallPlacementStatus wallRejectionStatus(MazeEditStatus status) {
    return switch (status) {
      case REJECTED_OUTSIDE_GRID -> WallPlacementStatus.REJECTED_OUTSIDE_GRID;
      case REJECTED_PROTECTED_CELL -> WallPlacementStatus.REJECTED_PROTECTED_CELL;
      case REJECTED_EXHAUSTED_SUPPLY -> WallPlacementStatus.REJECTED_EXHAUSTED_SUPPLY;
      case REJECTED_BLOCKS_PATH -> WallPlacementStatus.REJECTED_BLOCKS_PATH;
      default -> throw new IllegalArgumentException("unexpected wall edit status: " + status);
    };
  }
}
