# M2-09: Define Milestone 3's New Mouse Type

Status: pending

Depends on: M2-08

## Goal

Turn the new-mouse direction into a reviewed Milestone 3 design and executable task-card set without
implementing the mouse prematurely or mixing it with the proposed block-inventory mechanic.

## Scope

- Review playtest evidence from both Milestone 2 levels and identify the player problem a second
  mouse behavior should create.
- Choose one mouse type whose decisions are observably different from the existing random mouse and
  give it a clear player-facing name and explanation.
- Specify movement priorities, legal moves, tie-breaking, cadence, seed usage, terminal behavior, and
  replay determinism precisely enough to test independently of rendering.
- Decide how an authored level selects its mouse type while keeping level definitions immutable and
  avoiding a framework for hypothetical behaviors.
- Define the authored level or existing-level variant that introduces and teaches the new mouse.
- Identify required sprite/UI differentiation, balancing evidence, persistence impact, and
  cross-platform release coverage.
- Create `roadmap/milestone-3.md` and the complete ordered Milestone 3 task-card set covering design,
  model changes, implementation, UI, tests, playtesting, and release.
- Keep multiple block types, limited inventories, and drag-and-drop building in the Milestone 4
  candidate rather than pulling them into Milestone 3.

## Acceptance Criteria

- The selected mouse behavior is mechanically distinct, understandable to a player, deterministic,
  and fully specified for every legal-move and tie condition.
- The design explains why the behavior adds a useful challenge after the random mouse rather than
  merely changing constants.
- The level-to-mouse contract adds only the abstraction needed for the two known mouse types.
- Existing random-mouse levels, results, replay, and persistence remain compatible.
- Milestone 3 has an explicit definition of done, scope exclusions, balancing plan, and ordered cards
  with clear dependencies and verification.
- The plan includes desktop, mobile portrait/landscape, JavaScript, WebAssembly, and live Safari
  coverage.
- Every Milestone 3 card receives reviewer approval before implementation begins.

## Verification

- Walk through representative mazes by hand and with small deterministic examples to validate the
  proposed decision rules and tie-breaking.
- Compare the proposed behavior with the existing random simulation on both Milestone 2 grids.
- Review the milestone and every generated card for testability, sequencing, simplicity, and explicit
  separation from Milestone 4.
- Confirm all generated links resolve and the parent roadmap presents Milestone 3 consistently.
