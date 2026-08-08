# M2-06: Make Level-Selection and Game UI Data-Driven

Status: complete

Archived: yes

Depends on: M2-01, M2-03, M2-04, M2-05

## Goal

Present the authored catalog, progression state, and active level consistently without hard-coded
Level 1 labels, six fixed placeholder cards, or desktop-only assumptions.

## Scope

- Render one selection card per authored level from catalog-backed display data.
- Show each level's title, locked or unlocked state, and its own best passing result.
- Route card interaction through stable level identity and suppress interaction for locked levels.
- Show the active level's identity and relevant target information during play and results.
- Enable a next-level action only after a pass when the next catalog level is unlocked.
- Apply the responsive layout from M2-01 to level selection, larger grids, build controls, and result
  actions in portrait and landscape.
- Keep rendering dependent on a compact presentation snapshot rather than persistence services or
  catalog globals.

## Acceptance Criteria

- The selection screen contains exactly the catalog's authored levels in catalog order.
- Locked Level 2 is visibly distinct and cannot be launched; it becomes actionable immediately after
  unlock.
- Best-result text is correct and independent for both cards.
- Level 1 and Level 2 titles and target information remain clear throughout their flows.
- Next-level, retry, replay, main-menu, and back actions are shown only when valid and route correctly.
- Both grids and all essential controls remain readable and touchable in supported mobile layouts.
- Desktop presentation and mouse behavior remain usable without maintaining a separate UI code path.

## Verification

- Add renderer and layout tests driven by zero, one, and multiple catalog presentation entries where
  each state is meaningful.
- Add input-routing tests for unlocked, locked, and next-level actions using stable IDs.
- Add portrait, constrained-landscape, and desktop screenshot or pixel assertions for both grid sizes.
- Exercise rotation and resize on level selection, build, and result screens.
- Confirm JavaScript and WebAssembly use the same presentation data and action routing.
