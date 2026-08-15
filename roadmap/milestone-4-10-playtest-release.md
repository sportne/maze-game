# M4-10: Playtest, Tune, and Release Milestone 4

Status: pending

Depends on: M4-01 through M4-09

## Goal

Validate that people understand the two-type inventory and can comfortably place and reposition cells
on desktop and phone before accepting the Milestone 4 release baseline.

## Scope

- Complete all four levels repeatedly on desktop and at least one physical iPhone in portrait and
  landscape using both select-then-place and drag-from-palette.
- Observe type recognition, finite/infinite count understanding, tap-versus-drag mistakes, replacement,
  tap-again removal, repositioning, invalid feedback, retries, and build-time pressure.
- Confirm the fourth level feels meaningfully different and rewards both types rather than inventory
  trial and error.
- Tune only accepted Level 4 parameters or bounded thresholds/layout values supported by observations;
  do not add another type, solver, tutorial system, or persistence feature.
- Re-run deterministic fixtures after every parameter change.
- Record device/browser versions when available, orientations, qualitative evidence, screenshots,
  accepted parameters, support claims, unobserved criteria, and known limitations in a Milestone 4
  release document.
- Confirm JavaScript remains production default and WebAssembly remains equivalent.

## Acceptance Criteria

- First-time players can distinguish Wall from Slow Floor and explain finite versus infinite supply.
- Players can intentionally use both placement mechanisms and reposition an item without frequent
  accidental removals or lost placements.
- Physical-device controls remain comfortable and readable in portrait and landscape.
- The fourth level is passable through deliberate construction using both types and feels fair.
- No unresolved severity-high gameplay, inventory, gesture, mobile, persistence, asset, or release
  defect remains.
- Documentation claims only evidence actually observed and records any approved waiver explicitly.

## Verification

- Record playtest participants, devices, browsers, orientations, attempts, outcomes, and adjustments.
- Complete all levels from clean and existing three-level profiles on both browser targets.
- Re-run all representative edit, gesture, simulation, balancing, persistence, and replay fixtures.
- Run formatting, static analysis, coverage, architecture, browser, Pages, branded Safari, and native
  packaging gates.
- Review every Milestone 4 definition-of-done item, archive the card after acceptance, deploy under the
  approved push workflow, and validate the live release.
