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

  /** Start the solver run early from the build phase. */
  START_RUN,

  /** Leave an unstarted level attempt and return to level selection. */
  BACK_TO_LEVEL_SELECT,

  /** Select a placeable cell type from the build palette. */
  SELECT_CELL_TYPE,

  /** Apply the selected type to a clicked grid cell. */
  PLACE_OR_REPLACE_CELL,

  /** Remove any placeable type from a clicked grid cell. */
  REMOVE_CELL,

  /** Consume a build-grid click whose mouse button has no game effect. */
  IGNORED_GRID_CLICK,

  /** Retry the current level from the result screen. */
  RETRY,

  /** Replay the completed solver run. */
  REPLAY,

  /** Start the next unlocked authored level after a passing result. */
  NEXT_LEVEL,

  /** Return from the result screen to the startup menu and reset level state. */
  RESULT_MAIN_MENU
}
