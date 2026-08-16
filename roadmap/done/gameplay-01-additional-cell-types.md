# GAMEPLAY-01: Brainstorm and Select Additional Cell Types

Status: complete

Archived: yes

Depends on: completion of the Milestone 4 release baseline

## Goal

Identify a small set of cell mechanics that create new maze-building decisions without duplicating
Wall or Slow Floor, overwhelming the palette, or making solver behavior difficult to understand.

## Completed Scope

- Compared eleven mechanically distinct candidates across blocking, walkable timing, movement-choice,
  solver-information, run-mutating, and topology-changing mechanics.
- Specified path validation, all four solver rules, timing, finite/infinite inventory, placement,
  replacement, removal, repositioning, fixed ownership, and multi-solver behavior for every candidate.
- Evaluated deterministic replay, board explainability, unique strategic value, rendering,
  accessibility, asset/browser transfer, implementation, balancing, testing, and migration cost.
- Prototyped only test-side Right-Turn Floor and North–South Rail Gate rules; no production enum,
  palette item, runtime asset, fixed type, or authored level was added.
- Selected Right-Turn Floor as the primary follow-up and North–South Rail Gate as the secondary
  follow-up. Recorded every rejected candidate and its reason.
- Verified that four total visible palette items fit all supported reference and minimum-policy
  viewports while remaining at least 44x44.

## Acceptance Evidence

- The accepted [additional-cell design](../../docs/additional-cell-types-design.md) contains the
  candidate matrix, player rules, complete interaction semantics, non-color visual sketches, supply
  badges, two-line hover/touch tooltips, costs, prerequisites, fixtures, rejected alternatives, and
  independent-review resolution.
- The Right-Turn junction keeps north, west, and east physically open while forcing every solver east;
  blocked-right fallback exactly preserves normal trace and decision state.
- The Rail multi-solver fixture preserves a four-move vertical route while changing a horizontal
  shortcut from four to six moves. Wall removes the required vertical route and Slow Floor preserves
  the shortcut, demonstrating unique strategic value.
- Test-side reference behavior is cross-checked against Random, Scout, Tracker, and Seeker production
  traces before candidate effects are applied. Replays are deterministic and concurrent solvers keep
  independent state on one immutable board.
- Four-item geometry passes desktop 1280x720, portrait 390x844, constrained landscape 844x286, safe
  landscape 756x286, intermediate 600x421, and minimum 568x270/7x7 references without scrolling,
  overlap, or targets below 44 px.
- Independent gameplay/domain, UI/accessibility, and release/test reviews were completed and their
  differing recommendations were resolved explicitly in the design.

## Follow-Up Tasks

- [GAMEPLAY-02: Add Right-Turn Floor](../gameplay-02-right-turn-floor.md)
- [GAMEPLAY-03: Add North–South Rail Gate](../gameplay-03-north-south-rail-gate.md)
- [GAMEPLAY-04: Design and release additional-cell levels](../gameplay-04-additional-cell-levels.md)

## Verification

- Run `GameplayAdditionalCellTypesDesignTest` for production-reference parity, finalist rules,
  multi-solver topology, deterministic replay, and responsive palette counts.
- Run documentation formatting and link checks, focused tests, the repository quality gate, and
  native packaging before independent review and commit.
