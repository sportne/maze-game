package io.github.sportne.mazegame.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.state.GamePhase;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

final class MazeGameLayoutTest {
  private static final GridSize GRID_SIZE = GridSize.square(5);

  @ParameterizedTest
  @MethodSource("phaseAndViewportArguments")
  void layoutsAreValidForRepresentativeViewports(
      GamePhase phase, int screenWidth, int screenHeight) {
    ScreenLayout layout = MazeGameLayout.forPhase(phase, screenWidth, screenHeight, GRID_SIZE);

    assertTrue(
        LayoutValidator.validate(layout).isEmpty(),
        () -> "Expected valid layout for " + phase + " at " + screenWidth + "x" + screenHeight);
  }

  @ParameterizedTest
  @EnumSource(GamePhase.class)
  void allCurrentElementsMustFitTheViewport(GamePhase phase) {
    ScreenLayout layout = MazeGameLayout.forPhase(phase, 1280, 720, GRID_SIZE);

    assertTrue(
        layout.elements().stream()
            .allMatch(element -> element.fitPolicy() == LayoutFitPolicy.MUST_FIT));
  }

  @ParameterizedTest
  @MethodSource("expectedElementArguments")
  void layoutsDeclareExpectedElements(GamePhase phase, List<String> expectedIds) {
    ScreenLayout layout = MazeGameLayout.forPhase(phase, 1280, 720, GRID_SIZE);

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
  void supportedIntermediateViewportsUseCompactLayouts(GamePhase phase) {
    assertMobileTargets(phase, 601, 844);
    assertMobileTargets(phase, 600, 421);
    assertMobileTargets(phase, 799, 600);
  }

  @Test
  void desktopReferenceLayoutRemainsUnchanged() {
    ScreenLayout layout = MazeGameLayout.forPhase(GamePhase.BUILDING, 1280, 720, GRID_SIZE);

    assertEquals(
        new ScreenRectangle(417.5F, 137.5F, 445.0F, 445.0F),
        layout.bounds(MazeGameLayout.GAME_GRID));
    assertEquals(
        new ScreenRectangle(452.0F, 41.5F, 180.0F, 44.0F),
        layout.bounds(MazeGameLayout.BUILD_WALL_MODE));
    assertEquals(
        new ScreenRectangle(648.0F, 41.5F, 180.0F, 44.0F),
        layout.bounds(MazeGameLayout.BUILD_START));
  }

  private static void assertMobileTargets(GamePhase phase, int width, int height) {
    ScreenLayout layout = MazeGameLayout.forPhase(phase, width, height, GRID_SIZE, false);

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
                    grid.bounds().width() / GRID_SIZE.columns() >= 32.0F,
                    () -> "grid cell too small for " + phase + " at " + width + "x" + height));
  }

  private static Stream<Arguments> phaseAndViewportArguments() {
    List<int[]> viewports =
        List.of(
            new int[] {1280, 720},
            new int[] {390, 844},
            new int[] {844, 286},
            new int[] {601, 844},
            new int[] {600, 421},
            new int[] {799, 600},
            new int[] {900, 900},
            new int[] {800, 600},
            new int[] {1920, 1080});
    return Arrays.stream(GamePhase.values())
        .flatMap(
            phase ->
                viewports.stream().map(viewport -> Arguments.of(phase, viewport[0], viewport[1])));
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
                MazeGameLayout.levelCardId(4),
                MazeGameLayout.levelCardId(5),
                MazeGameLayout.levelCardId(6),
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
                MazeGameLayout.BUILD_WALL_MODE,
                MazeGameLayout.BUILD_START)),
        Arguments.of(
            GamePhase.MOUSE_RUNNING, List.of(MazeGameLayout.GAME_GRID, MazeGameLayout.RUN_STATUS)),
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
