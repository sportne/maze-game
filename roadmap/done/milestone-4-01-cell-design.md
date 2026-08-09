# M4-01: Validate Cell Mechanics and Balance Level 4

Status: complete

Archived: yes

Depends on: M3-08

## Goal

Prove with test-side models that Wall, Slow Floor, authored inventory, and a fourth level create a
distinct, solvable challenge before production contracts are changed.

## Scope

- Build a test-only reference editor and simulation timing model from the accepted Milestone 4 design.
- Evaluate candidate Level 4 grid, mouse behavior, build time, target, timeout, and finite Wall and
  Slow Floor supplies while retaining released levels as the infinite-supply compatibility evidence.
- Require at least one small solution that uses both cell types and preserves a viable path.
- Compare empty, wall-only, slow-only, combined, insufficient-supply, replacement, and timeout layouts.
- Record literal expected paths, timing waits, move counts, inventory transitions, and terminal results.
- Compare Random and Scout on representative states and select one existing mouse for Level 4 without
  changing either behavior.
- Update the design record with accepted parameters, coordinates, diagrams, and balancing rationale.
- Keep every prototype in tests or documentation; do not add production types in this card.

## Acceptance Criteria

- The accepted level cannot pass by doing nothing or by merely recreating an unlimited-wall strategy.
- A deliberate combined-type layout passes with supply remaining/counts exactly documented.
- Slow Floor changes elapsed time but not route choice or move count for both existing mice.
- Whole-duration and chunked updates produce identical path, elapsed time, move count, and result.
- The fixture set covers cheese arrival and timeout boundaries during a Slow Floor delay.
- Parameters are explicit enough for later cards to implement without balancing guesses.

## Verification

- Add literal trace and inventory assertions for every accepted fixture.
- Reproduce the selected level under both mouse behaviors and explain the final choice.
- Run formatting, static analysis, tests, coverage, and architecture checks before review and commit.
