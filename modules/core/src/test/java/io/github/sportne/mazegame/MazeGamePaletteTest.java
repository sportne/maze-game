package io.github.sportne.mazegame;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelCatalog;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.CellPaletteState;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameSession;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

final class MazeGamePaletteTest {
  private static final int WIDTH = 1280;
  private static final int HEIGHT = 720;
  private static final GridPosition FIRST = new GridPosition(2, 0);
  private static final GridPosition SECOND = new GridPosition(2, 1);

  @Test
  void selectThenPlaceSupportsFiniteSelectionReplacementRecoveryAndRightClickRemoval() {
    MazeGame game = finiteGame();

    assertEquals(PlaceableCellType.WALL, selected(game).type());
    clickPalette(game, PlaceableCellType.SLOW_FLOOR);
    assertEquals(PlaceableCellType.SLOW_FLOOR, selected(game).type());

    clickCell(game, FIRST, Input.Buttons.LEFT);
    assertEquals(PlaceableCellType.SLOW_FLOOR, game.mazeState().placedCellAt(FIRST));
    assertEquals(CellSupply.finite(0), selected(game).remainingSupply());

    clickCell(game, SECOND, Input.Buttons.LEFT);
    assertEquals(SECOND, game.rejectedPosition());
    assertEquals(1, game.mazeState().placedCells().size());

    clickCell(game, FIRST, Input.Buttons.LEFT);
    assertTrue(game.mazeState().placedCells().isEmpty());
    assertEquals(CellSupply.finite(1), selected(game).remainingSupply());

    clickPalette(game, PlaceableCellType.WALL);
    clickCell(game, SECOND, Input.Buttons.LEFT);
    assertEquals(PlaceableCellType.WALL, game.mazeState().placedCellAt(SECOND));

    clickPalette(game, PlaceableCellType.SLOW_FLOOR);
    clickCell(game, SECOND, Input.Buttons.LEFT);
    assertEquals(PlaceableCellType.SLOW_FLOOR, game.mazeState().placedCellAt(SECOND));
    assertEquals(CellSupply.finite(1), palette(game, PlaceableCellType.WALL).remainingSupply());
    assertEquals(CellSupply.finite(0), selected(game).remainingSupply());

    clickCell(game, SECOND, Input.Buttons.RIGHT);
    assertTrue(game.mazeState().placedCells().isEmpty());
    assertEquals(PlaceableCellType.SLOW_FLOOR, selected(game).type());
    assertEquals(CellSupply.finite(1), selected(game).remainingSupply());
  }

  @Test
  void releasedPaletteShowsOnlyTheInitiallyUsableWall() {
    MazeGame game = new MazeGame();
    game.startLevel(io.github.sportne.mazegame.model.level.Levels.levelOne().id());

    assertEquals(1, game.paletteState().size());
    assertEquals(PlaceableCellType.WALL, selected(game).type());
    assertEquals(CellSupply.infinite(), selected(game).remainingSupply());
    ScreenLayout layout = game.debugScreenLayout(GamePhase.BUILDING, WIDTH, HEIGHT);
    assertTrue(layout.element(MazeGameLayout.paletteItemId(PlaceableCellType.WALL)).isPresent());
    assertTrue(
        layout.element(MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR)).isEmpty());

