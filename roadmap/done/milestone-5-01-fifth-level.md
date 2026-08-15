# M5-01: Add a Fifth Level with Random and Scout

Status: complete

Archived: yes

## Goal

Add a fifth authored level where Random and Scout start separately and concurrently pursue cheese
and acorn goals placed at and diagonally beside the grid center.

## Acceptance Criteria

- Random and Scout have distinct protected starts and retain independent deterministic behaviors.
- Cheese is centered and the acorn is one diagonal cell away.
- Every accepted edit preserves a route for each character to its own goal.
- Both goal and character sprites render together in build, run, result, and replay flows.
- The concurrent attempt stops when the first character reaches its matching goal.
- The empty level fails, an authored finite-inventory fixture passes, and both results replay exactly.
- Progression, persistence, level selection, and released single-character levels remain compatible.

## Verification

- Add model, maze validation, simulation, session, renderer, progression, and desktop harness tests.
- Run the full formatting, static-analysis, coverage, architecture, browser, Pages, and packaging gates.
