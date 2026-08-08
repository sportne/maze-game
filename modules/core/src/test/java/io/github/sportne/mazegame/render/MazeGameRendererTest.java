package io.github.sportne.mazegame.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.mouse.MouseRunStatus;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.LevelProgress;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class MazeGameRendererTest {
  private static final LevelDefinition LEVEL = Levels.milestoneOne();

  @Test
  void cellColorReflectsMazeContentAndRejectedPosition() {
    GridPosition wall = new GridPosition(1, 1);
    MazeState maze = MazeState.empty(LEVEL).withWall(wall);

    assertEquals(Color.BLACK, MazeGameRenderer.cellColor(maze, null, 0.0F, new GridPosition(2, 2)));
    assertEquals(Color.WHITE, MazeGameRenderer.cellColor(maze, null, 0.0F, wall));
    assertEquals(
        new Color(0.24F, 0.62F, 0.95F, 1.0F),
        MazeGameRenderer.cellColor(maze, null, 0.0F, LEVEL.mouseStart()));
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
                LEVEL.mouseStart(),
                0.0F,
                100.0F));
  }

  @Test
  void renderSnapshotExposesFrameData() {
    MazeState maze = MazeState.empty(LEVEL);
    GridPosition rejected = new GridPosition(2, 2);
    MouseRunResult runResult =
        new MouseRunResult(LEVEL.mouseStart(), Duration.ofMillis(250L), 1, MouseRunStatus.RUNNING);
    BestResult bestResult = new BestResult(Duration.ofSeconds(10L), 40);

    GameRenderSnapshot snapshot =
        new GameRenderSnapshot(
            GamePhase.MOUSE_RUNNING,
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
            false,
            true);

    assertEquals(GamePhase.MOUSE_RUNNING, snapshot.phase());
    assertEquals(LEVEL, snapshot.levelDefinition());
    assertEquals(maze, snapshot.mazeState());
    assertEquals(12.0F, snapshot.buildTimeRemainingSeconds());
    assertEquals(rejected, snapshot.rejectedPosition());
    assertEquals(0.4F, snapshot.rejectedFlashRemainingSeconds());
    assertEquals(runResult, snapshot.mouseRunResult());
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
            new GameRenderSnapshot(
                null, LEVEL, maze, 12.0F, null, 0.0F, null, null, List.of(), true, false, false,
                false));
  }

  @Test
  void rendersMenuScreensWithExpectedLabels() {
    RecordingSpriteBatch spriteBatch = allocate(RecordingSpriteBatch.class);
    RecordingShapeRenderer shapeRenderer = allocate(RecordingShapeRenderer.class);
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer = new MazeGameRenderer(spriteBatch, shapeRenderer, font, null, null);

    renderer.render(layout(GamePhase.MAIN_MENU), snapshot(GamePhase.MAIN_MENU, null));
    renderer.render(layout(GamePhase.LEVEL_SELECT), snapshot(GamePhase.LEVEL_SELECT, null));
    renderer.render(layout(GamePhase.SETTINGS), snapshot(GamePhase.SETTINGS, null));

    assertTrue(font.capturedText().contains("Maze Game"));
    assertTrue(font.capturedText().contains("Start"));
    assertTrue(font.capturedText().contains("Settings"));
    assertTrue(font.capturedText().contains("Quit"));
    assertTrue(font.capturedText().contains("Select Level"));
    assertTrue(font.capturedText().contains("Milestone 1"));
    assertTrue(font.capturedText().contains("Milestone 2"));
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
        new MazeGameRenderer(
            allocate(RecordingSpriteBatch.class),
            allocate(RecordingShapeRenderer.class),
            font,
            null,
            null);
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
        new MazeGameRenderer(
            allocate(RecordingSpriteBatch.class),
            allocate(RecordingShapeRenderer.class),
            font,
            null,
            null);
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    BestResult secondBest = new BestResult(Duration.ofSeconds(15), 60);
    GameRenderSnapshot snapshot =
        new GameRenderSnapshot(
            GamePhase.LEVEL_SELECT,
            LEVEL,
            MazeState.empty(LEVEL),
            30.0F,
            null,
            0.0F,
            null,
            firstBest,
            List.of(
                new LevelProgress(Levels.milestoneOne(), true, firstBest),
                new LevelProgress(Levels.milestoneTwo(), true, secondBest)),
            true,
            false,
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
        new MazeGameRenderer(
            allocate(RecordingSpriteBatch.class),
            allocate(RecordingShapeRenderer.class),
            font,
            null,
            null);
    MouseRunResult result =
        new MouseRunResult(LEVEL.cheese(), Duration.ofSeconds(10), 40, MouseRunStatus.TIMED_OUT);
    GameRenderSnapshot snapshot =
        new GameRenderSnapshot(
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
                    Levels.milestoneOne(), true, new BestResult(Duration.ofSeconds(10), 40)),
                new LevelProgress(Levels.milestoneTwo(), true, null)),
            true,
            false,
            true,
            true);
    ScreenLayout resultLayout =
        MazeGameLayout.forPhase(GamePhase.RESULT, 1280, 720, LEVEL.gridSize(), true, 2, true);

    renderer.render(resultLayout, snapshot);

    assertTrue(font.capturedText().contains("Next Level"));
    assertFalse(font.capturedText().contains("Pass this level to unlock the next"));
  }

  @Test
  void rendersBuildAndResultScreensWithExpectedLabels() {
    RecordingSpriteBatch spriteBatch = allocate(RecordingSpriteBatch.class);
    RecordingShapeRenderer shapeRenderer = allocate(RecordingShapeRenderer.class);
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer = new MazeGameRenderer(spriteBatch, shapeRenderer, font, null, null);
    MouseRunResult running =
        new MouseRunResult(
            LEVEL.mouseStart(), Duration.ofMillis(2500L), 10, MouseRunStatus.RUNNING);
    MouseRunResult result =
        new MouseRunResult(LEVEL.cheese(), Duration.ofSeconds(10L), 40, MouseRunStatus.TIMED_OUT);
    MouseRunResult failedResult =
        new MouseRunResult(
            LEVEL.cheese(), Duration.ofSeconds(2L), 8, MouseRunStatus.REACHED_CHEESE);

    renderer.render(layout(GamePhase.BUILDING), snapshot(GamePhase.BUILDING, null));
    renderer.render(layout(GamePhase.MOUSE_RUNNING), snapshot(GamePhase.MOUSE_RUNNING, running));
    renderer.render(layout(GamePhase.RESULT), snapshot(GamePhase.RESULT, result));
    renderer.render(layout(GamePhase.RESULT), snapshot(GamePhase.RESULT, failedResult));

    assertTrue(font.capturedText().contains("Milestone 1"));
    assertTrue(font.capturedText().contains("Build: 30.0s"));
    assertTrue(font.capturedText().contains("Delay past 5.0s; keep a path to the cheese"));
    assertTrue(font.capturedText().contains("Mode: Place"));
    assertTrue(font.capturedText().contains("Start Mouse"));
    assertTrue(font.capturedText().contains("Milestone 1 | 7.5s | >5.0s"));
    assertTrue(font.capturedText().contains("Milestone 1 | Success | >5.0s"));
    assertTrue(font.capturedText().contains("Milestone 1 | Failed | >5.0s"));
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
  void compactPresentationUsesLabelsThatFitNarrowCardsAndActions() {
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer =
        new MazeGameRenderer(
            allocate(RecordingSpriteBatch.class),
            allocate(RecordingShapeRenderer.class),
            font,
            null,
            null);
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    BestResult secondBest = new BestResult(Duration.ofSeconds(15), 60);
    List<LevelProgress> progress =
        List.of(
            new LevelProgress(Levels.milestoneOne(), true, firstBest),
            new LevelProgress(Levels.milestoneTwo(), true, secondBest));
    GameRenderSnapshot selectSnapshot =
        new GameRenderSnapshot(
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
            false,
            false);
    ScreenLayout selectLayout =
        MazeGameLayout.forPhase(
            GamePhase.LEVEL_SELECT, 390, 844, LEVEL.gridSize(), false, 2, false);

    renderer.render(selectLayout, selectSnapshot);

    MouseRunResult result =
        new MouseRunResult(LEVEL.cheese(), Duration.ofSeconds(10), 40, MouseRunStatus.TIMED_OUT);
    GameRenderSnapshot resultSnapshot =
        new GameRenderSnapshot(
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
            false,
            true,
            true);
    ScreenLayout resultLayout =
        MazeGameLayout.forPhase(GamePhase.RESULT, 390, 844, LEVEL.gridSize(), false, 2, true);
    renderer.render(resultLayout, resultSnapshot);

    assertTrue(font.capturedText().contains("Best 10.0s / 40"));
    assertTrue(font.capturedText().contains("Best 15.0s / 60"));
    assertTrue(font.capturedText().contains("Menu"));
    assertTrue(font.capturedText().contains("Next"));
    assertEquals("Menu", MazeGameRenderer.resultActionLabel(83.5F, "Main Menu", "Menu"));
    assertEquals("Next", MazeGameRenderer.resultActionLabel(83.5F, "Next Level", "Next"));
  }

  @Test
  void rendererAcceptsNullSpriteRegions() {
    MazeGameRenderer renderer =
        new MazeGameRenderer(
            allocate(RecordingSpriteBatch.class),
            allocate(RecordingShapeRenderer.class),
            recordingFont(),
            null,
            null);

    assertNotNull(renderer);
  }

  private static ScreenLayout layout(GamePhase phase) {
    return MazeGameLayout.forPhase(phase, 1280, 720, LEVEL.gridSize(), true, 2, false);
  }

  private static GameRenderSnapshot snapshot(GamePhase phase, MouseRunResult mouseRunResult) {
    boolean resultPassed =
        mouseRunResult != null
            && mouseRunResult.elapsedTime().compareTo(LEVEL.targetSolveTime()) > 0;
    return new GameRenderSnapshot(
        phase,
        LEVEL,
        MazeState.empty(LEVEL),
        30.0F,
        null,
        0.0F,
        mouseRunResult,
        mouseRunResult == null ? null : new BestResult(Duration.ofSeconds(10L), 40),
        levelProgress(mouseRunResult == null ? null : new BestResult(Duration.ofSeconds(10L), 40)),
        true,
        false,
        resultPassed,
        false);
  }

  private static List<LevelProgress> levelProgress(BestResult firstBestResult) {
    return List.of(
        new LevelProgress(Levels.milestoneOne(), true, firstBestResult),
        new LevelProgress(Levels.milestoneTwo(), false, null));
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
      // Sprites are intentionally not asserted in these primitive renderer tests.
    }
  }

  private static final class RecordingShapeRenderer extends ShapeRenderer {
    private int beginCount;
    private int endCount;
    private int rects;
    private int rectLines;

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
    }

    @Override
    public void end() {
      endCount++;
    }
  }

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
