package io.github.sportne.mazegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Main libGDX application for Maze Game.
 *
 * <p>This class is the bridge between the immutable core model and the desktop runtime. It owns the
 * current level, maze state, build timer, mouse simulation, simple primitive rendering, sprite
 * rendering, and optional one-frame screenshot capture. The domain rules remain in {@code
 * io.github.sportne.mazegame.model}; this class turns those rules into input handling and drawing.
 */
public final class MazeGame extends ApplicationAdapter {
  /** Background clear color for every frame. */
  private static final Color BACKGROUND = new Color(0.07F, 0.08F, 0.10F, 1.0F);

  /** Fill color for simple rectangle buttons. */
  private static final Color BUTTON = new Color(0.18F, 0.20F, 0.24F, 1.0F);

  /** Border color for simple rectangle buttons. */
  private static final Color BUTTON_BORDER = new Color(0.70F, 0.76F, 0.84F, 1.0F);

  /** Fill color for empty walkable cells. */
  private static final Color CELL_OPEN = Color.BLACK;

  /** Temporary fill color for rejected wall placements. */
  private static final Color CELL_REJECTED = new Color(0.95F, 0.42F, 0.42F, 1.0F);

  /** Fill color for the mouse start cell before the mouse sprite is active. */
  private static final Color CELL_START = new Color(0.24F, 0.62F, 0.95F, 1.0F);

  /** Fill color for player-placed wall cells. */
  private static final Color CELL_WALL = Color.WHITE;

  /** Grid line color drawn over cell fills. */
  private static final Color GRID_LINE = new Color(0.28F, 0.31F, 0.36F, 1.0F);

  /** Secondary text color for instructions and non-primary result messages. */
  private static final Color PANEL_TEXT = new Color(0.62F, 0.70F, 0.78F, 1.0F);

  /** Primary text color. */
  private static final Color TEXT = new Color(0.88F, 0.92F, 0.96F, 1.0F);

  /** Horizontal space between result-phase buttons. */
  private static final float RESULT_BUTTON_GAP = 16.0F;

  /** Result button height in pixels. */
  private static final float RESULT_BUTTON_HEIGHT = 44.0F;

  /** Result button width in pixels. */
  private static final float RESULT_BUTTON_WIDTH = 140.0F;

  /** Shared menu button height in virtual pixels. */
  private static final float MENU_BUTTON_HEIGHT = 52.0F;

  /** Shared menu button width in virtual pixels. */
  private static final float MENU_BUTTON_WIDTH = 220.0F;

  /** Vertical space between stacked menu buttons. */
  private static final float MENU_BUTTON_GAP = 18.0F;

  /** Level-select card height in virtual pixels. */
  private static final float LEVEL_BUTTON_HEIGHT = 88.0F;

  /** Level-select card width in virtual pixels. */
  private static final float LEVEL_BUTTON_WIDTH = 220.0F;

  /** Horizontal and vertical gap between level-select cards. */
  private static final float LEVEL_BUTTON_GAP = 24.0F;

  /** Shared back button height in virtual pixels. */
  private static final float BACK_BUTTON_HEIGHT = 44.0F;

  /** Shared back button width in virtual pixels. */
  private static final float BACK_BUTTON_WIDTH = 140.0F;

  /** Baseline y coordinate for the title at the default desktop size. */
  private static final float TITLE_TEXT_Y = 682.0F;

  /** Environment variable that can point the app at an external asset directory. */
  private static final String ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE = "MAZE_GAME_ASSETS_DIR";

  /** Asset-relative path for background music. */
  private static final String BACKGROUND_MUSIC_PATH = "audio/exploreMaze_T1.mp3";

  /** Project-relative fallback for background music when the working directory is not assets/. */
  private static final String PROJECT_BACKGROUND_MUSIC_PATH = "assets/" + BACKGROUND_MUSIC_PATH;

  /** Asset-relative path for the mouse and cheese sprite sheet. */
  private static final String SPRITE_SHEET_PATH = "mouse-sprites.png";

  /** Project-relative fallback for the sprite sheet when the working directory is not assets/. */
  private static final String PROJECT_SPRITE_SHEET_PATH = "assets/" + SPRITE_SHEET_PATH;

  /** Fraction of a cell occupied by centered sprites. */
  private static final float CELL_SPRITE_SCALE = 0.90F;

  /** Quiet default music volume. */
  private static final float BACKGROUND_MUSIC_VOLUME = 0.1F;

  /** Duration of rejected-placement visual feedback. */
  private static final float REJECTED_FLASH_SECONDS = 0.5F;

  /** Desktop window title and in-game title text. */
  private static final String TITLE = "Maze Game";

  /** Width of the stable virtual coordinate system used for rendering and input. */
  private static final int VIRTUAL_WIDTH = 1280;

