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

  /** Start the enabled milestone-one level. */
  START_MILESTONE_ONE,

  /** Consume a click on a locked level placeholder. */
  SELECT_LOCKED_LEVEL,

  /** Toggle session audio. */
  TOGGLE_AUDIO,

  /** Start the mouse run early from the build phase. */
  START_RUN,

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

  /** Return from the result screen to the startup menu and reset level state. */
  RESULT_MAIN_MENU
}
