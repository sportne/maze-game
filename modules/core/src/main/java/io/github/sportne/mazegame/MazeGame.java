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
import io.github.sportne.mazegame.assets.DirectionalSpriteSet;
import io.github.sportne.mazegame.assets.GameSpriteSheets;
import io.github.sportne.mazegame.input.BuildGestureController;
import io.github.sportne.mazegame.input.BuildGestureState;
import io.github.sportne.mazegame.input.GameInputAction;
import io.github.sportne.mazegame.input.GameInputRouter;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.maze.MazeEditResult;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverDecisionState;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.persistence.LibGdxBestResultStore;
import io.github.sportne.mazegame.render.GameRenderSnapshot;
import io.github.sportne.mazegame.render.MazeGameRenderer;
import io.github.sportne.mazegame.render.PaletteDragPreview;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.CellPaletteState;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.GameSession;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Main libGDX application for Maze Game.
 *
 * <p>This class is the bridge between the immutable core model and the desktop runtime. It owns the
 * current level, maze state, build timer, solver simulation, simple primitive rendering, sprite
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

  /** Pointer dwell time before an icon-only palette item reveals its tooltip. */
  private static final float PALETTE_TOOLTIP_DELAY_SECONDS = 0.5F;

  /** Platform capabilities and services supplied by the active launcher. */
  private final MazeGameRuntimeConfiguration runtimeConfiguration;

  /** Current mutable gameplay session. */
  private final GameSession session;

  /** Controller-owned transient state for one active build pointer gesture. */
  private final BuildGestureController buildGestureController = new BuildGestureController();

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

  /** Texture loaded from the processed classic mouse sprite sheet. */
  private Texture classicMouseSpriteSheet;

  /** Texture loaded from the processed basic-character sprite sheet. */
  private Texture basicCharacterSpriteSheet;

  /** Texture loaded from the processed goal sprite sheet. */
  private Texture goalSpriteSheet;

  /** Cropped cheese sprite drawn over the endpoint cell. */
  private TextureRegion cheeseSprite;

  /** Cropped acorn sprite drawn as Scout's endpoint goal. */
  private TextureRegion acornSprite;

  /** Cropped trash-can sprite drawn as Tracker's endpoint goal. */
  private TextureRegion trashCanSprite;

  /** Directional classic-mouse sprites drawn for Random solvers. */
  private DirectionalSpriteSet solverSprites;

  /** Directional squirrel sprites drawn when a level selects Scout's appearance. */
  private DirectionalSpriteSet scoutSprites;

  /** Directional raccoon sprites drawn when a level selects Tracker's appearance. */
  private DirectionalSpriteSet trackerSprites;

  /** Renderer that draws the current frame. */
  private MazeGameRenderer renderer;

  /** Whether the platform has delivered the gesture required to start browser audio. */
  private boolean audioGestureReceived;

  /** Whether lifecycle callbacks have paused gameplay updates. */
  private boolean paused;

  /** Palette type currently under the desktop pointer, or null outside the palette. */
  private PlaceableCellType hoveredPaletteType;

  /** Seconds the desktop pointer has continuously hovered the current palette type. */
  private float paletteHoverSeconds;

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

  /** Creates the game around an explicit session for platform integration fixtures. */
  public MazeGame(MazeGameRuntimeConfiguration runtimeConfiguration, GameSession gameSession) {
    this(null, runtimeConfiguration, gameSession);
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
    this(backgroundMusic, runtimeConfiguration, new GameSession(bestResultStore));
  }

  /** Creates the game around an explicit session for application-boundary tests. */
  MazeGame(
      Music backgroundMusic,
      MazeGameRuntimeConfiguration runtimeConfiguration,
      GameSession gameSession) {
    this.runtimeConfiguration =
        Objects.requireNonNull(runtimeConfiguration, "runtimeConfiguration");
    this.session = Objects.requireNonNull(gameSession, "gameSession");
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
  static String classicMouseSpriteSheetPath() {
    return AssetPaths.classicMouseSpriteSheetPath();
  }

  /**
   * Returns the asset-relative Scout sprite path.
   *
   * @return Scout sprite asset path
   */
  static String basicCharacterSpriteSheetPath() {
    return AssetPaths.basicCharacterSpriteSheetPath();
  }

  /** Returns the asset-relative processed goal sprite-sheet path. */
  static String goalSpriteSheetPath() {
    return AssetPaths.goalSpriteSheetPath();
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
  }

  /** Resets all session state for a fresh attempt of the selected level. */
  void startLevel(String levelId) {
    session.startLevel(levelId);
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
    classicMouseSpriteSheet = loadNearestTexture(classicMouseSpriteSheetFile());
    basicCharacterSpriteSheet = loadNearestTexture(basicCharacterSpriteSheetFile());
    goalSpriteSheet = loadNearestTexture(goalSpriteSheetFile());
    cheeseSprite = GameSpriteSheets.cheese(goalSpriteSheet);
    acornSprite = GameSpriteSheets.acorn(goalSpriteSheet);
    trashCanSprite = GameSpriteSheets.trashCan(goalSpriteSheet);
    solverSprites = GameSpriteSheets.randomSolverSprites(classicMouseSpriteSheet);
    scoutSprites = GameSpriteSheets.scoutSquirrelSprites(basicCharacterSpriteSheet);
    trackerSprites = GameSpriteSheets.trackerRaccoonSprites(basicCharacterSpriteSheet);
    renderer =
        new MazeGameRenderer(
            spriteBatch,
            shapeRenderer,
            font,
            cheeseSprite,
            acornSprite,
            trashCanSprite,
            solverSprites,
            scoutSprites,
            trackerSprites);
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
    cancelBuildGesture();
    if (viewport != null) {
      viewport.update(width, height, true);
      updateProjectionMatrices();
    }
  }

  /** Pauses gameplay updates and active music while the application is backgrounded. */
  @Override
  public void pause() {
    cancelBuildGesture();
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
    classicMouseSpriteSheet = disposeTexture(classicMouseSpriteSheet);
    basicCharacterSpriteSheet = disposeTexture(basicCharacterSpriteSheet);
    goalSpriteSheet = disposeTexture(goalSpriteSheet);
    cheeseSprite = null;
    acornSprite = null;
    trashCanSprite = null;
    solverSprites = null;
    scoutSprites = null;
    trackerSprites = null;
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
   * Returns whether the solver run has been started for the current attempt.
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

  /** Returns immutable build-palette state in authored order. */
  public List<CellPaletteState> paletteState() {
    return session.paletteState();
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
   * Returns the current solver run snapshot.
   *
   * @return latest run result, or null before the solver starts
   */
  public SolverRunResult solverRunResult() {
    return session.solverRunResult();
  }

  /** Returns the latest results for every solver in authored order. */
  public List<SolverRunResult> solverRunResults() {
    return session.solverRunResults();
  }

  /** Returns immutable decision memory for every active solver in authored order. */
  public List<SolverDecisionState> solverDecisionStates() {
    return session.solverDecisionStates();
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

  /**
   * Advances the active phase by a frame delta.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  public void updateGame(float deltaSeconds) {
    if (gamePhase() == GamePhase.BUILDING
        && buildGestureController.state().isPresent()
        && deltaSeconds >= session.buildTimeRemainingSeconds()) {
      cancelBuildGesture();
    }
    session.updateGame(deltaSeconds);
    updatePaletteHover(deltaSeconds);
  }

  private void updatePaletteHover(float deltaSeconds) {
    if (gamePhase() != GamePhase.BUILDING || buildGestureController.state().isPresent()) {
      clearPaletteHover();
      return;
    }
    if (hoveredPaletteType != null) {
      paletteHoverSeconds += Math.max(0.0F, deltaSeconds);
    }
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
   * Advances the build timer and starts the solver when it reaches zero.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  void updateBuildTimer(float deltaSeconds) {
    if (gamePhase() == GamePhase.BUILDING
        && buildGestureController.state().isPresent()
        && deltaSeconds >= session.buildTimeRemainingSeconds()) {
      cancelBuildGesture();
    }
    session.updateBuildTimer(deltaSeconds);
  }

  /** Starts the solver run from the current maze if the player is still building. */
  void startRun() {
    cancelBuildGesture();
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
      session.placeOrReplaceCell(position);
    } else if (button == Input.Buttons.RIGHT) {
      session.removeCell(position);
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
    clearPaletteHover();
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
   * Starts a palette or grid gesture or dispatches another press through normal click routing.
   *
   * @param screenX CSS-pixel x coordinate from the left edge
   * @param screenY CSS-pixel y coordinate from the top edge
   * @param pointer pointer id
   * @param button libGDX mouse button code
   * @param screenWidth logical screen width
   * @param screenHeight logical screen height
   * @return true when the press is consumed
   */
  public boolean handlePointerDown(
      int screenX, int screenY, int pointer, int button, int screenWidth, int screenHeight) {
    activateAudioFromGesture();
    clearPaletteHover();
    if (buildGestureController.state().isPresent()) {
      return true;
    }
    if (gamePhase() == GamePhase.BUILDING && button == Input.Buttons.LEFT) {
      ScreenLayout layout = screenLayout(GamePhase.BUILDING, screenWidth, screenHeight);
      Optional<PlaceableCellType> paletteType =
          GameInputRouter.paletteTypeAt(layout, screenX, screenHeight - screenY);
      if (paletteType.isPresent()) {
        return buildGestureController.pressPalette(
            pointer, paletteType.get(), screenX, screenY, 1.0F);
      }
      Optional<GridPosition> gridPosition =
          GameInputRouter.gridPositionAt(
              layout.bounds(MazeGameLayout.GAME_GRID),
              screenX,
              screenHeight - screenY,
              session.levelDefinition().gridSize());
      if (gridPosition.isPresent()) {
        GridPosition source = gridPosition.get();
        return buildGestureController.pressCell(
            pointer,
            source,
            Optional.ofNullable(session.mazeState().placedCellAt(source)),
            screenX,
            screenY,
            1.0F);
      }
    }
    return handleScreenClick(screenX, screenY, button, screenWidth, screenHeight);
  }

  /**
   * Updates the owning build pointer.
   *
   * @param screenX CSS-pixel x coordinate from the left edge
   * @param screenY CSS-pixel y coordinate from the top edge
   * @param pointer pointer id
   * @return true when the pointer owns the gesture
   */
  public boolean handlePointerDragged(int screenX, int screenY, int pointer) {
    clearPaletteHover();
    return buildGestureController.move(pointer, screenX, screenY, 1.0F);
  }

  /**
   * Tracks desktop-pointer hover over the icon-only build palette.
   *
   * @return true when the pointer is over a palette item
   */
  public boolean handlePointerMoved(int screenX, int screenY, int screenWidth, int screenHeight) {
    if (gamePhase() != GamePhase.BUILDING || buildGestureController.state().isPresent()) {
      clearPaletteHover();
      return false;
    }
    Optional<PlaceableCellType> candidate =
        GameInputRouter.paletteTypeAt(
            screenLayout(GamePhase.BUILDING, screenWidth, screenHeight),
            screenX,
            screenHeight - screenY);
    PlaceableCellType nextType = candidate.orElse(null);
    if (nextType != hoveredPaletteType) {
      hoveredPaletteType = nextType;
      paletteHoverSeconds = 0.0F;
    }
    return candidate.isPresent();
  }

  /** Returns the palette type whose half-second hover tooltip is currently visible. */
  public Optional<PlaceableCellType> paletteTooltipType() {
    return gamePhase() == GamePhase.BUILDING
            && hoveredPaletteType != null
            && paletteHoverSeconds >= PALETTE_TOOLTIP_DELAY_SECONDS
            && buildGestureController.state().isEmpty()
        ? Optional.of(hoveredPaletteType)
        : Optional.empty();
  }

  private void clearPaletteHover() {
    hoveredPaletteType = null;
    paletteHoverSeconds = 0.0F;
  }

  /**
   * Completes selection, placement, click editing, or movement for the owning pointer.
   *
   * @param screenX CSS-pixel x coordinate from the left edge
   * @param screenY CSS-pixel y coordinate from the top edge
   * @param pointer pointer id
   * @param screenWidth logical screen width
   * @param screenHeight logical screen height
   * @return exact edit result, or empty for selection, cancellation, or a non-owner
   */
  public Optional<MazeEditResult> handlePointerUp(
      int screenX, int screenY, int pointer, int screenWidth, int screenHeight) {
    Optional<BuildGestureState> released =
        buildGestureController.release(pointer, screenX, screenY, 1.0F);
    if (released.isEmpty() || gamePhase() != GamePhase.BUILDING) {
      return Optional.empty();
    }
    BuildGestureState gesture = released.get();
    if (!gesture.dragThresholdCrossed() && gesture.paletteOrigin()) {
      session.selectCellType(gesture.originType());
      return Optional.empty();
    }
    if (!gesture.dragThresholdCrossed()) {
      return session.placeOrReplaceCell(gesture.originPosition());
    }
    if (!gesture.dragging()) {
      return Optional.empty();
    }
    ScreenLayout layout = screenLayout(GamePhase.BUILDING, screenWidth, screenHeight);
    Optional<GridPosition> destination =
        GameInputRouter.gridPositionAt(
            layout.bounds(MazeGameLayout.GAME_GRID),
            screenX,
            screenHeight - screenY,
            session.levelDefinition().gridSize());
    if (destination.isEmpty()) {
      return Optional.empty();
    }
    if (gesture.paletteOrigin()) {
      session.selectCellType(gesture.originType());
      return session.placeOrReplaceCell(destination.get());
    }
    return session.moveCell(gesture.originPosition(), destination.get());
  }

  /** Clears active build preview and pointer ownership without editing. */
  public void cancelBuildGesture() {
    buildGestureController.cancel();
    clearPaletteHover();
  }

  /**
   * Returns the active controller state for lifecycle and debug verification.
   *
   * @return active palette gesture, or empty while idle
   */
  public Optional<BuildGestureState> buildGestureState() {
    return buildGestureController.state();
  }

  /**
   * Builds the current palette or placed-cell preview for a logical screen size.
   *
   * @param screenWidth logical screen width
   * @param screenHeight logical screen height
   * @return preview with domain-validity feedback, or null before the drag threshold
   */
  PaletteDragPreview paletteDragPreview(int screenWidth, int screenHeight) {
    Optional<BuildGestureState> active = buildGestureController.state();
    if (active.isEmpty() || !active.get().dragging() || gamePhase() != GamePhase.BUILDING) {
      return null;
    }
    BuildGestureState gesture = active.get();
    ScreenLayout layout = screenLayout(GamePhase.BUILDING, screenWidth, screenHeight);
    Optional<GridPosition> destination =
        GameInputRouter.gridPositionAt(
            layout.bounds(MazeGameLayout.GAME_GRID),
            gesture.currentX(),
            screenHeight - gesture.currentY(),
            session.levelDefinition().gridSize());
    boolean valid;
    if (gesture.paletteOrigin()) {
      valid =
          destination
              .flatMap(
                  position -> session.previewPlaceOrReplaceCell(gesture.originType(), position))
              .map(result -> result.status().accepted())
              .orElse(false);
    } else {
      valid =
          destination
              .flatMap(position -> session.previewMoveCell(gesture.originPosition(), position))
              .map(result -> result.status().accepted())
              .orElse(false);
    }
    return new PaletteDragPreview(
        gesture.originType(),
        gesture.currentX(),
        screenHeight - gesture.currentY(),
        destination.orElse(null),
        valid,
        gesture.originPosition());
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
      case BACK_TO_LEVEL_SELECT -> {
        cancelBuildGesture();
        session.returnToLevelSelect();
      }
      case TOGGLE_AUDIO -> toggleAudio();
      case SELECT_LEVEL -> startLevel(action.levelId());
      case SELECT_LOCKED_LEVEL, IGNORED_GRID_CLICK, NONE -> {
        // Recognized but intentionally state-neutral actions.
      }
      case START_RUN -> startRun();
      case SELECT_CELL_TYPE -> session.selectCellType(action.cellType());
      case PLACE_OR_REPLACE_CELL -> session.placeOrReplaceCell(action.position());
      case REMOVE_CELL -> session.removeCell(action.position());
      case RETRY -> retryLevel();
      case REPLAY -> replayRun();
      case NEXT_LEVEL -> session.nextLevelId().ifPresent(this::startLevel);
      case RESULT_MAIN_MENU -> returnToMainMenu();
    }
  }

  /** Resets the current level to a fresh build phase attempt. */
  void retryLevel() {
    cancelBuildGesture();
    session.retryLevel();
  }

  /** Replays the completed maze from the same deterministic seed. */
  void replayRun() {
    cancelBuildGesture();
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
    PaletteDragPreview preview = null;
    if (viewport != null) {
      preview =
          paletteDragPreview(
              Math.round(viewport.getWorldWidth()), Math.round(viewport.getWorldHeight()));
    }
    return new GameRenderSnapshot(
        gamePhase(),
        session.levelDefinition(),
        session.mazeState(),
        session.buildTimeRemainingSeconds(),
        session.rejectedPosition(),
        session.rejectedFlashRemainingSeconds(),
        session.bestResult(),
        session.levelProgress(),
        session.paletteState(),
        preview,
        paletteTooltipType().orElse(null),
        audioEnabled(),
        resultPassed(),
        hasNextLevel(),
        session.solverRunResults(),
        session.solverDirections());
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
        session.hasNextLevel(),
        session.levelDefinition().initiallyAvailableCellTypes());
  }

  private void activateAudioFromGesture() {
    if (runtimeConfiguration.audioRequiresUserGesture() && !audioGestureReceived) {
      audioGestureReceived = true;
      startBackgroundMusic();
    }
  }

  /**
   * Advances the active solver simulation.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  void updateSolverRun(float deltaSeconds) {
    session.updateSolverRun(deltaSeconds);
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
  private FileHandle classicMouseSpriteSheetFile() {
    return runtimeConfiguration.assetResolver().resolve(classicMouseSpriteSheetPath());
  }

  /**
   * Returns a libGDX handle for Scout's sprite asset.
   *
   * @return file handle resolved through the app's asset fallback rules
   */
  private FileHandle basicCharacterSpriteSheetFile() {
    return runtimeConfiguration.assetResolver().resolve(basicCharacterSpriteSheetPath());
  }

  /** Returns a handle for the processed goal sprite sheet. */
  private FileHandle goalSpriteSheetFile() {
    return runtimeConfiguration.assetResolver().resolve(goalSpriteSheetPath());
  }

  private static Texture loadNearestTexture(FileHandle file) {
    Texture texture = new Texture(file);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    return texture;
  }

  private static Texture disposeTexture(Texture texture) {
    if (texture != null) {
      texture.dispose();
    }
    return null;
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

  /** Input adapter that forwards pointer lifecycle events into testable gesture handling. */
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
      PointerPosition position = pointerPosition(screenX, screenY);
      if (position == null) {
        return false;
      }
      return handlePointerDown(
          position.x(),
          position.y(),
          pointer,
          button,
          position.screenWidth(),
          position.screenHeight());
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
      PointerPosition position = pointerPosition(screenX, screenY);
      return position != null && handlePointerDragged(position.x(), position.y(), pointer);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
      PointerPosition position = pointerPosition(screenX, screenY);
      if (position == null) {
        return false;
      }
      boolean consumed =
          buildGestureController.owns(pointer) || buildGestureController.state().isPresent();
      handlePointerUp(
          position.x(), position.y(), pointer, position.screenWidth(), position.screenHeight());
      return consumed;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
      boolean consumed = buildGestureController.owns(pointer);
      if (consumed) {
        cancelBuildGesture();
      }
      return consumed;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
      PointerPosition position = pointerPosition(screenX, screenY);
      return position != null
          && handlePointerMoved(
              position.x(), position.y(), position.screenWidth(), position.screenHeight());
    }

    private PointerPosition pointerPosition(int screenX, int screenY) {
      if (viewport == null) {
        return null;
      }
      Vector2 worldPosition = viewport.unproject(new Vector2(screenX, screenY));
      int worldWidth = Math.round(viewport.getWorldWidth());
      int worldHeight = Math.round(viewport.getWorldHeight());
      return new PointerPosition(
          Math.round(worldPosition.x),
          Math.round(worldHeight - worldPosition.y),
          worldWidth,
          worldHeight);
    }
  }

  private record PointerPosition(int x, int y, int screenWidth, int screenHeight) {}
}
