package io.github.sportne.mazegame.state;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelCatalog;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.maze.WallPlacementResult;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.mouse.MouseRunStatus;
import io.github.sportne.mazegame.model.mouse.RandomMouseSimulation;
import io.github.sportne.mazegame.model.result.BestResult;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** Mutable session state for one Maze Game play session. */
public final class GameSession {
  /** Duration of rejected-placement visual feedback. */
  private static final float REJECTED_FLASH_SECONDS = 0.5F;

  /** Authored levels available to this session. */
  private final LevelCatalog levelCatalog;

  /** Stable level id used before the player makes a selection. */
  private final String initialLevelId;

  /** Current level definition. */
  private LevelDefinition levelDefinition;

  /** Persistence boundary for level best results. */
  private final BestResultStore bestResultStore;

  /** Best saved result for the current level, or null when none has been saved. */
  private BestResult bestResult;

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

  /** Creates a session initialized to the startup menu. */
  public GameSession() {
    this(BestResultStore.none());
  }

  /**
   * Creates a session initialized to the startup menu.
   *
   * @param bestResultStore persistence boundary for best results
   */
  public GameSession(BestResultStore bestResultStore) {
    this(Levels.catalog(), Levels.milestoneOne().id(), bestResultStore);
  }

  /**
   * Creates a session with an authored catalog and explicit initial level.
   *
   * @param levelCatalog authored levels available to select
   * @param initialLevelId stable level id used at startup
   * @param bestResultStore persistence boundary for best results
   */
  public GameSession(
      LevelCatalog levelCatalog, String initialLevelId, BestResultStore bestResultStore) {
    this.levelCatalog = Objects.requireNonNull(levelCatalog, "levelCatalog");
    this.initialLevelId = Objects.requireNonNull(initialLevelId, "initialLevelId");
    this.bestResultStore = Objects.requireNonNull(bestResultStore, "bestResultStore");
    if (levelCatalog.findById(initialLevelId).isEmpty()) {
      throw new IllegalArgumentException(
          "initial level id is not in the catalog: ".concat(initialLevelId));
    }
    initializeMainMenu();
  }

  /**
   * Returns authored levels in stable display order.
   *
   * @return immutable ordered levels
   */
  public List<LevelDefinition> levels() {
    return levelCatalog.levels();
  }

  /**
   * Returns the current level definition.
   *
   * @return current level definition
   */
  public LevelDefinition levelDefinition() {
    return levelDefinition;
  }

  /**
   * Returns the current immutable maze state.
   *
   * @return current maze
   */
  public MazeState mazeState() {
    return mazeState;
  }

  /**
   * Returns the best saved result for the current level.
   *
   * @return best result, or null when none has been saved
   */
  public BestResult bestResult() {
    return bestResult;
  }

  /**
   * Returns the current game phase.
   *
   * @return active phase
   */
  public GamePhase gamePhase() {
    return gamePhase;
  }

  /**
   * Returns build time remaining.
   *
   * @return seconds left before automatic run start
   */
  public float buildTimeRemainingSeconds() {
    return buildTimeRemainingSeconds;
  }

  /**
   * Returns the cell currently shown as a rejected placement.
   *
   * @return rejected cell, or null when no rejection flash is active
   */
  public GridPosition rejectedPosition() {
    return rejectedPosition;
  }

  /**
   * Returns seconds remaining in the rejected-placement flash.
   *
   * @return flash time remaining
   */
  public float rejectedFlashRemainingSeconds() {
    return rejectedFlashRemainingSeconds;
  }

  /**
   * Returns whether the mouse run has been started for the current attempt.
   *
   * @return true after manual or automatic run start
   */
  public boolean runRequested() {
    return runRequested;
  }

  /**
   * Returns the current mouse run snapshot.
   *
   * @return latest run result, or null before the mouse starts
   */
  public MouseRunResult mouseRunResult() {
    return mouseRunResult;
  }

  /**
   * Resets all model state and enters the startup menu.
   *
   * <p>The selected level model remains initialized so menu rendering, debug snapshots, and tests
   * can read stable defaults before another level starts.
   */
  public void initializeMainMenu() {
    LevelDefinition selectedLevel =
        levelDefinition == null
            ? levelCatalog.findById(initialLevelId).orElseThrow()
            : levelDefinition;
    initializeLevelState(selectedLevel, GamePhase.MAIN_MENU);
  }

