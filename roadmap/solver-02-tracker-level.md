# SOLVER-02: Design and Deliver a Tracker Level

Status: proposed

Depends on: SOLVER-01

## Goal

Add one authored level that teaches and balances Tracker's least-visited-cell behavior without
introducing another solver behavior or cell type.

## Scope

- Compare candidate grid sizes, starts, trash-can goal positions, fixed cells, and player inventory.
- Make visit memory materially relevant through loops or competing branches while retaining a valid
  route after every accepted edit.
- Establish empty, wall-only, Slow-Floor-only, combined, pass, fail, and timeout fixtures.
- Add the level to progression only after exhaustive balance evidence and responsive presentation
  review.

## Acceptance Criteria

- A new player can infer that Tracker changes preference after visiting a route without explanatory
  gameplay text.
- The accepted layout cannot be solved equivalently by merely substituting Random or Scout.
- Replay, persistence, multi-viewport rendering, browser smoke, Pages, Safari, and native packaging
  coverage include the released level.
