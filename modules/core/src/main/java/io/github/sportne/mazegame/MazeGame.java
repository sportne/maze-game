package io.github.sportne.mazegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.sportne.mazegame.model.CellContent;
import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.LevelDefinition;
import io.github.sportne.mazegame.model.Levels;
import io.github.sportne.mazegame.model.MazeState;
import io.github.sportne.mazegame.model.MouseRunResult;
import io.github.sportne.mazegame.model.MouseRunStatus;
import io.github.sportne.mazegame.model.RandomMouseSimulation;
import io.github.sportne.mazegame.model.WallPlacementResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/** Main libGDX application for Maze Game. */
public final class MazeGame extends ApplicationAdapter {
  private static final Color BACKGROUND = new Color(0.07F, 0.08F, 0.10F, 1.0F);
  private static final Color BUTTON = new Color(0.18F, 0.20F, 0.24F, 1.0F);
  private static final Color BUTTON_BORDER = new Color(0.70F, 0.76F, 0.84F, 1.0F);
  private static final Color CELL_CHEESE = new Color(0.95F, 0.77F, 0.18F, 1.0F);
  private static final Color CELL_OPEN = Color.BLACK;
  private static final Color CELL_REJECTED = new Color(0.95F, 0.42F, 0.42F, 1.0F);
  private static final Color CELL_START = new Color(0.24F, 0.62F, 0.95F, 1.0F);
  private static final Color CELL_WALL = Color.WHITE;
  private static final Color GRID_LINE = new Color(0.28F, 0.31F, 0.36F, 1.0F);
  private static final Color MOUSE = new Color(0.78F, 0.58F, 0.42F, 1.0F);
  private static final Color PANEL_TEXT = new Color(0.62F, 0.70F, 0.78F, 1.0F);
  private static final Color TEXT = new Color(0.88F, 0.92F, 0.96F, 1.0F);
  private static final float RESULT_BUTTON_GAP = 16.0F;
  private static final float RESULT_BUTTON_HEIGHT = 44.0F;
  private static final float RESULT_BUTTON_WIDTH = 140.0F;
  private static final float TITLE_TEXT_Y = 682.0F;
  private static final String ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE = "MAZE_GAME_ASSETS_DIR";
  private static final String BACKGROUND_MUSIC_PATH = "audio/exploreMaze_T1.mp3";
  private static final String PROJECT_BACKGROUND_MUSIC_PATH = "assets/" + BACKGROUND_MUSIC_PATH;
  private static final float BACKGROUND_MUSIC_VOLUME = 0.1F;
  private static final float REJECTED_FLASH_SECONDS = 0.5F;
  private static final String TITLE = "Maze Game";
  private Music backgroundMusic;
  private SpriteBatch spriteBatch;
  private ShapeRenderer shapeRenderer;
  private BitmapFont font;
  private LevelDefinition levelDefinition;
  private MazeState mazeState;
  private float buildTimeRemainingSeconds;
  private GridPosition rejectedPosition;
  private float rejectedFlashRemainingSeconds;
  private boolean runRequested;
  private RandomMouseSimulation mouseSimulation;
  private MouseRunResult mouseRunResult;
  private GamePhase gamePhase;

  public MazeGame() {
    this(null);
  }

  MazeGame(Music backgroundMusic) {
    this.backgroundMusic = backgroundMusic;
    initializeBuildPhase();
  }

  /** Returns the display title used by launchers. */
  public static String title() {
    return TITLE;
  }

  static Color background() {
    return new Color(BACKGROUND);
  }

  static String backgroundMusicPath() {
    return BACKGROUND_MUSIC_PATH;
  }

  static String backgroundMusicPath(String assetsDirectory, String userDirectory) {
    if (assetsDirectory != null && !assetsDirectory.isBlank()) {
      return Path.of(assetsDirectory, BACKGROUND_MUSIC_PATH).toString();
    }
    if (Files.exists(Path.of(userDirectory, BACKGROUND_MUSIC_PATH))) {
      return BACKGROUND_MUSIC_PATH;
    }
    return PROJECT_BACKGROUND_MUSIC_PATH;
  }

  static float backgroundMusicVolume() {
    return BACKGROUND_MUSIC_VOLUME;
  }

  static void configureBackgroundMusic(Music music) {
    music.setLooping(true);
    music.setVolume(BACKGROUND_MUSIC_VOLUME);
  }

