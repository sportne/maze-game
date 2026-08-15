# SOLVER-01: Design and Deliver a Third Solver Type

Status: proposed

Depends on: completion of the Milestone 4 release baseline

## Goal

Add one solver behavior that is strategically and visibly distinct from Random and Scout while
preserving deterministic replay, shared timing rules, and multi-solver level semantics.

## Scope

- Compare at least three candidate movement behaviors and select one whose decisions can be explained
  from local board state and produce challenges not equivalent to a different Random seed or Scout
  starting heading.
- Specify movement priority, required memory, initial state, tie-breaking, dead-end behavior, random
  seed requirements, and interaction with every supported cell effect before production code changes.
- Build a small reference model with exact traces for headings or equivalent internal state,
  obstruction combinations, boundaries, loops, backtracking, no-legal-move cases, goal arrival, and
  timeout.
- Record the accepted behavior and balancing evidence in a solver design document, then add one closed
  authored behavior value and one production simulation selected exhaustively by the existing factory.
- Reuse the shared fixed-step timing and terminal semantics, including Slow Floor delay, without
  modifying Random or Scout behavior.
- Choose a distinct existing processed character appearance and compatible goal presentation. If the
  choice requires an optional atlas not already shipped, make ASSET-01 a prerequisite rather than
  increasing startup transfer silently.
- Integrate rendering, result/replay presentation, debug snapshots, and multi-solver runs without
  adding solver-specific branches outside the behavior, factory, and presentation mapping boundaries.
- Do not add a new authored level, cell type, configurable behavior scripting system, reflection,
  service loading, or public solver plugin API in this task.
- Produce a follow-up level-design card that demonstrates and balances the accepted solver after the
  behavior itself is complete.

## Acceptance Criteria

- The accepted behavior has an unambiguous decision table and reference traces that distinguish it
  from Random and Scout on the same representative mazes.
- Repeated runs and replay reproduce identical positions, elapsed times, move counts, internal
  decision state, and terminal outcome for the same authored inputs.
- Chunked and whole-delta updates agree at movement, Slow Floor, goal, and timeout boundaries.
- The simulation factory handles every solver behavior explicitly and rejects missing or invalid
  authoring without a nullable or stringly typed fallback.
- Existing Random and Scout fixtures remain unchanged, including all five released level results.
- The new character, goal, status, and replay presentation are distinct, accessible, and correct in
  single- and multi-solver views on every supported viewport.
- The implementation remains independent of libGDX, rendering, persistence, and platform launchers.

## Verification

- Review and accept the candidate comparison, reference model, visual choice, and behavior contract
  before implementing production simulation code.
- Add mutation-resistant tests for decision ordering, state transitions, boundaries, dead ends,
  backtracking, loops, timing, Slow Floor entry, large/zero/negative deltas, terminal updates, and
  replay.
- Cross-check production traces against the accepted reference model and run all three behaviors on
  identical authored maze states.
- Add exhaustive factory, level-definition, renderer, debug-harness, multi-solver, JavaScript, and
  WebAssembly coverage without duplicating the full simulation suite in browser tests.
- Run formatting, static analysis, coverage, architecture, browser smoke, Pages, Safari, and native
  packaging gates before independent review and commit.
