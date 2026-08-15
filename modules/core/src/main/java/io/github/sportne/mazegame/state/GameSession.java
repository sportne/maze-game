package io.github.sportne.mazegame.state;

import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelCatalog;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.maze.MazeEditResult;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import io.github.sportne.mazegame.model.solver.SolverSimulation;
import io.github.sportne.mazegame.model.solver.SolverSimulationFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

  /** Best results loaded or recorded during this session, keyed by stable level id. */
  private final Map<String, BestResult> bestResults = new HashMap<>();

  /** Best saved result for the current level, or null when none has been saved. */
  private BestResult bestResult;

  /** Current immutable maze layout. */
  private MazeState mazeState;

  /** Active placeable type, or null when every authored supply starts exhausted. */
  private PlaceableCellType selectedCellType;

  /** Seconds remaining before the solver starts automatically. */
  private float buildTimeRemainingSeconds;

  /** Cell currently flashing as a rejected placement, or null when no flash is active. */
  private GridPosition rejectedPosition;

  /** Seconds remaining in the rejected-placement flash. */
  private float rejectedFlashRemainingSeconds;

  /** Whether a run has been requested or auto-started for the current level attempt. */
  private boolean runRequested;

  /** Active simulations in the level's authored solver order. */
  private List<SolverSimulation> solverSimulations = List.of();

  /** Latest results in the level's authored solver order. */
  private List<SolverRunResult> solverRunResults = List.of();

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
    loadBestResults();
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
   * Returns progression state in catalog order.
   *
   * @return immutable ordered progression entries
   */
  public List<LevelProgress> levelProgress() {
    List<LevelProgress> progress = new ArrayList<>();
    boolean unlocked = true;
    for (LevelDefinition level : levelCatalog.levels()) {
      BestResult levelBestResult = bestResults.get(level.id());
      progress.add(new LevelProgress(level, unlocked, levelBestResult));
      unlocked = unlocked && levelBestResult != null;
    }
    return List.copyOf(progress);
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
   * Returns the selected placeable type.
   *
   * @return active type, or an empty value when no authored type starts available
   */
  public Optional<PlaceableCellType> selectedCellType() {
    return Optional.ofNullable(selectedCellType);
  }

  /**
   * Returns immutable palette state in authored display order.
   *
   * @return authored supply, remaining supply, availability, and selection for every type
   */
  public List<CellPaletteState> paletteState() {
    List<CellPaletteState> palette = new ArrayList<>();
    for (PlaceableCellSupply authored : levelDefinition.placeableCellSupplies()) {
      palette.add(
          new CellPaletteState(
              authored.type(),
              authored.supply(),
              mazeState.remainingSupply(authored.type()),
              authored.type() == selectedCellType));
    }
    return List.copyOf(palette);
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
   * Returns whether the solver run has been started for the current attempt.
   *
   * @return true after manual or automatic run start
   */
  public boolean runRequested() {
    return runRequested;
  }

  /**
   * Returns the current solver run snapshot.
   *
   * @return latest run result, or null before the solver starts
   */
  public SolverRunResult solverRunResult() {
    return solverRunResults.isEmpty() ? null : solverRunResults.get(0);
  }

  /** Returns current results in authored solver order, or an empty list before a run starts. */
  public List<SolverRunResult> solverRunResults() {
    return List.copyOf(solverRunResults);
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
        .filter(ignored -> isLevelUnlocked(levelId))
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

  /** Leaves an unstarted level attempt and returns to the level-select menu. */
  public void returnToLevelSelect() {
    if (gamePhase == GamePhase.BUILDING) {
      initializeLevelState(levelDefinition, GamePhase.LEVEL_SELECT);
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
    } else if (gamePhase == GamePhase.SOLVER_RUNNING || gamePhase == GamePhase.REPLAY) {
      updateSolverRun(deltaSeconds);
    }
  }

  /**
   * Advances the build timer and starts the solver when it reaches zero.
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

  /** Starts the solver run from the current maze if the player is still building. */
  public void startRun() {
    if (gamePhase != GamePhase.BUILDING) {
      return;
    }
    runRequested = true;
    gamePhase = GamePhase.SOLVER_RUNNING;
    rejectedPosition = null;
    rejectedFlashRemainingSeconds = 0.0F;
    initializeSolverSimulations();
  }

  /**
   * Selects a placeable type without consuming inventory.
   *
   * @param type authored type to keep active
   */
  public void selectCellType(PlaceableCellType type) {
    Objects.requireNonNull(type, "type");
    if (gamePhase == GamePhase.BUILDING) {
      selectedCellType = type;
    }
  }

  /**
   * Applies the selected type to one grid cell during the build phase.
   *
   * @param position destination cell
   * @return the domain edit result, or empty when editing is inactive or no tool is selected
   */
  public Optional<MazeEditResult> placeOrReplaceCell(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (gamePhase != GamePhase.BUILDING || selectedCellType == null) {
      return Optional.empty();
    }
    return Optional.of(applyEdit(mazeState.placeOrReplace(selectedCellType, position), position));
  }

  /**
   * Evaluates a palette placement without publishing maze, inventory, or feedback changes.
   *
   * @param type palette type being previewed
   * @param position proposed destination
   * @return domain result against the current state, or empty outside the build phase
   */
  public Optional<MazeEditResult> previewPlaceOrReplaceCell(
      PlaceableCellType type, GridPosition position) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(position, "position");
    if (gamePhase != GamePhase.BUILDING) {
      return Optional.empty();
    }
    return Optional.of(mazeState.placeOrReplace(type, position));
  }

  /**
   * Removes any placed type from one grid cell during the build phase.
   *
   * @param position cell to clear
   * @return the domain edit result, or empty when editing is inactive
   */
  public Optional<MazeEditResult> removeCell(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (gamePhase != GamePhase.BUILDING) {
      return Optional.empty();
    }
    return Optional.of(applyEdit(mazeState.remove(position), position));
  }

  /**
   * Moves one placed type atomically during the build phase.
   *
   * @param source occupied source cell
   * @param destination empty destination cell
   * @return the domain edit result, or empty when editing is inactive
   */
  public Optional<MazeEditResult> moveCell(GridPosition source, GridPosition destination) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(destination, "destination");
    if (gamePhase != GamePhase.BUILDING) {
      return Optional.empty();
    }
    return Optional.of(applyEdit(mazeState.move(source, destination), destination));
  }

  /** Evaluates a placed-cell move without publishing maze, inventory, or feedback changes. */
  public Optional<MazeEditResult> previewMoveCell(GridPosition source, GridPosition destination) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(destination, "destination");
    if (gamePhase != GamePhase.BUILDING) {
      return Optional.empty();
    }
    return Optional.of(mazeState.move(source, destination));
  }

  /** Places a Wall through the shared atomic edit path for compatibility with released tests. */
  public void placeWall(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (gamePhase != GamePhase.BUILDING) {
      return;
    }
    if (!position.isWithin(levelDefinition.gridSize()) || !mazeState.hasWallAt(position)) {
      applyEdit(mazeState.placeOrReplace(PlaceableCellType.WALL, position), position);
    }
  }

  /** Clears a Wall through the shared atomic edit path for compatibility with released tests. */
  public void clearWall(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (gamePhase == GamePhase.BUILDING && mazeState.hasWallAt(position)) {
      applyEdit(mazeState.remove(position), position);
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
    initializeSolverSimulations();
  }

  /**
   * Returns whether the completed result passed the level target.
   *
   * @return true when result phase is active and elapsed solve time exceeded the target
   */
  public boolean resultPassed() {
    return GameResultEvaluator.passedAll(gamePhase, solverRunResults, levelDefinition);
  }

  /**
   * Returns whether another level can be selected after this result.
   *
   * @return true when a passing result unlocked the next catalog entry
   */
  public boolean hasNextLevel() {
    return nextLevelId().isPresent();
  }

  /**
   * Returns the next unlocked level after a passing result.
   *
   * @return stable next level id when advancement is available
   */
  public Optional<String> nextLevelId() {
    if (!resultPassed()) {
      return Optional.empty();
    }
    List<LevelProgress> progress = levelProgress();
    for (int index = 0; index < progress.size() - 1; index++) {
      if (progress.get(index).levelDefinition().id().equals(levelDefinition.id())) {
        LevelProgress nextLevel = progress.get(index + 1);
        return nextLevel.unlocked()
            ? Optional.of(nextLevel.levelDefinition().id())
            : Optional.empty();
      }
    }
    return Optional.empty();
  }

  /**
   * Advances the active solver simulation.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  public void updateSolverRun(float deltaSeconds) {
    if ((gamePhase != GamePhase.SOLVER_RUNNING && gamePhase != GamePhase.REPLAY)
        || solverSimulations.isEmpty()
        || solverRunResults.stream()
            .noneMatch(result -> result.status() == SolverRunStatus.RUNNING)) {
      return;
    }
    boolean shouldRecordBestResult = gamePhase == GamePhase.SOLVER_RUNNING;
    long deltaMillis = Math.max(0L, Math.round(deltaSeconds * 1000.0F));
    List<SolverRunResult> updatedResults = new ArrayList<>();
    for (int index = 0; index < solverSimulations.size(); index++) {
      SolverRunResult current = solverRunResults.get(index);
      updatedResults.add(
          current.status() == SolverRunStatus.RUNNING
              ? solverSimulations.get(index).update(Duration.ofMillis(deltaMillis))
              : current);
    }
    solverRunResults = List.copyOf(updatedResults);
    if (solverRunResults.stream().noneMatch(result -> result.status() == SolverRunStatus.RUNNING)) {
      gamePhase = GamePhase.RESULT;
      if (shouldRecordBestResult && resultPassed()) {
        recordBestResult(bestResultFromCompletedRuns());
      }
    }
  }

  private BestResult bestResultFromCompletedRuns() {
    Duration shortestElapsed =
        solverRunResults.stream()
            .map(SolverRunResult::elapsedTime)
            .min(Duration::compareTo)
            .orElseThrow();
    int totalMoves = solverRunResults.stream().mapToInt(SolverRunResult::moveCount).sum();
    return new BestResult(shortestElapsed, totalMoves);
  }

  private void recordBestResult(BestResult candidate) {
    if (candidate.beats(bestResult)) {
      bestResult = candidate;
      bestResults.put(levelDefinition.id(), candidate);
      saveBestResult(candidate);
    }
  }

  private boolean isLevelUnlocked(String levelId) {
    return levelProgress().stream()
        .anyMatch(
            progress -> progress.unlocked() && progress.levelDefinition().id().equals(levelId));
  }

  private void loadBestResults() {
    for (LevelDefinition level : levelCatalog.levels()) {
      loadBestResult(level.id()).ifPresent(result -> bestResults.put(level.id(), result));
    }
  }

  private Optional<BestResult> loadBestResult(String levelId) {
    try {
      return bestResultStore.load(levelId);
    } catch (RuntimeException exception) {
      return Optional.empty();
    }
  }

  private void saveBestResult(BestResult candidate) {
    try {
      bestResultStore.save(levelDefinition.id(), candidate);
    } catch (RuntimeException exception) {
      return;
    }
  }

  private MazeEditResult applyEdit(MazeEditResult result, GridPosition rejectedDestination) {
    if (result.accepted()) {
      mazeState = result.mazeState();
    } else {
      rejectedPosition = rejectedDestination;
      rejectedFlashRemainingSeconds = REJECTED_FLASH_SECONDS;
    }
    return result;
  }

  /*
   * Resets all model state and moves to the requested phase.
   */
  private void initializeLevelState(LevelDefinition selectedLevel, GamePhase initialPhase) {
    levelDefinition = Objects.requireNonNull(selectedLevel, "selectedLevel");
    bestResult = bestResults.get(levelDefinition.id());
    mazeState = MazeState.empty(levelDefinition);
    selectedCellType = initialSelectedCellType(levelDefinition);
    buildTimeRemainingSeconds = levelDefinition.buildTime().toMillis() / 1000.0F;
    rejectedPosition = null;
    rejectedFlashRemainingSeconds = 0.0F;
    runRequested = false;
    solverSimulations = List.of();
    solverRunResults = List.of();
    gamePhase = initialPhase;
  }

  private void initializeSolverSimulations() {
    solverSimulations =
        levelDefinition.solvers().stream()
            .map(solver -> SolverSimulationFactory.create(mazeState, solver))
            .toList();
    solverRunResults = solverSimulations.stream().map(SolverSimulation::result).toList();
  }

  private static PlaceableCellType initialSelectedCellType(LevelDefinition levelDefinition) {
    return levelDefinition.placeableCellSupplies().stream()
        .filter(entry -> entry.supply().available())
        .map(PlaceableCellSupply::type)
        .findFirst()
        .orElse(null);
  }
}
