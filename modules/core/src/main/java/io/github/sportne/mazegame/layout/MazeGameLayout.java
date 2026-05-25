package io.github.sportne.mazegame.layout;

import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridSize;
import java.util.ArrayList;
import java.util.List;

/** Creates declared frontend layouts for every Maze Game phase. */
public final class MazeGameLayout {
  /** Main menu title region id. */
  public static final String MAIN_MENU_TITLE = "main-menu.title";

  /** Main menu Start button id. */
  public static final String MAIN_MENU_START = "main-menu.start";

  /** Main menu Settings button id. */
  public static final String MAIN_MENU_SETTINGS = "main-menu.settings";

  /** Main menu Quit button id. */
  public static final String MAIN_MENU_QUIT = "main-menu.quit";

  /** Level select title region id. */
  public static final String LEVEL_SELECT_TITLE = "level-select.title";

  /** Prefix for level select card ids. */
  public static final String LEVEL_CARD_PREFIX = "level-select.level-";

  /** Level select Back button id. */
  public static final String LEVEL_SELECT_BACK = "level-select.back";

  /** Settings title region id. */
  public static final String SETTINGS_TITLE = "settings.title";

  /** Settings audio toggle id. */
  public static final String SETTINGS_AUDIO = "settings.audio";

  /** Settings Back button id. */
  public static final String SETTINGS_BACK = "settings.back";

  /** Playable grid id. */
  public static final String GAME_GRID = "game.grid";

  /** Build-phase title region id. */
  public static final String BUILD_TITLE = "build.title";

  /** Build timer region id. */
  public static final String BUILD_STATUS = "build.status";

  /** Build instructions region id. */
  public static final String BUILD_INSTRUCTIONS = "build.instructions";

  /** Build start button id. */
  public static final String BUILD_START = "build.start";

  /** Result status region id. */
  public static final String RESULT_STATUS = "result.status";

  /** Result time/move count region id. */
  public static final String RESULT_STATS = "result.stats";

  /** Result no-next-level region id. */
  public static final String RESULT_NO_NEXT_LEVEL = "result.no-next-level";

  /** Result Retry button id. */
  public static final String RESULT_RETRY = "result.retry";

  /** Result Replay button id. */
  public static final String RESULT_REPLAY = "result.replay";

  /** Result Main Menu button id. */
  public static final String RESULT_MAIN_MENU = "result.main-menu";

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

  /** Fraction of the shorter screen dimension used for the square grid. */
  private static final float GRID_SCREEN_RATIO = 0.62F;

  /** Pixel width of the primary build-phase button. */
  private static final float BUILD_BUTTON_WIDTH = 180.0F;

  /** Pixel height of the primary build-phase button. */
  private static final float BUILD_BUTTON_HEIGHT = 44.0F;

  /** Vertical space between the grid/instructions area and the primary button. */
  private static final float BUILD_BUTTON_GAP = 52.0F;

  /** Horizontal space between result-phase buttons. */
  private static final float RESULT_BUTTON_GAP = 12.0F;

  /** Result button height in pixels. */
  private static final float RESULT_BUTTON_HEIGHT = 44.0F;

  /** Result button width in pixels. */
  private static final float RESULT_BUTTON_WIDTH = 140.0F;

  /** Standard text region height. */
  private static final float TEXT_REGION_HEIGHT = 28.0F;

  /** Prevents instantiation of this layout factory. */
  private MazeGameLayout() {}

  /**
   * Creates a layout for the requested phase.
   *
   * @param phase game phase to describe
   * @param screenWidth viewport width in pixels
   * @param screenHeight viewport height in pixels
   * @param gridSize current level grid size
   * @return declared screen layout
   */
  public static ScreenLayout forPhase(
      GamePhase phase, int screenWidth, int screenHeight, GridSize gridSize) {
    return switch (phase) {
      case MAIN_MENU -> mainMenu(screenWidth, screenHeight);
      case LEVEL_SELECT -> levelSelect(screenWidth, screenHeight);
      case SETTINGS -> settings(screenWidth, screenHeight);
      case BUILDING -> building(screenWidth, screenHeight, gridSize);
      case MOUSE_RUNNING, REPLAY -> running(phase, screenWidth, screenHeight, gridSize);
      case RESULT -> result(screenWidth, screenHeight, gridSize);
    };
  }

  /**
   * Returns the stable id for a level card.
   *
   * @param levelNumber one-based level number
   * @return stable element id
   */
  public static String levelCardId(int levelNumber) {
    return LEVEL_CARD_PREFIX + levelNumber;
  }

