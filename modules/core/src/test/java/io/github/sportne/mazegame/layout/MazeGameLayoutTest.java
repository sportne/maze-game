package io.github.sportne.mazegame.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.state.GamePhase;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

final class MazeGameLayoutTest {
  private static final GridSize GRID_SIZE = GridSize.square(5);

  @ParameterizedTest
  @MethodSource("phaseAndViewportArguments")
  void layoutsAreValidForRepresentativeViewports(
      GamePhase phase,
      int screenWidth,
      int screenHeight,
      LevelDefinition level,
      boolean hasNextLevel) {
    ScreenLayout layout = layout(phase, screenWidth, screenHeight, level.gridSize(), hasNextLevel);

    assertTrue(
        LayoutValidator.validate(layout).isEmpty(),
        () -> "Expected valid layout for " + phase + " at " + screenWidth + "x" + screenHeight);
  }

  @ParameterizedTest
  @EnumSource(GamePhase.class)
  void allCurrentElementsMustFitTheViewport(GamePhase phase) {
    ScreenLayout layout = layout(phase, 1280, 720, false);

    assertTrue(
        layout.elements().stream()
            .allMatch(element -> element.fitPolicy() == LayoutFitPolicy.MUST_FIT));
  }

  @ParameterizedTest
  @MethodSource("expectedElementArguments")
  void layoutsDeclareExpectedElements(GamePhase phase, List<String> expectedIds) {
    ScreenLayout layout = layout(phase, 1280, 720, false);

    assertEquals(expectedIds, layout.elements().stream().map(LayoutElement::id).toList());
  }

  @Test
  void mainMenuOmitsQuitWhenThePlatformCannotExit() {
    ScreenLayout layout = MazeGameLayout.forPhase(GamePhase.MAIN_MENU, 1280, 720, GRID_SIZE, false);

    assertFalse(layout.element(MazeGameLayout.MAIN_MENU_QUIT).isPresent());
    assertTrue(LayoutValidator.validate(layout).isEmpty());
  }

  @ParameterizedTest
  @EnumSource(GamePhase.class)
  void phonePortraitControlsAndGridRemainTouchable(GamePhase phase) {
    assertMobileTargets(phase, 390, 844);
  }

  @ParameterizedTest
  @EnumSource(GamePhase.class)
  void constrainedLandscapeControlsAndGridRemainTouchable(GamePhase phase) {
    assertMobileTargets(phase, 844, 286);
  }

  @ParameterizedTest
  @EnumSource(GamePhase.class)
  void safeContentLandscapeControlsAndGridRemainTouchable(GamePhase phase) {
    assertMobileTargets(phase, 756, 286);
  }

  @ParameterizedTest
  @EnumSource(GamePhase.class)
  void supportedIntermediateViewportsUseCompactLayouts(GamePhase phase) {
    assertMobileTargets(phase, 601, 844);
    assertMobileTargets(phase, 600, 421);
    assertMobileTargets(phase, 799, 600);
  }

