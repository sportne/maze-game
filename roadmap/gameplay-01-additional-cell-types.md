# GAMEPLAY-01: Brainstorm and Select Additional Cell Types

Status: proposed

Depends on: completion of the Milestone 4 release baseline

## Goal

Identify a small set of cell mechanics that create new maze-building decisions without duplicating
Wall or Slow Floor, overwhelming the palette, or making solver behavior difficult to understand.

## Scope

- Brainstorm at least six candidate cell types across blocking, walkable timing, movement-choice, and
  topology-changing mechanics rather than producing cosmetic variants of existing cells.
- Specify each candidate's effect on path validation, solver movement, timing, finite/infinite
  inventory, placement, replacement, removal, repositioning, fixed authored cells, and multi-solver
  levels.
- Evaluate whether each effect is deterministic, explainable from the board, compatible with Random
  and Scout, and capable of producing a level that Wall and Slow Floor cannot reproduce.
- Sketch an icon, grid treatment, supply badge behavior, and concise hover tooltip for each candidate;
  require non-color cues and usable compact layouts when all selected types are available.
- Estimate implementation, balancing, asset, browser-transfer, rendering, test, and migration cost.
- Prototype only test-side rules or reference simulations needed to reject ambiguous or degenerate
  candidates. Do not add a production enum value, palette item, runtime asset, or authored level.
- Rank the candidates and select no more than two for follow-up design. It is acceptable to select
  none when the evidence does not justify the added complexity.
- Record rejected candidates and the reason each was rejected so the same ideas are not repeatedly
  reconsidered without new evidence.

## Acceptance Criteria

- The comparison matrix covers at least six mechanically distinct candidates and every interaction
  named in scope.
- Each shortlisted type has one concise player-facing rule, a distinct non-color visual identity,
  and at least one reproducible maze fixture demonstrating unique strategic value.
- No shortlisted type makes an existing solver nondeterministic, invalidates replay, or bypasses the
  requirement that every solver retain a route to its matching goal.
- The proposed visible palette still fits all supported desktop, portrait, constrained-landscape,
  and safe-landscape viewports, or the design explicitly scopes the prerequisite UI change.
- Every shortlist decision identifies prerequisite infrastructure, including fixed authored cells or
  lazy asset delivery when applicable.
- Follow-up implementation cards can be written without leaving behavior, inventory, interaction,
  rendering, accessibility, or verification semantics undecided.

## Verification

- Review the candidate matrix with independent gameplay, domain-model, UI/accessibility, and release
  perspectives.
- Run test-side reference fixtures for timing-, direction-, or topology-changing finalists against
  both existing solver behaviors and a representative multi-solver board.
- Exercise the proposed palette count in declared layouts for all released reference viewports.
- Record the accepted shortlist, rejected alternatives, fixtures, visual sketches, prerequisites,
  open risks, and follow-up task boundaries in a design document.
- Run documentation formatting and link checks plus any focused reference-model tests added by this
  task before independent review and commit.
