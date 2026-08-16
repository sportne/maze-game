package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

final class PresetCellDefinitionTest {
  private static final GridPosition START = p(3, 0);
  private static final GridPosition GOAL = p(0, 3);

  @Test
  void presetCellRequiresPositionAndPlaceableType() {
    assertThrows(NullPointerException.class, () -> new PresetCell(null, PlaceableCellType.WALL));
    assertThrows(NullPointerException.class, () -> new PresetCell(p(1, 1), null));
  }

  @Test
  void presetsAreOrderedDefensivelyCopiedAndParticipateInEquality() {
    List<PresetCell> mutable =
        new ArrayList<>(
            List.of(
                new PresetCell(p(2, 1), PlaceableCellType.WALL),
                new PresetCell(p(1, 2), PlaceableCellType.SLOW_FLOOR)));
    LevelDefinition level = level(List.of(), mutable, 2, 2);
    mutable.clear();

    assertEquals(
        List.of(
            new PresetCell(p(2, 1), PlaceableCellType.WALL),
            new PresetCell(p(1, 2), PlaceableCellType.SLOW_FLOOR)),
        level.presetCells());
    assertEquals(PlaceableCellType.WALL, level.presetCellAt(p(2, 1)).orElseThrow());
    assertEquals(java.util.Optional.empty(), level.presetCellAt(p(0, 0)));
    assertThrows(
        UnsupportedOperationException.class,
        () -> level.presetCells().add(new PresetCell(p(2, 2), PlaceableCellType.WALL)));
    assertNotEquals(level, level(List.of(), List.of(), 2, 2));
  }

  @Test
  void rejectsNullOutsideDuplicateProtectedAndFixedOverlappingPresets() {
    assertThrows(NullPointerException.class, () -> level(List.of(), null, 2, 2));
    assertThrows(
        NullPointerException.class, () -> level(List.of(), Arrays.asList((PresetCell) null), 2, 2));
    assertThrows(
        IllegalArgumentException.class,
        () -> level(List.of(), List.of(new PresetCell(p(-1, 0), PlaceableCellType.WALL)), 2, 2));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            level(
                List.of(),
                List.of(
                    new PresetCell(p(2, 1), PlaceableCellType.WALL),
                    new PresetCell(p(2, 1), PlaceableCellType.SLOW_FLOOR)),
                2,
                2));
    assertThrows(
        IllegalArgumentException.class,
        () -> level(List.of(), List.of(new PresetCell(START, PlaceableCellType.SLOW_FLOOR)), 2, 2));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            level(
                List.of(new FixedCell(p(1, 1), FixedCellType.WALL)),
                List.of(new PresetCell(p(1, 1), PlaceableCellType.SLOW_FLOOR)),
                2,
                2));
  }

  @Test
  void rejectsPresetsBeyondFiniteSupplyOrWithoutABaselinePath() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            level(
                List.of(),
                List.of(
                    new PresetCell(p(2, 1), PlaceableCellType.WALL),
                    new PresetCell(p(2, 2), PlaceableCellType.WALL)),
                1,
                2));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            level(
                List.of(),
                List.of(
                    new PresetCell(p(1, 0), PlaceableCellType.WALL),
                    new PresetCell(p(1, 1), PlaceableCellType.WALL),
                    new PresetCell(p(1, 2), PlaceableCellType.WALL),
                    new PresetCell(p(1, 3), PlaceableCellType.WALL)),
                4,
                2));
  }

  @Test
  void infiniteSupplyAcceptsAnyValidatedPresetCount() {
    LevelDefinition level =
        new LevelDefinition(
            "infinite-preset",
            "Infinite Preset",
            GridSize.square(4),
            Duration.ofSeconds(10),
            Duration.ofSeconds(1),
            Duration.ofSeconds(5),
            Duration.ofMillis(250),
            List.of(
                PlaceableCellSupply.infinite(PlaceableCellType.WALL),
                PlaceableCellSupply.infinite(PlaceableCellType.SLOW_FLOOR)),
            List.of(),
            List.of(
                new PresetCell(p(2, 1), PlaceableCellType.WALL),
                new PresetCell(p(2, 2), PlaceableCellType.SLOW_FLOOR)),
            List.of(solver()));

    assertEquals(2, level.presetCells().size());
  }

  @Test
  void initiallyAvailableTypesAccountForInventoryConsumedByPresets() {
    LevelDefinition level =
        level(
            List.of(),
            List.of(
                new PresetCell(p(2, 1), PlaceableCellType.WALL),
                new PresetCell(p(1, 2), PlaceableCellType.SLOW_FLOOR)),
            1,
            2);

    assertEquals(List.of(PlaceableCellType.SLOW_FLOOR), level.initiallyAvailableCellTypes());
  }

  private static LevelDefinition level(
      List<FixedCell> fixedCells, List<PresetCell> presetCells, int walls, int slowFloors) {
    return new LevelDefinition(
        "preset-test",
        "Preset Test",
        GridSize.square(4),
        Duration.ofSeconds(10),
        Duration.ofSeconds(1),
        Duration.ofSeconds(5),
        Duration.ofMillis(250),
        List.of(
            PlaceableCellSupply.finite(PlaceableCellType.WALL, walls),
            PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, slowFloors)),
        fixedCells,
        presetCells,
        List.of(solver()));
  }

  private static LevelSolver solver() {
    return new LevelSolver(
        START,
        GOAL,
        SolverBehavior.LEFT_PRIORITY,
        OptionalLong.empty(),
        SolverAppearance.SCOUT_SQUIRREL,
        GoalType.ACORN);
  }

  private static GridPosition p(int row, int column) {
    return new GridPosition(row, column);
  }
}