  /** Height of the stable virtual coordinate system used for rendering and input. */
  private static final int VIRTUAL_HEIGHT = 720;

  /** Optional one-frame screenshot request supplied by the launcher. */
  private final ScreenshotCapture screenshotCapture;

  /** Whether the backend audio system is available for this run. */
  private final boolean audioAvailable;

  /** Hook used by the quit menu action. */
  private final Runnable exitAction;

  /** Currently playing background music instance. */
  private Music backgroundMusic;

  /** Sprite batch used for fonts and sprite sheet regions. */
  private SpriteBatch spriteBatch;

  /** Primitive renderer used for cells, grid lines, and buttons. */
  private ShapeRenderer shapeRenderer;

  /** Default libGDX bitmap font used by the simple UI. */
  private BitmapFont font;

  /** Viewport that preserves the virtual 1280x720 game canvas during window resizes. */
  private Viewport viewport;

  /** Texture loaded from the mouse/cheese sprite sheet asset. */
  private Texture spriteSheet;

  /** Cropped cheese sprite drawn over the endpoint cell. */
  private TextureRegion cheeseSprite;

  /** Cropped mouse sprite drawn at the current mouse position. */
  private TextureRegion mouseSprite;

  /** Current level definition. */
  private LevelDefinition levelDefinition;

  /** Current immutable maze layout. */
  private MazeState mazeState;

  /** Seconds remaining before the mouse starts automatically. */
  private float buildTimeRemainingSeconds;

  /** Cell currently flashing as a rejected placement, or null when no flash is active. */
  private GridPosition rejectedPosition;

  /** Seconds remaining in the rejected-placement flash. */
  private float rejectedFlashRemainingSeconds;

  /** Whether a run has been requested or auto-started for the current level attempt. */
  private boolean runRequested;

  /** Active deterministic mouse simulation, or null before a run starts. */
  private RandomMouseSimulation mouseSimulation;

  /** Latest mouse simulation snapshot, or null before a run starts. */
  private MouseRunResult mouseRunResult;

  /** Current high-level game phase. */
  private GamePhase gamePhase;

  /** Whether session audio is currently enabled. */
  private boolean audioEnabled;

  /** Whether the optional screenshot request has already been fulfilled. */
  private boolean screenshotCaptured;

  /** Accumulated rendered time used to support delayed screenshot capture. */
  private float screenshotElapsedSeconds;

  /** Creates the game without screenshot capture. */
  public MazeGame() {
    this(null, true);
  }

  /**
   * Creates the game with an optional screenshot request.
   *
   * @param screenshotCapture screenshot request to fulfill after rendering, or null
   */
  public MazeGame(ScreenshotCapture screenshotCapture) {
    this(screenshotCapture, true);
  }

  /**
   * Creates the game with an optional screenshot request and audio availability.
   *
   * @param screenshotCapture screenshot request to fulfill after rendering, or null
   * @param audioAvailable true when the backend audio system should be used
   */
  public MazeGame(ScreenshotCapture screenshotCapture, boolean audioAvailable) {
    this(null, screenshotCapture, audioAvailable, MazeGame::requestApplicationExit);
  }

  /**
   * Creates the game with injected music for tests.
   *
   * @param backgroundMusic music instance to dispose when the game is disposed
   */
  MazeGame(Music backgroundMusic) {
    this(backgroundMusic, null, true, MazeGame::requestApplicationExit);
  }

  /**
   * Creates the game with test/runtime dependencies.
   *
   * @param backgroundMusic optional injected music
   * @param screenshotCapture optional screenshot capture request
   * @param audioAvailable true when audio can be toggled on
   * @param exitAction action to invoke for the Quit menu command
   */
  MazeGame(
      Music backgroundMusic,
      ScreenshotCapture screenshotCapture,
      boolean audioAvailable,
      Runnable exitAction) {
    this.backgroundMusic = backgroundMusic;
    this.screenshotCapture = screenshotCapture;
    this.audioAvailable = audioAvailable;
    this.audioEnabled = audioAvailable;
    this.exitAction = Objects.requireNonNull(exitAction, "exitAction");
    initializeMainMenu();
  }

  /** Requests a libGDX application exit when the backend is available. */
  private static void requestApplicationExit() {
    if (Gdx.app != null) {
      Gdx.app.exit();
    }
  }

  /**
   * Returns the display title used by launchers.
   *
   * @return desktop window title
   */
  public static String title() {
    return TITLE;
  }

  /**
   * Returns a copy of the frame clear color.
   *
   * @return background color for the renderer
   */
  static Color background() {
    return new Color(BACKGROUND);
  }

  /**
   * Returns the asset-relative background music path.
   *
   * @return default music asset path
   */
  static String backgroundMusicPath() {
    return BACKGROUND_MUSIC_PATH;
  }

