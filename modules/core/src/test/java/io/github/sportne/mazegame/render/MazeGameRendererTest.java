package io.github.sportne.mazegame.render;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.sportne.mazegame.assets.DirectionalSpriteSet;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.FixedCell;
import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.CardinalDirection;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import io.github.sportne.mazegame.state.CellPaletteState;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.LevelProgress;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

final class MazeGameRendererTest {
  private static final LevelDefinition LEVEL = Levels.levelOne();

  @Test
  void cellColorReflectsMazeContentAndRejectedPosition() {
    GridPosition wall = new GridPosition(1, 1);
    MazeState maze =
        MazeState.empty(LEVEL).placeOrReplace(PlaceableCellType.WALL, wall).mazeState();

    assertEquals(Color.BLACK, MazeGameRenderer.cellColor(maze, null, 0.0F, new GridPosition(2, 2)));
    assertEquals(Color.WHITE, MazeGameRenderer.cellColor(maze, null, 0.0F, wall));
    assertEquals(
        new Color(0.24F, 0.62F, 0.95F, 1.0F),
        MazeGameRenderer.cellColor(maze, null, 0.0F, LEVEL.primarySolver().start()));
    assertEquals(
        new Color(0.95F, 0.42F, 0.42F, 1.0F), MazeGameRenderer.cellColor(maze, wall, 0.2F, wall));
  }

  @Test
  void cellColorReturnsDefensiveCopies() {
    MazeState maze = MazeState.empty(LEVEL);

    Color first = MazeGameRenderer.cellColor(maze, null, 0.0F, new GridPosition(2, 2));
    Color second = MazeGameRenderer.cellColor(maze, null, 0.0F, new GridPosition(2, 2));

    assertEquals(first, second);
    assertNotSame(first, second);
  }

  @Test
  void spriteDestinationCentersAndPreservesAspectRatio() {
    ScreenRectangle destination =
        MazeGameRenderer.spriteDestination(
            new ScreenRectangle(400.0F, 100.0F, 500.0F, 500.0F),
            LEVEL,
            new GridPosition(4, 2),
            200.0F,
            100.0F);

    assertEquals(605.0F, destination.x());
    assertEquals(127.5F, destination.y());
    assertEquals(90.0F, destination.width());
    assertEquals(45.0F, destination.height());
  }