  private void initializeBuildPhase() {
    levelDefinition = Levels.milestoneOne();
    mazeState = MazeState.empty(levelDefinition);
    buildTimeRemainingSeconds = levelDefinition.buildTime().toMillis() / 1000.0F;
    rejectedPosition = null;
    rejectedFlashRemainingSeconds = 0.0F;
    runRequested = false;
    mouseSimulation = null;
    mouseRunResult = null;
    gamePhase = GamePhase.BUILDING;
  }

  @Override
  public void create() {
    initializeBuildPhase();
    spriteBatch = new SpriteBatch();
    shapeRenderer = new ShapeRenderer();
    font = new BitmapFont();
    font.setColor(TEXT);
    backgroundMusic = Gdx.audio.newMusic(backgroundMusicFile());
    configureBackgroundMusic(backgroundMusic);
    backgroundMusic.play();
    Gdx.input.setInputProcessor(new BuildInputProcessor());
  }

  @Override
  public void render() {
    updateGame(Gdx.graphics.getDeltaTime());
    ScreenUtils.clear(background());
    BuildPhaseLayout layout =
        BuildPhaseLayout.centered(
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), levelDefinition.gridSize());
    drawGrid(layout.gridBounds());
    drawMouse(layout.gridBounds());
    drawControls(layout);
    drawText(layout);
  }

  @Override
  public void resize(int width, int height) {
    if (spriteBatch != null) {
      spriteBatch.getProjectionMatrix().setToOrtho2D(0.0F, 0.0F, (float) width, (float) height);
    }
    if (shapeRenderer != null) {
      shapeRenderer.getProjectionMatrix().setToOrtho2D(0.0F, 0.0F, (float) width, (float) height);
    }
  }

  @Override
  public void dispose() {
    if (Gdx.input != null) {
      Gdx.input.setInputProcessor(null);
    }
    if (backgroundMusic != null) {
      backgroundMusic.stop();
      backgroundMusic.dispose();
      backgroundMusic = null;
    }
    if (font != null) {
      font.dispose();
      font = null;
    }
    if (shapeRenderer != null) {
      shapeRenderer.dispose();
      shapeRenderer = null;
    }
    if (spriteBatch != null) {
      spriteBatch.dispose();
      spriteBatch = null;
    }
  }

  boolean runRequested() {
    return runRequested;
  }

  GamePhase gamePhase() {
    return gamePhase;
  }

  MazeState mazeState() {
    return mazeState;
  }

  MouseRunResult mouseRunResult() {
    return mouseRunResult;
  }

  float buildTimeRemainingSeconds() {
    return buildTimeRemainingSeconds;
  }

  void updateGame(float deltaSeconds) {
    if (gamePhase == GamePhase.BUILDING) {
      updateBuildTimer(deltaSeconds);
    } else {
      updateMouseRun(deltaSeconds);
    }
  }

  GridPosition rejectedPosition() {
    return rejectedPosition;
  }

  void updateBuildTimer(float deltaSeconds) {
    if (gamePhase != GamePhase.BUILDING) {
      return;
    }
    buildTimeRemainingSeconds = Math.max(0.0F, buildTimeRemainingSeconds - deltaSeconds);
    if (rejectedFlashRemainingSeconds > 0.0F) {
      rejectedFlashRemainingSeconds = Math.max(0.0F, rejectedFlashRemainingSeconds - deltaSeconds);
      if (rejectedFlashRemainingSeconds == 0.0F) {
        rejectedPosition = null;
      }
    }
    if (buildTimeRemainingSeconds == 0.0F) {
      startRun();
    }
  }

  void startRun() {
    if (gamePhase != GamePhase.BUILDING) {
      return;
    }
    runRequested = true;
    gamePhase = GamePhase.MOUSE_RUNNING;
    rejectedPosition = null;
    rejectedFlashRemainingSeconds = 0.0F;
    mouseSimulation = new RandomMouseSimulation(mazeState);
    mouseRunResult = mouseSimulation.result();
  }

  void handleGridClick(GridPosition position, int button) {
    if (gamePhase != GamePhase.BUILDING) {
      return;
    }
    if (button == Input.Buttons.LEFT) {
      WallPlacementResult result = mazeState.placeWall(position);
      if (result.accepted()) {
        mazeState = result.mazeState();
      } else {
        rejectedPosition = position;
        rejectedFlashRemainingSeconds = REJECTED_FLASH_SECONDS;
      }
    } else if (button == Input.Buttons.RIGHT) {
      mazeState = mazeState.withoutWall(position);
    }
  }

  void retryLevel() {
    initializeBuildPhase();
  }

  void replayRun() {
    if (gamePhase != GamePhase.RESULT) {
      return;
    }
    gamePhase = GamePhase.REPLAY;
    runRequested = true;
    mouseSimulation = new RandomMouseSimulation(mazeState);
    mouseRunResult = mouseSimulation.result();
  }

  boolean resultPassed() {
    if (gamePhase != GamePhase.RESULT || mouseRunResult == null) {
      return false;
    }
    return mouseRunResult.elapsedTime().compareTo(levelDefinition.targetSolveTime()) > 0;
  }

  boolean hasNextLevel() {
    return false;
  }

  private void drawGrid(GridBounds gridBounds) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    for (int row = 0; row < levelDefinition.gridSize().rows(); row++) {
      for (int column = 0; column < levelDefinition.gridSize().columns(); column++) {
        GridPosition position = new GridPosition(row, column);
        shapeRenderer.setColor(cellColor(position));
        shapeRenderer.rect(
            gridBounds.x() + column * gridBounds.cellSize(),
            gridBounds.y() + (levelDefinition.gridSize().rows() - 1 - row) * gridBounds.cellSize(),
            gridBounds.cellSize(),
            gridBounds.cellSize());
      }
    }
    shapeRenderer.setColor(GRID_LINE);
    for (int row = 0; row <= levelDefinition.gridSize().rows(); row++) {
      shapeRenderer.rectLine(
          gridBounds.x(),
          gridBounds.y() + row * gridBounds.cellSize(),
          gridBounds.x() + gridBounds.width(),
          gridBounds.y() + row * gridBounds.cellSize(),
          1.0F);
    }
    for (int column = 0; column <= levelDefinition.gridSize().columns(); column++) {
      shapeRenderer.rectLine(
          gridBounds.x() + column * gridBounds.cellSize(),
          gridBounds.y(),
          gridBounds.x() + column * gridBounds.cellSize(),
          gridBounds.y() + gridBounds.height(),
          1.0F);
    }
    shapeRenderer.end();
  }

  private void drawMouse(GridBounds gridBounds) {
    if (mouseRunResult == null) {
      return;
    }
    GridPosition position = mouseRunResult.position();
    float centerX =
        gridBounds.x() + position.column() * gridBounds.cellSize() + gridBounds.cellSize() / 2.0F;
    float centerY =
        gridBounds.y()
            + (levelDefinition.gridSize().rows() - 1 - position.row()) * gridBounds.cellSize()
            + gridBounds.cellSize() / 2.0F;
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(MOUSE);
    shapeRenderer.circle(centerX, centerY, gridBounds.cellSize() * 0.22F);
    shapeRenderer.end();
  }

  Color cellColor(GridPosition position) {
    if (position.equals(rejectedPosition) && rejectedFlashRemainingSeconds > 0.0F) {
      return CELL_REJECTED;
    }
    CellContent content = mazeState.cellContentAt(position);
    return switch (content) {
      case EMPTY -> CELL_OPEN;
      case NORMAL_WALL -> CELL_WALL;
      case MOUSE_START -> CELL_START;
      case CHEESE -> CELL_CHEESE;
    };
  }

  private void drawControls(BuildPhaseLayout layout) {
    if (gamePhase == GamePhase.BUILDING) {
      drawButton(layout.startButtonBounds());
    } else if (gamePhase == GamePhase.RESULT) {
      drawButton(retryButtonBounds(layout));
      drawButton(replayButtonBounds(layout));
    }
  }

  private void drawButton(ButtonBounds bounds) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(BUTTON);
    shapeRenderer.rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    shapeRenderer.setColor(BUTTON_BORDER);
    shapeRenderer.rectLine(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y(), 2.0F);
    shapeRenderer.rectLine(
        bounds.x(),
        bounds.y() + bounds.height(),
        bounds.x() + bounds.width(),
        bounds.y() + bounds.height(),
        2.0F);
    shapeRenderer.rectLine(bounds.x(), bounds.y(), bounds.x(), bounds.y() + bounds.height(), 2.0F);
    shapeRenderer.rectLine(
        bounds.x() + bounds.width(),
        bounds.y(),
        bounds.x() + bounds.width(),
        bounds.y() + bounds.height(),
        2.0F);
    shapeRenderer.end();
  }

  private void drawText(BuildPhaseLayout layout) {
    spriteBatch.begin();
    if (gamePhase == GamePhase.BUILDING) {
      font.setColor(TEXT);
      font.draw(spriteBatch, "Maze Game", layout.gridBounds().x(), TITLE_TEXT_Y);
      font.draw(
          spriteBatch,
          "Build: " + String.format(Locale.ROOT, "%.1fs", buildTimeRemainingSeconds),
          layout.gridBounds().x(),
          layout.gridBounds().y() + layout.gridBounds().height() + 32.0F);
      font.setColor(PANEL_TEXT);
      font.draw(
          spriteBatch,
          "Left click: wall   Right click: clear",
          layout.gridBounds().x(),
          layout.gridBounds().y() - 16.0F);
      font.setColor(TEXT);
      font.draw(
          spriteBatch,
          "Start Mouse",
          layout.startButtonBounds().x() + 44.0F,
          layout.startButtonBounds().y() + 28.0F);
    } else if (gamePhase == GamePhase.RESULT) {
      font.setColor(TEXT);
      font.draw(
          spriteBatch,
          resultPassed() ? "Pass" : "Fail",
          layout.gridBounds().x(),
          layout.gridBounds().y() + layout.gridBounds().height() + 48.0F);
      font.draw(
          spriteBatch,
          "Time: "
              + String.format(
                  Locale.ROOT, "%.2fs", mouseRunResult.elapsedTime().toMillis() / 1000.0F)
              + "  Moves: "
              + mouseRunResult.moveCount(),
          layout.gridBounds().x(),
          layout.gridBounds().y() + layout.gridBounds().height() + 24.0F);
      font.draw(
          spriteBatch,
          "Retry",
          retryButtonBounds(layout).x() + 46.0F,
          retryButtonBounds(layout).y() + 28.0F);
      font.draw(
          spriteBatch,
          "Replay",
          replayButtonBounds(layout).x() + 42.0F,
          replayButtonBounds(layout).y() + 28.0F);
      if (!hasNextLevel()) {
        font.setColor(PANEL_TEXT);
        font.draw(
            spriteBatch,
            "No next level in this milestone",
            layout.gridBounds().x(),
            layout.gridBounds().y() - 16.0F);
      }
    }
    font.setColor(TEXT);
    spriteBatch.end();
  }

  private static ButtonBounds retryButtonBounds(BuildPhaseLayout layout) {
    float left =
        layout.gridBounds().x()
            + layout.gridBounds().width() / 2.0F
            - RESULT_BUTTON_WIDTH
            - RESULT_BUTTON_GAP / 2.0F;
    return new ButtonBounds(
        left, layout.startButtonBounds().y(), RESULT_BUTTON_WIDTH, RESULT_BUTTON_HEIGHT);
  }

  private static ButtonBounds replayButtonBounds(BuildPhaseLayout layout) {
    float left =
        layout.gridBounds().x() + layout.gridBounds().width() / 2.0F + RESULT_BUTTON_GAP / 2.0F;
    return new ButtonBounds(
        left, layout.startButtonBounds().y(), RESULT_BUTTON_WIDTH, RESULT_BUTTON_HEIGHT);
  }

  void updateMouseRun(float deltaSeconds) {
    if ((gamePhase != GamePhase.MOUSE_RUNNING && gamePhase != GamePhase.REPLAY)
        || mouseSimulation == null
        || mouseRunResult == null
        || mouseRunResult.status() != MouseRunStatus.RUNNING) {
      return;
    }
    long deltaMillis = Math.max(0L, Math.round(deltaSeconds * 1000.0F));
    mouseRunResult = mouseSimulation.update(Duration.ofMillis(deltaMillis));
    if (mouseRunResult.status() != MouseRunStatus.RUNNING) {
      gamePhase = GamePhase.RESULT;
    }
  }

  private static FileHandle backgroundMusicFile() {
    String path =
        backgroundMusicPath(
            System.getenv(ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE), System.getProperty("user.dir"));
    if (Path.of(path).isAbsolute()) {
      return Gdx.files.absolute(path);
    }
    return Gdx.files.internal(path);
  }

  private final class BuildInputProcessor extends InputAdapter {
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
      BuildPhaseLayout layout =
          BuildPhaseLayout.centered(
              Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), levelDefinition.gridSize());
      if (gamePhase == GamePhase.BUILDING
          && button == Input.Buttons.LEFT
          && layout.startButtonContains(screenX, screenY, Gdx.graphics.getHeight())) {
        startRun();
        return true;
      }
      if (button == Input.Buttons.LEFT && gamePhase == GamePhase.RESULT) {
        float screenHeight = Gdx.graphics.getHeight();
        if (retryButtonBounds(layout).contains(screenX, screenHeight - screenY)) {
          retryLevel();
          return true;
        }
        if (replayButtonBounds(layout).contains(screenX, screenHeight - screenY)) {
          replayRun();
          return true;
        }
      }
      Optional<GridPosition> position =
          layout.gridPositionAt(screenX, screenY, Gdx.graphics.getHeight());
      if (position.isPresent()) {
        handleGridClick(position.get(), button);
        return true;
      }
      return false;
    }
  }
}