  /**
   * Resolves the background music path for the current runtime environment.
   *
   * @param assetsDirectory optional explicit assets directory
   * @param userDirectory process working directory
   * @return absolute, asset-relative, or project-relative path to the music file
   */
  static String backgroundMusicPath(String assetsDirectory, String userDirectory) {
    return assetPath(
        assetsDirectory, userDirectory, BACKGROUND_MUSIC_PATH, PROJECT_BACKGROUND_MUSIC_PATH);
  }

  /**
   * Returns the asset-relative sprite sheet path.
   *
   * @return default sprite sheet asset path
   */
  static String spriteSheetPath() {
    return SPRITE_SHEET_PATH;
  }

  /**
   * Resolves the sprite sheet path for the current runtime environment.
   *
   * @param assetsDirectory optional explicit assets directory
   * @param userDirectory process working directory
   * @return absolute, asset-relative, or project-relative path to the sprite sheet
   */
  static String spriteSheetPath(String assetsDirectory, String userDirectory) {
    return assetPath(assetsDirectory, userDirectory, SPRITE_SHEET_PATH, PROJECT_SPRITE_SHEET_PATH);
  }

  /**
   * Resolves an asset using the app's environment/working-directory fallback order.
   *
   * @param assetsDirectory optional explicit assets directory
   * @param userDirectory process working directory
   * @param assetPath path relative to an assets directory
   * @param projectAssetPath project-relative fallback path
   * @return path suitable for libGDX file lookup
   */
  private static String assetPath(
      String assetsDirectory, String userDirectory, String assetPath, String projectAssetPath) {
    if (assetsDirectory != null && !assetsDirectory.isBlank()) {
      return Path.of(assetsDirectory, assetPath).toString();
    }
    if (Files.exists(Path.of(userDirectory, assetPath))) {
      return assetPath;
    }
    return projectAssetPath;
  }

  /**
   * Returns the configured startup music volume.
   *
   * @return volume from 0.0 to 1.0
   */
  static float backgroundMusicVolume() {
    return BACKGROUND_MUSIC_VOLUME;
  }

  /**
   * Applies looping and volume settings to the background music.
   *
   * @param music music instance created by libGDX
   */
  static void configureBackgroundMusic(Music music) {
    music.setLooping(true);
    music.setVolume(BACKGROUND_MUSIC_VOLUME);
  }

  /** Creates, configures, and starts background music when audio is available. */
  private void startBackgroundMusic() {
    if (!audioAvailable) {
      return;
    }
    if (backgroundMusic == null && Gdx.audio != null) {
      backgroundMusic = Gdx.audio.newMusic(backgroundMusicFile());
    }
    if (backgroundMusic != null) {
      configureBackgroundMusic(backgroundMusic);
      backgroundMusic.play();
    }
  }

  /**
   * Resets all model state and enters the startup menu.
   *
   * <p>The level model is still initialized so menu rendering, debug snapshots, and tests can read
   * stable milestone-one defaults before the player starts a level.
   */
  private void initializeMainMenu() {
    initializeLevelState(GamePhase.MAIN_MENU);
  }

  /** Resets all model and phase state for a fresh attempt of the first level. */
  void startMilestoneOneLevel() {
    initializeLevelState(GamePhase.BUILDING);
  }

  /**
   * Resets all model state and moves to the requested phase.
   *
   * @param initialPhase phase to enter after resetting level state
   */
  private void initializeLevelState(GamePhase initialPhase) {
    levelDefinition = Levels.milestoneOne();
    mazeState = MazeState.empty(levelDefinition);
    buildTimeRemainingSeconds = levelDefinition.buildTime().toMillis() / 1000.0F;
    rejectedPosition = null;
    rejectedFlashRemainingSeconds = 0.0F;
    runRequested = false;
    mouseSimulation = null;
    mouseRunResult = null;
    gamePhase = initialPhase;
    screenshotElapsedSeconds = 0.0F;
  }

  /**
   * Creates libGDX resources after the backend is initialized.
   *
   * <p>libGDX objects such as textures and fonts require an active application context, so they are
   * loaded here rather than in the constructor.
   */
  @Override
  public void create() {
    initializeMainMenu();
    spriteBatch = new SpriteBatch();
    shapeRenderer = new ShapeRenderer();
    viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    font = new BitmapFont();
    font.setColor(TEXT);
    spriteSheet = new Texture(spriteSheetFile());
    spriteSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    cheeseSprite = new TextureRegion(spriteSheet, 1168, 819, 186, 145);
    mouseSprite = new TextureRegion(spriteSheet, 718, 671, 325, 416);
    if (audioEnabled) {
      startBackgroundMusic();
    }
    Gdx.input.setInputProcessor(new BuildInputProcessor());
  }