  /**
   * Starts a fresh attempt of a known authored level.
   *
   * @param levelId stable level id
   * @return true when the level exists and was started
   */
  public boolean startLevel(String levelId) {
    Objects.requireNonNull(levelId, "levelId");
    return levelCatalog
        .findById(levelId)
        .map(
            selectedLevel -> {
              initializeLevelState(selectedLevel, GamePhase.BUILDING);
              return true;
            })
        .orElse(false);
  }

  /** Opens the level-select menu from the startup menu. */
  public void openLevelSelect() {
    if (gamePhase == GamePhase.MAIN_MENU) {
      gamePhase = GamePhase.LEVEL_SELECT;
    }
  }

  /** Opens the settings menu from the startup menu. */
  public void openSettings() {
    if (gamePhase == GamePhase.MAIN_MENU) {
      gamePhase = GamePhase.SETTINGS;
    }
  }

  /** Returns to the startup menu and clears any in-progress level attempt. */
  public void returnToMainMenu() {
    initializeMainMenu();
  }

  /**
   * Advances the active phase by a frame delta.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  public void updateGame(float deltaSeconds) {
    if (gamePhase == GamePhase.BUILDING) {
      updateBuildTimer(deltaSeconds);
    } else if (gamePhase == GamePhase.MOUSE_RUNNING || gamePhase == GamePhase.REPLAY) {
      updateMouseRun(deltaSeconds);
    }
  }

  /**
   * Advances the build timer and starts the mouse when it reaches zero.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  public void updateBuildTimer(float deltaSeconds) {
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
  public void startRun() {
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
   * Places a wall during the build phase.
   *
   * @param position clicked grid cell
   */
  public void placeWall(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (gamePhase != GamePhase.BUILDING) {
      return;
    }
    WallPlacementResult result = mazeState.placeWall(position);
    if (result.accepted()) {
      mazeState = result.mazeState();
    } else {
      rejectedPosition = position;
      rejectedFlashRemainingSeconds = REJECTED_FLASH_SECONDS;
    }
  }

  /**
   * Clears a wall during the build phase.
   *
   * @param position clicked grid cell
   */
  public void clearWall(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (gamePhase == GamePhase.BUILDING) {
      mazeState = mazeState.withoutWall(position);
    }
  }

  /** Resets the current level to a fresh build phase attempt. */
  public void retryLevel() {
    initializeLevelState(levelDefinition, GamePhase.BUILDING);
  }

  /** Replays the completed maze from the same deterministic seed. */
  public void replayRun() {
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
  public boolean resultPassed() {
    return GameResultEvaluator.passed(gamePhase, mouseRunResult, levelDefinition);
  }

  /**
   * Returns whether another level can be selected after this result.
   *
   * @return false until progression policy is implemented
   */
  public boolean hasNextLevel() {
    return false;
  }

  /**
   * Advances the active mouse simulation.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  public void updateMouseRun(float deltaSeconds) {
    if ((gamePhase != GamePhase.MOUSE_RUNNING && gamePhase != GamePhase.REPLAY)
        || mouseSimulation == null
        || mouseRunResult == null
        || mouseRunResult.status() != MouseRunStatus.RUNNING) {
      return;
    }
    boolean shouldRecordBestResult = gamePhase == GamePhase.MOUSE_RUNNING;
    long deltaMillis = Math.max(0L, Math.round(deltaSeconds * 1000.0F));
    mouseRunResult = mouseSimulation.update(Duration.ofMillis(deltaMillis));
    if (mouseRunResult.status() != MouseRunStatus.RUNNING) {
      gamePhase = GamePhase.RESULT;
      if (shouldRecordBestResult && resultPassed()) {
        recordBestResult(BestResult.from(mouseRunResult));
      }
    }
  }

  private void recordBestResult(BestResult candidate) {
    if (candidate.beats(bestResult)) {
      bestResult = candidate;
      bestResultStore.save(levelDefinition.id(), candidate);
    }
  }

  /**
   * Resets all model state and moves to the requested phase.
   *
   * @param selectedLevel level definition to initialize
   * @param initialPhase phase to enter after resetting level state
   */
  private void initializeLevelState(LevelDefinition selectedLevel, GamePhase initialPhase) {
    levelDefinition = Objects.requireNonNull(selectedLevel, "selectedLevel");
    bestResult = bestResultStore.load(levelDefinition.id()).orElse(null);
    mazeState = MazeState.empty(levelDefinition);
    buildTimeRemainingSeconds = levelDefinition.buildTime().toMillis() / 1000.0F;
    rejectedPosition = null;
    rejectedFlashRemainingSeconds = 0.0F;
    runRequested = false;
    mouseSimulation = null;
    mouseRunResult = null;
    gamePhase = initialPhase;
  }
}