  private static ScreenLayout mainMenu(int screenWidth, int screenHeight) {
    List<LayoutElement> elements = new ArrayList<>();
    elements.add(
        text(MAIN_MENU_TITLE, centered(screenWidth, screenHeight / 2.0F + 140.0F, 260.0F, 32.0F)));
    elements.add(button(MAIN_MENU_START, menuButton(screenWidth, screenHeight, 0)));
    elements.add(button(MAIN_MENU_SETTINGS, menuButton(screenWidth, screenHeight, 1)));
    elements.add(button(MAIN_MENU_QUIT, menuButton(screenWidth, screenHeight, 2)));
    return screen(GamePhase.MAIN_MENU, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout levelSelect(int screenWidth, int screenHeight) {
    List<LayoutElement> elements = new ArrayList<>();
    elements.add(
        text(
            LEVEL_SELECT_TITLE,
            centered(screenWidth, screenHeight / 2.0F + 180.0F, 260.0F, 32.0F)));
    for (int index = 0; index < 6; index++) {
      elements.add(button(levelCardId(index + 1), levelButton(screenWidth, screenHeight, index)));
    }
    elements.add(button(LEVEL_SELECT_BACK, backButton()));
    return screen(GamePhase.LEVEL_SELECT, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout settings(int screenWidth, int screenHeight) {
    List<LayoutElement> elements = new ArrayList<>();
    elements.add(
        text(SETTINGS_TITLE, centered(screenWidth, screenHeight / 2.0F + 140.0F, 260.0F, 32.0F)));
    elements.add(button(SETTINGS_AUDIO, menuButton(screenWidth, screenHeight, 0)));
    elements.add(button(SETTINGS_BACK, backButton()));
    return screen(GamePhase.SETTINGS, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout building(int screenWidth, int screenHeight, GridSize gridSize) {
    List<LayoutElement> elements = gameplayBase(screenWidth, screenHeight, gridSize);
    ScreenRectangle grid = gridRectangle(screenWidth, screenHeight, gridSize);
    elements.add(
        text(
            BUILD_TITLE,
            new ScreenRectangle(grid.x(), screenHeight - 60.0F, 220.0F, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            BUILD_STATUS,
            new ScreenRectangle(grid.x(), grid.top() + 10.0F, 260.0F, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            BUILD_INSTRUCTIONS,
            new ScreenRectangle(grid.x(), grid.y() - 34.0F, 320.0F, TEXT_REGION_HEIGHT)));
    elements.add(button(BUILD_START, buildStartButton(screenWidth, screenHeight, gridSize)));
    return screen(GamePhase.BUILDING, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout running(
      GamePhase phase, int screenWidth, int screenHeight, GridSize gridSize) {
    return screen(
        phase, screenWidth, screenHeight, gameplayBase(screenWidth, screenHeight, gridSize));
  }

  private static ScreenLayout result(int screenWidth, int screenHeight, GridSize gridSize) {
    List<LayoutElement> elements = gameplayBase(screenWidth, screenHeight, gridSize);
    ScreenRectangle grid = gridRectangle(screenWidth, screenHeight, gridSize);
    elements.add(
        text(
            RESULT_STATUS,
            new ScreenRectangle(grid.x(), grid.top() + 26.0F, 260.0F, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            RESULT_STATS,
            new ScreenRectangle(grid.x(), grid.top() + 2.0F, 360.0F, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            RESULT_NO_NEXT_LEVEL,
            new ScreenRectangle(grid.x(), grid.y() - 34.0F, 320.0F, TEXT_REGION_HEIGHT)));
    ScreenRectangle retry = resultRetryButton(screenWidth, screenHeight, gridSize);
    ScreenRectangle replay =
        new ScreenRectangle(
            retry.right() + RESULT_BUTTON_GAP,
            retry.y(),
            RESULT_BUTTON_WIDTH,
            RESULT_BUTTON_HEIGHT);
    ScreenRectangle mainMenu =
        new ScreenRectangle(
            replay.right() + RESULT_BUTTON_GAP,
            retry.y(),
            RESULT_BUTTON_WIDTH,
            RESULT_BUTTON_HEIGHT);
    elements.add(button(RESULT_RETRY, retry));
    elements.add(button(RESULT_REPLAY, replay));
    elements.add(button(RESULT_MAIN_MENU, mainMenu));
    return screen(GamePhase.RESULT, screenWidth, screenHeight, elements);
  }

  private static List<LayoutElement> gameplayBase(
      int screenWidth, int screenHeight, GridSize gridSize) {
    List<LayoutElement> elements = new ArrayList<>();
    elements.add(
        new LayoutElement(
            GAME_GRID,
            LayoutElementKind.GRID,
            gridRectangle(screenWidth, screenHeight, gridSize),
            LayoutFitPolicy.MUST_FIT));
    return elements;
  }

  private static ScreenLayout screen(
      GamePhase phase, int screenWidth, int screenHeight, List<LayoutElement> elements) {
    return new ScreenLayout(
        phase, new ScreenRectangle(0.0F, 0.0F, screenWidth, screenHeight), elements);
  }

  private static LayoutElement button(String id, ScreenRectangle bounds) {
    return new LayoutElement(id, LayoutElementKind.BUTTON, bounds, LayoutFitPolicy.MUST_FIT);
  }

  private static LayoutElement text(String id, ScreenRectangle bounds) {
    return new LayoutElement(id, LayoutElementKind.TEXT_REGION, bounds, LayoutFitPolicy.MUST_FIT);
  }

  private static ScreenRectangle centered(int screenWidth, float y, float width, float height) {
    return new ScreenRectangle(screenWidth / 2.0F - width / 2.0F, y, width, height);
  }

  private static ScreenRectangle menuButton(int screenWidth, int screenHeight, int index) {
    float left = screenWidth / 2.0F - MENU_BUTTON_WIDTH / 2.0F;
    float topButtonY = screenHeight / 2.0F + 54.0F;
    float y = topButtonY - index * (MENU_BUTTON_HEIGHT + MENU_BUTTON_GAP);
    return new ScreenRectangle(left, y, MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT);
  }

  private static ScreenRectangle levelButton(int screenWidth, int screenHeight, int index) {
    int row = index / 3;
    int column = index % 3;
    float totalWidth = 3.0F * LEVEL_BUTTON_WIDTH + 2.0F * LEVEL_BUTTON_GAP;
    float left = screenWidth / 2.0F - totalWidth / 2.0F;
    float topRowY = screenHeight / 2.0F + 38.0F;
    return new ScreenRectangle(
        left + column * (LEVEL_BUTTON_WIDTH + LEVEL_BUTTON_GAP),
        topRowY - row * (LEVEL_BUTTON_HEIGHT + LEVEL_BUTTON_GAP),
        LEVEL_BUTTON_WIDTH,
        LEVEL_BUTTON_HEIGHT);
  }

  private static ScreenRectangle backButton() {
    return new ScreenRectangle(40.0F, 40.0F, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT);
  }

  private static ScreenRectangle gridRectangle(
      int screenWidth, int screenHeight, GridSize gridSize) {
    int longestGridSide = Math.max(gridSize.rows(), gridSize.columns());
    float availableGridSize = Math.min(screenWidth, screenHeight) * GRID_SCREEN_RATIO;
    float cellSize = (float) Math.floor(availableGridSize / longestGridSide);
    float gridWidth = cellSize * gridSize.columns();
    float gridHeight = cellSize * gridSize.rows();
    float gridX = (screenWidth - gridWidth) / 2.0F;
    float gridY = (screenHeight - gridHeight) / 2.0F;
    return new ScreenRectangle(gridX, gridY, gridWidth, gridHeight);
  }

  private static ScreenRectangle buildStartButton(
      int screenWidth, int screenHeight, GridSize gridSize) {
    ScreenRectangle grid = gridRectangle(screenWidth, screenHeight, gridSize);
    float buttonX = (screenWidth - BUILD_BUTTON_WIDTH) / 2.0F;
    float buttonY = Math.max(24.0F, grid.y() - BUILD_BUTTON_GAP - BUILD_BUTTON_HEIGHT);
    return new ScreenRectangle(buttonX, buttonY, BUILD_BUTTON_WIDTH, BUILD_BUTTON_HEIGHT);
  }

  private static ScreenRectangle resultRetryButton(
      int screenWidth, int screenHeight, GridSize gridSize) {
    ScreenRectangle grid = gridRectangle(screenWidth, screenHeight, gridSize);
    ScreenRectangle startButton = buildStartButton(screenWidth, screenHeight, gridSize);
    float totalButtonWidth = 3.0F * RESULT_BUTTON_WIDTH + 2.0F * RESULT_BUTTON_GAP;
    float left = grid.x() + grid.width() / 2.0F - totalButtonWidth / 2.0F;
    return new ScreenRectangle(left, startButton.y(), RESULT_BUTTON_WIDTH, RESULT_BUTTON_HEIGHT);
  }
}