    clickCell(game, FIRST, Input.Buttons.LEFT);
    assertEquals(PlaceableCellType.WALL, game.mazeState().placedCellAt(FIRST));
  }

  @Test
  void paletteTooltipAppearsAfterHalfSecondAndResetsOnExitOrInteraction() {
    MazeGame game = finiteGame();

    assertTrue(moveToPalette(game, PlaceableCellType.WALL));
    game.updateGame(0.49F);
    assertTrue(game.paletteTooltipType().isEmpty());
    game.updateGame(0.02F);
    assertEquals(PlaceableCellType.WALL, game.paletteTooltipType().orElseThrow());

    assertTrue(moveToPalette(game, PlaceableCellType.SLOW_FLOOR));
    assertTrue(game.paletteTooltipType().isEmpty());
    game.updateGame(0.5F);
    assertEquals(PlaceableCellType.SLOW_FLOOR, game.paletteTooltipType().orElseThrow());

    assertFalse(game.handlePointerMoved(0, 0, WIDTH, HEIGHT));
    assertTrue(game.paletteTooltipType().isEmpty());
    assertTrue(moveToPalette(game, PlaceableCellType.WALL));
    game.updateGame(0.5F);
    clickPalette(game, PlaceableCellType.WALL);
    assertTrue(game.paletteTooltipType().isEmpty());
  }

  private static MazeGame finiteGame() {
    LevelDefinition level =
        singleSolverLevel(
            "palette-test",
            "Palette Test",
            GridSize.square(5),
            new GridPosition(4, 2),
            new GridPosition(0, 2),
            Duration.ofSeconds(30),
            Duration.ofSeconds(5),
            Duration.ofSeconds(10),
            Duration.ofMillis(250),
            List.of(
                PlaceableCellSupply.finite(PlaceableCellType.WALL, 1),
                PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 1)),
            SolverBehavior.RANDOM,
            1L);
    GameSession session =
        new GameSession(new LevelCatalog(List.of(level)), level.id(), BestResultStore.none());
    MazeGame game =
        new MazeGame(
            null,
            new MazeGameRuntimeConfiguration(
                FileHandle::new, ignored -> {}, () -> {}, true, false, false),
            session);
    game.startLevel(level.id());
    return game;
  }

  private static void clickPalette(MazeGame game, PlaceableCellType type) {
    clickBounds(
        game,
        game.debugScreenLayout(GamePhase.BUILDING, WIDTH, HEIGHT)
            .bounds(MazeGameLayout.paletteItemId(type)),
        Input.Buttons.LEFT);
  }

  private static boolean moveToPalette(MazeGame game, PlaceableCellType type) {
    ScreenRectangle bounds =
        game.debugScreenLayout(GamePhase.BUILDING, WIDTH, HEIGHT)
            .bounds(MazeGameLayout.paletteItemId(type));
    return game.handlePointerMoved(
        Math.round(bounds.x() + bounds.width() / 2.0F),
        Math.round(HEIGHT - bounds.y() - bounds.height() / 2.0F),
        WIDTH,
        HEIGHT);
  }

  private static void clickCell(MazeGame game, GridPosition position, int button) {
    ScreenLayout layout = game.debugScreenLayout(GamePhase.BUILDING, WIDTH, HEIGHT);
    ScreenRectangle grid = layout.bounds(MazeGameLayout.GAME_GRID);
    float cellSize = grid.width() / game.mazeState().levelDefinition().gridSize().columns();
    float x = grid.x() + (position.column() + 0.5F) * cellSize;
    float y =
        grid.y()
            + (game.mazeState().levelDefinition().gridSize().rows() - position.row() - 0.5F)
                * cellSize;
    game.handleScreenClick(Math.round(x), Math.round(HEIGHT - y), button, WIDTH, HEIGHT);
  }

  private static void clickBounds(MazeGame game, ScreenRectangle bounds, int button) {
    game.handleScreenClick(
        Math.round(bounds.x() + bounds.width() / 2.0F),
        Math.round(HEIGHT - bounds.y() - bounds.height() / 2.0F),
        button,
        WIDTH,
        HEIGHT);
  }

  private static CellPaletteState selected(MazeGame game) {
    return game.paletteState().stream()
        .filter(CellPaletteState::selected)
        .findFirst()
        .orElseThrow();
  }

  private static CellPaletteState palette(MazeGame game, PlaceableCellType type) {
    return game.paletteState().stream()
        .filter(state -> state.type() == type)
        .findFirst()
        .orElseThrow();
  }
}
