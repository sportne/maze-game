package io.github.sportne.mazegame.model.maze;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

final class MazeInventoryTest {
  private static final GridPosition FIRST = position(3, 1);
  private static final GridPosition SECOND = position(3, 2);

  @Test
  void supplyValuesExposeFiniteCountsAndInfiniteAvailability() {
    CellSupply finite = CellSupply.finite(2);
    CellSupply infinite = CellSupply.infinite();

    assertEquals(2, finite.finiteCount().orElseThrow());
    assertTrue(infinite.finiteCount().isEmpty());
    assertTrue(infinite.available());
    assertSame(infinite, infinite.consume());
    assertEquals(CellSupply.finite(1), finite.consume());
    assertThrows(IllegalStateException.class, () -> CellSupply.finite(0).consume());
    assertNotEquals(finite, infinite);
  }

  @Test
  void finitePlaceReplaceAndRemoveFollowAtomicConsumeReturnRules() {
    MazeState empty = MazeState.empty(level(CellSupply.finite(2), CellSupply.finite(1)));

    MazeEditResult wallPlaced = empty.placeOrReplace(PlaceableCellType.WALL, FIRST);
    MazeEditResult slowPlaced =
        wallPlaced.mazeState().placeOrReplace(PlaceableCellType.SLOW_FLOOR, SECOND);
    MazeEditResult slowRecovered =
        slowPlaced.mazeState().placeOrReplace(PlaceableCellType.SLOW_FLOOR, SECOND);
    MazeEditResult replaced =
        slowRecovered.mazeState().placeOrReplace(PlaceableCellType.SLOW_FLOOR, FIRST);
    MazeEditResult lastItemRecovered =
        replaced.mazeState().placeOrReplace(PlaceableCellType.SLOW_FLOOR, FIRST);

    assertAccepted(wallPlaced, MazeEditStatus.PLACED, PlaceableCellType.WALL, FIRST);
    assertEquals(
        CellSupply.finite(1), wallPlaced.mazeState().remainingSupply(PlaceableCellType.WALL));
    assertAccepted(slowPlaced, MazeEditStatus.PLACED, PlaceableCellType.SLOW_FLOOR, SECOND);
    assertEquals(
        CellSupply.finite(0), slowPlaced.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
    assertEquals(MazeEditStatus.REMOVED, slowRecovered.status());
    assertEquals(
        CellSupply.finite(1),
        slowRecovered.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
    assertAccepted(replaced, MazeEditStatus.REPLACED, PlaceableCellType.SLOW_FLOOR, FIRST);
    assertEquals(
        CellSupply.finite(2), replaced.mazeState().remainingSupply(PlaceableCellType.WALL));
    assertEquals(
        CellSupply.finite(0), replaced.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
    assertEquals(MazeEditStatus.REMOVED, lastItemRecovered.status());
    assertNull(lastItemRecovered.mazeState().placedCellAt(FIRST));
    assertEquals(
        CellSupply.finite(1),
        lastItemRecovered.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
  }

  @Test
  void exhaustedTypeRejectsEmptyAndDifferentDestinationsButAllowsSameTypeRecovery() {
    MazeState oneSlowFloor =
        MazeState.empty(level(CellSupply.finite(1), CellSupply.finite(1)))
            .placeOrReplace(PlaceableCellType.SLOW_FLOOR, FIRST)
            .mazeState();
    MazeState differentType =
        oneSlowFloor.placeOrReplace(PlaceableCellType.WALL, SECOND).mazeState();

    assertRejectedSame(
        oneSlowFloor.placeOrReplace(PlaceableCellType.SLOW_FLOOR, SECOND),
        oneSlowFloor,
        MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
    assertRejectedSame(
        differentType.placeOrReplace(PlaceableCellType.SLOW_FLOOR, SECOND),
        differentType,
        MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);

    MazeEditResult recovered = differentType.placeOrReplace(PlaceableCellType.SLOW_FLOOR, FIRST);
    assertEquals(MazeEditStatus.REMOVED, recovered.status());
    assertEquals(
        CellSupply.finite(1), recovered.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
  }

  @ParameterizedTest
  @EnumSource(PlaceableCellType.class)
  void finiteReplacementAtZeroAndSameTypeRecoveryAreSymmetric(PlaceableCellType selectedType) {
    PlaceableCellType replacedType = otherType(selectedType);
    MazeState bothExhausted =
        MazeState.empty(level(CellSupply.finite(1), CellSupply.finite(1)))
            .placeOrReplace(selectedType, FIRST)
            .mazeState()
            .placeOrReplace(replacedType, SECOND)
            .mazeState();

    assertRejectedSame(
        bothExhausted.placeOrReplace(selectedType, SECOND),
        bothExhausted,
        MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
    MazeEditResult recovered = bothExhausted.placeOrReplace(selectedType, FIRST);
    assertEquals(MazeEditStatus.REMOVED, recovered.status());
    assertEquals(CellSupply.finite(1), recovered.mazeState().remainingSupply(selectedType));

    MazeEditResult replaced = recovered.mazeState().placeOrReplace(selectedType, SECOND);
    assertAccepted(replaced, MazeEditStatus.REPLACED, selectedType, SECOND);
    assertEquals(CellSupply.finite(0), replaced.mazeState().remainingSupply(selectedType));
    assertEquals(CellSupply.finite(1), replaced.mazeState().remainingSupply(replacedType));
  }

  @ParameterizedTest
  @EnumSource(PlaceableCellType.class)
  void infiniteSupplyNeverChangesAcrossPlacementAndRemoval(PlaceableCellType type) {
    MazeState empty = MazeState.empty(level(CellSupply.infinite(), CellSupply.infinite()));
    MazeEditResult placed = empty.placeOrReplace(type, FIRST);
    MazeEditResult removed = placed.mazeState().remove(FIRST);

    assertEquals(CellSupply.infinite(), placed.mazeState().remainingSupply(type));
    assertEquals(CellSupply.infinite(), removed.mazeState().remainingSupply(type));
    assertEquals(MazeEditStatus.REMOVED, removed.status());
  }

  @ParameterizedTest
  @EnumSource(PlaceableCellType.class)
  void infiniteReplacementReturnsAndConsumesWithoutChangingEitherSupply(
      PlaceableCellType selectedType) {
    PlaceableCellType replacedType = otherType(selectedType);
    MazeState occupied =
        MazeState.empty(level(CellSupply.infinite(), CellSupply.infinite()))
            .placeOrReplace(replacedType, FIRST)
            .mazeState();

    MazeEditResult replaced = occupied.placeOrReplace(selectedType, FIRST);

    assertAccepted(replaced, MazeEditStatus.REPLACED, selectedType, FIRST);
    assertEquals(CellSupply.infinite(), replaced.mazeState().remainingSupply(selectedType));
    assertEquals(CellSupply.infinite(), replaced.mazeState().remainingSupply(replacedType));
  }

  @Test
  void slowFloorIsWalkableWhileWallPlacementStillProtectsConnectivity() {
    LevelDefinition level = level(CellSupply.infinite(), CellSupply.infinite());
    Map<GridPosition, PlaceableCellType> almostBlocking =
        Map.of(
            position(2, 0), PlaceableCellType.WALL,
            position(2, 1), PlaceableCellType.WALL,
            position(2, 2), PlaceableCellType.SLOW_FLOOR,
            position(2, 3), PlaceableCellType.WALL,
            position(2, 4), PlaceableCellType.WALL);
    MazeState maze = new MazeState(level, almostBlocking);

    assertTrue(maze.hasPathFromStartToGoal());
    assertEquals(CellContent.SLOW_FLOOR, maze.cellContentAt(position(2, 2)));
    assertRejectedSame(
        maze.placeOrReplace(PlaceableCellType.WALL, position(2, 2)),
        maze,
        MazeEditStatus.REJECTED_BLOCKS_PATH);
  }

  @ParameterizedTest
  @EnumSource(PlaceableCellType.class)
  void moveChangesOnlySourceAndDestinationWithoutChangingInventory(PlaceableCellType type) {
    MazeState placed =
        MazeState.empty(level(CellSupply.finite(2), CellSupply.finite(2)))
            .placeOrReplace(type, FIRST)
            .mazeState();
    Map<PlaceableCellType, CellSupply> inventoryBefore = placed.remainingSupplies();

    MazeEditResult moved = placed.move(FIRST, SECOND);

    assertEquals(MazeEditStatus.MOVED, moved.status());
    assertTrue(moved.accepted());
    assertNull(moved.mazeState().placedCellAt(FIRST));
    assertEquals(type, moved.mazeState().placedCellAt(SECOND));
    assertEquals(inventoryBefore, moved.mazeState().remainingSupplies());
  }

  @Test
  void moveRejectsMissingOccupiedProtectedOutsideAndPathBlockingDestinations() {
    LevelDefinition level = level(CellSupply.infinite(), CellSupply.infinite());
    MazeState placed =
        MazeState.empty(level)
            .placeOrReplace(PlaceableCellType.WALL, FIRST)
            .mazeState()
            .placeOrReplace(PlaceableCellType.SLOW_FLOOR, SECOND)
            .mazeState();

    assertRejectedSame(
        placed.move(position(4, 4), position(4, 3)),
        placed,
        MazeEditStatus.REJECTED_MISSING_SOURCE);
    assertRejectedSame(
        placed.move(level.primarySolver().start(), position(4, 3)),
        placed,
        MazeEditStatus.REJECTED_MISSING_SOURCE);
    assertRejectedSame(
        placed.move(level.primarySolver().goal(), position(4, 3)),
        placed,
        MazeEditStatus.REJECTED_MISSING_SOURCE);
    assertRejectedSame(
        placed.move(FIRST, SECOND), placed, MazeEditStatus.REJECTED_OCCUPIED_DESTINATION);
    assertRejectedSame(
        placed.move(FIRST, level.primarySolver().start()),
        placed,
        MazeEditStatus.REJECTED_PROTECTED_CELL);
    assertRejectedSame(
        placed.move(FIRST, position(5, 0)), placed, MazeEditStatus.REJECTED_OUTSIDE_GRID);

    Map<GridPosition, PlaceableCellType> cells = new HashMap<>();
    cells.put(position(2, 0), PlaceableCellType.WALL);
    cells.put(position(2, 1), PlaceableCellType.WALL);
    cells.put(position(2, 3), PlaceableCellType.WALL);
    cells.put(position(2, 4), PlaceableCellType.WALL);
    cells.put(position(4, 0), PlaceableCellType.WALL);
    MazeState pathSensitive = new MazeState(level, cells);
    assertRejectedSame(
        pathSensitive.move(position(4, 0), position(2, 2)),
        pathSensitive,
        MazeEditStatus.REJECTED_BLOCKS_PATH);
  }

  @Test
  void sourceEqualsDestinationIsANoOpOnlyForAnExistingSource() {
    MazeState placed =
        MazeState.empty(level(CellSupply.finite(1), CellSupply.finite(1)))
            .placeOrReplace(PlaceableCellType.WALL, FIRST)
            .mazeState();

    MazeEditResult noOp = placed.move(FIRST, FIRST);
    assertSame(placed, noOp.mazeState());
    assertEquals(MazeEditStatus.NO_OP, noOp.status());
    assertTrue(noOp.accepted());
    assertRejectedSame(placed.move(SECOND, SECOND), placed, MazeEditStatus.REJECTED_MISSING_SOURCE);
  }

  @Test
  void removeEmptyIsNoOpAndInvalidDestinationsAreRejectedUnchanged() {
    MazeState maze = MazeState.empty(level(CellSupply.finite(1), CellSupply.finite(1)));

    MazeEditResult empty = maze.remove(FIRST);
    assertSame(maze, empty.mazeState());
    assertEquals(MazeEditStatus.NO_OP, empty.status());
    assertRejectedSame(
        maze.remove(maze.levelDefinition().primarySolver().goal()),
        maze,
        MazeEditStatus.REJECTED_PROTECTED_CELL);
    assertRejectedSame(maze.remove(position(-1, 0)), maze, MazeEditStatus.REJECTED_OUTSIDE_GRID);
  }

  @Test
  void placeOrReplaceRejectsProtectedOutsideAndExhaustedCellsWithOriginalIdentity() {
    MazeState maze = MazeState.empty(level(CellSupply.finite(0), CellSupply.finite(0)));

    assertRejectedSame(
        maze.placeOrReplace(PlaceableCellType.WALL, maze.levelDefinition().primarySolver().start()),
        maze,
        MazeEditStatus.REJECTED_PROTECTED_CELL);
    assertRejectedSame(
        maze.placeOrReplace(PlaceableCellType.SLOW_FLOOR, position(0, 5)),
        maze,
        MazeEditStatus.REJECTED_OUTSIDE_GRID);
    assertRejectedSame(
        maze.placeOrReplace(PlaceableCellType.WALL, FIRST),
        maze,
        MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
  }

  @ParameterizedTest
  @EnumSource(PlaceableCellType.class)
  void zeroSupplyRejectsPlacementForBothTypes(PlaceableCellType type) {
    MazeState maze = MazeState.empty(level(CellSupply.finite(0), CellSupply.finite(0)));

    assertRejectedSame(
        maze.placeOrReplace(type, FIRST), maze, MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
  }

  @Test
  void canonicalStateDefensivelyCopiesCellsAndParticipatesInEquality() {
    LevelDefinition level = level(CellSupply.finite(2), CellSupply.finite(2));
    Map<GridPosition, PlaceableCellType> mutable = new HashMap<>();
    mutable.put(FIRST, PlaceableCellType.SLOW_FLOOR);
    MazeState state = new MazeState(level, mutable);
    mutable.clear();

    assertEquals(PlaceableCellType.SLOW_FLOOR, state.placedCellAt(FIRST));
    assertThrows(
        UnsupportedOperationException.class,
        () -> state.placedCells().put(SECOND, PlaceableCellType.WALL));
    assertEquals(new MazeState(level, Map.of(FIRST, PlaceableCellType.SLOW_FLOOR)), state);
    assertNotEquals(MazeState.empty(level), state);
  }

  @Test
  void canonicalStateRejectsInvalidCellsAndSupplyOverflow() {
    LevelDefinition level = level(CellSupply.finite(1), CellSupply.finite(1));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MazeState(
                level, Map.of(level.primarySolver().start(), PlaceableCellType.SLOW_FLOOR)));
    assertThrows(
        IllegalArgumentException.class,
        () -> new MazeState(level, Map.of(position(-1, 0), PlaceableCellType.WALL)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MazeState(
                level, Map.of(FIRST, PlaceableCellType.WALL, SECOND, PlaceableCellType.WALL)));
  }

  @Test
  void canonicalWallEditsRemainInventoryAware() {
    LevelDefinition finite = level(CellSupply.finite(1), CellSupply.finite(1));
    MazeState maze = MazeState.empty(finite);

    MazeEditResult placed = maze.placeOrReplace(PlaceableCellType.WALL, FIRST);
    MazeEditResult removed = placed.mazeState().placeOrReplace(PlaceableCellType.WALL, FIRST);
    MazeEditResult exhausted = placed.mazeState().placeOrReplace(PlaceableCellType.WALL, SECOND);

    assertEquals(MazeEditStatus.PLACED, placed.status());
    assertEquals(MazeEditStatus.REMOVED, removed.status());
    assertEquals(MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY, exhausted.status());
    assertTrue(removed.mazeState().placedCells().isEmpty());
    assertSame(placed.mazeState(), exhausted.mazeState());
  }

  private static void assertAccepted(
      MazeEditResult result, MazeEditStatus status, PlaceableCellType type, GridPosition position) {
    assertTrue(result.accepted());
    assertEquals(status, result.status());
    assertEquals(type, result.mazeState().placedCellAt(position));
  }

  private static void assertRejectedSame(
      MazeEditResult result, MazeState expectedState, MazeEditStatus status) {
    assertFalse(result.accepted());
    assertEquals(status, result.status());
    assertSame(expectedState, result.mazeState());
    assertEquals(expectedState.remainingSupplies(), result.mazeState().remainingSupplies());
  }

  private static PlaceableCellType otherType(PlaceableCellType type) {
    return type == PlaceableCellType.WALL ? PlaceableCellType.SLOW_FLOOR : PlaceableCellType.WALL;
  }

  private static LevelDefinition level(CellSupply wallSupply, CellSupply slowFloorSupply) {
    return singleSolverLevel(
        "inventory-test",
        "Inventory Test",
        GridSize.square(5),
        position(4, 2),
        position(0, 2),
        Duration.ofSeconds(30),
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        Duration.ofMillis(250),
        List.of(
            new PlaceableCellSupply(PlaceableCellType.WALL, wallSupply),
            new PlaceableCellSupply(PlaceableCellType.SLOW_FLOOR, slowFloorSupply),
            new PlaceableCellSupply(PlaceableCellType.ALTERNATING_GATE, wallSupply)),
        SolverBehavior.RANDOM,
        1L);
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }
}
