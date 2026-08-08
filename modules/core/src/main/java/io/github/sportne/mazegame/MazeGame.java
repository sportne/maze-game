package io.github.sportne.mazegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.sportne.mazegame.assets.AssetPaths;
import io.github.sportne.mazegame.assets.BackgroundMusicController;
import io.github.sportne.mazegame.assets.MouseSpriteSheet;
import io.github.sportne.mazegame.input.GameInputAction;
import io.github.sportne.mazegame.input.GameInputRouter;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.persistence.LibGdxBestResultStore;
import io.github.sportne.mazegame.render.GameRenderSnapshot;
import io.github.sportne.mazegame.render.MazeGameRenderer;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameSession;
import java.util.Objects;

/**
 * Main libGDX application for Maze Game.
 *
 * <p>This class is the bridge between the immutable core model and the desktop runtime. It owns the
 * current level, maze state, build timer, mouse simulation, simple primitive rendering, sprite
 * rendering, and platform callbacks. The domain rules remain in {@code
 * io.github.sportne.mazegame.model}; this class turns those rules into input handling and drawing.
 */
public final class MazeGame extends ApplicationAdapter {
  /** Background clear color for every frame. */
  private static final Color BACKGROUND = new Color(0.07F, 0.08F, 0.10F, 1.0F);

  /** Primary text color. */
  private static final Color TEXT = new Color(0.88F, 0.92F, 0.96F, 1.0F);

  /** Desktop window title and in-game title text. */
  private static final String TITLE = "Maze Game";

  /** Platform capabilities and services supplied by the active launcher. */
  private final MazeGameRuntimeConfiguration runtimeConfiguration;

  /** Current mutable gameplay session. */
  private final GameSession session;

  /** Controller for optional background music. */
  private final BackgroundMusicController backgroundMusicController;

  /** Sprite batch used for fonts and sprite sheet regions. */
  private SpriteBatch spriteBatch;

  /** Primitive renderer used for cells, grid lines, and buttons. */
  private ShapeRenderer shapeRenderer;

  /** Default libGDX bitmap font used by the simple UI. */
  private BitmapFont font;

  /** Viewport that keeps one game unit aligned with one logical screen pixel. */
  private Viewport viewport;

  /** Texture loaded from the mouse/cheese sprite sheet asset. */
  private Texture spriteSheet;

  /** Cropped cheese sprite drawn over the endpoint cell. */
  private TextureRegion cheeseSprite;

  /** Cropped mouse sprite drawn at the current mouse position. */
  private TextureRegion mouseSprite;

  /** Renderer that draws the current frame. */
  private MazeGameRenderer renderer;

  /** Whether primary-pointer grid clicks clear walls instead of placing them. */
  private boolean clearWallMode;

  /** Whether the platform has delivered the gesture required to start browser audio. */
  private boolean audioGestureReceived;

  /** Whether lifecycle callbacks have paused gameplay updates. */
  private boolean paused;

  /** Creates the game with default platform services. */
  public MazeGame() {
    this(null, defaultRuntimeConfiguration(), new LibGdxBestResultStore());
  }

  /**
   * Creates the game with platform services and capabilities.
   *
   * @param runtimeConfiguration platform runtime configuration
   */
  public MazeGame(MazeGameRuntimeConfiguration runtimeConfiguration) {
    this(null, runtimeConfiguration, new LibGdxBestResultStore());
  }

  /**
   * Creates the game with platform services and a platform-specific best-result store.
   *
   * @param runtimeConfiguration platform runtime configuration
   * @param bestResultStore persistence boundary for level best results
   */
  public MazeGame(
      MazeGameRuntimeConfiguration runtimeConfiguration, BestResultStore bestResultStore) {
    this(null, runtimeConfiguration, bestResultStore);
  }

  /**
   * Creates the game with injected music for tests.
   *
   * @param backgroundMusic music instance to dispose when the game is disposed
   */
  MazeGame(Music backgroundMusic) {
    this(backgroundMusic, defaultRuntimeConfiguration(), new LibGdxBestResultStore());
  }

  /**
   * Creates the game with test/runtime dependencies.
   *
   * @param backgroundMusic optional injected music
   * @param runtimeConfiguration platform runtime configuration
   * @param bestResultStore persistence boundary for level best results
   */
  MazeGame(
      Music backgroundMusic,
      MazeGameRuntimeConfiguration runtimeConfiguration,
      BestResultStore bestResultStore) {
    this.runtimeConfiguration =
        Objects.requireNonNull(runtimeConfiguration, "runtimeConfiguration");
    this.session = new GameSession(bestResultStore);
    this.backgroundMusicController =
        new BackgroundMusicController(runtimeConfiguration.audioAvailable());
    if (backgroundMusic != null) {
      this.backgroundMusicController.adopt(backgroundMusic);
    }
  }