  @Test
  void desktopReferenceLayoutRemainsUnchanged() {
    ScreenLayout layout = layout(GamePhase.BUILDING, 1280, 720, false);

    assertEquals(
        new ScreenRectangle(417.5F, 137.5F, 445.0F, 445.0F),
        layout.bounds(MazeGameLayout.GAME_GRID));
    assertEquals(
        new ScreenRectangle(646.0F, 41.5F, 180.0F, 44.0F),
        layout.bounds(MazeGameLayout.BUILD_START));
    assertEquals(
        new ScreenRectangle(454.0F, 41.5F, 180.0F, 44.0F),
        layout.bounds(MazeGameLayout.BUILD_BACK));
    assertEquals(
        new ScreenRectangle(578.0F, 89.5F, 56.0F, 44.0F),
        layout.bounds(MazeGameLayout.paletteItemId(PlaceableCellType.WALL)));
    assertEquals(
        new ScreenRectangle(646.0F, 89.5F, 56.0F, 44.0F),
        layout.bounds(MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR)));
  }

  @Test
  void paletteCentersOnlyTheInitiallyUsableTypes() {
    ScreenLayout wallOnly =
        MazeGameLayout.forPhase(
            GamePhase.BUILDING,
            1280,
            720,
            GRID_SIZE,
            false,
            3,
            false,
            List.of(PlaceableCellType.WALL));
    ScreenRectangle wall = wallOnly.bounds(MazeGameLayout.paletteItemId(PlaceableCellType.WALL));

    assertEquals(640.0F, wall.x() + wall.width() / 2.0F);
    assertTrue(
        wallOnly.element(MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR)).isEmpty());
    assertTrue(LayoutValidator.validate(wallOnly).isEmpty());

    ScreenLayout empty =
        MazeGameLayout.forPhase(
            GamePhase.BUILDING, 1280, 720, GRID_SIZE, false, 3, false, List.of());
    assertTrue(empty.element(MazeGameLayout.paletteItemId(PlaceableCellType.WALL)).isEmpty());
    assertTrue(empty.element(MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR)).isEmpty());
    assertTrue(LayoutValidator.validate(empty).isEmpty());
  }

  @Test
  void paletteLayoutRejectsDuplicateTypes() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            MazeGameLayout.forPhase(
                GamePhase.BUILDING,
                1280,
                720,
                GRID_SIZE,
                false,
                3,
                false,
                List.of(PlaceableCellType.WALL, PlaceableCellType.WALL)));
  }

  @Test
  void paletteUsesTheBottomRegionWithoutOverlappingGridOrActions() {
    for (int[] viewport :
        List.of(
            new int[] {1280, 720},
            new int[] {390, 844},
            new int[] {844, 286},
            new int[] {756, 286})) {
      ScreenLayout layout =
          MazeGameLayout.forPhase(
              GamePhase.BUILDING,
              viewport[0],
              viewport[1],
              Levels.levelThree().gridSize(),
              false,
              3,
              false);
      ScreenRectangle grid = layout.bounds(MazeGameLayout.GAME_GRID);
      ScreenRectangle wall = layout.bounds(MazeGameLayout.paletteItemId(PlaceableCellType.WALL));
      ScreenRectangle slow =
          layout.bounds(MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR));

      assertFalse(wall.overlaps(slow));
      assertFalse(wall.overlaps(grid));
      assertFalse(slow.overlaps(grid));
      assertTrue(wall.width() >= 44.0F && wall.height() >= 44.0F);
      assertTrue(slow.width() >= 44.0F && slow.height() >= 44.0F);
      assertTrue(LayoutValidator.validate(layout).isEmpty());
    }
  }

  private static void assertMobileTargets(GamePhase phase, int width, int height) {
    for (LevelDefinition level : Levels.catalog().levels()) {
      GridSize gridSize = level.gridSize();
      for (boolean hasNextLevel : List.of(false, true)) {
        ScreenLayout layout =
            MazeGameLayout.forPhase(
                phase,
                width,
                height,
                gridSize,
                false,
                Levels.catalog().levels().size(),
                hasNextLevel);

        assertTrue(
            LayoutValidator.validate(layout).isEmpty(),
            () -> LayoutValidator.validate(layout).toString());
        assertTrue(
            layout.elements().stream()
                .filter(element -> element.kind() == LayoutElementKind.BUTTON)
                .allMatch(
                    element ->
                        element.bounds().width() >= 44.0F && element.bounds().height() >= 44.0F));
        layout
            .element(MazeGameLayout.GAME_GRID)
            .ifPresent(
                grid ->
                    assertTrue(
                        grid.bounds().width() / gridSize.columns() >= 32.0F,
                        () -> "grid cell too small for " + phase + " at " + width + "x" + height));
      }
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 5})
  void levelSelectionDeclaresExactlyTheRequestedCards(int levelCount) {
    ScreenLayout layout =
        MazeGameLayout.forPhase(
            GamePhase.LEVEL_SELECT, 1280, 720, GRID_SIZE, true, levelCount, false);

    assertEquals(
        levelCount,
        layout.elements().stream()
            .map(LayoutElement::id)
            .filter(id -> id.startsWith(MazeGameLayout.LEVEL_CARD_PREFIX))
            .count());
  }

  @Test
  void resultLayoutShowsOnlyTheValidProgressionAlternative() {
    for (int[] viewport :
        List.of(new int[] {390, 844}, new int[] {844, 286}, new int[] {1280, 720})) {
      for (LevelDefinition level : Levels.catalog().levels()) {
        GridSize gridSize = level.gridSize();
        ScreenLayout withNext = layout(GamePhase.RESULT, viewport[0], viewport[1], gridSize, true);
        ScreenLayout withoutNext =
            layout(GamePhase.RESULT, viewport[0], viewport[1], gridSize, false);

        assertTrue(withNext.element(MazeGameLayout.RESULT_NEXT_LEVEL).isPresent());
        assertFalse(withNext.element(MazeGameLayout.RESULT_NO_NEXT_LEVEL).isPresent());
        assertFalse(withoutNext.element(MazeGameLayout.RESULT_NEXT_LEVEL).isPresent());
        assertTrue(withoutNext.element(MazeGameLayout.RESULT_NO_NEXT_LEVEL).isPresent());
        assertTrue(LayoutValidator.validate(withNext).isEmpty());
        assertTrue(LayoutValidator.validate(withoutNext).isEmpty());
      }
    }
  }

  private static ScreenLayout layout(
      GamePhase phase, int screenWidth, int screenHeight, boolean hasNextLevel) {
    return layout(phase, screenWidth, screenHeight, GRID_SIZE, hasNextLevel);
  }

  private static ScreenLayout layout(
      GamePhase phase, int screenWidth, int screenHeight, GridSize gridSize, boolean hasNextLevel) {
    return MazeGameLayout.forPhase(
        phase, screenWidth, screenHeight, gridSize, true, 3, hasNextLevel);
  }

  private static Stream<Arguments> phaseAndViewportArguments() {
    List<int[]> viewports =
        List.of(
            new int[] {1280, 720},
            new int[] {390, 844},
            new int[] {844, 286},
            new int[] {756, 286},
            new int[] {601, 844},
            new int[] {600, 421},
            new int[] {799, 600},
            new int[] {900, 900},
            new int[] {800, 600},
            new int[] {1920, 1080});
    return Arrays.stream(GamePhase.values())
        .flatMap(
            phase ->
                viewports.stream()
                    .flatMap(
                        viewport ->
                            Levels.catalog().levels().stream()
                                .flatMap(
                                    level ->
                                        Stream.of(false, true)
                                            .map(
                                                hasNextLevel ->
                                                    Arguments.of(
                                                        phase,
                                                        viewport[0],
                                                        viewport[1],
                                                        level,
                                                        hasNextLevel)))));
  }

  private static Stream<Arguments> expectedElementArguments() {
    return Stream.of(
        Arguments.of(
            GamePhase.MAIN_MENU,
            List.of(
                MazeGameLayout.MAIN_MENU_TITLE,
                MazeGameLayout.MAIN_MENU_START,
                MazeGameLayout.MAIN_MENU_SETTINGS,
                MazeGameLayout.MAIN_MENU_QUIT)),
        Arguments.of(
            GamePhase.LEVEL_SELECT,
            List.of(
                MazeGameLayout.LEVEL_SELECT_TITLE,
                MazeGameLayout.levelCardId(1),
                MazeGameLayout.levelCardId(2),
                MazeGameLayout.levelCardId(3),
                MazeGameLayout.LEVEL_SELECT_BACK)),
        Arguments.of(
            GamePhase.SETTINGS,
            List.of(
                MazeGameLayout.SETTINGS_TITLE,
                MazeGameLayout.SETTINGS_AUDIO,
                MazeGameLayout.SETTINGS_BACK)),
        Arguments.of(
            GamePhase.BUILDING,
            List.of(
                MazeGameLayout.GAME_GRID,
                MazeGameLayout.BUILD_TITLE,
                MazeGameLayout.BUILD_STATUS,
                MazeGameLayout.BUILD_INSTRUCTIONS,
                MazeGameLayout.paletteItemId(PlaceableCellType.WALL),
                MazeGameLayout.paletteItemId(PlaceableCellType.SLOW_FLOOR),
                MazeGameLayout.BUILD_BACK,
                MazeGameLayout.BUILD_START)),
        Arguments.of(
            GamePhase.SOLVER_RUNNING, List.of(MazeGameLayout.GAME_GRID, MazeGameLayout.RUN_STATUS)),
        Arguments.of(
            GamePhase.RESULT,
            List.of(
                MazeGameLayout.GAME_GRID,
                MazeGameLayout.RESULT_STATUS,
                MazeGameLayout.RESULT_STATS,
                MazeGameLayout.RESULT_BEST,
                MazeGameLayout.RESULT_NO_NEXT_LEVEL,
                MazeGameLayout.RESULT_RETRY,
                MazeGameLayout.RESULT_REPLAY,
                MazeGameLayout.RESULT_MAIN_MENU)),
        Arguments.of(
            GamePhase.REPLAY, List.of(MazeGameLayout.GAME_GRID, MazeGameLayout.RUN_STATUS)));
  }
}
