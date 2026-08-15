package io.github.sportne.mazegame;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import io.github.sportne.mazegame.model.maze.MazeEditResult;
import io.github.sportne.mazegame.model.maze.MazeEditStatus;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.render.PaletteDragPreview;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameSession;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

final class MazeGameBuildGestureTest {
  private static final int WIDTH = 1280;
  private static final int HEIGHT = 720;
  private static final GridPosition DESTINATION = new GridPosition(2, 1);
  private static final GridPosition EXHAUSTION_SOURCE = new GridPosition(3, 1);

  @Test
  void paletteTapSelectsOnlyOnReleaseAndThresholdDragPlacesOriginType() {
    MazeGame game = game(level(CellSupply.finite(1), CellSupply.finite(1), GridSize.square(5)));
    ScreenPoint slow = paletteCenter(game, PlaceableCellType.SLOW_FLOOR);
    ScreenPoint destination = cellCenter(game, DESTINATION);

    assertTrue(game.handlePointerDown(slow.x(), slow.y(), 4, Input.Buttons.LEFT, WIDTH, HEIGHT));
    assertEquals(PlaceableCellType.WALL, game.paletteState().get(0).type());
    assertTrue(game.paletteState().get(0).selected());
    assertTrue(game.handlePointerDragged(slow.x() + 7, slow.y(), 4));
    assertNull(game.paletteDragPreview(WIDTH, HEIGHT));
    assertTrue(game.handlePointerUp(slow.x() + 7, slow.y(), 4, WIDTH, HEIGHT).isEmpty());
    assertTrue(
        game.paletteState().stream()
            .anyMatch(state -> state.type() == PlaceableCellType.SLOW_FLOOR && state.selected()));

    assertTrue(game.handlePointerDown(slow.x(), slow.y(), 4, Input.Buttons.LEFT, WIDTH, HEIGHT));
    assertTrue(game.handlePointerDragged(destination.x(), destination.y(), 4));
    PaletteDragPreview preview = game.paletteDragPreview(WIDTH, HEIGHT);
    assertEquals(DESTINATION, preview.destination());
    assertTrue(preview.validDestination());
    MazeEditResult result =
        game.handlePointerUp(destination.x(), destination.y(), 4, WIDTH, HEIGHT).orElseThrow();

    assertEquals(MazeEditStatus.PLACED, result.status());
    assertEquals(PlaceableCellType.SLOW_FLOOR, game.mazeState().placedCellAt(DESTINATION));
    assertTrue(game.buildGestureState().isEmpty());
  }

