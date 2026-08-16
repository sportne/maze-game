# GAMEPLAY-04: Design and Release Additional-Cell Levels

Status: proposed

Depends on: GAMEPLAY-02 and GAMEPLAY-03

## Goal

Teach and balance the accepted Right-Turn Floor and North–South Rail Gate in authored gameplay only
after both mechanics are independently verified.

## Scope

- Compare separate introductory levels with a combined follow-up rather than exposing both rules at
  once without evidence.
- Establish empty, Wall-only, Slow-Floor-only, finalist-only, mixed, passing, failing, fallback, and
  timeout fixtures for every participating solver behavior.
- Exhaustively enumerate legal layouts through each proposed finite supply to reject trivial loops,
  unavoidable solver acceleration, and solutions that an existing type reproduces.
- Exercise fixed and mutable forms deliberately, keep a route for every matching solver, and retain
  first-solver-wins completion on multi-solver boards.
- Add progression, persistence, responsive presentation, browser release coverage, and physical-phone
  portrait/landscape playtest only after the balance contract is accepted.

## Acceptance Criteria

- Players can infer each rule from board motion, its non-color mark, and transient tooltip without
  persistent explanatory clutter.
- Each level requires the mechanic it teaches and remains solvable through at least one deliberate
  fallback strategy.
- Random seeds, replay, timeout boundaries, progression, saved results, all supported viewports, and
  startup asset budgets remain deterministic and compatible.

## Verification

- Cross-check test-side balance models against production Random, Scout, Tracker, and Seeker traces
  before candidate effects are applied.
- Enumerate every legal layout through each proposed finite supply and assert exact traces, decision
  times, move counts, result status, whole/chunked equality, and at least one non-degenerate fallback.
- Add fixed/mutable, single/multi-solver, progression, persistence, renderer, tooltip, responsive
  touch, JavaScript, WebAssembly, Pages, Safari, and native-package release coverage.
- Record portrait and landscape physical-phone observations separately from automated evidence before
  accepting balance or comprehension claims.