  /** Advances game state, draws one frame, and fulfills screenshot capture when requested. */
  @Override
  public void render() {
    updateGame(Gdx.graphics.getDeltaTime());
    ScreenUtils.clear(background());
    viewport.apply();
    updateProjectionMatrices();
    if (gamePhase == GamePhase.MAIN_MENU) {
      drawMainMenu();
      captureScreenshotIfRequested(Gdx.graphics.getDeltaTime());
      return;
    }
    if (gamePhase == GamePhase.LEVEL_SELECT) {
      drawLevelSelect();
      captureScreenshotIfRequested(Gdx.graphics.getDeltaTime());
      return;
    }
    if (gamePhase == GamePhase.SETTINGS) {
      drawSettings();
      captureScreenshotIfRequested(Gdx.graphics.getDeltaTime());
      return;
    }
    BuildPhaseLayout layout =
        BuildPhaseLayout.centered(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, levelDefinition.gridSize());
    drawGrid(layout.gridBounds());
    drawCellSprites(layout.gridBounds());
    drawMouse(layout.gridBounds());
    drawControls(layout);
    drawText(layout);
    captureScreenshotIfRequested(Gdx.graphics.getDeltaTime());
  }

  /**
   * Updates projection matrices when the desktop window changes size.
   *
   * @param width new window width in pixels
   * @param height new window height in pixels
   */
  @Override
  public void resize(int width, int height) {
    if (viewport != null) {
      viewport.update(width, height, true);
      updateProjectionMatrices();
    }
  }

