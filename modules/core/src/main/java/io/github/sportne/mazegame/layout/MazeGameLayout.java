package io.github.sportne.mazegame.layout;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.state.GamePhase;
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

  /** Build Back button id. */
  public static final String BUILD_BACK = "build.back";

  /** Prefix for build-palette item ids. */
  public static final String BUILD_PALETTE_PREFIX = "build.palette-";

  /** Running-phase countdown region id. */
  public static final String RUN_STATUS = "run.status";

  /** Result status region id. */
  public static final String RESULT_STATUS = "result.status";

  /** Result time/move count region id. */
  public static final String RESULT_STATS = "result.stats";

  /** Result best saved run region id. */
  public static final String RESULT_BEST = "result.best";

  /** Result no-next-level region id. */
  public static final String RESULT_NO_NEXT_LEVEL = "result.no-next-level";

  /** Result Next Level button id. */
  public static final String RESULT_NEXT_LEVEL = "result.next-level";

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

  /** Maximum width of one build-palette item. */
  private static final float PALETTE_ITEM_WIDTH = 160.0F;

  /** Minimum height of one build-palette item. */
  private static final float PALETTE_ITEM_HEIGHT = 44.0F;

  /** Gap between build-palette items. */
  private static final float PALETTE_ITEM_GAP = 12.0F;

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

  /** Width below which the fixed three-column layout no longer has comfortable margins. */
  private static final int COMPACT_WIDTH = 800;

  /** Height below which gameplay controls move beside the grid. */
  private static final int COMPACT_HEIGHT = 600;

  /** Outer margin used by compact layouts. */
  private static final float COMPACT_MARGIN = 16.0F;

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
    return forPhase(phase, screenWidth, screenHeight, gridSize, true, 0, false);
  }

  /**
   * Creates a layout with platform-specific optional controls.
   *
   * @param phase game phase to describe
   * @param screenWidth viewport width in pixels
   * @param screenHeight viewport height in pixels
   * @param gridSize current level grid size
   * @param quitAvailable whether the platform offers a Quit command
   * @return declared screen layout
   */
  public static ScreenLayout forPhase(
      GamePhase phase,
      int screenWidth,
      int screenHeight,
      GridSize gridSize,
      boolean quitAvailable) {
    return forPhase(phase, screenWidth, screenHeight, gridSize, quitAvailable, 0, false);
  }

  /**
   * Creates a layout with presentation-dependent controls.
   *
   * @param phase game phase to describe
   * @param screenWidth viewport width in pixels
   * @param screenHeight viewport height in pixels
   * @param gridSize current level grid size
   * @param quitAvailable whether the platform offers a Quit command
   * @param levelCount number of authored level cards to show
   * @param hasNextLevel whether the result screen offers advancement
   * @return declared screen layout
   */
  public static ScreenLayout forPhase(
      GamePhase phase,
      int screenWidth,
      int screenHeight,
      GridSize gridSize,
      boolean quitAvailable,
      int levelCount,
      boolean hasNextLevel) {
    if (levelCount < 0) {
      throw new IllegalArgumentException("level count must not be negative");
    }
    ScreenLayout layout =
        switch (phase) {
          case MAIN_MENU -> mainMenu(screenWidth, screenHeight, quitAvailable);
          case LEVEL_SELECT -> levelSelect(screenWidth, screenHeight, levelCount);
          case SETTINGS -> settings(screenWidth, screenHeight);
          case BUILDING -> building(screenWidth, screenHeight, gridSize);
          case MOUSE_RUNNING, REPLAY -> running(phase, screenWidth, screenHeight, gridSize);
          case RESULT -> result(screenWidth, screenHeight, gridSize, hasNextLevel);
        };
    return new ScreenLayout(layout.phase(), layout.viewport(), layout.elements(), gridSize);
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

  /** Returns the stable layout id for a placeable palette type. */
  public static String paletteItemId(PlaceableCellType type) {
    return BUILD_PALETTE_PREFIX
        + switch (type) {
          case WALL -> "wall";
          case SLOW_FLOOR -> "slow-floor";
        };
  }

  private static ScreenLayout mainMenu(int screenWidth, int screenHeight, boolean quitAvailable) {
    if (isCompactLandscape(screenWidth, screenHeight)) {
      return compactLandscapeMainMenu(screenWidth, screenHeight, quitAvailable);
    }
    List<LayoutElement> elements = new ArrayList<>();
    elements.add(
        text(MAIN_MENU_TITLE, centered(screenWidth, screenHeight / 2.0F + 140.0F, 260.0F, 32.0F)));
    elements.add(button(MAIN_MENU_START, menuButton(screenWidth, screenHeight, 0)));
    elements.add(button(MAIN_MENU_SETTINGS, menuButton(screenWidth, screenHeight, 1)));
    if (quitAvailable) {
      elements.add(button(MAIN_MENU_QUIT, menuButton(screenWidth, screenHeight, 2)));
    }
    return screen(GamePhase.MAIN_MENU, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout levelSelect(int screenWidth, int screenHeight, int levelCount) {
    if (isCompact(screenWidth, screenHeight)) {
      return compactLevelSelect(screenWidth, screenHeight, levelCount);
    }
    List<LayoutElement> elements = new ArrayList<>();
    elements.add(
        text(
            LEVEL_SELECT_TITLE,
            centered(screenWidth, screenHeight / 2.0F + 180.0F, 260.0F, 32.0F)));
    for (int index = 0; index < levelCount; index++) {
      elements.add(button(levelCardId(index + 1), levelButton(screenWidth, screenHeight, index)));
    }
    elements.add(button(LEVEL_SELECT_BACK, backButton()));
    return screen(GamePhase.LEVEL_SELECT, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout settings(int screenWidth, int screenHeight) {
    if (isCompactLandscape(screenWidth, screenHeight)) {
      return compactLandscapeSettings(screenWidth, screenHeight);
    }
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
    if (isCompactLandscape(screenWidth, screenHeight)) {
      float panelX = compactPanelX(grid);
      float panelWidth = screenWidth - panelX - COMPACT_MARGIN;
      elements.add(
          text(
              BUILD_TITLE,
              new ScreenRectangle(panelX, screenHeight - 44.0F, panelWidth, TEXT_REGION_HEIGHT)));
      elements.add(
          text(
              BUILD_STATUS,
              new ScreenRectangle(panelX, screenHeight - 76.0F, panelWidth, TEXT_REGION_HEIGHT)));
      elements.add(
          text(
              BUILD_INSTRUCTIONS,
              new ScreenRectangle(panelX, screenHeight - 108.0F, panelWidth, TEXT_REGION_HEIGHT)));
      addPalette(elements, grid, screenWidth, screenHeight);
      elements.add(button(BUILD_BACK, buildActionButton(grid, screenWidth, screenHeight, 0)));
      elements.add(button(BUILD_START, buildActionButton(grid, screenWidth, screenHeight, 1)));
      return screen(GamePhase.BUILDING, screenWidth, screenHeight, elements);
    }
    if (isCompactPortrait(screenWidth, screenHeight)) {
      elements.add(
          text(
              BUILD_TITLE,
              new ScreenRectangle(
                  grid.x(), screenHeight - 44.0F, grid.width(), TEXT_REGION_HEIGHT)));
      elements.add(
          text(
              BUILD_STATUS,
              new ScreenRectangle(grid.x(), grid.top() + 10.0F, grid.width(), TEXT_REGION_HEIGHT)));
      elements.add(
          text(
              BUILD_INSTRUCTIONS,
              new ScreenRectangle(grid.x(), grid.y() - 34.0F, grid.width(), TEXT_REGION_HEIGHT)));
      addPalette(elements, grid, screenWidth, screenHeight);
      elements.add(button(BUILD_BACK, buildActionButton(grid, screenWidth, screenHeight, 0)));
      elements.add(button(BUILD_START, buildActionButton(grid, screenWidth, screenHeight, 1)));
      return screen(GamePhase.BUILDING, screenWidth, screenHeight, elements);
    }
    elements.add(
        text(
            BUILD_TITLE,
            new ScreenRectangle(grid.x(), screenHeight - 60.0F, 220.0F, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            BUILD_STATUS,
            new ScreenRectangle(grid.x(), grid.top() + 4.0F, 260.0F, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            BUILD_INSTRUCTIONS,
            new ScreenRectangle(grid.x(), grid.top() + 32.0F, 320.0F, TEXT_REGION_HEIGHT)));
    addPalette(elements, grid, screenWidth, screenHeight);
    elements.add(button(BUILD_BACK, buildActionButton(grid, screenWidth, screenHeight, 0)));
    elements.add(button(BUILD_START, buildActionButton(grid, screenWidth, screenHeight, 1)));
    return screen(GamePhase.BUILDING, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout running(
      GamePhase phase, int screenWidth, int screenHeight, GridSize gridSize) {
    List<LayoutElement> elements = gameplayBase(screenWidth, screenHeight, gridSize);
    ScreenRectangle grid = gridRectangle(screenWidth, screenHeight, gridSize);
    ScreenRectangle statusBounds =
        isCompactLandscape(screenWidth, screenHeight)
            ? new ScreenRectangle(
                compactPanelX(grid),
                screenHeight - 76.0F,
                screenWidth - compactPanelX(grid) - COMPACT_MARGIN,
                TEXT_REGION_HEIGHT)
            : new ScreenRectangle(grid.x(), grid.top() + 10.0F, 260.0F, TEXT_REGION_HEIGHT);
    elements.add(text(RUN_STATUS, statusBounds));
    return screen(phase, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout result(
      int screenWidth, int screenHeight, GridSize gridSize, boolean hasNextLevel) {
    List<LayoutElement> elements = gameplayBase(screenWidth, screenHeight, gridSize);
    ScreenRectangle grid = gridRectangle(screenWidth, screenHeight, gridSize);
    if (isCompactLandscape(screenWidth, screenHeight)) {
      return compactLandscapeResult(screenWidth, screenHeight, grid, elements, hasNextLevel);
    }
    elements.add(
        text(
            RESULT_STATUS,
            new ScreenRectangle(grid.x(), grid.top() + 50.0F, 260.0F, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            RESULT_STATS,
            new ScreenRectangle(grid.x(), grid.top() + 26.0F, 360.0F, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            RESULT_BEST,
            new ScreenRectangle(grid.x(), grid.top() + 2.0F, 360.0F, TEXT_REGION_HEIGHT)));
    if (!hasNextLevel) {
      elements.add(
          text(
              RESULT_NO_NEXT_LEVEL,
              new ScreenRectangle(grid.x(), grid.y() - 34.0F, 320.0F, TEXT_REGION_HEIGHT)));
    }
    int buttonCount = hasNextLevel ? 4 : 3;
    ScreenRectangle retry = resultButton(screenWidth, screenHeight, gridSize, buttonCount, 0);
    float resultButtonWidth = retry.width();
    float resultButtonGap = isCompactPortrait(screenWidth, screenHeight) ? 8.0F : RESULT_BUTTON_GAP;
    ScreenRectangle replay =
        new ScreenRectangle(
            retry.right() + resultButtonGap, retry.y(), resultButtonWidth, RESULT_BUTTON_HEIGHT);
    ScreenRectangle mainMenu =
        new ScreenRectangle(
            replay.right() + resultButtonGap, retry.y(), resultButtonWidth, RESULT_BUTTON_HEIGHT);
    elements.add(button(RESULT_RETRY, retry));
    elements.add(button(RESULT_REPLAY, replay));
    elements.add(button(RESULT_MAIN_MENU, mainMenu));
    if (hasNextLevel) {
      elements.add(
          button(
              RESULT_NEXT_LEVEL,
              new ScreenRectangle(
                  mainMenu.right() + resultButtonGap,
                  retry.y(),
                  resultButtonWidth,
                  RESULT_BUTTON_HEIGHT)));
    }
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
    float availableGridSize;
    if (isCompactLandscape(screenWidth, screenHeight)) {
      availableGridSize =
          Math.min(screenHeight - 2.0F * COMPACT_MARGIN, Math.min(240.0F, screenWidth - 320.0F));
    } else if (isCompactPortrait(screenWidth, screenHeight)) {
      availableGridSize = Math.min(screenWidth - 2.0F * COMPACT_MARGIN, screenHeight * 0.48F);
    } else {
      availableGridSize =
          Math.min(Math.min(screenWidth, screenHeight) * GRID_SCREEN_RATIO, screenHeight - 240.0F);
    }
    float cellSize = (float) Math.floor(availableGridSize / longestGridSide);
    float gridWidth = cellSize * gridSize.columns();
    float gridHeight = cellSize * gridSize.rows();
    float gridX =
        isCompactLandscape(screenWidth, screenHeight)
            ? COMPACT_MARGIN
            : (screenWidth - gridWidth) / 2.0F;
    float gridY = (screenHeight - gridHeight) / 2.0F;
    return new ScreenRectangle(gridX, gridY, gridWidth, gridHeight);
  }

  private static ScreenRectangle buildStartButton(
      int screenWidth, int screenHeight, GridSize gridSize) {
    ScreenRectangle grid = gridRectangle(screenWidth, screenHeight, gridSize);
    float buttonY = Math.max(24.0F, grid.y() - BUILD_BUTTON_GAP - BUILD_BUTTON_HEIGHT);
    return new ScreenRectangle(
        (screenWidth - BUILD_BUTTON_WIDTH) / 2.0F,
        buttonY,
        BUILD_BUTTON_WIDTH,
        BUILD_BUTTON_HEIGHT);
  }

  private static ScreenRectangle buildActionButton(
      ScreenRectangle grid, int screenWidth, int screenHeight, int index) {
    boolean landscape = isCompactLandscape(screenWidth, screenHeight);
    float areaX = landscape ? compactPanelX(grid) : COMPACT_MARGIN;
    float areaWidth = screenWidth - areaX - COMPACT_MARGIN;
    float gap = RESULT_BUTTON_GAP;
    float buttonWidth = Math.min(BUILD_BUTTON_WIDTH, (areaWidth - gap) / 2.0F);
    float totalWidth = 2.0F * buttonWidth + gap;
    float left = areaX + (areaWidth - totalWidth) / 2.0F;
    float y;
    if (landscape) {
      y = Math.max(COMPACT_MARGIN, screenHeight / 2.0F - BUILD_BUTTON_HEIGHT / 2.0F);
    } else if (isCompactPortrait(screenWidth, screenHeight)) {
      y = COMPACT_MARGIN;
    } else {
      y = Math.max(24.0F, grid.y() - BUILD_BUTTON_GAP - BUILD_BUTTON_HEIGHT);
    }
    return new ScreenRectangle(
        left + index * (buttonWidth + gap), y, buttonWidth, BUILD_BUTTON_HEIGHT);
  }

  private static void addPalette(
      List<LayoutElement> elements, ScreenRectangle grid, int screenWidth, int screenHeight) {
    for (PlaceableCellType type : PlaceableCellType.values()) {
      elements.add(button(paletteItemId(type), paletteItem(grid, screenWidth, screenHeight, type)));
    }
  }

  private static ScreenRectangle paletteItem(
      ScreenRectangle grid, int screenWidth, int screenHeight, PlaceableCellType type) {
    boolean landscape = isCompactLandscape(screenWidth, screenHeight);
    float areaX = landscape ? compactPanelX(grid) : COMPACT_MARGIN;
    float areaWidth = screenWidth - areaX - COMPACT_MARGIN;
    float itemWidth = Math.min(PALETTE_ITEM_WIDTH, (areaWidth - PALETTE_ITEM_GAP) / 2.0F);
    float totalWidth = 2.0F * itemWidth + PALETTE_ITEM_GAP;
    float left = areaX + (areaWidth - totalWidth) / 2.0F;
    float height = isCompactPortrait(screenWidth, screenHeight) ? 56.0F : PALETTE_ITEM_HEIGHT;
    float y;
    if (landscape) {
      y = COMPACT_MARGIN;
    } else {
      ScreenRectangle action = buildActionButton(grid, screenWidth, screenHeight, 0);
      y = action.top() + (isCompactPortrait(screenWidth, screenHeight) ? 12.0F : 4.0F);
    }
    int index =
        switch (type) {
          case WALL -> 0;
          case SLOW_FLOOR -> 1;
        };
    return new ScreenRectangle(left + index * (itemWidth + PALETTE_ITEM_GAP), y, itemWidth, height);
  }

  private static ScreenRectangle resultButton(
      int screenWidth, int screenHeight, GridSize gridSize, int buttonCount, int index) {
    ScreenRectangle grid = gridRectangle(screenWidth, screenHeight, gridSize);
    ScreenRectangle startButton = buildStartButton(screenWidth, screenHeight, gridSize);
    float gap = isCompactPortrait(screenWidth, screenHeight) ? 8.0F : RESULT_BUTTON_GAP;
    float buttonWidth =
        isCompactPortrait(screenWidth, screenHeight)
            ? (screenWidth - 2.0F * COMPACT_MARGIN - (buttonCount - 1) * gap) / buttonCount
            : RESULT_BUTTON_WIDTH;
    float totalButtonWidth = buttonCount * buttonWidth + (buttonCount - 1) * gap;
    float left = grid.x() + grid.width() / 2.0F - totalButtonWidth / 2.0F;
    float buttonY = isCompactPortrait(screenWidth, screenHeight) ? COMPACT_MARGIN : startButton.y();
    return new ScreenRectangle(
        left + index * (buttonWidth + gap), buttonY, buttonWidth, RESULT_BUTTON_HEIGHT);
  }

  private static ScreenLayout compactLandscapeMainMenu(
      int screenWidth, int screenHeight, boolean quitAvailable) {
    List<LayoutElement> elements = new ArrayList<>();
    elements.add(
        text(
            MAIN_MENU_TITLE,
            centered(screenWidth, screenHeight - 44.0F, 260.0F, TEXT_REGION_HEIGHT)));
    int buttonCount = quitAvailable ? 3 : 2;
    float gap = 12.0F;
    float buttonWidth =
        Math.min(
            180.0F, (screenWidth - 2.0F * COMPACT_MARGIN - gap * (buttonCount - 1)) / buttonCount);
    float totalWidth = buttonCount * buttonWidth + (buttonCount - 1) * gap;
    float left = (screenWidth - totalWidth) / 2.0F;
    float y = (screenHeight - MENU_BUTTON_HEIGHT) / 2.0F - 8.0F;
    elements.add(
        button(MAIN_MENU_START, new ScreenRectangle(left, y, buttonWidth, MENU_BUTTON_HEIGHT)));
    elements.add(
        button(
            MAIN_MENU_SETTINGS,
            new ScreenRectangle(left + buttonWidth + gap, y, buttonWidth, MENU_BUTTON_HEIGHT)));
    if (quitAvailable) {
      elements.add(
          button(
              MAIN_MENU_QUIT,
              new ScreenRectangle(
                  left + 2.0F * (buttonWidth + gap), y, buttonWidth, MENU_BUTTON_HEIGHT)));
    }
    return screen(GamePhase.MAIN_MENU, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout compactLevelSelect(
      int screenWidth, int screenHeight, int levelCount) {
    List<LayoutElement> elements = new ArrayList<>();
    boolean landscape = isCompactLandscape(screenWidth, screenHeight);
    int columns = landscape ? 3 : 2;
    float gap = 12.0F;
    float buttonWidth =
        Math.min(160.0F, (screenWidth - 2.0F * COMPACT_MARGIN - gap * (columns - 1)) / columns);
    float buttonHeight = 80.0F;
    float totalWidth = columns * buttonWidth + (columns - 1) * gap;
    float left = (screenWidth - totalWidth) / 2.0F;
    float topRowY = landscape ? screenHeight / 2.0F + 16.0F : screenHeight / 2.0F + 100.0F;
    elements.add(
        text(
            LEVEL_SELECT_TITLE,
            centered(screenWidth, screenHeight - 44.0F, 260.0F, TEXT_REGION_HEIGHT)));
    for (int index = 0; index < levelCount; index++) {
      int row = index / columns;
      int column = index % columns;
      elements.add(
          button(
              levelCardId(index + 1),
              new ScreenRectangle(
                  left + column * (buttonWidth + gap),
                  topRowY - row * (buttonHeight + gap),
                  buttonWidth,
                  buttonHeight)));
    }
    elements.add(button(LEVEL_SELECT_BACK, compactBackButton()));
    return screen(GamePhase.LEVEL_SELECT, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout compactLandscapeSettings(int screenWidth, int screenHeight) {
    List<LayoutElement> elements = new ArrayList<>();
    elements.add(
        text(
            SETTINGS_TITLE,
            centered(screenWidth, screenHeight - 44.0F, 260.0F, TEXT_REGION_HEIGHT)));
    elements.add(
        button(
            SETTINGS_AUDIO,
            centered(
                screenWidth,
                (screenHeight - MENU_BUTTON_HEIGHT) / 2.0F,
                MENU_BUTTON_WIDTH,
                MENU_BUTTON_HEIGHT)));
    elements.add(button(SETTINGS_BACK, compactBackButton()));
    return screen(GamePhase.SETTINGS, screenWidth, screenHeight, elements);
  }

  private static ScreenLayout compactLandscapeResult(
      int screenWidth,
      int screenHeight,
      ScreenRectangle grid,
      List<LayoutElement> elements,
      boolean hasNextLevel) {
    float panelX = compactPanelX(grid);
    float panelWidth = screenWidth - panelX - COMPACT_MARGIN;
    elements.add(
        text(
            RESULT_STATUS,
            new ScreenRectangle(panelX, screenHeight - 44.0F, panelWidth, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            RESULT_STATS,
            new ScreenRectangle(panelX, screenHeight - 76.0F, panelWidth, TEXT_REGION_HEIGHT)));
    elements.add(
        text(
            RESULT_BEST,
            new ScreenRectangle(panelX, screenHeight - 108.0F, panelWidth, TEXT_REGION_HEIGHT)));
    if (!hasNextLevel) {
      elements.add(
          text(
              RESULT_NO_NEXT_LEVEL,
              new ScreenRectangle(panelX, COMPACT_MARGIN, panelWidth, TEXT_REGION_HEIGHT)));
    }
    float gap = 8.0F;
    int buttonCount = hasNextLevel ? 4 : 3;
    float buttonWidth = (panelWidth - (buttonCount - 1) * gap) / buttonCount;
    float buttonY = 64.0F;
    elements.add(
        button(
            RESULT_RETRY, new ScreenRectangle(panelX, buttonY, buttonWidth, RESULT_BUTTON_HEIGHT)));
    elements.add(
        button(
            RESULT_REPLAY,
            new ScreenRectangle(
                panelX + buttonWidth + gap, buttonY, buttonWidth, RESULT_BUTTON_HEIGHT)));
    elements.add(
        button(
            RESULT_MAIN_MENU,
            new ScreenRectangle(
                panelX + 2.0F * (buttonWidth + gap), buttonY, buttonWidth, RESULT_BUTTON_HEIGHT)));
    if (hasNextLevel) {
      elements.add(
          button(
              RESULT_NEXT_LEVEL,
              new ScreenRectangle(
                  panelX + 3.0F * (buttonWidth + gap),
                  buttonY,
                  buttonWidth,
                  RESULT_BUTTON_HEIGHT)));
    }
    return screen(GamePhase.RESULT, screenWidth, screenHeight, elements);
  }

  private static ScreenRectangle compactBackButton() {
    return new ScreenRectangle(
        COMPACT_MARGIN, COMPACT_MARGIN, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT);
  }

  private static float compactPanelX(ScreenRectangle grid) {
    return grid.right() + 24.0F;
  }

  private static boolean isCompact(int screenWidth, int screenHeight) {
    return isCompactLandscape(screenWidth, screenHeight)
        || isCompactPortrait(screenWidth, screenHeight);
  }

  private static boolean isCompactLandscape(int screenWidth, int screenHeight) {
    return screenWidth > screenHeight
        && (screenWidth < COMPACT_WIDTH || screenHeight < COMPACT_HEIGHT);
  }

  private static boolean isCompactPortrait(int screenWidth, int screenHeight) {
    return screenWidth <= screenHeight && screenWidth < COMPACT_WIDTH;
  }
}
