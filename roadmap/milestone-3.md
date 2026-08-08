# Milestone 3: Scout Mouse

Status: planned

## Goal

Add one visually distinct, deterministic mouse whose discoverable search pattern changes how players
shape a maze, while preserving the released three-platform progression and the existing normal-wall
build interaction.

## Product Decisions

- The player-facing mouse name is **Scout**; the internal behavior value is `LEFT_PRIORITY`.
- Scout starts facing north and chooses the first open direction in the fixed order left, straight,
  right, back, but the initial UI does not reveal that order.
- Milestone 3 adds a third authored 7x7 level so the new behavior—not a larger grid or new block—is
  the main challenge.
- Passing Milestone 2 unlocks the third level, which stores an independent best passing result under
  the stable `milestone-3` id.
- Existing levels retain the deterministic random mouse and their exact replay behavior.
- Scout has a new sprite with a blue cap and high-contrast star badge, making it recognizable without
  color alone while avoiding a visual spoiler.
- Initial in-game text says only “Scout follows a consistent search pattern.” Result feedback may
  encourage watching intersections but does not disclose the priority order.

The complete behavior specification is recorded in
[`docs/milestone-3-mouse-design.md`](../docs/milestone-3-mouse-design.md).

## Out of Scope

- Multiple block or grid-cell types.
- Finite block inventories or per-level palettes.
- Dragging blocks from a palette or repositioning placed blocks by drag.
- Multiple mice in one level or player-selected mouse behavior.
- A behavior registry, plugin system, or externally scripted mouse AI.
- Changes to the existing win condition, result format, or persistence schema.

## Definition of Done

- Scout's direction, movement, backtracking, timing, timeout, and replay behavior match the accepted
  deterministic specification for every heading and obstruction case.
- The level model selects exactly one of the two known mouse behaviors without changing existing
  random paths, stable ids, saved results, or unlock state.
- A balanced third level lets players discover and exploit Scout using only normal walls and the
  existing build interaction.
- Scout has a unique, non-color-only visual identity and is named consistently on selection, build,
  running, and result screens.
- The third level supports unlock, selection, retry, replay, next-level behavior, persistence, reload,
  and clean-profile migration.
- Desktop, portrait and landscape touch, JavaScript, WebAssembly, live Pages, branded Safari, native
  packaging, formatting, static analysis, coverage, and architecture gates remain green.
- Physical-device and desktop playtesting find the new rule understandable and the new level
  noticeably different but fair.

## Task Order

1. [M3-01: Validate Scout and balance the third level](done/milestone-3-01-scout-design.md)
2. [M3-02: Add the minimal mouse-behavior contract](done/milestone-3-02-mouse-contract.md)
3. [M3-03: Implement the deterministic Scout simulation](done/milestone-3-03-scout-simulation.md)
4. [M3-04: Add the third authored level and progression](done/milestone-3-04-third-level.md)
5. [M3-05: Give Scout a unique visual and UI identity](done/milestone-3-05-scout-presentation.md)
6. [M3-06: Extend cross-platform release coverage](done/milestone-3-06-release-coverage.md)
7. [M3-07: Playtest, tune, and release Milestone 3](milestone-3-07-playtest-release.md)
8. [M3-08: Define Milestone 4](milestone-3-08-define-milestone-4.md)

M3-08 is non-gating follow-up planning for the next milestone. Milestone 3 product work is complete
after M3-07; the final card turns the accepted Milestone 4 direction into a reviewed task-card set.

## Future Direction

Milestone 4 will replace the single implicit wall tool with authored grid-cell types whose per-level
availability may be finite or infinite. Players can drag a type from a bottom palette or select it
and then click cells, and can drag placed cells around the grid before starting the mouse. M3-08 will
specify inventory accounting, validation, responsive input, domain modeling, testing, and release
tasks without implementing them during Milestone 3.
