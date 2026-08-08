# M3-01: Validate Scout and Balance the Third Level

Status: pending

Depends on: M2-09

## Goal

Turn the accepted Scout rules into reproducible decision examples and a balanced third-level
specification before production behavior or catalog code is added.

## Scope

- Encode or document a small reference model for the fixed left, straight, right, back priority with
  an initial north heading.
- Cover every absolute heading, obstruction combination, boundary, backtrack, repeated corridor, and
  no-legal-move case.
- Compare Scout and the existing seeded random mouse on representative Milestone 2 mazes to show that
  the new challenge comes from behavior rather than renamed constants.
- Design one third 7x7 level with bottom-center start, top-center cheese, normal walls, and Scout.
- Select build time, target time, timeout, and any geometry adjustment using reproducible empty,
  passing, failing, backtracking, and loop/timeout fixtures.
- Record accepted coordinates, traces, timing, move counts, and rationale in the Milestone 3 design
  documentation.
- Keep the work test-only or documentation-only until the mouse contract and implementation cards.

## Acceptance Criteria

- All four headings map to the exact relative priority order without ambiguity.
- Examples prove that reverse is chosen only when left, straight, and right are unavailable, and that
  the reversed direction becomes the new heading.
- Repeated evaluation produces the same trace without a random seed.
- At least two distinct legal wall layouts pass the proposed target and an empty or naive layout
  fails it.
- The authored level has a viable start-to-cheese path in every accepted fixture.
- The final parameters create a noticeable but explainable challenge without a new grid or block rule.

## Verification

- Walk every direction table row and obstruction case by hand.
- Run a small deterministic reference simulation with whole-duration and chunked updates.
- Compare documented traces against the existing random simulation on the same maze states.
- Review fixture paths, timing boundaries, and target outcomes independently.
- Run formatting, static analysis, tests, coverage, browser builds, Pages assembly, and native-image
  packaging before commit.