  @Test
  void spriteDestinationRejectsInvalidSpriteDimensions() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            MazeGameRenderer.spriteDestination(
                new ScreenRectangle(400.0F, 100.0F, 500.0F, 500.0F),
                LEVEL,
                LEVEL.primarySolver().start(),
                0.0F,
                100.0F));
  }

  @Test
  void renderSnapshotExposesFrameData() {
    MazeState maze = MazeState.empty(LEVEL);
    GridPosition rejected = new GridPosition(2, 2);
    SolverRunResult runResult =
        new SolverRunResult(
            LEVEL.primarySolver().start(), Duration.ofMillis(250L), 1, SolverRunStatus.RUNNING);
    BestResult bestResult = new BestResult(Duration.ofSeconds(10L), 40);

    GameRenderSnapshot snapshot =
        snapshot(
            GamePhase.SOLVER_RUNNING,
            LEVEL,
            maze,
            12.0F,
            rejected,
            0.4F,
            runResult,
            bestResult,
            levelProgress(bestResult),
            true,
            false,
            true);

    assertEquals(GamePhase.SOLVER_RUNNING, snapshot.phase());
    assertEquals(LEVEL, snapshot.levelDefinition());
    assertEquals(maze, snapshot.mazeState());
    assertEquals(12.0F, snapshot.buildTimeRemainingSeconds());
    assertEquals(rejected, snapshot.rejectedPosition());
    assertEquals(0.4F, snapshot.rejectedFlashRemainingSeconds());
    assertEquals(runResult, snapshot.solverRunResult());
    assertEquals(List.of(runResult), snapshot.solverRunResults());
    assertEquals(bestResult, snapshot.bestResult());
    assertEquals(levelProgress(bestResult), snapshot.levelProgress());
    assertTrue(snapshot.audioEnabled());
    assertTrue(snapshot.hasNextLevel());
  }

  @Test
  void renderSnapshotRequiresFrameState() {
    MazeState maze = MazeState.empty(LEVEL);

    assertThrows(
        NullPointerException.class,
        () ->
            snapshot(
                null, LEVEL, maze, 12.0F, null, 0.0F, null, null, List.of(), true, false, false));
  }

  @Test
  void rendersMenuScreensWithExpectedLabels() {
    RecordingSpriteBatch spriteBatch = allocate(RecordingSpriteBatch.class);
    RecordingShapeRenderer shapeRenderer = allocate(RecordingShapeRenderer.class);
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer = renderer(spriteBatch, shapeRenderer, font);

    renderer.render(layout(GamePhase.MAIN_MENU), snapshot(GamePhase.MAIN_MENU, null));
    renderer.render(layout(GamePhase.LEVEL_SELECT), snapshot(GamePhase.LEVEL_SELECT, null));
    renderer.render(layout(GamePhase.SETTINGS), snapshot(GamePhase.SETTINGS, null));

    assertTrue(font.capturedText().contains("Maze Game"));
    assertTrue(font.capturedText().contains("Start"));
    assertTrue(font.capturedText().contains("Settings"));
    assertTrue(font.capturedText().contains("Quit"));
    assertTrue(font.capturedText().contains("Select Level"));
    assertTrue(font.capturedText().contains("Level 1"));
    assertTrue(font.capturedText().contains("Level 2"));
    assertTrue(font.capturedText().contains("Best: --"));
    assertTrue(font.capturedText().contains("Locked"));
    assertTrue(font.capturedText().contains("Audio: On"));
    assertTrue(shapeRenderer.rects >= 8);
    assertTrue(shapeRenderer.rectLines >= 32);
    assertEquals(spriteBatch.beginCount, spriteBatch.endCount);
    assertEquals(shapeRenderer.beginCount, shapeRenderer.endCount);
  }

  @Test
  void rendersMainMenuWithoutQuitWhenUnavailable() {
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer =
        renderer(
            allocate(RecordingSpriteBatch.class), allocate(RecordingShapeRenderer.class), font);
    ScreenLayout layout =
        MazeGameLayout.forPhase(GamePhase.MAIN_MENU, 1280, 720, LEVEL.gridSize(), false);

    renderer.render(layout, snapshot(GamePhase.MAIN_MENU, null));

    assertTrue(font.capturedText().contains("Start"));
    assertTrue(font.capturedText().contains("Settings"));
    assertFalse(font.capturedText().contains("Quit"));
  }

  @Test
  void rendersCatalogBackedCardsWithIndependentBestResults() {
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer =
        renderer(
            allocate(RecordingSpriteBatch.class), allocate(RecordingShapeRenderer.class), font);
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    BestResult secondBest = new BestResult(Duration.ofSeconds(15), 60);
    GameRenderSnapshot snapshot =
        snapshot(
            GamePhase.LEVEL_SELECT,
            LEVEL,
            MazeState.empty(LEVEL),
            30.0F,
            null,
            0.0F,
            null,
            firstBest,
            List.of(
                new LevelProgress(Levels.levelOne(), true, firstBest),
                new LevelProgress(Levels.levelTwo(), true, secondBest)),
            true,
            false,
            false);

    renderer.render(layout(GamePhase.LEVEL_SELECT), snapshot);

    assertTrue(font.capturedText().contains("Best: 10.00s  Moves: 40"));
    assertTrue(font.capturedText().contains("Best: 15.00s  Moves: 60"));
    assertFalse(font.capturedText().contains("Locked"));
  }

  @Test
  void rendersNextLevelOnlyWhenAdvancementIsAvailable() {
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer =
        renderer(
            allocate(RecordingSpriteBatch.class), allocate(RecordingShapeRenderer.class), font);
    SolverRunResult result =
        new SolverRunResult(
            LEVEL.primarySolver().goal(), Duration.ofSeconds(10), 40, SolverRunStatus.TIMED_OUT);
    GameRenderSnapshot snapshot =
        snapshot(
            GamePhase.RESULT,
            LEVEL,
            MazeState.empty(LEVEL),
            0.0F,
            null,
            0.0F,
            result,
            new BestResult(Duration.ofSeconds(10), 40),
            List.of(
                new LevelProgress(
                    Levels.levelOne(), true, new BestResult(Duration.ofSeconds(10), 40)),
                new LevelProgress(Levels.levelTwo(), true, null)),
            true,
            true,
            true);
    ScreenLayout resultLayout =
        MazeGameLayout.forPhase(GamePhase.RESULT, 1280, 720, LEVEL.gridSize(), true, 2, true);

    renderer.render(resultLayout, snapshot);

    assertTrue(font.capturedText().contains("Next Level"));
    assertFalse(font.capturedText().contains("Pass this level to unlock the next"));
  }

  @Test
  void labelsTheLastCatalogEntryAsTheFinalAvailableLevel() {
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer =
        renderer(
            allocate(RecordingSpriteBatch.class), allocate(RecordingShapeRenderer.class), font);
    LevelDefinition finalLevel = Levels.levelTen();
    SolverRunResult result =
        new SolverRunResult(
            finalLevel.primarySolver().goal(),
            Duration.ofSeconds(10),
            40,
            SolverRunStatus.TIMED_OUT);
    List<LevelProgress> progress =
        Levels.catalog().levels().stream()
            .map(level -> new LevelProgress(level, true, null))
            .toList();
    GameRenderSnapshot snapshot =
        snapshot(
            GamePhase.RESULT,
            finalLevel,
            MazeState.empty(finalLevel),
            0.0F,
            null,
            0.0F,
            result,
            null,
            progress,
            true,
            true,
            false);
    ScreenLayout resultLayout =
        MazeGameLayout.forPhase(
            GamePhase.RESULT, 1280, 720, finalLevel.gridSize(), true, progress.size(), false);

    renderer.render(resultLayout, snapshot);

    assertTrue(font.capturedText().contains("Final available level"));
    assertFalse(font.capturedText().stream().anyMatch(text -> text.contains("milestone")));
  }

  @Test
  void rendersBuildAndResultScreensWithExpectedLabels() {
    RecordingSpriteBatch spriteBatch = allocate(RecordingSpriteBatch.class);
    RecordingShapeRenderer shapeRenderer = allocate(RecordingShapeRenderer.class);
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer = renderer(spriteBatch, shapeRenderer, font);
    SolverRunResult running =
        new SolverRunResult(
            LEVEL.primarySolver().start(), Duration.ofMillis(2500L), 10, SolverRunStatus.RUNNING);
    SolverRunResult result =
        new SolverRunResult(
            LEVEL.primarySolver().goal(), Duration.ofSeconds(10L), 40, SolverRunStatus.TIMED_OUT);
    SolverRunResult failedResult =
        new SolverRunResult(
            LEVEL.primarySolver().goal(), Duration.ofSeconds(2L), 8, SolverRunStatus.REACHED_GOAL);

    renderer.render(layout(GamePhase.BUILDING), snapshot(GamePhase.BUILDING, null));
    renderer.render(layout(GamePhase.SOLVER_RUNNING), snapshot(GamePhase.SOLVER_RUNNING, running));
    renderer.render(layout(GamePhase.RESULT), snapshot(GamePhase.RESULT, result));
    renderer.render(layout(GamePhase.RESULT), snapshot(GamePhase.RESULT, failedResult));

    assertTrue(font.capturedText().contains("Level 1"));
    assertTrue(font.capturedText().contains("Build: 30.0s"));
    assertTrue(font.capturedText().contains("Delay past 5.0s; keep a path to the cheese"));
    assertTrue(font.capturedText().contains("Start Solver"));
    assertTrue(font.capturedText().contains("Level 1 | 7.5s | >5.0s"));
    assertTrue(font.capturedText().contains("Level 1 | Success | >5.0s"));
    assertTrue(font.capturedText().contains("Level 1 | Failed | >5.0s"));
    assertTrue(font.capturedText().contains("Time: 10.00s  Moves: 40"));
    assertTrue(font.capturedText().contains("Best: 10.00s  Moves: 40"));
    assertTrue(font.capturedText().contains("Retry"));
    assertTrue(font.capturedText().contains("Replay"));
    assertTrue(font.capturedText().contains("Main Menu"));
    assertTrue(font.capturedText().contains("Pass this level to unlock the next"));
    assertTrue(shapeRenderer.rects >= 50);
    assertTrue(shapeRenderer.rectLines >= 20);
  }

  @Test
  void fourthLevelBuildFeedbackTeachesToolsWithoutRevealingScoutRule() {
    String instructions = MazeGameRenderer.buildInstructions(buildSnapshot(Levels.levelFour()));

    assertEquals("Tap or drag tools; delay past 5.5s; keep a path", instructions);
    assertFalse(instructions.toLowerCase(java.util.Locale.ROOT).contains("left"));
    assertEquals(
        "Delay past 5.0s; keep a path to the cheese",
        MazeGameRenderer.buildInstructions(buildSnapshot(Levels.levelOne())));
    assertEquals(
        "Tap or drag tools; delay past 7.3s; keep a path",
        MazeGameRenderer.buildInstructions(buildSnapshot(Levels.levelEight())));
    assertEquals(
        "Tap or drag tools; delay past 7.5s; keep a path",
        MazeGameRenderer.buildInstructions(buildSnapshot(Levels.levelNine())));
    assertEquals(
        "Tap or drag tools; delay past 12.5s; keep a path",
        MazeGameRenderer.buildInstructions(buildSnapshot(Levels.levelTen())));
  }

  @Test
  void rendersCompactPaletteSupplyBadgesAndShowsDescriptionOnlyInDelayedTooltip() {
    LevelDefinition level = paletteLevel();
    GridPosition slowFloor = new GridPosition(2, 1);
    MazeState maze = new MazeState(level, Map.of(slowFloor, PlaceableCellType.SLOW_FLOOR));
    List<CellPaletteState> palette =
        List.of(
            new CellPaletteState(
                PlaceableCellType.WALL, CellSupply.infinite(), CellSupply.infinite(), false),
            new CellPaletteState(
                PlaceableCellType.SLOW_FLOOR, CellSupply.finite(1), CellSupply.finite(0), true));
    RecordingShapeRenderer shapes = allocate(RecordingShapeRenderer.class);
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer = renderer(allocate(RecordingSpriteBatch.class), shapes, font);
    GameRenderSnapshot snapshot =
        snapshot(
            GamePhase.BUILDING,
            level,
            maze,
            25.0F,
            null,
            0.0F,
            null,
            null,
            List.of(),
            palette,
            true,
            false,
            false);

    renderer.render(
        MazeGameLayout.forPhase(GamePhase.BUILDING, 1280, 720, level.gridSize()), snapshot);

    assertEquals(
        new Color(0.62F, 0.36F, 0.08F, 1.0F),
        MazeGameRenderer.cellColor(maze, null, 0.0F, slowFloor));
    assertFalse(font.capturedText().contains("Wall inf"));
    assertFalse(font.capturedText().contains("* Slow 0 OUT"));
    assertTrue(font.capturedText().contains("0"));
    assertEquals("* Slow 0 OUT", MazeGameRenderer.paletteLabel(palette.get(1)));
    assertEquals("", MazeGameRenderer.paletteSupplyBadgeLabel(palette.get(0)));
    assertEquals("0", MazeGameRenderer.paletteSupplyBadgeLabel(palette.get(1)));
    assertEquals(
        "4",
        MazeGameRenderer.paletteSupplyBadgeLabel(
            new CellPaletteState(
                PlaceableCellType.SLOW_FLOOR, CellSupply.finite(4), CellSupply.finite(4), false)));
    assertTrue(
        shapes.rectLines >= 42,
        "supply badges include an infinity mark and exhausted icon has a strike mark");

    RecordingFont tooltipFont = recordingFont();
    GameRenderSnapshot tooltipSnapshot =
        snapshot(
            GamePhase.BUILDING,
            level,
            maze,
            25.0F,
            null,
            0.0F,
            null,
            null,
            List.of(),
            palette,
            null,
            PlaceableCellType.SLOW_FLOOR,
            true,
            false,
            false);
    renderer(
            allocate(RecordingSpriteBatch.class),
            allocate(RecordingShapeRenderer.class),
            tooltipFont)
        .render(
            MazeGameLayout.forPhase(GamePhase.BUILDING, 1280, 720, level.gridSize()),
            tooltipSnapshot);

    assertTrue(tooltipFont.capturedText().contains("* Slow 0 OUT"));
  }

  @Test
  void fixedCellsKeepTheirEffectsAndAddANonColorLockMarker() {
    LevelDefinition base = paletteLevel();
    GridPosition wall = new GridPosition(1, 0);
    GridPosition slowFloor = new GridPosition(1, 4);
    LevelDefinition fixedLevel =
        new LevelDefinition(
            base.id(),
            base.name(),
            base.gridSize(),
            base.buildTime(),
            base.targetSolveTime(),
            base.maximumSolveTime(),
            base.solverMoveInterval(),
            base.placeableCellSupplies(),
            List.of(
                new FixedCell(wall, FixedCellType.WALL),
                new FixedCell(slowFloor, FixedCellType.SLOW_FLOOR)),
            base.solvers());
    MazeState maze = MazeState.empty(fixedLevel);
    RecordingShapeRenderer withoutFixed = allocate(RecordingShapeRenderer.class);
    RecordingShapeRenderer withFixed = allocate(RecordingShapeRenderer.class);

    renderer(allocate(RecordingSpriteBatch.class), withoutFixed, recordingFont())
        .render(
            MazeGameLayout.forPhase(GamePhase.BUILDING, 1280, 720, base.gridSize()),
            buildSnapshot(base));
    renderer(allocate(RecordingSpriteBatch.class), withFixed, recordingFont())
        .render(
            MazeGameLayout.forPhase(GamePhase.BUILDING, 1280, 720, fixedLevel.gridSize()),
            buildSnapshot(fixedLevel));

    assertEquals(Color.WHITE, MazeGameRenderer.cellColor(maze, null, 0.0F, wall));
    assertEquals(
        new Color(0.62F, 0.36F, 0.08F, 1.0F),
        MazeGameRenderer.cellColor(maze, null, 0.0F, slowFloor));
    assertEquals(withoutFixed.rectLines + 18, withFixed.rectLines);
  }

  @Test
  void paletteTooltipStaysWithinTheViewport() {
    ScreenRectangle viewport = new ScreenRectangle(0.0F, 0.0F, 390.0F, 286.0F);
    ScreenRectangle leftItem = new ScreenRectangle(0.0F, 240.0F, 56.0F, 44.0F);
    ScreenRectangle bounds = MazeGameRenderer.paletteTooltipBounds(viewport, leftItem);

    assertTrue(bounds.fitsWithin(viewport));
    assertEquals(8.0F, bounds.x());
    assertTrue(bounds.top() <= leftItem.y());
  }

  @Test
  void supplyBadgeOverlapsTheIconsBottomRightCorner() {
    ScreenRectangle icon = new ScreenRectangle(16.0F, 10.0F, 24.0F, 24.0F);
    ScreenRectangle badge = MazeGameRenderer.paletteSupplyBadgeBounds(icon);

    assertTrue(badge.x() < icon.right());
    assertTrue(badge.right() > icon.right());
    assertTrue(badge.y() < icon.y());
    assertTrue(badge.top() > icon.y());
  }

  @Test
  void rendersRejectedDestinationWithNonColorMarker() {
    GridPosition rejected = new GridPosition(2, 2);
    RecordingShapeRenderer withoutMarker = allocate(RecordingShapeRenderer.class);
    RecordingShapeRenderer withMarker = allocate(RecordingShapeRenderer.class);
    MazeGameRenderer withoutRenderer =
        renderer(allocate(RecordingSpriteBatch.class), withoutMarker, recordingFont());
    MazeGameRenderer withRenderer =
        renderer(allocate(RecordingSpriteBatch.class), withMarker, recordingFont());

    withoutRenderer.render(layout(GamePhase.BUILDING), snapshot(GamePhase.BUILDING, null));
    withRenderer.render(
        layout(GamePhase.BUILDING),
        snapshot(
            GamePhase.BUILDING,
            LEVEL,
            MazeState.empty(LEVEL),
            30.0F,
            rejected,
            0.4F,
            null,
            null,
            List.of(),
            true,
            false,
            false));

    assertEquals(withoutMarker.rectLines + 6, withMarker.rectLines);
  }

  @Test
  void rendersClampedPaletteDragWithDistinctValidAndRejectedShapeFeedback() {
    GridPosition destination = new GridPosition(2, 2);
    PaletteDragPreview validPreview =
        new PaletteDragPreview(PlaceableCellType.WALL, -50.0F, -50.0F, destination, true);
    PaletteDragPreview rejectedPreview =
        new PaletteDragPreview(PlaceableCellType.WALL, 1400.0F, 900.0F, destination, false);
    RecordingShapeRenderer validShapes = allocate(RecordingShapeRenderer.class);
    RecordingShapeRenderer rejectedShapes = allocate(RecordingShapeRenderer.class);

    renderer(allocate(RecordingSpriteBatch.class), validShapes, recordingFont())
        .render(layout(GamePhase.BUILDING), snapshotWithPreview(validPreview));
    renderer(allocate(RecordingSpriteBatch.class), rejectedShapes, recordingFont())
        .render(layout(GamePhase.BUILDING), snapshotWithPreview(rejectedPreview));

    assertEquals(10, previewLineCount(validShapes));
    assertEquals(10, previewLineCount(rejectedShapes));
    assertFalse(validShapes.recordedLines().equals(rejectedShapes.recordedLines()));
    assertEquals(
        new ScreenRectangle(0.0F, 0.0F, 28.0F, 28.0F),
        MazeGameRenderer.dragIconBounds(
            new ScreenRectangle(0.0F, 0.0F, 100.0F, 100.0F), validPreview));
    assertEquals(
        new ScreenRectangle(72.0F, 72.0F, 28.0F, 28.0F),
        MazeGameRenderer.dragIconBounds(
            new ScreenRectangle(0.0F, 0.0F, 100.0F, 100.0F), rejectedPreview));
  }

  @Test
  void outsidePaletteDragRendersOnlyClampedTypePreview() {
    RecordingShapeRenderer baseline = allocate(RecordingShapeRenderer.class);
    RecordingShapeRenderer previewShapes = allocate(RecordingShapeRenderer.class);
    renderer(allocate(RecordingSpriteBatch.class), baseline, recordingFont())
        .render(layout(GamePhase.BUILDING), snapshot(GamePhase.BUILDING, null));
    renderer(allocate(RecordingSpriteBatch.class), previewShapes, recordingFont())
        .render(
            layout(GamePhase.BUILDING),
            snapshotWithPreview(
                new PaletteDragPreview(PlaceableCellType.SLOW_FLOOR, 10.0F, 10.0F, null, false)));

    assertEquals(baseline.rectLines + 6, previewShapes.rectLines);
  }

  @Test
  void placedCellDragAddsNonColorSourceReservationWithoutHidingTypePreview() {
    GridPosition source = new GridPosition(3, 1);
    GridPosition destination = new GridPosition(2, 2);
    PaletteDragPreview palettePreview =
        new PaletteDragPreview(PlaceableCellType.SLOW_FLOOR, 300.0F, 300.0F, destination, true);
    PaletteDragPreview cellPreview =
        new PaletteDragPreview(
            PlaceableCellType.SLOW_FLOOR, 300.0F, 300.0F, destination, true, source);
    RecordingShapeRenderer paletteShapes = allocate(RecordingShapeRenderer.class);
    RecordingShapeRenderer cellShapes = allocate(RecordingShapeRenderer.class);

    renderer(allocate(RecordingSpriteBatch.class), paletteShapes, recordingFont())
        .render(layout(GamePhase.BUILDING), snapshotWithPreview(palettePreview));
    renderer(allocate(RecordingSpriteBatch.class), cellShapes, recordingFont())
        .render(layout(GamePhase.BUILDING), snapshotWithPreview(cellPreview));

    assertEquals(12, previewLineCount(paletteShapes));
    assertEquals(20, previewLineCount(cellShapes));
    assertFalse(paletteShapes.recordedLines().equals(cellShapes.recordedLines()));
  }

  @Test
  void compactPresentationUsesLabelsThatFitNarrowCardsAndActions() {
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer =
        renderer(
            allocate(RecordingSpriteBatch.class), allocate(RecordingShapeRenderer.class), font);
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    BestResult secondBest = new BestResult(Duration.ofSeconds(15), 60);
    List<LevelProgress> progress =
        List.of(
            new LevelProgress(Levels.levelOne(), true, firstBest),
            new LevelProgress(Levels.levelTwo(), true, secondBest),
            new LevelProgress(Levels.levelThree(), false, null));
    GameRenderSnapshot selectSnapshot =
        snapshot(
            GamePhase.LEVEL_SELECT,
            LEVEL,
            MazeState.empty(LEVEL),
            30.0F,
            null,
            0.0F,
            null,
            firstBest,
            progress,
            true,
            false,
            false);
    ScreenLayout selectLayout =
        MazeGameLayout.forPhase(
            GamePhase.LEVEL_SELECT, 390, 844, LEVEL.gridSize(), false, 3, false);

    renderer.render(selectLayout, selectSnapshot);

    BestResult scoutBest = new BestResult(Duration.ofMillis(6500), 26);
    List<LevelProgress> completedProgress =
        List.of(
            new LevelProgress(Levels.levelOne(), true, firstBest),
            new LevelProgress(Levels.levelTwo(), true, secondBest),
            new LevelProgress(Levels.levelThree(), true, scoutBest));
    renderer.render(
        selectLayout,
        snapshot(
            GamePhase.LEVEL_SELECT,
            LEVEL,
            MazeState.empty(LEVEL),
            30.0F,
            null,
            0.0F,
            null,
            firstBest,
            completedProgress,
            true,
            false,
            false));

    SolverRunResult result =
        new SolverRunResult(
            LEVEL.primarySolver().goal(), Duration.ofSeconds(10), 40, SolverRunStatus.TIMED_OUT);
    GameRenderSnapshot resultSnapshot =
        snapshot(
            GamePhase.RESULT,
            LEVEL,
            MazeState.empty(LEVEL),
            0.0F,
            null,
            0.0F,
            result,
            firstBest,
            progress,
            true,
            true,
            true);
    ScreenLayout resultLayout =
        MazeGameLayout.forPhase(GamePhase.RESULT, 390, 844, LEVEL.gridSize(), false, 2, true);
    renderer.render(resultLayout, resultSnapshot);

    assertTrue(font.capturedText().contains("Best 10.0s / 40"));
    assertTrue(font.capturedText().contains("Best 15.0s / 60"));
    assertFalse(font.capturedText().contains("Scout follows a"));
    assertFalse(font.capturedText().contains("consistent search"));
    assertFalse(font.capturedText().contains("pattern"));
    assertTrue(font.capturedText().contains("Locked"));
    assertTrue(font.capturedText().contains("Best 6.5s / 26"));
    assertTrue(font.capturedText().contains("Menu"));
    assertTrue(font.capturedText().contains("Next"));
    assertEquals("Menu", MazeGameRenderer.resultActionLabel(83.5F, "Main Menu", "Menu"));
    assertEquals("Next", MazeGameRenderer.resultActionLabel(83.5F, "Next Level", "Next"));
  }

  @Test
  void scoutPresentationFollowsTheLevelThroughEveryGameplayPhase() {
    LevelDefinition scoutLevel = Levels.levelThree();
    RecordingSpriteBatch spriteBatch = allocate(RecordingSpriteBatch.class);
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer = renderer(spriteBatch, allocate(RecordingShapeRenderer.class), font);
    SolverRunResult running =
        new SolverRunResult(
            scoutLevel.primarySolver().start(), Duration.ofSeconds(1), 4, SolverRunStatus.RUNNING);
    SolverRunResult result =
        new SolverRunResult(
            scoutLevel.primarySolver().goal(),
            Duration.ofMillis(6500),
            26,
            SolverRunStatus.REACHED_GOAL);
    List<LevelProgress> progress =
        List.of(
            new LevelProgress(Levels.levelOne(), true, null),
            new LevelProgress(Levels.levelTwo(), true, null),
            new LevelProgress(scoutLevel, true, new BestResult(Duration.ofMillis(6500), 26)));

    renderer.render(
        MazeGameLayout.forPhase(
            GamePhase.LEVEL_SELECT, 1280, 720, scoutLevel.gridSize(), true, 3, false),
        snapshot(
            GamePhase.LEVEL_SELECT,
            scoutLevel,
            MazeState.empty(scoutLevel),
            25.0F,
            null,
            0.0F,
            null,
            null,
            progress,
            true,
            false,
            false));
    renderer.render(
        scoutLayout(GamePhase.BUILDING), scoutSnapshot(GamePhase.BUILDING, null, progress));
    renderer.render(
        scoutLayout(GamePhase.SOLVER_RUNNING),
        scoutSnapshot(GamePhase.SOLVER_RUNNING, running, progress));
    renderer.render(
        scoutLayout(GamePhase.RESULT), scoutSnapshot(GamePhase.RESULT, result, progress));

    assertTrue(font.capturedText().contains("Delay past 6.0s; keep a path to the acorn"));
    assertFalse(font.capturedText().contains("Scout follows a consistent search pattern"));
    assertTrue(font.capturedText().contains("Scout | 7.0s | >6.0s"));
    assertTrue(font.capturedText().contains("Scout | Success | >6.0s"));
    assertTrue(font.capturedText().contains("Back"));
    assertTrue(spriteBatch.drawnRegionXs().contains(20));
  }

  @Test
  void characterAndGoalSpriteSelectionUsesAuthoredPresentationInsteadOfBehavior() {
    RecordingSpriteBatch spriteBatch = allocate(RecordingSpriteBatch.class);
    MazeGameRenderer renderer =
        renderer(spriteBatch, allocate(RecordingShapeRenderer.class), recordingFont());
    SolverRunResult randomResult =
        new SolverRunResult(
            LEVEL.primarySolver().start(), Duration.ZERO, 0, SolverRunStatus.RUNNING);

    renderer.render(
        layout(GamePhase.SOLVER_RUNNING), snapshot(GamePhase.SOLVER_RUNNING, randomResult));

    assertTrue(spriteBatch.drawnRegionXs().contains(10));
    assertFalse(spriteBatch.drawnRegionXs().contains(20));
    assertTrue(spriteBatch.drawnRegionXs().contains(1));
    assertFalse(spriteBatch.drawnRegionXs().contains(2));

    LevelDefinition scoutAppearanceWithRandomBehavior =
        new LevelDefinition(
            LEVEL.id(),
            LEVEL.name(),
            LEVEL.gridSize(),
            LEVEL.buildTime(),
            LEVEL.targetSolveTime(),
            LEVEL.maximumSolveTime(),
            LEVEL.solverMoveInterval(),
            LEVEL.placeableCellSupplies(),
            List.of(
                new LevelSolver(
                    LEVEL.primarySolver().start(),
                    LEVEL.primarySolver().goal(),
                    SolverBehavior.RANDOM,
                    OptionalLong.of(LEVEL.primarySolver().randomSeed().orElseThrow()),
                    SolverAppearance.SCOUT_SQUIRREL,
                    GoalType.ACORN)));
    SolverRunResult scoutResult =
        new SolverRunResult(
            scoutAppearanceWithRandomBehavior.primarySolver().start(),
            Duration.ZERO,
            0,
            SolverRunStatus.RUNNING);
    spriteBatch.drawnRegionXs().clear();
    renderer.render(
        MazeGameLayout.forPhase(
            GamePhase.SOLVER_RUNNING,
            1280,
            720,
            scoutAppearanceWithRandomBehavior.gridSize(),
            true,
            3,
            false),
        snapshot(
            GamePhase.SOLVER_RUNNING,
            scoutAppearanceWithRandomBehavior,
            MazeState.empty(scoutAppearanceWithRandomBehavior),
            30.0F,
            null,
            0.0F,
            scoutResult,
            null,
            List.of(),
            true,
            false,
            false));

    assertTrue(spriteBatch.drawnRegionXs().contains(20));
    assertFalse(spriteBatch.drawnRegionXs().contains(10));
    assertTrue(spriteBatch.drawnRegionXs().contains(2));
    assertFalse(spriteBatch.drawnRegionXs().contains(1));

    LevelDefinition trackerLevel = Levels.levelSix();
    SolverRunResult trackerResult =
        new SolverRunResult(
            trackerLevel.primarySolver().start(), Duration.ZERO, 0, SolverRunStatus.RUNNING);
    spriteBatch.drawnRegionXs().clear();
    renderer.render(
        MazeGameLayout.forPhase(
            GamePhase.SOLVER_RUNNING, 1280, 720, trackerLevel.gridSize(), true, 1, false),
        snapshot(
            GamePhase.SOLVER_RUNNING,
            trackerLevel,
            MazeState.empty(trackerLevel),
            30.0F,
            null,
            0.0F,
            trackerResult,
            null,
            List.of(),
            true,
            false,
            false));

    assertTrue(spriteBatch.drawnRegionXs().contains(30));
    assertFalse(spriteBatch.drawnRegionXs().contains(10));
    assertFalse(spriteBatch.drawnRegionXs().contains(20));
    assertTrue(spriteBatch.drawnRegionXs().contains(3));

    LevelDefinition seekerLevel = seekerLevel();
    SolverRunResult seekerResult =
        new SolverRunResult(
            seekerLevel.primarySolver().start(), Duration.ZERO, 0, SolverRunStatus.RUNNING);
    spriteBatch.drawnRegionXs().clear();
    renderer.render(
        MazeGameLayout.forPhase(
            GamePhase.SOLVER_RUNNING, 1280, 720, seekerLevel.gridSize(), true, 1, false),
        snapshot(
            GamePhase.SOLVER_RUNNING,
            seekerLevel,
            MazeState.empty(seekerLevel),
            30.0F,
            null,
            0.0F,
            seekerResult,
            null,
            List.of(),
            true,
            false,
            false));

    assertTrue(spriteBatch.drawnRegionXs().contains(40));
    assertFalse(spriteBatch.drawnRegionXs().contains(30));
    assertTrue(spriteBatch.drawnRegionXs().contains(4));
  }

  @Test
  void multiSolverLevelDrawsBothCharactersAndBothMatchingGoals() {
    LevelDefinition level = Levels.levelFive();
    RecordingSpriteBatch spriteBatch = allocate(RecordingSpriteBatch.class);
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer = renderer(spriteBatch, allocate(RecordingShapeRenderer.class), font);
    List<SolverRunResult> results =
        List.of(
            new SolverRunResult(
                level.solvers().get(0).start(), Duration.ZERO, 0, SolverRunStatus.RUNNING),
            new SolverRunResult(
                level.solvers().get(1).start(), Duration.ZERO, 0, SolverRunStatus.RUNNING));
    GameRenderSnapshot snapshot =
        snapshot(
            GamePhase.SOLVER_RUNNING,
            level,
            MazeState.empty(level),
            0.0F,
            null,
            0.0F,
            null,
            List.of(new LevelProgress(level, true, null)),
            List.of(),
            null,
            null,
            true,
            false,
            false,
            results);

    renderer.render(
        MazeGameLayout.forPhase(
            GamePhase.SOLVER_RUNNING, 1280, 720, level.gridSize(), true, 1, false),
        snapshot);

    assertTrue(spriteBatch.drawnRegionXs().contains(1));
    assertTrue(spriteBatch.drawnRegionXs().contains(2));
    assertTrue(spriteBatch.drawnRegionXs().contains(10));
    assertTrue(spriteBatch.drawnRegionXs().contains(20));
    assertTrue(font.capturedText().stream().anyMatch(text -> text.contains("Solver + Scout")));
  }

  @Test
  void solverSpritesFollowEachCharactersMostRecentMovementDirection() {
    LevelDefinition level = threeSolverLevel();
    RecordingSpriteBatch spriteBatch = allocate(RecordingSpriteBatch.class);
    MazeGameRenderer renderer =
        new MazeGameRenderer(
            spriteBatch,
            allocate(RecordingShapeRenderer.class),
            recordingFont(),
            sprite(1),
            sprite(2),
            sprite(3),
            directionalSprites(100),
            directionalSprites(200),
            directionalSprites(300));
    List<SolverRunResult> results =
        List.of(
            new SolverRunResult(
                level.solvers().get(0).start(), Duration.ZERO, 1, SolverRunStatus.RUNNING),
            new SolverRunResult(
                level.solvers().get(1).start(), Duration.ZERO, 1, SolverRunStatus.RUNNING));
    results =
        List.of(
            results.get(0),
            results.get(1),
            new SolverRunResult(
                level.solvers().get(2).start(), Duration.ZERO, 1, SolverRunStatus.RUNNING));
    GameRenderSnapshot snapshot =
        new GameRenderSnapshot(
            GamePhase.SOLVER_RUNNING,
            level,
            MazeState.empty(level),
            0.0F,
            null,
            0.0F,
            null,
            List.of(new LevelProgress(level, true, null)),
            List.of(),
            null,
            null,
            true,
            false,
            false,
            results,
            List.of(
                Optional.of(CardinalDirection.NORTH),
                Optional.of(CardinalDirection.WEST),
                Optional.of(CardinalDirection.EAST)));

    renderer.render(
        MazeGameLayout.forPhase(
            GamePhase.SOLVER_RUNNING, 1280, 720, level.gridSize(), true, 1, false),
        snapshot);

    assertTrue(spriteBatch.drawnRegionXs().contains(110));
    assertTrue(spriteBatch.drawnRegionXs().contains(220));
    assertTrue(spriteBatch.drawnRegionXs().contains(330));
    assertFalse(spriteBatch.drawnRegionXs().contains(130));
    assertFalse(spriteBatch.drawnRegionXs().contains(230));
    assertFalse(spriteBatch.drawnRegionXs().contains(300));
  }

  @Test
  void renderSnapshotRequiresOneDirectionEntryPerSolverResult() {
    SolverRunResult result =
        new SolverRunResult(
            LEVEL.primarySolver().start(), Duration.ZERO, 0, SolverRunStatus.RUNNING);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new GameRenderSnapshot(
                GamePhase.SOLVER_RUNNING,
                LEVEL,
                MazeState.empty(LEVEL),
                0.0F,
                null,
                0.0F,
                null,
                List.of(),
                List.of(),
                null,
                null,
                true,
                false,
                false,
                List.of(result),
                List.of()));
  }

  @Test
  void arbitrarySolverCountsReceiveStableDisambiguatedTitlesAndStats() {
    LevelDefinition level = threeSolverLevel();
    List<SolverRunResult> results =
        List.of(
            new SolverRunResult(
                level.solvers().get(0).goal(),
                Duration.ofSeconds(1),
                4,
                SolverRunStatus.REACHED_GOAL),
            new SolverRunResult(
                level.solvers().get(1).goal(),
                Duration.ofSeconds(2),
                8,
                SolverRunStatus.REACHED_GOAL),
            new SolverRunResult(
                level.solvers().get(2).goal(),
                Duration.ofSeconds(3),
                12,
                SolverRunStatus.REACHED_GOAL));
    GameRenderSnapshot snapshot = resultSnapshot(level, results);

    assertEquals(
        "Level 1 | Solver + Scout + Tracker",
        MazeGameRenderer.levelTitle(level, false, Float.MAX_VALUE));
    assertEquals("Solver + Scout + Tracker", MazeGameRenderer.levelTitle(level, true, 300.0F));
    assertEquals(
        "Solver 1.00s/4  Scout 2.00s/8  Tracker 3.00s/12", MazeGameRenderer.resultStats(snapshot));
    assertThrows(
        IllegalArgumentException.class,
        () -> MazeGameRenderer.resultStats(resultSnapshot(level, results.subList(0, 2))));
  }

  @Test
  void rendererRejectsMissingSpriteRegions() {
    assertThrows(
        NullPointerException.class,
        () ->
            new MazeGameRenderer(
                allocate(RecordingSpriteBatch.class),
                allocate(RecordingShapeRenderer.class),
                recordingFont(),
                null,
                sprite(2),
                sprite(10),
                sprite(20)));
  }

  private static MazeGameRenderer renderer(
      RecordingSpriteBatch spriteBatch, RecordingShapeRenderer shapeRenderer, RecordingFont font) {
    return new MazeGameRenderer(
        spriteBatch,
        shapeRenderer,
        font,
        sprite(1),
        sprite(2),
        sprite(3),
        sprite(4),
        DirectionalSpriteSet.single(sprite(10)),
        DirectionalSpriteSet.single(sprite(20)),
        DirectionalSpriteSet.single(sprite(30)),
        DirectionalSpriteSet.single(sprite(40)));
  }

  private static TextureRegion sprite(int regionX) {
    return new TextureRegion(new TestTexture(), regionX, 0, 100, 100);
  }

  private static DirectionalSpriteSet directionalSprites(int firstRegionX) {
    return new DirectionalSpriteSet(
        sprite(firstRegionX),
        sprite(firstRegionX + 10),
        sprite(firstRegionX + 20),
        sprite(firstRegionX + 30));
  }

  private static ScreenLayout layout(GamePhase phase) {
    return MazeGameLayout.forPhase(phase, 1280, 720, LEVEL.gridSize(), true, 2, false);
  }

  private static ScreenLayout scoutLayout(GamePhase phase) {
    return MazeGameLayout.forPhase(
        phase, 1280, 720, Levels.levelThree().gridSize(), true, 3, false);
  }

  private static GameRenderSnapshot scoutSnapshot(
      GamePhase phase, SolverRunResult result, List<LevelProgress> progress) {
    LevelDefinition level = Levels.levelThree();
    return snapshot(
        phase,
        level,
        MazeState.empty(level),
        25.0F,
        null,
        0.0F,
        result,
        result == null ? null : new BestResult(Duration.ofMillis(6500), 26),
        progress,
        true,
        result != null && result.status() == SolverRunStatus.REACHED_GOAL,
        false);
  }

  private static GameRenderSnapshot snapshot(GamePhase phase, SolverRunResult solverRunResult) {
    boolean resultPassed =
        solverRunResult != null
            && solverRunResult.elapsedTime().compareTo(LEVEL.targetSolveTime()) > 0;
    return snapshot(
        phase,
        LEVEL,
        MazeState.empty(LEVEL),
        30.0F,
        null,
        0.0F,
        solverRunResult,
        solverRunResult == null ? null : new BestResult(Duration.ofSeconds(10L), 40),
        levelProgress(solverRunResult == null ? null : new BestResult(Duration.ofSeconds(10L), 40)),
        true,
        resultPassed,
        false);
  }

  private static List<LevelProgress> levelProgress(BestResult firstBestResult) {
    return List.of(
        new LevelProgress(Levels.levelOne(), true, firstBestResult),
        new LevelProgress(Levels.levelTwo(), false, null));
  }

  private static LevelDefinition paletteLevel() {
    return singleSolverLevel(
        "palette-render",
        "Palette Render",
        GridSize.square(5),
        new GridPosition(4, 2),
        new GridPosition(0, 2),
        Duration.ofSeconds(25),
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        Duration.ofMillis(250),
        List.of(
            PlaceableCellSupply.infinite(PlaceableCellType.WALL),
            PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 1)),
        SolverBehavior.RANDOM,
        1L);
  }

  private static LevelDefinition threeSolverLevel() {
    return new LevelDefinition(
        LEVEL.id(),
        LEVEL.name(),
        LEVEL.gridSize(),
        LEVEL.buildTime(),
        LEVEL.targetSolveTime(),
        LEVEL.maximumSolveTime(),
        LEVEL.solverMoveInterval(),
        LEVEL.placeableCellSupplies(),
        List.of(
            new LevelSolver(
                new GridPosition(4, 0),
                new GridPosition(0, 0),
                SolverBehavior.RANDOM,
                OptionalLong.of(1L),
                SolverAppearance.CLASSIC_MOUSE,
                GoalType.CHEESE),
            new LevelSolver(
                new GridPosition(4, 2),
                new GridPosition(0, 2),
                SolverBehavior.LEFT_PRIORITY,
                OptionalLong.empty(),
                SolverAppearance.SCOUT_SQUIRREL,
                GoalType.ACORN),
            new LevelSolver(
                new GridPosition(4, 4),
                new GridPosition(0, 4),
                SolverBehavior.LEAST_VISITED,
                OptionalLong.empty(),
                SolverAppearance.TRACKER_RACCOON,
                GoalType.TRASH_CAN)));
  }

  private static LevelDefinition seekerLevel() {
    return singleSolverLevel(
        "seeker-fixture",
        "Seeker Fixture",
        LEVEL.gridSize(),
        LEVEL.primarySolver().start(),
        LEVEL.primarySolver().goal(),
        LEVEL.buildTime(),
        LEVEL.targetSolveTime(),
        LEVEL.maximumSolveTime(),
        LEVEL.solverMoveInterval(),
        LEVEL.placeableCellSupplies(),
        SolverBehavior.LINE_OF_SIGHT,
        17L);
  }

  private static GameRenderSnapshot resultSnapshot(
      LevelDefinition level, List<SolverRunResult> results) {
    return snapshot(
        GamePhase.RESULT,
        level,
        MazeState.empty(level),
        0.0F,
        null,
        0.0F,
        null,
        List.of(),
        List.of(),
        null,
        null,
        true,
        true,
        false,
        results);
  }

  private static GameRenderSnapshot snapshotWithPreview(PaletteDragPreview preview) {
    return snapshot(
        GamePhase.BUILDING,
        LEVEL,
        MazeState.empty(LEVEL),
        30.0F,
        null,
        0.0F,
        null,
        null,
        List.of(),
        List.of(),
        preview,
        true,
        false,
        false);
  }

  private static GameRenderSnapshot buildSnapshot(LevelDefinition level) {
    return snapshot(
        GamePhase.BUILDING,
        level,
        MazeState.empty(level),
        level.buildTime().toMillis() / 1000.0F,
        null,
        0.0F,
        null,
        null,
        List.of(),
        true,
        false,
        false);
  }

  private static int previewLineCount(RecordingShapeRenderer shapes) {
    return shapes.rectLines - snapshotBaselineLineCount();
  }

  private static int snapshotBaselineLineCount() {
    RecordingShapeRenderer baseline = allocate(RecordingShapeRenderer.class);
    renderer(allocate(RecordingSpriteBatch.class), baseline, recordingFont())
        .render(layout(GamePhase.BUILDING), snapshot(GamePhase.BUILDING, null));
    return baseline.rectLines;
  }

  private static <T> T allocate(Class<T> type) {
    try {
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      Field field = unsafeClass.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      Object unsafe = field.get(null);
      Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
      return type.cast(allocateInstance.invoke(unsafe, type));
    } catch (ReflectiveOperationException exception) {
      throw new LinkageError("Unable to allocate libGDX test double", exception);
    }
  }

  private static RecordingFont recordingFont() {
    return allocate(RecordingFont.class);
  }

  private static final class RecordingSpriteBatch extends SpriteBatch {
    private int beginCount;
    private int endCount;
    private List<Integer> drawnRegionXs = new ArrayList<>();

    @Override
    public void begin() {
      beginCount++;
    }

    @Override
    public void end() {
      endCount++;
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {
      drawnRegionXs().add(region.getRegionX());
    }

    private List<Integer> drawnRegionXs() {
      if (drawnRegionXs == null) {
        drawnRegionXs = new ArrayList<>();
      }
      return drawnRegionXs;
    }
  }

  private static GameRenderSnapshot snapshot(
      GamePhase phase,
      LevelDefinition level,
      MazeState maze,
      float buildTimeRemaining,
      GridPosition rejectedPosition,
      float rejectedFlashRemaining,
      BestResult bestResult,
      List<LevelProgress> progress,
      List<CellPaletteState> palette,
      PaletteDragPreview dragPreview,
      PlaceableCellType tooltipType,
      boolean audioEnabled,
      boolean resultPassed,
      boolean hasNextLevel,
      List<SolverRunResult> results) {
    return new GameRenderSnapshot(
        phase,
        level,
        maze,
        buildTimeRemaining,
        rejectedPosition,
        rejectedFlashRemaining,
        bestResult,
        progress,
        palette,
        dragPreview,
        tooltipType,
        audioEnabled,
        resultPassed,
        hasNextLevel,
        results);
  }

  private static GameRenderSnapshot snapshot(
      GamePhase phase,
      LevelDefinition level,
      MazeState maze,
      float buildTimeRemaining,
      GridPosition rejectedPosition,
      float rejectedFlashRemaining,
      SolverRunResult result,
      BestResult bestResult,
      List<LevelProgress> progress,
      boolean audioEnabled,
      boolean resultPassed,
      boolean hasNextLevel) {
    return snapshot(
        phase,
        level,
        maze,
        buildTimeRemaining,
        rejectedPosition,
        rejectedFlashRemaining,
        bestResult,
        progress,
        List.of(),
        null,
        null,
        audioEnabled,
        resultPassed,
        hasNextLevel,
        result == null ? List.of() : List.of(result));
  }

  private static GameRenderSnapshot snapshot(
      GamePhase phase,
      LevelDefinition level,
      MazeState maze,
      float buildTimeRemaining,
      GridPosition rejectedPosition,
      float rejectedFlashRemaining,
      SolverRunResult result,
      BestResult bestResult,
      List<LevelProgress> progress,
      List<CellPaletteState> palette,
      boolean audioEnabled,
      boolean resultPassed,
      boolean hasNextLevel) {
    return snapshot(
        phase,
        level,
        maze,
        buildTimeRemaining,
        rejectedPosition,
        rejectedFlashRemaining,
        bestResult,
        progress,
        palette,
        null,
        null,
        audioEnabled,
        resultPassed,
        hasNextLevel,
        result == null ? List.of() : List.of(result));
  }

  private static GameRenderSnapshot snapshot(
      GamePhase phase,
      LevelDefinition level,
      MazeState maze,
      float buildTimeRemaining,
      GridPosition rejectedPosition,
      float rejectedFlashRemaining,
      SolverRunResult result,
      BestResult bestResult,
      List<LevelProgress> progress,
      List<CellPaletteState> palette,
      PaletteDragPreview dragPreview,
      boolean audioEnabled,
      boolean resultPassed,
      boolean hasNextLevel) {
    return snapshot(
        phase,
        level,
        maze,
        buildTimeRemaining,
        rejectedPosition,
        rejectedFlashRemaining,
        bestResult,
        progress,
        palette,
        dragPreview,
        null,
        audioEnabled,
        resultPassed,
        hasNextLevel,
        result == null ? List.of() : List.of(result));
  }

  private static GameRenderSnapshot snapshot(
      GamePhase phase,
      LevelDefinition level,
      MazeState maze,
      float buildTimeRemaining,
      GridPosition rejectedPosition,
      float rejectedFlashRemaining,
      SolverRunResult result,
      BestResult bestResult,
      List<LevelProgress> progress,
      List<CellPaletteState> palette,
      PaletteDragPreview dragPreview,
      PlaceableCellType tooltipType,
      boolean audioEnabled,
      boolean resultPassed,
      boolean hasNextLevel) {
    return snapshot(
        phase,
        level,
        maze,
        buildTimeRemaining,
        rejectedPosition,
        rejectedFlashRemaining,
        bestResult,
        progress,
        palette,
        dragPreview,
        tooltipType,
        audioEnabled,
        resultPassed,
        hasNextLevel,
        result == null ? List.of() : List.of(result));
  }

  private static final class TestTexture extends Texture {
    private TestTexture() {
      super();
    }

    @Override
    public int getWidth() {
      return 1000;
    }

    @Override
    public int getHeight() {
      return 1000;
    }
  }

  private static final class RecordingShapeRenderer extends ShapeRenderer {
    private int beginCount;
    private int endCount;
    private int rects;
    private int rectLines;
    private List<RecordedLine> lines = new ArrayList<>();

    @Override
    public void begin(ShapeType shapeType) {
      beginCount++;
    }

    @Override
    public void setColor(Color color) {}

    @Override
    public void rect(float x, float y, float width, float height) {
      rects++;
    }

    @Override
    public void rectLine(float x1, float y1, float x2, float y2, float width) {
      rectLines++;
      recordedLines().add(new RecordedLine(x1, y1, x2, y2, width));
    }

    @Override
    public void end() {
      endCount++;
    }

    private List<RecordedLine> recordedLines() {
      if (lines == null) {
        lines = new ArrayList<>();
      }
      return lines;
    }
  }

  private record RecordedLine(float x1, float y1, float x2, float y2, float width) {}

  private static final class RecordingFont extends BitmapFont {
    private List<String> text = new ArrayList<>();

    @Override
    public void setColor(Color color) {}

    @Override
    public GlyphLayout draw(Batch batch, CharSequence str, float x, float y) {
      capturedText().add(str.toString());
      return null;
    }

    @Override
    public GlyphLayout draw(
        Batch batch,
        CharSequence str,
        float x,
        float y,
        int start,
        int end,
        float targetWidth,
        int horizontalAlignment,
        boolean wrap,
        String truncate) {
      capturedText().add(str.toString());
      return null;
    }

    private List<String> capturedText() {
      if (text == null) {
        text = new ArrayList<>();
      }
      return text;
    }
  }
}
