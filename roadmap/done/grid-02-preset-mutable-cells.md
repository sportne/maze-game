# GRID-02: Add Movable Preset Grid Cells

Status: complete

Archived: yes

Depends on: GRID-01

## Goal

Let levels author initially occupied cells that consume inventory and remain fully editable, clearly
separating empty, fixed, and preset-mutable grid states.

## Completed Scope

- Added immutable, validated `PresetCell` authoring to `LevelDefinition` with compatibility
  constructors for existing levels.
- Materialized presets into ordinary `MazeState.placedCells` for every fresh attempt.
- Applied finite/infinite inventory, path validation, move, remove, replacement, retry, replay, and
  navigation semantics without a parallel runtime ownership state.
- Added two preset Slow Floors to Level 10, leaving four of its six authored units in the palette.
- Exercised preset movement through session, desktop debug, JavaScript, and WebAssembly paths while
  retaining the existing normal-cell rendering and fixed-cell lock distinction.

## Acceptance Evidence

- The accepted [preset-cell contract](../../docs/preset-authored-cells.md) records authoring,
  inventory, editing, lifecycle, and presentation decisions.
- Definition tests reject invalid location, overlap, duplicates, exhausted supply, and blocked
  baseline routes while proving immutability and infinite-supply compatibility.
- Session tests prove preset inventory consumption, ordinary editing, fixed-cell rejection, replay
  retention, and fresh-attempt restoration.
- Level 10 still reaches its accepted `12750:34` result with two presets and all four remaining Slow
  Floors; three or fewer remaining units cannot pass.

## Verification

- Run focused preset definition/session/Level 10 tests and the complete core suite.
- Run formatting, static analysis, duplication, coverage, architecture, JavaScript/WebAssembly
  browser smoke, Safari compilation, and native-image packaging gates.
