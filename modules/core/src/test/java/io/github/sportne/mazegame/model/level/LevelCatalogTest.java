package io.github.sportne.mazegame.model.level;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class LevelCatalogTest {
  private static final LevelDefinition FIRST_LEVEL = Levels.levelOne();
  private static final LevelDefinition SECOND_LEVEL = levelWithId("test-level-2");

  @Test
  void preservesDisplayOrderAndFindsLevelsByStableId() {
    LevelCatalog catalog = new LevelCatalog(List.of(FIRST_LEVEL, SECOND_LEVEL));

    assertEquals(List.of(FIRST_LEVEL, SECOND_LEVEL), catalog.levels());
    assertEquals(FIRST_LEVEL, catalog.findById(FIRST_LEVEL.id()).orElseThrow());
    assertEquals(SECOND_LEVEL, catalog.findById(SECOND_LEVEL.id()).orElseThrow());
    assertTrue(catalog.findById("missing-level").isEmpty());
  }

  @Test
  void protectsItsOrderedLevelsFromCallerMutation() {
    List<LevelDefinition> suppliedLevels = new ArrayList<>(List.of(FIRST_LEVEL));
    LevelCatalog catalog = new LevelCatalog(suppliedLevels);

    suppliedLevels.add(SECOND_LEVEL);

    assertEquals(List.of(FIRST_LEVEL), catalog.levels());
    assertThrows(UnsupportedOperationException.class, () -> catalog.levels().add(SECOND_LEVEL));
  }

  @Test
  void rejectsEmptyCatalogsAndDuplicateIds() {
    LevelDefinition duplicate = levelWithId(FIRST_LEVEL.id());

    assertThrows(IllegalArgumentException.class, () -> new LevelCatalog(List.of()));
    assertThrows(
        IllegalArgumentException.class, () -> new LevelCatalog(List.of(FIRST_LEVEL, duplicate)));
  }

  private static LevelDefinition levelWithId(String id) {
    return singleSolverLevel(
        id,
        "Test Level",
        FIRST_LEVEL.gridSize(),
        FIRST_LEVEL.primarySolver().start(),
        FIRST_LEVEL.primarySolver().goal(),
        FIRST_LEVEL.buildTime(),
        FIRST_LEVEL.targetSolveTime(),
        FIRST_LEVEL.maximumSolveTime(),
        FIRST_LEVEL.solverMoveInterval(),
        FIRST_LEVEL.placeableCellSupplies(),
        SolverBehavior.RANDOM,
        2L);
  }
}