  private static MazeGameRuntimeConfiguration defaultRuntimeConfiguration() {
    return MazeGameRuntimeConfiguration.defaults(MazeGame::resolveDefaultAsset);
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
    return AssetPaths.backgroundMusicPath();
  }

  /**
   * Returns the asset-relative sprite sheet path.
   *
   * @return default sprite sheet asset path
   */
  static String spriteSheetPath() {
    return AssetPaths.spriteSheetPath();
  }

  /**
   * Returns the configured startup music volume.
   *
   * @return volume from 0.0 to 1.0
   */
  static float backgroundMusicVolume() {
    return BackgroundMusicController.backgroundMusicVolume();
  }

  /**
   * Applies looping and volume settings to the background music.
   *
   * @param music music instance created by libGDX
   */
  static void configureBackgroundMusic(Music music) {
    BackgroundMusicController.configureBackgroundMusic(music);
  }

  /** Creates, configures, and starts background music when audio is available. */
  private void startBackgroundMusic() {
    backgroundMusicController.start(
        () -> Gdx.audio == null ? null : Gdx.audio.newMusic(backgroundMusicFile()));
  }

  /** Resets all session state and enters the startup menu. */
  private void initializeMainMenu() {
    session.initializeMainMenu();
    clearWallMode = false;
  }

  /** Resets all session state for a fresh attempt of the selected level. */
  void startLevel(String levelId) {
    session.startLevel(levelId);
    clearWallMode = false;
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
    viewport = new ScreenViewport();
    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    font = new BitmapFont();
    font.setColor(TEXT);
    spriteSheet = new Texture(spriteSheetFile());
    spriteSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    cheeseSprite = MouseSpriteSheet.cheese(spriteSheet);
    mouseSprite = MouseSpriteSheet.mouse(spriteSheet);
    renderer = new MazeGameRenderer(spriteBatch, shapeRenderer, font, cheeseSprite, mouseSprite);
    if (!runtimeConfiguration.audioRequiresUserGesture()) {
      startBackgroundMusic();
    }
    Gdx.input.setInputProcessor(new BuildInputProcessor());
  }

