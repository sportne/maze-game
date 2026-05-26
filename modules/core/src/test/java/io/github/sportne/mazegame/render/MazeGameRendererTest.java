package io.github.sportne.mazegame.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.LevelDefinition;
import io.github.sportne.mazegame.model.Levels;
import io.github.sportne.mazegame.model.MazeState;
import io.github.sportne.mazegame.model.MouseRunResult;
import io.github.sportne.mazegame.model.MouseRunStatus;
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

    GameRenderSnapshot snapshot =
        new GameRenderSnapshot(
            GamePhase.MOUSE_RUNNING,
            LEVEL,
            maze,
            12.0F,
            rejected,
            0.4F,
            runResult,
            true,
            false,
            true);

    assertEquals(GamePhase.MOUSE_RUNNING, snapshot.phase());
    assertEquals(LEVEL, snapshot.levelDefinition());
    assertEquals(maze, snapshot.mazeState());
    assertEquals(12.0F, snapshot.buildTimeRemainingSeconds());
    assertEquals(rejected, snapshot.rejectedPosition());
    assertEquals(0.4F, snapshot.rejectedFlashRemainingSeconds());
    assertEquals(runResult, snapshot.mouseRunResult());
    assertTrue(snapshot.audioEnabled());
    assertTrue(snapshot.hasNextLevel());
  }

  @Test
  void renderSnapshotRequiresFrameState() {
    MazeState maze = MazeState.empty(LEVEL);

    assertThrows(
        NullPointerException.class,
        () ->
            new GameRenderSnapshot(null, LEVEL, maze, 12.0F, null, 0.0F, null, true, false, false));
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
    assertTrue(font.capturedText().contains("Locked"));
    assertTrue(font.capturedText().contains("Audio: On"));
    assertTrue(shapeRenderer.rects >= 12);
    assertTrue(shapeRenderer.rectLines >= 48);
    assertEquals(spriteBatch.beginCount, spriteBatch.endCount);
    assertEquals(shapeRenderer.beginCount, shapeRenderer.endCount);
  }

  @Test
  void rendersBuildAndResultScreensWithExpectedLabels() {
    RecordingSpriteBatch spriteBatch = allocate(RecordingSpriteBatch.class);
    RecordingShapeRenderer shapeRenderer = allocate(RecordingShapeRenderer.class);
    RecordingFont font = recordingFont();
    MazeGameRenderer renderer = new MazeGameRenderer(spriteBatch, shapeRenderer, font, null, null);
    MouseRunResult result =
        new MouseRunResult(LEVEL.cheese(), Duration.ofSeconds(10L), 40, MouseRunStatus.TIMED_OUT);

    renderer.render(layout(GamePhase.BUILDING), snapshot(GamePhase.BUILDING, null));
    renderer.render(layout(GamePhase.RESULT), snapshot(GamePhase.RESULT, result));

    assertTrue(font.capturedText().contains("Maze Game"));
    assertTrue(font.capturedText().contains("Build: 30.0s"));
    assertTrue(font.capturedText().contains("Left click: wall   Right click: clear"));
    assertTrue(font.capturedText().contains("Start Mouse"));
    assertTrue(font.capturedText().contains("Pass"));
    assertTrue(font.capturedText().contains("Time: 10.00s  Moves: 40"));
    assertTrue(font.capturedText().contains("Retry"));
    assertTrue(font.capturedText().contains("Replay"));
    assertTrue(font.capturedText().contains("Main Menu"));
    assertTrue(font.capturedText().contains("No next level in this milestone"));
    assertTrue(shapeRenderer.rects >= 50);
    assertTrue(shapeRenderer.rectLines >= 20);
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
    return MazeGameLayout.forPhase(phase, 1280, 720, LEVEL.gridSize());
  }

  private static GameRenderSnapshot snapshot(GamePhase phase, MouseRunResult mouseRunResult) {
    return new GameRenderSnapshot(
        phase, LEVEL, MazeState.empty(LEVEL), 30.0F, null, 0.0F, mouseRunResult, true, true, false);
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

    private List<String> capturedText() {
      if (text == null) {
        text = new ArrayList<>();
      }
      return text;
    }
  }
}
