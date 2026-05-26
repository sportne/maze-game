package io.github.sportne.mazegame.state;

import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.LevelDefinition;
import io.github.sportne.mazegame.model.Levels;
import io.github.sportne.mazegame.model.MazeState;
import io.github.sportne.mazegame.model.MouseRunResult;
import io.github.sportne.mazegame.model.MouseRunStatus;
import io.github.sportne.mazegame.model.RandomMouseSimulation;
import io.github.sportne.mazegame.model.WallPlacementResult;
import java.time.Duration;
import java.util.Objects;

/** Mutable session state for one Maze Game play session. */
public final class GameSession {
  /** Duration of rejected-placement visual feedback. */
  private static final float REJECTED_FLASH_SECONDS = 0.5F;

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

  /** Creates a session initialized to the startup menu. */
  public GameSession() {
    initializeMainMenu();
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
   * <p>The level model is still initialized so menu rendering, debug snapshots, and tests can read
   * stable milestone-one defaults before the player starts a level.
   */
  public void initializeMainMenu() {
    initializeLevelState(GamePhase.MAIN_MENU);
  }

  /** Resets all model and phase state for a fresh attempt of the first level. */
  public void startMilestoneOneLevel() {
    initializeLevelState(GamePhase.BUILDING);
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
    startMilestoneOneLevel();
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
   * @return false for milestone 1 because only one level exists
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
    long deltaMillis = Math.max(0L, Math.round(deltaSeconds * 1000.0F));
    mouseRunResult = mouseSimulation.update(Duration.ofMillis(deltaMillis));
    if (mouseRunResult.status() != MouseRunStatus.RUNNING) {
      gamePhase = GamePhase.RESULT;
    }
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
  }
}
