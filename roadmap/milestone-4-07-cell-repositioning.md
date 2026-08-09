# M4-07: Reposition Placed Cells by Drag

Status: pending

Depends on: M4-06

## Goal

Allow a placed Wall or Slow Floor to move to an empty grid cell before exploration while preserving
the source and inventory on every invalid or cancelled gesture.

## Scope

- Start an existing-item drag only after the shared threshold is crossed from an occupied grid cell.
- Keep the canonical source cell unchanged while visually reserving it during the gesture.
- Commit one session/domain move only when released over a different empty, unprotected grid cell.
- Treat release on the source as a no-op and occupied, protected, outside, or path-blocking
  destinations as rejected/cancelled according to the design.
- Reuse pointer ownership, capture, preview, lifecycle cancellation, and phase lock from M4-06.
- Ensure a pre-threshold release continues to perform the active tool's click behavior, including
  tap-again removal or replacement.
- Do not add occupied-cell swaps, drag-to-trash removal, undo, or multi-select.

## Acceptance Criteria

- Successful movement changes one source/destination pair and no inventory count.
- Invalid/cancelled movement keeps the exact original immutable maze and inventory.
- A moved Wall is path-validated against the final board once; no transient source removal is visible
  to simulation or persistence.
- Mouse and touch use the same threshold and outcomes at all supported viewports.
- Starting exploration during a drag cancels it before freezing the maze.

## Verification

- Test both cell types, source no-op, every rejected destination, cancellation signal, timer race,
  selection interaction, and finite/infinite count invariance.
- Add mouse and real-touch browser flows that move each type, then start and complete a run.
- Inspect preview/reservation/rejection captures without depending on color alone.
- Run full quality, browser, Pages, Safari-where-available, and native-image gates before review and
  commit.