  /** Advances game state, draws one frame, and runs platform post-render work. */
  @Override
  public void render() {
    float deltaSeconds = Gdx.graphics.getDeltaTime();
    if (!paused) {
      updateGame(deltaSeconds);
    }
    ScreenUtils.clear(background());
    viewport.apply();
    updateProjectionMatrices();
    ScreenLayout layout =
        screenLayout(
            gamePhase(),
            Math.round(viewport.getWorldWidth()),
            Math.round(viewport.getWorldHeight()));
    renderer.render(layout, renderSnapshot());
    completeFrame(deltaSeconds);
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

  /** Pauses gameplay updates and active music while the application is backgrounded. */
  @Override
  public void pause() {
    paused = true;
    backgroundMusicController.pause();
  }

  /** Resumes gameplay updates and music that was active before the application paused. */
  @Override
  public void resume() {
    paused = false;
    backgroundMusicController.resume();
  }

  /** Releases libGDX resources and clears the input processor. */
  @Override
  public void dispose() {
    if (Gdx.input != null) {
      Gdx.input.setInputProcessor(null);
    }
    backgroundMusicController.dispose();
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
  public GamePhase gamePhase() {
    return session.gamePhase();
  }

  /**
   * Returns the current immutable maze state.
   *
   * @return current maze
   */
  public MazeState mazeState() {
    return session.mazeState();
  }

  /**
   * Returns the best saved result for the current level.
   *
   * @return best result, or null when none has been saved
   */
  public BestResult bestResult() {
    return session.bestResult();
  }

  /**
   * Returns the current mouse run snapshot.
   *
   * @return latest run result, or null before the mouse starts
   */
  public MouseRunResult mouseRunResult() {
    return session.mouseRunResult();
  }

  /**
   * Returns build time remaining.
   *
   * @return seconds left before automatic run start
   */
  public float buildTimeRemainingSeconds() {
    return session.buildTimeRemainingSeconds();
  }

  /**
   * Returns whether session audio is currently enabled.
   *
   * @return true when settings allow music playback
   */
  boolean audioEnabled() {
    return backgroundMusicController.audioEnabled();
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
    backgroundMusicController.toggle(
        () -> Gdx.audio == null ? null : Gdx.audio.newMusic(backgroundMusicFile()));
  }

  /** Toggles the touch-safe primary-pointer wall edit mode. */
  void toggleWallMode() {
    clearWallMode = !clearWallMode;
  }

  /**
   * Advances the active phase by a frame delta.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  public void updateGame(float deltaSeconds) {
    session.updateGame(deltaSeconds);
  }

  /**
   * Returns the cell currently shown as a rejected placement.
   *
   * @return rejected cell, or null when no rejection flash is active
   */
  public GridPosition rejectedPosition() {
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
  public boolean handleScreenClick(
      int screenX, int screenY, int button, int screenWidth, int screenHeight) {
    activateAudioFromGesture();
    ScreenLayout layout = screenLayout(gamePhase(), screenWidth, screenHeight);
    GameInputAction action =
        GameInputRouter.route(
            layout,
            gamePhase(),
            screenX,
            screenY,
            button,
            session.levelDefinition().gridSize(),
            session.levelProgress());
    applyInputAction(action);
    return action.consumed();
  }

  /**
   * Returns the declared screen layout for an external debug harness.
   *
   * @param phase phase to describe
   * @param screenWidth virtual screen width
   * @param screenHeight virtual screen height
   * @return declared screen layout
   */
  public ScreenLayout debugScreenLayout(GamePhase phase, int screenWidth, int screenHeight) {
    return screenLayout(phase, screenWidth, screenHeight);
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
      case QUIT -> runtimeConfiguration.exitAction().run();
      case BACK_TO_MAIN_MENU -> session.returnToMainMenu();
      case TOGGLE_AUDIO -> toggleAudio();
      case TOGGLE_WALL_MODE -> toggleWallMode();
      case SELECT_LEVEL -> startLevel(action.levelId());
      case SELECT_LOCKED_LEVEL, IGNORED_GRID_CLICK, NONE -> {
        // Recognized but intentionally state-neutral actions.
      }
      case START_RUN -> startRun();
      case PLACE_WALL ->
          handleGridClick(
              action.position(), clearWallMode ? Input.Buttons.RIGHT : Input.Buttons.LEFT);
      case CLEAR_WALL -> handleGridClick(action.position(), Input.Buttons.RIGHT);
      case RETRY -> retryLevel();
      case REPLAY -> replayRun();
      case NEXT_LEVEL -> session.nextLevelId().ifPresent(this::startLevel);
      case RESULT_MAIN_MENU -> returnToMainMenu();
    }
  }

  /** Resets the current level to a fresh build phase attempt. */
  void retryLevel() {
    session.retryLevel();
    clearWallMode = false;
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
  public boolean resultPassed() {
    return session.resultPassed();
  }

  /**
   * Returns whether another level can be selected after this result.
   *
   * @return true when a passing result unlocked the next catalog entry
   */
  public boolean hasNextLevel() {
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
        session.bestResult(),
        session.levelProgress(),
        audioEnabled(),
        clearWallMode,
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
        phase,
        screenWidth,
        screenHeight,
        session.levelDefinition().gridSize(),
        runtimeConfiguration.quitAvailable(),
        session.levelProgress().size(),
        session.hasNextLevel());
  }

  private void activateAudioFromGesture() {
    if (runtimeConfiguration.audioRequiresUserGesture() && !audioGestureReceived) {
      audioGestureReceived = true;
      startBackgroundMusic();
    }
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
  private FileHandle backgroundMusicFile() {
    return runtimeConfiguration.assetResolver().resolve(backgroundMusicPath());
  }

  /**
   * Returns a libGDX handle for the sprite sheet asset.
   *
   * @return file handle resolved through the app's asset fallback rules
   */
  private FileHandle spriteSheetFile() {
    return runtimeConfiguration.assetResolver().resolve(spriteSheetPath());
  }

  private static FileHandle resolveDefaultAsset(String assetPath) {
    return Gdx.files.internal(assetPath);
  }

  /**
   * Completes post-render work for one frame.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  void completeFrame(float deltaSeconds) {
    runtimeConfiguration.afterRenderHook().afterRender(deltaSeconds);
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
      int worldWidth = Math.round(viewport.getWorldWidth());
      int worldHeight = Math.round(viewport.getWorldHeight());
      return handleScreenClick(
          Math.round(worldPosition.x),
          Math.round(worldHeight - worldPosition.y),
          button,
          worldWidth,
          worldHeight);
    }
  }
}
