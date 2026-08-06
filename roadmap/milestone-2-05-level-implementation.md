# M2-05: Implement the Second Authored Level

Status: pending

Depends on: M2-02, M2-03, M2-04

## Goal

Add the reviewed Level 2 specification to the production catalog and make its complete game loop
behave correctly with the existing normal-wall and random-mouse mechanics.

## Scope

- Add the exact Level 2 definition accepted in M2-02 to the authored-level catalog.
- Integrate the real Level 2 catalog entry with the generic unlock and per-level result behavior from
  M2-04.
- Initialize grid, timer, target, timeout, cadence, deterministic seed, start, and cheese from the
  selected definition without Level 2-specific branches.
- Ensure wall validation and protected cells work for the larger grid.
- Preserve selected-level identity through build, run, result, retry, replay, and return-to-selection
  flows.
- Keep Level 1 parameters and behavior unchanged.
- Avoid adding extension points for future mouse or block types before those milestones define them.

## Acceptance Criteria

- Level 2 starts with every parameter from the accepted authored specification.
- Valid and invalid wall edits behave correctly across the full 7x7 grid.
- Automatic and early mouse starts use Level 2 timing and deterministic movement.
- Pass, fail, timeout, retry, and replay produce Level 2-specific results and persistence.
- Returning to Level 1 restores Level 1 state rather than leaking Level 2 state.
- No new gameplay mechanic is introduced as part of the implementation.

## Verification

- Add definition tests for the exact Level 2 values and protected cells.
- Reuse parameterized maze, session, simulation, and result tests across both authored levels where
  behavior is shared.
- Add focused Level 2 tests for its authored seed and representative wall layouts from M2-02.
- Run the desktop debug harness through both levels, including retry and replay.
