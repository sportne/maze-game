# M3-03: Implement the Deterministic Scout Simulation

Status: complete

Archived: yes

Depends on: M3-02

## Goal

Implement Scout's north-facing left, straight, right, back search as a deterministic core simulation
with the same timing and terminal semantics as the existing random mouse.

## Scope

- Add a small cardinal-direction model that can derive left, right, and reverse directions.
- Implement Scout with an initial north heading and a fixed relative candidate order.
- Add one closed authored mouse-behavior value with exactly `RANDOM` and `LEFT_PRIORITY` choices,
  make it required immutable level data, and set both existing production levels to `RANDOM`.
- Add the explicit two-case simulation factory or switch after both concrete implementations exist,
  and route session construction through it without a default or placeholder behavior.
- Keep the existing seed on level definitions for Random; Scout ignores it. Keep Scout's initial north
  heading in its concrete rule until another authored level requires a configurable heading.
- Move to the first in-bounds, non-wall candidate and update the heading to the absolute movement
  direction.
- Preserve exact movement-interval, elapsed-time, move-count, cheese, and timeout boundary behavior.
- Keep heading unchanged when no move is legal and count the consumed movement decision consistently.
- Ensure reverse moves affect the next decision exactly like any other successful direction.
- Do not read the level seed or share random-choice code.
- Keep the implementation independent of libGDX, rendering, persistence, and platform code.

## Acceptance Criteria

- Every direction and obstruction fixture from M3-01 produces its exact expected path.
- Left is selected whenever open, even if straight or right is also open.
- Straight, right, and reverse are selected only after every higher-priority candidate is blocked.
- Chunked and single-call updates produce equal results at movement and timeout boundaries.
- Replay from the same maze produces the same positions, times, move counts, headings, and terminal
  status without consulting a random seed.
- Existing random behavior and tests remain unchanged.
- Missing behavior values fail fast, and no nullable or stringly typed selection exists.
- Factory tests prove `RANDOM` creates the random implementation and `LEFT_PRIORITY` creates Scout.

## Verification

- Add focused tests for rotations, boundaries, dead ends, backtracking, loops, cheese arrival, timeout,
  large deltas, zero deltas, negative deltas, and post-terminal updates.
- Add level-definition validation and equality tests for both behavior values.
- Run the accepted third-level fixtures through the production implementation and compare their traces
  to the reference evidence.
- Add mutation-resistant assertions for priority order rather than testing only final status.
- Run formatting, static analysis, tests, coverage, architecture checks, browser builds, Pages
  assembly, and native-image packaging before commit.