  @Test
  void clickAndPaletteDragHaveIdenticalDomainOutcomes() {
    assertEquivalent(
        level(CellSupply.finite(1), CellSupply.finite(1), GridSize.square(5)),
        ignored -> {},
        PlaceableCellType.SLOW_FLOOR,
        DESTINATION,
        MazeEditStatus.PLACED);
    assertEquivalent(
        level(CellSupply.finite(1), CellSupply.finite(1), GridSize.square(5)),
        game -> place(game, PlaceableCellType.SLOW_FLOOR, DESTINATION),
        PlaceableCellType.SLOW_FLOOR,
        DESTINATION,
        MazeEditStatus.REMOVED);
    assertEquivalent(
        level(CellSupply.finite(1), CellSupply.finite(1), GridSize.square(5)),
        game -> place(game, PlaceableCellType.WALL, DESTINATION),
        PlaceableCellType.SLOW_FLOOR,
        DESTINATION,
        MazeEditStatus.REPLACED);
    assertEquivalent(
        level(CellSupply.finite(1), CellSupply.finite(1), GridSize.square(5)),
        ignored -> {},
        PlaceableCellType.WALL,
        new GridPosition(4, 2),
        MazeEditStatus.REJECTED_PROTECTED_CELL);
    assertEquivalent(
        level(CellSupply.finite(1), CellSupply.finite(1), GridSize.square(5)),
        game -> place(game, PlaceableCellType.SLOW_FLOOR, EXHAUSTION_SOURCE),
        PlaceableCellType.SLOW_FLOOR,
        DESTINATION,
        MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
    assertEquivalent(
        level(CellSupply.finite(1), CellSupply.finite(1), GridSize.square(5)),
        game -> {
          place(game, PlaceableCellType.SLOW_FLOOR, EXHAUSTION_SOURCE);
          place(game, PlaceableCellType.WALL, DESTINATION);
        },
        PlaceableCellType.SLOW_FLOOR,
        DESTINATION,
        MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
    assertEquivalent(
        level(CellSupply.finite(1), CellSupply.finite(1), new GridSize(3, 1)),
        ignored -> {},
        PlaceableCellType.WALL,
        new GridPosition(1, 0),
        MazeEditStatus.REJECTED_BLOCKS_PATH);
  }

  @Test
  void exhaustedSameTypeDragRecoversItemWhileOtherDropsRejectUnchanged() {
    MazeGame game = game(level(CellSupply.finite(1), CellSupply.finite(1), GridSize.square(5)));
    place(game, PlaceableCellType.SLOW_FLOOR, DESTINATION);
    MazeState placed = game.mazeState();

    assertEquals(
        MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY,
        drag(game, PlaceableCellType.SLOW_FLOOR, new GridPosition(2, 2)).status());
    assertEquals(placed, game.mazeState());
    assertEquals(
        MazeEditStatus.REMOVED, drag(game, PlaceableCellType.SLOW_FLOOR, DESTINATION).status());
    assertEquals(
        CellSupply.finite(1), game.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
  }

  @Test
  void dragPreviewUsesTheSameDomainValidityAndMarksOutsideGrid() {
    MazeGame game = game(level(CellSupply.infinite(), CellSupply.finite(1), GridSize.square(5)));
    place(game, PlaceableCellType.SLOW_FLOOR, EXHAUSTION_SOURCE);
    ScreenPoint palette = paletteCenter(game, PlaceableCellType.SLOW_FLOOR);
    ScreenPoint destination = cellCenter(game, DESTINATION);

    beginDrag(game, palette, destination, 0);
    PaletteDragPreview exhausted = game.paletteDragPreview(WIDTH, HEIGHT);
    assertEquals(DESTINATION, exhausted.destination());
    assertFalse(exhausted.validDestination());
    game.cancelBuildGesture();

    assertTrue(
        game.handlePointerDown(palette.x(), palette.y(), 0, Input.Buttons.LEFT, WIDTH, HEIGHT));
    assertTrue(game.handlePointerDragged(0, 0, 0));
    PaletteDragPreview outside = game.paletteDragPreview(WIDTH, HEIGHT);
    assertNull(outside.destination());
    assertFalse(outside.validDestination());
    assertTrue(game.handlePointerUp(0, 0, 0, WIDTH, HEIGHT).isEmpty());
    assertEquals(PlaceableCellType.SLOW_FLOOR, game.mazeState().placedCellAt(EXHAUSTION_SOURCE));
  }

  @Test
  void pointerOwnershipAndEveryApplicationCancellationPathPreventLateDrop() {
    MazeGame game = game(level(CellSupply.infinite(), CellSupply.finite(1), GridSize.square(5)));
    ScreenPoint palette = paletteCenter(game, PlaceableCellType.WALL);
    ScreenPoint destination = cellCenter(game, DESTINATION);
    MazeState initial = game.mazeState();

    beginDrag(game, palette, destination, 3);
    assertTrue(
        game.handlePointerDown(palette.x(), palette.y(), 9, Input.Buttons.LEFT, WIDTH, HEIGHT));
    assertFalse(game.handlePointerDragged(destination.x(), destination.y(), 9));
    assertTrue(game.handlePointerUp(destination.x(), destination.y(), 9, WIDTH, HEIGHT).isEmpty());
    assertTrue(game.buildGestureState().orElseThrow().pointerId() == 3);

    game.cancelBuildGesture();
    assertLateReleaseDoesNotEdit(game, destination, 3, initial);

    beginDrag(game, palette, destination, 3);
    game.resize(844, 286);
    assertLateReleaseDoesNotEdit(game, destination, 3, initial);

    beginDrag(game, palette, destination, 3);
    game.pause();
    assertLateReleaseDoesNotEdit(game, destination, 3, initial);
    game.resume();

    beginDrag(game, palette, destination, 3);
    assertTrue(game.handlePointerUp(0, 0, 3, WIDTH, HEIGHT).isEmpty());
    assertEquals(initial, game.mazeState());

    MazeGame backGame =
        game(level(CellSupply.infinite(), CellSupply.finite(1), GridSize.square(5)));
    beginDrag(backGame, paletteCenter(backGame, PlaceableCellType.WALL), destination, 3);
    click(
        backGame,
        center(
            backGame
                .debugScreenLayout(GamePhase.BUILDING, WIDTH, HEIGHT)
                .bounds(MazeGameLayout.BUILD_BACK)));
    assertEquals(GamePhase.LEVEL_SELECT, backGame.gamePhase());
    assertTrue(backGame.buildGestureState().isEmpty());

    beginDrag(game, palette, destination, 3);
    game.startRun();
    assertEquals(GamePhase.SOLVER_RUNNING, game.gamePhase());
    assertLateReleaseDoesNotEdit(game, destination, 3, initial);
  }

  @Test
  void timerExpiryCancelsPreviewBeforeFreezingMaze() {
    MazeGame game = game(level(CellSupply.infinite(), CellSupply.finite(1), GridSize.square(5)));
    ScreenPoint palette = paletteCenter(game, PlaceableCellType.WALL);
    ScreenPoint destination = cellCenter(game, DESTINATION);
    MazeState initial = game.mazeState();
    beginDrag(game, palette, destination, 0);

    game.updateGame(game.buildTimeRemainingSeconds());

    assertEquals(GamePhase.SOLVER_RUNNING, game.gamePhase());
    assertTrue(game.buildGestureState().isEmpty());
    assertLateReleaseDoesNotEdit(game, destination, 0, initial);
  }

  @Test
  void placedWallsAndSlowFloorsMoveAtomicallyWithoutChangingFiniteOrInfiniteInventory() {
    MazeGame game = game(level(CellSupply.finite(2), CellSupply.infinite(), GridSize.square(5)));
    GridPosition wallSource = new GridPosition(3, 0);
    GridPosition slowSource = new GridPosition(3, 4);
    GridPosition wallDestination = new GridPosition(2, 0);
    GridPosition slowDestination = new GridPosition(2, 4);
    place(game, PlaceableCellType.WALL, wallSource);
    place(game, PlaceableCellType.SLOW_FLOOR, slowSource);
    Map<PlaceableCellType, CellSupply> suppliesBefore = game.mazeState().remainingSupplies();

    MazeState beforeWall = game.mazeState();
    beginCellDrag(game, wallSource, wallDestination, 2);
    assertSame(beforeWall, game.mazeState());
    PaletteDragPreview wallPreview = game.paletteDragPreview(WIDTH, HEIGHT);
    assertEquals(wallSource, wallPreview.sourcePosition());
    assertEquals(PlaceableCellType.WALL, wallPreview.type());
    assertTrue(wallPreview.validDestination());
    assertEquals(
        MazeEditStatus.MOVED, releaseCellDrag(game, wallDestination, 2).orElseThrow().status());

    MazeState beforeSlow = game.mazeState();
    beginCellDrag(game, slowSource, slowDestination, 2);
    assertSame(beforeSlow, game.mazeState());
    PaletteDragPreview slowPreview = game.paletteDragPreview(WIDTH, HEIGHT);
    assertEquals(slowSource, slowPreview.sourcePosition());
    assertEquals(PlaceableCellType.SLOW_FLOOR, slowPreview.type());
    assertTrue(slowPreview.validDestination());
    assertEquals(
        MazeEditStatus.MOVED, releaseCellDrag(game, slowDestination, 2).orElseThrow().status());

    assertNull(game.mazeState().placedCellAt(wallSource));
    assertEquals(PlaceableCellType.WALL, game.mazeState().placedCellAt(wallDestination));
    assertNull(game.mazeState().placedCellAt(slowSource));
    assertEquals(PlaceableCellType.SLOW_FLOOR, game.mazeState().placedCellAt(slowDestination));
    assertEquals(suppliesBefore, game.mazeState().remainingSupplies());
  }

  @Test
  void placedCellMoveRejectsEveryInvalidDestinationWithoutPublishingTransientState() {
    MazeGame game = game(level(CellSupply.finite(4), CellSupply.infinite(), GridSize.square(3)));
    GridPosition source = new GridPosition(2, 0);
    GridPosition occupied = new GridPosition(1, 2);
    GridPosition otherBarrier = new GridPosition(1, 0);
    GridPosition blocksPath = new GridPosition(1, 1);
    GridPosition protectedStart = new GridPosition(2, 1);
    place(game, PlaceableCellType.WALL, source);
    place(game, PlaceableCellType.WALL, occupied);
    place(game, PlaceableCellType.WALL, otherBarrier);
    MazeState original = game.mazeState();
    Map<PlaceableCellType, CellSupply> supplies = original.remainingSupplies();

    assertRejectedMove(
        game, source, occupied, MazeEditStatus.REJECTED_OCCUPIED_DESTINATION, original);
    assertRejectedMove(
        game, source, protectedStart, MazeEditStatus.REJECTED_PROTECTED_CELL, original);
    assertRejectedMove(game, source, blocksPath, MazeEditStatus.REJECTED_BLOCKS_PATH, original);

    beginCellDrag(game, source, source, 3);
    MazeEditResult sourceDrop = releaseCellDrag(game, source, 3).orElseThrow();
    assertEquals(MazeEditStatus.NO_OP, sourceDrop.status());
    assertSame(original, game.mazeState());

    beginCellDrag(game, source, blocksPath, 3);
    assertTrue(game.handlePointerUp(0, 0, 3, WIDTH, HEIGHT).isEmpty());
    assertSame(original, game.mazeState());
    assertEquals(supplies, game.mazeState().remainingSupplies());
  }

  @Test
  void gridTapKeepsActiveToolSemanticsAndEmptyCellDragDoesNothing() {
    MazeGame game = game(level(CellSupply.finite(2), CellSupply.finite(2), GridSize.square(5)));
    GridPosition source = new GridPosition(2, 1);
    GridPosition other = new GridPosition(2, 3);
    place(game, PlaceableCellType.SLOW_FLOOR, source);
    click(game, paletteCenter(game, PlaceableCellType.WALL));

    assertEquals(MazeEditStatus.REPLACED, tapCell(game, source, 5, 7).orElseThrow().status());
    assertEquals(PlaceableCellType.WALL, game.mazeState().placedCellAt(source));
    assertEquals(MazeEditStatus.REMOVED, tapCell(game, source, 5, 0).orElseThrow().status());
    assertNull(game.mazeState().placedCellAt(source));
    assertEquals(MazeEditStatus.PLACED, tapCell(game, source, 5, 0).orElseThrow().status());

    MazeState beforeEmptyDrag = game.mazeState();
    beginCellDrag(game, other, source, 5);
    assertFalse(game.buildGestureState().orElseThrow().dragging());
    assertNull(game.paletteDragPreview(WIDTH, HEIGHT));
    assertTrue(releaseCellDrag(game, source, 5).isEmpty());
    assertSame(beforeEmptyDrag, game.mazeState());
  }

  @Test
  void placedCellCancellationAndExplorationRacesPreserveTheExactSourceMaze() {
    GridPosition source = new GridPosition(2, 1);
    GridPosition destination = new GridPosition(2, 3);
    MazeGame game = game(level(CellSupply.finite(1), CellSupply.infinite(), GridSize.square(5)));
    place(game, PlaceableCellType.SLOW_FLOOR, source);
    MazeState original = game.mazeState();

    beginCellDrag(game, source, destination, 6);
    game.cancelBuildGesture();
    assertLateReleaseDoesNotEdit(game, cellCenter(game, destination), 6, original);

    beginCellDrag(game, source, destination, 6);
    game.resize(844, 286);
    assertLateReleaseDoesNotEdit(game, cellCenter(game, destination), 6, original);

    beginCellDrag(game, source, destination, 6);
    game.pause();
    assertLateReleaseDoesNotEdit(game, cellCenter(game, destination), 6, original);
    game.resume();

    beginCellDrag(game, source, destination, 6);
    game.updateGame(game.buildTimeRemainingSeconds());
    assertEquals(GamePhase.SOLVER_RUNNING, game.gamePhase());
    assertLateReleaseDoesNotEdit(game, cellCenter(game, destination), 6, original);
    assertEquals(PlaceableCellType.SLOW_FLOOR, game.mazeState().placedCellAt(source));
  }

  private static void assertEquivalent(
      LevelDefinition level,
      Consumer<MazeGame> setup,
      PlaceableCellType type,
      GridPosition destination,
      MazeEditStatus expectedStatus) {
    MazeGame clickGame = game(level);
    MazeGame dragGame = game(level);
    setup.accept(clickGame);
    setup.accept(dragGame);
    MazeEditResult expected = clickGame.mazeState().placeOrReplace(type, destination);

    place(clickGame, type, destination);
    MazeEditResult dragged = drag(dragGame, type, destination);

    assertEquals(expectedStatus, expected.status());
    assertEquals(expected.status(), dragged.status());
    assertEquals(expected.mazeState(), clickGame.mazeState());
    assertEquals(clickGame.mazeState(), dragGame.mazeState());
    assertEquals(
        clickGame.mazeState().remainingSupply(PlaceableCellType.WALL),
        dragGame.mazeState().remainingSupply(PlaceableCellType.WALL));
    assertEquals(
        clickGame.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR),
        dragGame.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
    if (!expectedStatus.accepted()) {
      assertEquals(destination, clickGame.rejectedPosition());
      assertEquals(destination, dragGame.rejectedPosition());
    }
  }

  private static void assertRejectedMove(
      MazeGame game,
      GridPosition source,
      GridPosition destination,
      MazeEditStatus status,
      MazeState original) {
    beginCellDrag(game, source, destination, 3);
    PaletteDragPreview preview = game.paletteDragPreview(WIDTH, HEIGHT);
    assertFalse(preview.validDestination());
    assertEquals(source, preview.sourcePosition());
    MazeEditResult result = releaseCellDrag(game, destination, 3).orElseThrow();
    assertEquals(status, result.status());
    assertSame(original, result.mazeState());
    assertSame(original, game.mazeState());
  }

  private static void beginDrag(
      MazeGame game, ScreenPoint palette, ScreenPoint destination, int pointer) {
    assertTrue(
        game.handlePointerDown(
            palette.x(), palette.y(), pointer, Input.Buttons.LEFT, WIDTH, HEIGHT));
    assertTrue(game.handlePointerDragged(destination.x(), destination.y(), pointer));
    assertTrue(game.buildGestureState().orElseThrow().dragThresholdCrossed());
  }

  private static void beginCellDrag(
      MazeGame game, GridPosition source, GridPosition destination, int pointer) {
    ScreenPoint sourcePoint = cellCenter(game, source);
    ScreenPoint destinationPoint = cellCenter(game, destination);
    assertTrue(
        game.handlePointerDown(
            sourcePoint.x(), sourcePoint.y(), pointer, Input.Buttons.LEFT, WIDTH, HEIGHT));
    int dragX = source.equals(destination) ? destinationPoint.x() + 8 : destinationPoint.x();
    assertTrue(game.handlePointerDragged(dragX, destinationPoint.y(), pointer));
    assertTrue(game.buildGestureState().orElseThrow().dragThresholdCrossed());
  }

  private static Optional<MazeEditResult> releaseCellDrag(
      MazeGame game, GridPosition destination, int pointer) {
    ScreenPoint destinationPoint = cellCenter(game, destination);
    return game.handlePointerUp(destinationPoint.x(), destinationPoint.y(), pointer, WIDTH, HEIGHT);
  }

  private static Optional<MazeEditResult> tapCell(
      MazeGame game, GridPosition position, int pointer, int deltaX) {
    ScreenPoint point = cellCenter(game, position);
    assertTrue(
        game.handlePointerDown(point.x(), point.y(), pointer, Input.Buttons.LEFT, WIDTH, HEIGHT));
    assertTrue(game.handlePointerDragged(point.x() + deltaX, point.y(), pointer));
    return game.handlePointerUp(point.x() + deltaX, point.y(), pointer, WIDTH, HEIGHT);
  }

  private static void assertLateReleaseDoesNotEdit(
      MazeGame game, ScreenPoint destination, int pointer, MazeState expected) {
    assertTrue(
        game.handlePointerUp(destination.x(), destination.y(), pointer, WIDTH, HEIGHT).isEmpty());
    assertEquals(expected, game.mazeState());
    assertTrue(game.buildGestureState().isEmpty());
  }

  private static MazeEditResult drag(
      MazeGame game, PlaceableCellType type, GridPosition destination) {
    ScreenPoint palette = paletteCenter(game, type);
    ScreenPoint cell = cellCenter(game, destination);
    beginDrag(game, palette, cell, 0);
    return game.handlePointerUp(cell.x(), cell.y(), 0, WIDTH, HEIGHT).orElseThrow();
  }

  private static void place(MazeGame game, PlaceableCellType type, GridPosition destination) {
    click(game, paletteCenter(game, type));
    click(game, cellCenter(game, destination));
  }

  private static void click(MazeGame game, ScreenPoint point) {
    game.handleScreenClick(point.x(), point.y(), Input.Buttons.LEFT, WIDTH, HEIGHT);
  }

  private static ScreenPoint paletteCenter(MazeGame game, PlaceableCellType type) {
    return center(
        game.debugScreenLayout(GamePhase.BUILDING, WIDTH, HEIGHT)
            .bounds(MazeGameLayout.paletteItemId(type)));
  }

  private static ScreenPoint cellCenter(MazeGame game, GridPosition position) {
    ScreenLayout layout = game.debugScreenLayout(GamePhase.BUILDING, WIDTH, HEIGHT);
    ScreenRectangle grid = layout.bounds(MazeGameLayout.GAME_GRID);
    float cellSize = grid.width() / game.mazeState().levelDefinition().gridSize().columns();
    return new ScreenPoint(
        Math.round(grid.x() + (position.column() + 0.5F) * cellSize),
        Math.round(
            HEIGHT
                - grid.y()
                - (game.mazeState().levelDefinition().gridSize().rows() - position.row() - 0.5F)
                    * cellSize));
  }

  private static ScreenPoint center(ScreenRectangle bounds) {
    return new ScreenPoint(
        Math.round(bounds.x() + bounds.width() / 2.0F),
        Math.round(HEIGHT - bounds.y() - bounds.height() / 2.0F));
  }

  private static MazeGame game(LevelDefinition level) {
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

  private static LevelDefinition level(
      CellSupply wallSupply, CellSupply slowSupply, GridSize gridSize) {
    int centerColumn = gridSize.columns() / 2;
    return singleSolverLevel(
        "palette-drag-%dx%d".formatted(gridSize.rows(), gridSize.columns()),
        "Palette Drag",
        gridSize,
        new GridPosition(gridSize.rows() - 1, centerColumn),
        new GridPosition(0, centerColumn),
        Duration.ofSeconds(30),
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        Duration.ofMillis(250),
        List.of(
            new PlaceableCellSupply(PlaceableCellType.WALL, wallSupply),
            new PlaceableCellSupply(PlaceableCellType.SLOW_FLOOR, slowSupply)),
        SolverBehavior.RANDOM,
        1L);
  }

  private record ScreenPoint(int x, int y) {}
}
