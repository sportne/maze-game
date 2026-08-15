package io.github.sportne.mazegame.teavm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import org.junit.jupiter.api.Test;

final class BrowserBuildGestureFixtureTest {
  @Test
  void fixtureRequiresAnExplicitLoopbackSessionToken() {
    assertTrue(BrowserBuildGestureFixture.requested("127.0.0.1", "enabled"));
    assertTrue(BrowserBuildGestureFixture.requested("localhost", "enabled"));
    assertTrue(BrowserBuildGestureFixture.requested("::1", "enabled"));
    assertFalse(BrowserBuildGestureFixture.requested("sportne.github.io", "enabled"));
    assertFalse(BrowserBuildGestureFixture.requested("127.0.0.1", null));
  }

  @Test
  void productionHostsDoNotAttemptToReadFixtureStorage() {
    assertFalse(
        TeaVMLauncher.browserBuildGestureFixtureRequested(
            "sportne.github.io",
            ignored -> {
              throw new AssertionError("production host read fixture storage");
            }));
    assertFalse(TeaVMLauncher.browserBuildGestureFixtureRequested("127.0.0.1", ignored -> null));
    assertTrue(
        TeaVMLauncher.browserBuildGestureFixtureRequested(
            "127.0.0.1", ignored -> BrowserBuildGestureFixture.STORAGE_VALUE));
    assertThrows(
        AssertionError.class,
        () ->
            TeaVMLauncher.browserBuildGestureFixtureRequested(
                "127.0.0.1",
                ignored -> {
                  throw new AssertionError("loopback host reads fixture storage");
                }));
  }

  @Test
  void fixtureSuppliesBothPlacedCellTypes() {
    LevelDefinition level = BrowserBuildGestureFixture.level();

    assertEquals(CellSupply.finite(2), level.supplyFor(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(2), level.supplyFor(PlaceableCellType.SLOW_FLOOR));
    assertEquals(FixedCellType.WALL, level.fixedCellAt(new GridPosition(1, 0)).orElseThrow());
    assertEquals(FixedCellType.SLOW_FLOOR, level.fixedCellAt(new GridPosition(1, 4)).orElseThrow());
  }
}
