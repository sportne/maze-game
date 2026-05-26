package io.github.sportne.mazegame.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.state.GamePhase;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
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

  private static Stream<Arguments> phaseAndViewportArguments() {
    List<int[]> viewports =
        List.of(
            new int[] {1280, 720},
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
                MazeGameLayout.BUILD_START)),
        Arguments.of(GamePhase.MOUSE_RUNNING, List.of(MazeGameLayout.GAME_GRID)),
        Arguments.of(
            GamePhase.RESULT,
            List.of(
                MazeGameLayout.GAME_GRID,
                MazeGameLayout.RESULT_STATUS,
                MazeGameLayout.RESULT_STATS,
                MazeGameLayout.RESULT_NO_NEXT_LEVEL,
                MazeGameLayout.RESULT_RETRY,
                MazeGameLayout.RESULT_REPLAY,
                MazeGameLayout.RESULT_MAIN_MENU)),
        Arguments.of(GamePhase.REPLAY, List.of(MazeGameLayout.GAME_GRID)));
  }
}
