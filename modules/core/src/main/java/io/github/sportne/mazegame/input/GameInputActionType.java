package io.github.sportne.mazegame.input;

/** Intent produced by routing a mouse click through the current screen layout. */
public enum GameInputActionType {
  /** The click did not hit an active control. */
  NONE,

  /** Open the level-select screen. */
  OPEN_LEVEL_SELECT,

  /** Open the settings screen. */
  OPEN_SETTINGS,

  /** Request application exit. */
  QUIT,

  /** Return from a menu screen to the startup menu. */
  BACK_TO_MAIN_MENU,

  /** Start a selectable authored level by stable id. */
  SELECT_LEVEL,

  /** Consume a click on a locked level placeholder. */
  SELECT_LOCKED_LEVEL,

  /** Toggle session audio. */
  TOGGLE_AUDIO,

  /** Start the mouse run early from the build phase. */
  START_RUN,

  /** Toggle whether a primary pointer clears or places walls. */
  TOGGLE_WALL_MODE,

  /** Place a wall at the clicked grid cell. */
  PLACE_WALL,

  /** Clear a wall at the clicked grid cell. */
  CLEAR_WALL,

  /** Consume a build-grid click whose mouse button has no game effect. */
  IGNORED_GRID_CLICK,

  /** Retry the current level from the result screen. */
  RETRY,

  /** Replay the completed mouse run. */
  REPLAY,

  /** Start the next unlocked authored level after a passing result. */
  NEXT_LEVEL,

  /** Return from the result screen to the startup menu and reset level state. */
  RESULT_MAIN_MENU
}
