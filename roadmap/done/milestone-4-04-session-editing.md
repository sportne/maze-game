# M4-04: Integrate Selected Tools and Atomic Edits into the Build Session

Status: complete

Archived: yes

Depends on: M4-02, M4-03

## Goal

Give each build attempt one selected placeable type and route all session edits through the accepted
transactional maze/inventory operations while preserving phase and timer safety.

## Scope

- Initialize the first nonzero/infinite authored type as the selected tool for a fresh attempt.
- Expose immutable palette state: authored supply, remaining count, availability, and selection.
- Add session operations to select a type, place/replace at a cell, remove a cell, and move a cell.
- Keep tap-versus-drag interpretation outside the session; accept grid-level intents only.
- Use the existing rejected-cell feedback for domain rejections and add only the minimum result detail
  needed for an exhausted or invalid tool indication.
- Reset selection, placed cells, and inventory on retry, Back, next level, and fresh selection; replay
  uses the completed immutable maze and does not reopen editing.
- Reject or ignore every edit outside `BUILDING`; pointer/gesture cancellation begins only when the
  real gesture controller is added in M4-06.

## Acceptance Criteria

- Session inventory always matches the immutable maze after place, replace, remove, and move.
- An exhausted type remains selectable for same-type removal but cannot place on an empty cell or
  replace a different type; selection changes do not consume supply.
- Timer expiry and Start freeze one complete maze, and later gesture cards must cancel transient
  controller state before applying these existing phase transitions.
- Retry and navigation restore authored supply, while replay preserves the completed layout.
- Released levels still start with Wall selected and behave exactly as before.

## Verification

- Add phase-transition and inventory sequences covering all navigation and timeout paths.
- Test both finite and infinite sessions, including last-item placement, exhausted selection,
  same-type recovery, and rejected exhausted placement without duplicating MazeState's exhaustive
  domain matrix.
- Extend debug snapshots only with stable semantic state required by browser tests.
- Run full formatting, analysis, tests, coverage, architecture, browser, Pages, and native-image gates
  before review and commit.
