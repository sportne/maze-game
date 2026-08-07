# M2-02: Design and Balance the Second Authored Level

Status: complete

Depends on: M2-01

## Goal

Define a Level 2 that is clearly harder than the introductory level while using only normal walls and
the existing deterministic random mouse.

## Scope

- Start from a 7x7 grid and select exact start and cheese positions, build time, target solve time,
  maximum solve time, mouse cadence, and deterministic seed.
- Define the intended player lesson and the difficulty increase relative to Level 1.
- Use deterministic simulations and representative valid wall layouts to understand the achievable
  solve-time range and avoid a trivial or effectively impossible target.
- Confirm the grid and controls remain usable in the responsive mobile layouts from M2-01.
- Record the final level specification and balancing rationale in the Milestone 2 documentation.
- Keep special walls, a new mouse behavior, and additional start positions out of the design.

## Acceptance Criteria

- Every Level 2 parameter has an explicit value and rationale.
- The empty maze is solvable and every accepted wall placement continues to preserve a path.
- The chosen seed makes the recorded empty-maze fixture fail the target, while at least two recorded
  valid wall layouts pass it.
- The deterministic mouse run remains replayable for a given seed and wall layout.
- The challenge increase comes from level geometry and timing rather than hidden rules.
- The grid remains readable and editable on the supported mobile layouts.

## Verification

- Add or extend simulation fixtures that report deterministic outcomes for candidate Level 2 layouts.
- Validate representative failing, passing, and timeout cases.
- Record the exact empty-maze and passing wall layouts, seed, elapsed time, move count, and terminal
  status in `docs/milestone-2-level-design.md` so balancing is reproducible.
- Review the final parameter set against Level 1 and the Milestone 2 definition of done.
- Record playtest observations that justify accepting the authored specification.

## Completion Notes

- Accepted the 7x7, seed-38 specification recorded in
  [`docs/milestone-2-level-design.md`](../docs/milestone-2-level-design.md).
- Added deterministic design fixtures for the empty-maze failure, two distinct path-preserving
  passes, a path-preserving timeout, and repeatability of the accepted layouts.
- Confirmed the 7x7 grid keeps cells at 51 CSS pixels in the supported portrait viewport and 34 CSS
  pixels in both full and safe-content constrained landscape viewports.
- Kept the definition test-only; the production catalog still contains only Milestone 1 until the
  later content implementation task.