  /** Releases libGDX resources and clears the input processor. */
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
    viewport = null;
    if (spriteSheet != null) {
      spriteSheet.dispose();
      spriteSheet = null;
      cheeseSprite = null;
      mouseSprite = null;
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

  /**
   * Returns whether the mouse run has been started for the current attempt.
   *
   * @return true after manual or automatic run start
   */
  boolean runRequested() {
    return runRequested;
  }

  /**
   * Returns the current game phase.
   *
   * @return active phase
   */
  GamePhase gamePhase() {
    return gamePhase;
  }

  /**
   * Returns the current immutable maze state.
   *
   * @return current maze
   */
  MazeState mazeState() {
    return mazeState;
  }

  /**
   * Returns the current mouse run snapshot.
   *
   * @return latest run result, or null before the mouse starts
   */
  MouseRunResult mouseRunResult() {
    return mouseRunResult;
  }

  /**
   * Returns build time remaining.
   *
   * @return seconds left before automatic run start
   */
  float buildTimeRemainingSeconds() {
    return buildTimeRemainingSeconds;
  }

  /**
   * Returns whether session audio is currently enabled.
   *
   * @return true when settings allow music playback
   */
  boolean audioEnabled() {
    return audioEnabled;
  }

  /** Opens the level-select menu from the startup menu. */
  void openLevelSelect() {
    if (gamePhase == GamePhase.MAIN_MENU) {
      gamePhase = GamePhase.LEVEL_SELECT;
    }
  }

  /** Opens the settings menu from the startup menu. */
  void openSettings() {
    if (gamePhase == GamePhase.MAIN_MENU) {
      gamePhase = GamePhase.SETTINGS;
    }
  }

  /** Returns to the startup menu and clears any in-progress level attempt. */
  void returnToMainMenu() {
    initializeMainMenu();
  }

  /** Toggles session audio when the backend audio system is available. */
  void toggleAudio() {
    if (!audioAvailable) {
      audioEnabled = false;
      return;
    }
    audioEnabled = !audioEnabled;
    if (audioEnabled) {
      startBackgroundMusic();
    } else if (backgroundMusic != null) {
      backgroundMusic.stop();
    }
  }

  /**
   * Advances the active phase by a frame delta.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  void updateGame(float deltaSeconds) {
    if (gamePhase == GamePhase.BUILDING) {
      updateBuildTimer(deltaSeconds);
    } else if (gamePhase == GamePhase.MOUSE_RUNNING || gamePhase == GamePhase.REPLAY) {
      updateMouseRun(deltaSeconds);
    }
  }

  /**
   * Returns the cell currently shown as a rejected placement.
   *
   * @return rejected cell, or null when no rejection flash is active
   */
  GridPosition rejectedPosition() {
    return rejectedPosition;
  }

  /**
   * Advances the build timer and starts the mouse when it reaches zero.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
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

  /** Starts the mouse run from the current maze if the player is still building. */
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

  /**
   * Applies a grid click to the current maze.
   *
   * @param position clicked grid cell
   * @param button libGDX mouse button code
   */
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

  /**
   * Handles a desktop mouse click in top-left input coordinates.
   *
   * @param screenX x coordinate from the left edge of the window
   * @param screenY y coordinate from the top edge of the window
   * @param button libGDX mouse button code
   * @param screenWidth current window width in pixels
   * @param screenHeight current window height in pixels
   * @return true when the click was consumed by a cell or control
   */
  boolean handleScreenClick(
      int screenX, int screenY, int button, int screenWidth, int screenHeight) {
    float screenYFromBottom = screenHeight - screenY;
    if (button == Input.Buttons.LEFT && gamePhase == GamePhase.MAIN_MENU) {
      if (mainMenuStartButtonBounds(screenWidth, screenHeight)
          .contains(screenX, screenYFromBottom)) {
        gamePhase = GamePhase.LEVEL_SELECT;
        return true;
      }
      if (mainMenuSettingsButtonBounds(screenWidth, screenHeight)
          .contains(screenX, screenYFromBottom)) {
        gamePhase = GamePhase.SETTINGS;
        return true;
      }
      if (mainMenuQuitButtonBounds(screenWidth, screenHeight)
          .contains(screenX, screenYFromBottom)) {
        exitAction.run();
        return true;
      }
    }
    if (button == Input.Buttons.LEFT && gamePhase == GamePhase.LEVEL_SELECT) {
      if (levelSelectBackButtonBounds(screenWidth, screenHeight)
          .contains(screenX, screenYFromBottom)) {
        gamePhase = GamePhase.MAIN_MENU;
        return true;
      }
      for (int index = 0; index < 6; index++) {
        if (levelButtonBounds(screenWidth, screenHeight, index)
            .contains(screenX, screenYFromBottom)) {
          if (index == 0) {
            startMilestoneOneLevel();
          }
          return true;
        }
      }
    }
    if (button == Input.Buttons.LEFT && gamePhase == GamePhase.SETTINGS) {
      if (settingsBackButtonBounds(screenWidth, screenHeight)
          .contains(screenX, screenYFromBottom)) {
        gamePhase = GamePhase.MAIN_MENU;
        return true;
      }
      if (settingsAudioButtonBounds(screenWidth, screenHeight)
          .contains(screenX, screenYFromBottom)) {
        toggleAudio();
        return true;
      }
    }
    BuildPhaseLayout layout =
        BuildPhaseLayout.centered(screenWidth, screenHeight, levelDefinition.gridSize());
    if (gamePhase == GamePhase.BUILDING
        && button == Input.Buttons.LEFT
        && layout.startButtonContains(screenX, screenY, screenHeight)) {
      startRun();
      return true;
    }
    if (button == Input.Buttons.LEFT && gamePhase == GamePhase.RESULT) {
      if (retryButtonBounds(layout).contains(screenX, screenYFromBottom)) {
        retryLevel();
        return true;
      }
      if (replayButtonBounds(layout).contains(screenX, screenYFromBottom)) {
        replayRun();
        return true;
      }
      if (resultMainMenuButtonBounds(layout).contains(screenX, screenYFromBottom)) {
        returnToMainMenu();
        return true;
      }
    }
    Optional<GridPosition> position =
        gamePhase == GamePhase.BUILDING
            ? layout.gridPositionAt(screenX, screenY, screenHeight)
            : Optional.empty();
    if (position.isPresent()) {
      handleGridClick(position.get(), button);
      return true;
    }
    return false;
  }

  /** Resets the current level to a fresh build phase attempt. */
  void retryLevel() {
    startMilestoneOneLevel();
  }

  /** Replays the completed maze from the same deterministic seed. */
  void replayRun() {
    if (gamePhase != GamePhase.RESULT) {
      return;
    }
    gamePhase = GamePhase.REPLAY;
    runRequested = true;
    mouseSimulation = new RandomMouseSimulation(mazeState);
    mouseRunResult = mouseSimulation.result();
  }

  /**
   * Returns whether the completed result passed the level target.
   *
   * @return true when result phase is active and elapsed solve time exceeded the target
   */
  boolean resultPassed() {
    if (gamePhase != GamePhase.RESULT || mouseRunResult == null) {
      return false;
    }
    return mouseRunResult.elapsedTime().compareTo(levelDefinition.targetSolveTime()) > 0;
  }

  /**
   * Returns whether another level can be selected after this result.
   *
   * @return false for milestone 1 because only one level exists
   */
  boolean hasNextLevel() {
    return false;
  }

  /** Draws the startup menu. */
  private void drawMainMenu() {
    drawButton(mainMenuStartButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
    drawButton(mainMenuSettingsButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
    drawButton(mainMenuQuitButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

    spriteBatch.begin();
    font.setColor(TEXT);
    font.draw(spriteBatch, TITLE, VIRTUAL_WIDTH / 2.0F - 46.0F, 520.0F);
    font.draw(
        spriteBatch,
        "Start",
        mainMenuStartButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).x() + 90.0F,
        mainMenuStartButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).y() + 32.0F);
    font.draw(
        spriteBatch,
        "Settings",
        mainMenuSettingsButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).x() + 78.0F,
        mainMenuSettingsButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).y() + 32.0F);
    font.draw(
        spriteBatch,
        "Quit",
        mainMenuQuitButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).x() + 94.0F,
        mainMenuQuitButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).y() + 32.0F);
    spriteBatch.end();
  }

  /** Draws the level-select menu. */
  private void drawLevelSelect() {
    for (int index = 0; index < 6; index++) {
      drawButton(levelButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, index));
    }
    drawButton(levelSelectBackButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

    spriteBatch.begin();
    font.setColor(TEXT);
    font.draw(spriteBatch, "Select Level", VIRTUAL_WIDTH / 2.0F - 58.0F, 560.0F);
    for (int index = 0; index < 6; index++) {
      ButtonBounds levelButton = levelButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, index);
      font.setColor(index == 0 ? TEXT : PANEL_TEXT);
      String title = index == 0 ? "Milestone 1" : "Level " + (index + 1);
      String subtitle = index == 0 ? "5x5" : "Locked";
      font.draw(spriteBatch, title, levelButton.x() + 24.0F, levelButton.y() + 56.0F);
      font.draw(spriteBatch, subtitle, levelButton.x() + 24.0F, levelButton.y() + 32.0F);
    }
    font.setColor(TEXT);
    font.draw(
        spriteBatch,
        "Back",
        levelSelectBackButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).x() + 52.0F,
        levelSelectBackButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).y() + 28.0F);
    spriteBatch.end();
  }

  /** Draws the session settings menu. */
  private void drawSettings() {
    drawButton(settingsAudioButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
    drawButton(settingsBackButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

    spriteBatch.begin();
    font.setColor(TEXT);
    font.draw(spriteBatch, "Settings", VIRTUAL_WIDTH / 2.0F - 42.0F, 520.0F);
    font.draw(
        spriteBatch,
        "Audio: " + (audioEnabled ? "On" : "Off"),
        settingsAudioButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).x() + 62.0F,
        settingsAudioButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).y() + 32.0F);
    font.draw(
        spriteBatch,
        "Back",
        settingsBackButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).x() + 52.0F,
        settingsBackButtonBounds(VIRTUAL_WIDTH, VIRTUAL_HEIGHT).y() + 28.0F);
    spriteBatch.end();
  }

  /**
   * Draws cell fills and grid lines.
   *
   * @param gridBounds rendered grid bounds
   */
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

  /**
   * Draws the mouse sprite at the current simulation position.
   *
   * @param gridBounds rendered grid bounds
   */
  private void drawMouse(GridBounds gridBounds) {
    if (mouseRunResult == null) {
      return;
    }
    drawSpriteInCell(gridBounds, mouseRunResult.position(), mouseSprite);
  }

  /**
   * Draws non-wall sprites that are attached to fixed cells.
   *
   * @param gridBounds rendered grid bounds
   */
  private void drawCellSprites(GridBounds gridBounds) {
    drawSpriteInCell(gridBounds, levelDefinition.cheese(), cheeseSprite);
  }

  /**
   * Draws a sprite centered inside one grid cell while preserving aspect ratio.
   *
   * @param gridBounds rendered grid bounds
   * @param position cell where the sprite should be drawn
   * @param spriteRegion cropped sprite sheet region to draw
   */
  private void drawSpriteInCell(
      GridBounds gridBounds, GridPosition position, TextureRegion spriteRegion) {
    if (spriteRegion == null) {
      return;
    }
    float maxSize = gridBounds.cellSize() * CELL_SPRITE_SCALE;
    float aspectRatio = spriteRegion.getRegionWidth() / (float) spriteRegion.getRegionHeight();
    float width = maxSize;
    float height = maxSize;
    if (aspectRatio > 1.0F) {
      height = maxSize / aspectRatio;
    } else {
      width = maxSize * aspectRatio;
    }
    float cellLeft = gridBounds.x() + position.column() * gridBounds.cellSize();
    float cellBottom =
        gridBounds.y()
            + (levelDefinition.gridSize().rows() - 1 - position.row()) * gridBounds.cellSize();
    spriteBatch.begin();
    spriteBatch.draw(
        spriteRegion,
        cellLeft + (gridBounds.cellSize() - width) / 2.0F,
        cellBottom + (gridBounds.cellSize() - height) / 2.0F,
        width,
        height);
    spriteBatch.end();
  }

  /**
   * Returns the background/fill color for a grid cell.
   *
   * @param position cell to inspect
   * @return color used before any sprite overlay is drawn
   */
  Color cellColor(GridPosition position) {
    if (position.equals(rejectedPosition) && rejectedFlashRemainingSeconds > 0.0F) {
      return CELL_REJECTED;
    }
    CellContent content = mazeState.cellContentAt(position);
    return switch (content) {
      case EMPTY -> CELL_OPEN;
      case NORMAL_WALL -> CELL_WALL;
      case MOUSE_START -> CELL_START;
      case CHEESE -> CELL_OPEN;
    };
  }

  /**
   * Draws phase-appropriate button rectangles.
   *
   * @param layout current screen layout
   */
  private void drawControls(BuildPhaseLayout layout) {
    if (gamePhase == GamePhase.BUILDING) {
      drawButton(layout.startButtonBounds());
    } else if (gamePhase == GamePhase.RESULT) {
      drawButton(retryButtonBounds(layout));
      drawButton(replayButtonBounds(layout));
      drawButton(resultMainMenuButtonBounds(layout));
    }
  }

  /**
   * Draws one simple rectangle button.
   *
   * @param bounds button bounds in bottom-left coordinates
   */
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

  /**
   * Draws all text for the current phase.
   *
   * @param layout current screen layout
   */
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
      font.draw(
          spriteBatch,
          "Main Menu",
          resultMainMenuButtonBounds(layout).x() + 38.0F,
          resultMainMenuButtonBounds(layout).y() + 28.0F);
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

  /**
   * Computes the retry button bounds for the result phase.
   *
   * @param layout current screen layout
   * @return retry button bounds in bottom-left coordinates
   */
  static ButtonBounds retryButtonBounds(BuildPhaseLayout layout) {
    float left =
        layout.gridBounds().x()
            + layout.gridBounds().width() / 2.0F
            - RESULT_BUTTON_WIDTH
            - RESULT_BUTTON_GAP / 2.0F;
    return new ButtonBounds(
        left, layout.startButtonBounds().y(), RESULT_BUTTON_WIDTH, RESULT_BUTTON_HEIGHT);
  }

  /**
   * Computes the replay button bounds for the result phase.
   *
   * @param layout current screen layout
   * @return replay button bounds in bottom-left coordinates
   */
  static ButtonBounds replayButtonBounds(BuildPhaseLayout layout) {
    float left =
        layout.gridBounds().x() + layout.gridBounds().width() / 2.0F + RESULT_BUTTON_GAP / 2.0F;
    return new ButtonBounds(
        left, layout.startButtonBounds().y(), RESULT_BUTTON_WIDTH, RESULT_BUTTON_HEIGHT);
  }

  /**
   * Computes the main-menu button bounds for the result phase.
   *
   * @param layout current screen layout
   * @return main-menu button bounds in bottom-left coordinates
   */
  static ButtonBounds resultMainMenuButtonBounds(BuildPhaseLayout layout) {
    return new ButtonBounds(
        layout.gridBounds().x() + layout.gridBounds().width() / 2.0F - RESULT_BUTTON_WIDTH / 2.0F,
        layout.startButtonBounds().y() - RESULT_BUTTON_HEIGHT - RESULT_BUTTON_GAP,
        RESULT_BUTTON_WIDTH,
        RESULT_BUTTON_HEIGHT);
  }

  /**
   * Computes the Start button bounds for the startup menu.
   *
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @return start button bounds in bottom-left coordinates
   */
  static ButtonBounds mainMenuStartButtonBounds(int screenWidth, int screenHeight) {
    return menuButtonBounds(screenWidth, screenHeight, 0);
  }

  /**
   * Computes the Settings button bounds for the startup menu.
   *
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @return settings button bounds in bottom-left coordinates
   */
  static ButtonBounds mainMenuSettingsButtonBounds(int screenWidth, int screenHeight) {
    return menuButtonBounds(screenWidth, screenHeight, 1);
  }

  /**
   * Computes the Quit button bounds for the startup menu.
   *
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @return quit button bounds in bottom-left coordinates
   */
  static ButtonBounds mainMenuQuitButtonBounds(int screenWidth, int screenHeight) {
    return menuButtonBounds(screenWidth, screenHeight, 2);
  }

  /**
   * Computes stacked startup/settings menu button bounds.
   *
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @param index zero-based button index from top to bottom
   * @return button bounds in bottom-left coordinates
   */
  private static ButtonBounds menuButtonBounds(int screenWidth, int screenHeight, int index) {
    float left = screenWidth / 2.0F - MENU_BUTTON_WIDTH / 2.0F;
    float topButtonY = screenHeight / 2.0F + 54.0F;
    float y = topButtonY - index * (MENU_BUTTON_HEIGHT + MENU_BUTTON_GAP);
    return new ButtonBounds(left, y, MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT);
  }

  /**
   * Computes one level-select card bounds.
   *
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @param index zero-based level card index
   * @return level button bounds in bottom-left coordinates
   */
  static ButtonBounds levelButtonBounds(int screenWidth, int screenHeight, int index) {
    int row = index / 3;
    int column = index % 3;
    float totalWidth = 3.0F * LEVEL_BUTTON_WIDTH + 2.0F * LEVEL_BUTTON_GAP;
    float left = screenWidth / 2.0F - totalWidth / 2.0F;
    float topRowY = screenHeight / 2.0F + 38.0F;
    return new ButtonBounds(
        left + column * (LEVEL_BUTTON_WIDTH + LEVEL_BUTTON_GAP),
        topRowY - row * (LEVEL_BUTTON_HEIGHT + LEVEL_BUTTON_GAP),
        LEVEL_BUTTON_WIDTH,
        LEVEL_BUTTON_HEIGHT);
  }

  /**
   * Computes the level-select Back button bounds.
   *
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @return back button bounds in bottom-left coordinates
   */
  static ButtonBounds levelSelectBackButtonBounds(int screenWidth, int screenHeight) {
    return backButtonBounds();
  }

  /**
   * Computes the settings audio toggle bounds.
   *
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @return audio toggle bounds in bottom-left coordinates
   */
  static ButtonBounds settingsAudioButtonBounds(int screenWidth, int screenHeight) {
    return menuButtonBounds(screenWidth, screenHeight, 0);
  }

  /**
   * Computes the settings Back button bounds.
   *
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @return back button bounds in bottom-left coordinates
   */
  static ButtonBounds settingsBackButtonBounds(int screenWidth, int screenHeight) {
    return backButtonBounds();
  }

  /**
   * Computes shared Back button bounds.
   *
   * @return back button bounds in bottom-left coordinates
   */
  private static ButtonBounds backButtonBounds() {
    return new ButtonBounds(40.0F, 40.0F, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT);
  }

  /**
   * Advances the active mouse simulation.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
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

  /**
   * Returns a libGDX handle for the background music asset.
   *
   * @return file handle resolved through the app's asset fallback rules
   */
  private static FileHandle backgroundMusicFile() {
    String path =
        backgroundMusicPath(
            System.getenv(ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE), System.getProperty("user.dir"));
    return fileHandle(path);
  }

  /**
   * Returns a libGDX handle for the sprite sheet asset.
   *
   * @return file handle resolved through the app's asset fallback rules
   */
  private static FileHandle spriteSheetFile() {
    String path =
        spriteSheetPath(
            System.getenv(ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE), System.getProperty("user.dir"));
    return fileHandle(path);
  }

  /**
   * Converts a path string into the correct libGDX file handle type.
   *
   * @param path absolute or internal path
   * @return absolute handle for absolute paths, internal handle otherwise
   */
  private static FileHandle fileHandle(String path) {
    if (Path.of(path).isAbsolute()) {
      return Gdx.files.absolute(path);
    }
    return Gdx.files.internal(path);
  }

  /** Updates renderers to use the viewport camera projection. */
  private void updateProjectionMatrices() {
    if (viewport == null) {
      return;
    }
    if (spriteBatch != null) {
      spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
    }
    if (shapeRenderer != null) {
      shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
    }
  }

  /**
   * Captures one PNG screenshot after the requested delay has elapsed.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  private void captureScreenshotIfRequested(float deltaSeconds) {
    if (screenshotCapture == null || screenshotCaptured) {
      return;
    }
    screenshotElapsedSeconds += Math.max(0.0F, deltaSeconds);
    float captureDelaySeconds = screenshotCapture.delay().toMillis() / 1000.0F;
    if (screenshotElapsedSeconds < captureDelaySeconds) {
      return;
    }
    screenshotCaptured = true;
    Path outputPath = screenshotCapture.outputPath().toAbsolutePath();
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      int width = Gdx.graphics.getWidth();
      int height = Gdx.graphics.getHeight();
      byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, width, height, true);
      Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
      BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
      try {
        PixmapIO.writePNG(Gdx.files.absolute(outputPath.toString()), pixmap);
      } finally {
        pixmap.dispose();
      }
    } catch (IOException exception) {
      throw new GdxRuntimeException("Failed to capture screenshot to " + outputPath, exception);
    }
  }

  /** Input adapter that forwards desktop clicks into testable screen-click handling. */
  private final class BuildInputProcessor extends InputAdapter {
    /**
     * Handles one mouse-button press from libGDX.
     *
     * @param screenX x coordinate from the left edge of the window
     * @param screenY y coordinate from the top edge of the window
     * @param pointer pointer index supplied by libGDX
     * @param button libGDX mouse button code
     * @return true when the click is consumed
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
      if (viewport == null) {
        return false;
      }
      Vector2 worldPosition = viewport.unproject(new Vector2(screenX, screenY));
      return handleScreenClick(
          Math.round(worldPosition.x),
          Math.round(VIRTUAL_HEIGHT - worldPosition.y),
          button,
          VIRTUAL_WIDTH,
          VIRTUAL_HEIGHT);
    }
  }
}
