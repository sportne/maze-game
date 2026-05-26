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
import io.github.sportne.mazegame.input.GameInputAction;
import io.github.sportne.mazegame.input.GameInputRouter;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.MazeState;
import io.github.sportne.mazegame.model.MouseRunResult;
import io.github.sportne.mazegame.render.GameRenderSnapshot;
import io.github.sportne.mazegame.render.MazeGameRenderer;
import io.github.sportne.mazegame.state.GameSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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

  /** Primary text color. */
  private static final Color TEXT = new Color(0.88F, 0.92F, 0.96F, 1.0F);

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

  /** Quiet default music volume. */
  private static final float BACKGROUND_MUSIC_VOLUME = 0.1F;

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

  /** Current mutable gameplay session. */
  private final GameSession session;

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

  /** Renderer that draws the current frame. */
  private MazeGameRenderer renderer;

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
    this.session = new GameSession();
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

  /** Resets all session state and enters the startup menu. */
  private void initializeMainMenu() {
    session.initializeMainMenu();
    screenshotElapsedSeconds = 0.0F;
  }

  /** Resets all session state for a fresh attempt of the first level. */
  void startMilestoneOneLevel() {
    session.startMilestoneOneLevel();
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
    renderer = new MazeGameRenderer(spriteBatch, shapeRenderer, font, cheeseSprite, mouseSprite);
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
    ScreenLayout layout = screenLayout(gamePhase(), VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
    renderer.render(layout, renderSnapshot());
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
    renderer = null;
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
    return session.runRequested();
  }

  /**
   * Returns the current game phase.
   *
   * @return active phase
   */
  GamePhase gamePhase() {
    return session.gamePhase();
  }

  /**
   * Returns the current immutable maze state.
   *
   * @return current maze
   */
  MazeState mazeState() {
    return session.mazeState();
  }

  /**
   * Returns the current mouse run snapshot.
   *
   * @return latest run result, or null before the mouse starts
   */
  MouseRunResult mouseRunResult() {
    return session.mouseRunResult();
  }

  /**
   * Returns build time remaining.
   *
   * @return seconds left before automatic run start
   */
  float buildTimeRemainingSeconds() {
    return session.buildTimeRemainingSeconds();
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
    session.openLevelSelect();
  }

  /** Opens the settings menu from the startup menu. */
  void openSettings() {
    session.openSettings();
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
    session.updateGame(deltaSeconds);
  }

  /**
   * Returns the cell currently shown as a rejected placement.
   *
   * @return rejected cell, or null when no rejection flash is active
   */
  GridPosition rejectedPosition() {
    return session.rejectedPosition();
  }

  /**
   * Advances the build timer and starts the mouse when it reaches zero.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  void updateBuildTimer(float deltaSeconds) {
    session.updateBuildTimer(deltaSeconds);
  }

  /** Starts the mouse run from the current maze if the player is still building. */
  void startRun() {
    session.startRun();
  }

  /**
   * Applies a grid click to the current maze.
   *
   * @param position clicked grid cell
   * @param button libGDX mouse button code
   */
  void handleGridClick(GridPosition position, int button) {
    if (button == Input.Buttons.LEFT) {
      session.placeWall(position);
    } else if (button == Input.Buttons.RIGHT) {
      session.clearWall(position);
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
    ScreenLayout layout = screenLayout(gamePhase(), screenWidth, screenHeight);
    GameInputAction action =
        GameInputRouter.route(
            layout, gamePhase(), screenX, screenY, button, session.levelDefinition().gridSize());
    applyInputAction(action);
    return action.consumed();
  }

  /**
   * Applies a routed input action to the current mutable game state.
   *
   * @param action routed input action
   */
  private void applyInputAction(GameInputAction action) {
    switch (action.type()) {
      case OPEN_LEVEL_SELECT -> openLevelSelect();
      case OPEN_SETTINGS -> openSettings();
      case QUIT -> exitAction.run();
      case BACK_TO_MAIN_MENU -> session.returnToMainMenu();
      case TOGGLE_AUDIO -> toggleAudio();
      case START_MILESTONE_ONE -> startMilestoneOneLevel();
      case SELECT_LOCKED_LEVEL, IGNORED_GRID_CLICK, NONE -> {
        // Recognized but intentionally state-neutral actions.
      }
      case START_RUN -> startRun();
      case PLACE_WALL -> handleGridClick(action.position(), Input.Buttons.LEFT);
      case CLEAR_WALL -> handleGridClick(action.position(), Input.Buttons.RIGHT);
      case RETRY -> retryLevel();
      case REPLAY -> replayRun();
      case RESULT_MAIN_MENU -> returnToMainMenu();
    }
  }

  /** Resets the current level to a fresh build phase attempt. */
  void retryLevel() {
    session.retryLevel();
    screenshotElapsedSeconds = 0.0F;
  }

  /** Replays the completed maze from the same deterministic seed. */
  void replayRun() {
    session.replayRun();
  }

  /**
   * Returns whether the completed result passed the level target.
   *
   * @return true when result phase is active and elapsed solve time exceeded the target
   */
  boolean resultPassed() {
    return session.resultPassed();
  }

  /**
   * Returns whether another level can be selected after this result.
   *
   * @return false for milestone 1 because only one level exists
   */
  boolean hasNextLevel() {
    return session.hasNextLevel();
  }

  /**
   * Returns the background/fill color for a grid cell.
   *
   * @param position cell to inspect
   * @return color used before any sprite overlay is drawn
   */
  Color cellColor(GridPosition position) {
    return MazeGameRenderer.cellColor(
        session.mazeState(),
        session.rejectedPosition(),
        session.rejectedFlashRemainingSeconds(),
        position);
  }

  /**
   * Creates the immutable data needed to render one frame.
   *
   * @return render snapshot for the current game state
   */
  private GameRenderSnapshot renderSnapshot() {
    return new GameRenderSnapshot(
        gamePhase(),
        session.levelDefinition(),
        session.mazeState(),
        session.buildTimeRemainingSeconds(),
        session.rejectedPosition(),
        session.rejectedFlashRemainingSeconds(),
        session.mouseRunResult(),
        audioEnabled,
        resultPassed(),
        hasNextLevel());
  }

  /**
   * Creates the current screen layout.
   *
   * @param phase phase to describe
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @return declared screen layout
   */
  private ScreenLayout screenLayout(GamePhase phase, int screenWidth, int screenHeight) {
    return MazeGameLayout.forPhase(
        phase, screenWidth, screenHeight, session.levelDefinition().gridSize());
  }

  /**
   * Advances the active mouse simulation.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  void updateMouseRun(float deltaSeconds) {
    session.updateMouseRun(deltaSeconds);
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
