# Additional Cell Types Design

## Decision

GAMEPLAY-01 shortlists two player-placeable mechanics for separate implementation and balancing:

1. **Right-Turn Floor** is the primary choice. After a solver enters it, the next movement decision
   takes the relative right exit when that exit is open; otherwise the solver uses its normal rule.
2. **North–South Rail Gate** is the secondary choice. Solvers may cross the cell vertically but may
   not enter or leave it horizontally.

Neither type is added to production by GAMEPLAY-01. The task contributes design evidence and
test-side reference rules only. Released levels, supplies, fixed cells, runtime assets, persistence,
and startup transfer remain unchanged.

Right-Turn Floor offers the best first increment because it needs no orientation value, pairing,
mutable board state, or new timing unit. Rail Gate offers stronger topology value and proves why a
future movement contract must validate edges rather than treating every effect as a boolean property
of one destination cell.

## Baseline Constraints

- The production board stores one closed `PlaceableCellType` per mutable position. It has no
  orientation, pairing, charge, or runtime-state payload.
- Same-type placement removes a cell. A design that overloads that gesture to rotate or configure a
  cell would break the accepted mobile interaction.
- Current path validation is node-based breadth-first search over a destination walkability
  predicate. It cannot validate directed edges, portal links, arrival headings, or time phases.
- Random and Seeker use authored seeds. A forced cell-effect move must say whether it consumes a
  random draw. Scout owns heading, Tracker owns visit counts, and Seeker owns line-of-sight priority.
- All solvers share one immutable board, but each solver owns its own movement state. Shared
  run-mutating cell state would make multi-solver update order observable.
- Fixed and player cells have separate identities. A fixed form has the same gameplay rule, never
  consumes inventory, and rejects every editing operation.
- The current palette normally shows only icons and corner supply badges. A 500 ms desktop hover
  opens a one-line tooltip; new rules require a bounded second line and a touch equivalent without
  making explanatory text permanently visible.
- Primitive shape rendering avoids any new browser transfer. Selecting bitmap art would make
  ASSET-01 a prerequisite.

The card originally named Random and Scout. The current game also contains Tracker and Seeker, so
the evidence and follow-up boundaries cover all four behaviors.

## Candidate Comparison

Costs use low (`L`), medium (`M`), high (`H`), and very high (`VH`). Detailed cost dimensions appear
in the next section.

| Candidate | Category and player rule | Path, solver, and timing behavior | Inventory, editing, fixed, and multi-solver behavior | Visual and palette sketch | Novelty, determinism, cost, and decision |
| --- | --- | --- | --- | --- | --- |
| **Right-Turn Floor** | Movement choice: “Next move turns right when that exit is open.” | Walkable. The cell effect is checked before the solver rule. A forced move uses one normal movement interval and no random draw; fallback delegates without changing the next draw. Scout adopts the forced heading, Tracker records the destination visit, and Seeker defers visible-goal pursuit for that decision. Static walkability remains unchanged. | Ordinary finite/infinite per-cell supply and every existing place, replace, remove, and move rule. Fixed form is identical but immutable. Incoming direction is per solver, so concurrent solvers share no mutable state. | Violet floor as a secondary color cue; bold clockwise bent-arrow/perimeter ticks as the non-color cue. Lower-right badge remains clear. Tooltip: `Right Turn · 3` / `Turn right when open.` | Changes a local choice while leaving all exits physically open, which Wall and Slow Floor cannot express. Fully deterministic. No orientation data or new asset. `M/H/L/L/M/H/M`. **Shortlist, primary.** |
| **North–South Rail Gate** | Directional blocking/topology: “Cross this cell only north–south.” | Vertical edges touching the gate are legal; horizontal edges touching it are closed. Every solver filters the same edges. Seeker sight may cross it vertically but not horizontally. Movement interval and state are otherwise unchanged. Requires edge-aware path validation for every solver route. | Ordinary per-cell supply and current editing rules because orientation is fixed by the type. Fixed form is immutable. Concurrent solvers see the same static edge rules and keep independent decision state. | Cool steel floor with two solid vertical rails and open top/bottom. Rails, not color, communicate direction. Tooltip: `N–S Rail · 2` / `Cross vertically only.` | Preserves a vertical route while closing a horizontal shortcut; neither Wall nor Slow Floor can do both. Fully deterministic. `M/M/L/L/M/H/M`. **Shortlist, secondary.** |
| Momentum / Ice | Movement choice: continue straight on the next decision when forward is open. | Forced move, ordinary timing, no random draw; solver heading/visits update. If forward is blocked, delegate normally. It is deterministic but often agrees with Tracker or Seeker and can produce long, less legible loops in chains. | Ordinary supply/editing/fixed/multi semantics; incoming direction is per solver. | Symmetric edge skid marks or crystal rails; no absolute arrow. | UI-simple and asset-free, but Right-Turn produces a more visibly distinct choice on the shared fixture. `M/H/L/L/M/H/M`. **Reserve/reject from shortlist.** |
| One-Way Arrow | Directed blocking: enter only while moving in the arrow direction. | Requires directed-edge validation and direction-aware Seeker sight. Deterministic once orientation is authored. | Needs orientation in placed/fixed data. Four palette variants either duplicate one shared badge or require four supplies; one rotatable tool conflicts with tap-again removal. Moving must define whether orientation follows. Opposite multi-solver approaches can conflict. | Four arrow icons or a rotation control. | Strong novelty but large domain/UI migration. Six total oriented tools fail the 44 px minimum in supported intermediate layouts. `H/H/L/H/M/H/H`. **Reject.** |
| First-Use Snare | Walkable timing: first entry per solver adds two waits; later entries are normal. | Needs integer delay state and per-solver armed/spent state. Replay can be deterministic, but concurrent solvers can see different visual state. | Ordinary build editing, but runtime state must reset on retry/replay and cannot be represented by one shared board rendering. | Loop/snare mark with armed and spent forms. | Usually reproduced by one or two Slow Floors and harder to read in multi-solver play. `M/H/L/L/H/H/M`. **Reject.** |
| Fast Floor | Walkable timing: shorten the next movement interval. | Routes stay identical for every solver; exact fractional/zero-time boundaries need a new timing value. | Ordinary supply/editing/fixed/multi semantics. | Lightning and speed ticks, visually close to Momentum at 24 px. | Deterministic but helps the solver in a game about delaying it and is dominated by not placing it. `M/M/L/L/M/H/M`. **Reject.** |
| Portal Pair | Topology: entering one endpoint emerges at its mate. | Requires linked graph edges, endpoint suppression, exact move/time semantics, Scout post-exit heading, Tracker visit policy, and Seeker sight boundaries. | Inventory counts pairs. Placement, replacement, removal, and repositioning must be atomic across endpoints; fixed authoring needs stable pair ids. Multi-solver endpoint state must not be shared accidentally. | Concentric patterned rings labeled `I` and `II`; badge must say pairs. | Highly novel but introduces a second editing model and easy cycles. `VH/H/M/M/M/VH/H`. **Reject/defer.** |
| Crumbling Wall/Floor | Run-mutating blocking: changes between walkable and blocked after contact. | Requires per-solver or shared mutable geometry and time-expanded route validation. Shared mutation makes solver iteration order gameplay-visible; per-solver mutation makes one cell render multiple states. | Build editing can be ordinary, but retry/replay lifecycle and fixed variants need separate runtime ownership. | Cracked masonry changing to rubble; crack competes with exhaustion slash. | Determinism and multi-solver presentation costs outweigh the mechanic. `VH/H/L/M/H/VH/H`. **Reject.** |
| Fog Floor | Solver-information effect: blocks goal sight without blocking movement. | Only Seeker changes behavior; Random, Scout, and Tracker ignore it. Static path and timing remain normal. | Ordinary supply/edit/fixed/multi behavior. | Dashed cloud perimeter with open center. | Deterministic but weak or meaningless for three of four solvers and not self-explanatory on a mixed board. `M/H/L/L/M/H/M`. **Reject.** |
| Switch Plate and Gate | Topology-changing set: crossing a plate toggles linked gates. | Requires linked identities, mutable run state, transition ordering, and stateful path validation. Shared state couples concurrent solvers; per-solver state makes rendering ambiguous. | Inventory counts sets rather than cells. Placement/removal/movement must preserve links; fixed sets need authoring ids. | Matching plate/gate patterns or letters, crowded at 32 px. | Novel but combines Portal pairing cost with mutable geometry. `VH/VH/M/M/H/VH/H`. **Reject.** |
| Alternating Gate | Time topology: open and closed on alternating decision beats. | Requires `(position,time phase)` validation and exact stay/wait semantics. A shared phase couples solvers; per-solver phases make one cell disagree visually. | Ordinary placement is possible, but fixed/runtime state and replay boundaries are complex. | Barred gate with phase marks; animation cannot be the only cue. | Deterministic only after arbitrary clock choices and difficult to infer from a static board. `H/VH/L/M/H/VH/M`. **Reject.** |

Every candidate was evaluated against bounds and protected positions, finite and infinite inventory,
placement, atomic replacement, tap-again removal, repositioning, fixed ownership, and multi-solver
route preservation. Unless a row explicitly says otherwise, those operations use the canonical
cell-editor semantics. Candidates were rejected when their runtime or configuration state made that
statement untrue.

### Candidate presentation ledger

The sketches below make the palette evaluation reproducible even for rejected candidates. `n` is
the normal lower-right finite count, `inf` is the text fallback paired with the line-drawn infinity
badge, and “sets” or “pairs” changes the inventory unit explicitly. Every grid mark is shape-based;
color may reinforce it but cannot carry the rule alone.

| Candidate | Palette icon and grid treatment | Supply badge | Two-line tooltip |
| --- | --- | --- | --- |
| Right-Turn Floor | Clockwise corner arrow in the icon; matching perimeter turn and ticks on the grid | Cells: `n` / infinity | `Right Turn · n` / `Turn right when open.` |
| North-South Rail Gate | Two open-ended vertical rails in both contexts | Cells: `n` / infinity | `N-S Rail · n` / `Cross vertically only.` |
| Momentum / Ice | Symmetric skid marks in the icon; matching marks on all four grid edges | Cells: `n` / infinity | `Momentum · n` / `Continue straight if open.` |
| One-Way Arrow | Four absolute-arrow palette variants; matching full-cell arrow and entry bar | One shared cell count repeated on all four variants | `North Gate · n` / `Enter moving north.` (direction changes per variant) |
| First-Use Snare | Loop-and-pause icon; armed loop then broken loop on the grid | Cells: `n` / infinity; runtime state never changes the badge | `First-Use Snare · n` / `First entry waits twice.` |
| Fast Floor | Lightning icon; perimeter speed ticks on the grid | Cells: `n` / infinity | `Fast Floor · n` / `Next move happens sooner.` |
| Portal Pair | Concentric-ring icon; linked grid rings labeled ASCII `I` and `II` | Remaining pairs: `n` / infinity | `Portal Pair · n pairs` / `Enter one, leave the other.` |
| Crumbling Floor | Cracked-tile icon; intact cracks then rubble outline on the grid | Cells: `n` / infinity; runtime state never changes the badge | `Crumbling Floor · n` / `Breaks after you leave.` |
| Fog Floor | Dashed-cloud icon; dashed perimeter with an open grid center | Cells: `n` / infinity | `Fog Floor · n` / `Blocks goal sight only.` |
| Switch Plate and Gate | Matching ASCII-letter plate/gate icons and grid marks | Remaining linked sets: `n` / infinity | `Switch Set · n sets` / `Plate toggles its gate.` |
| Alternating Gate | Barred-gate icon with two phase marks; static bars plus explicit open/closed grid state | Cells: `n` / infinity; runtime state never changes the badge | `Alternating Gate · n` / `Changes each decision beat.` |

The repeated shared count, extra variants, linked units, and ambiguous runtime states are part of the
cost assessment, not unfinished UI decisions. They help explain why those candidates were rejected.

## Cost Comparison

| Candidate | Implementation | Balance | Asset | Browser transfer | Rendering | Tests | Migration |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Right-Turn Floor | M | H | L | L | M | H | M |
| North–South Rail Gate | M | M | L | L | M | H | M |
| Momentum / Ice | M | H | L | L | M | H | M |
| One-Way Arrow | H | H | L | H | M | H | H |
| First-Use Snare | M | H | L | L | H | H | M |
| Fast Floor | M | M | L | L | M | H | M |
| Portal Pair | VH | H | M | M | M | VH | H |
| Crumbling Wall/Floor | VH | H | L | M | H | VH | H |
| Fog Floor | M | H | L | L | M | H | M |
| Switch Plate and Gate | VH | VH | M | M | H | VH | H |
| Alternating Gate | H | VH | L | M | H | VH | M |

The asset rating assumes line-drawn primitives. Selecting new bitmap art changes the relevant asset
and browser costs and makes ASSET-01 a prerequisite.

## Right-Turn Floor Contract

### Player rule

> Turn right on the next move when that exit is open.

The solver's incoming direction is its most recent completed orthogonal move. Start and goal cells
are protected, so a placeable or fixed Right-Turn Floor cannot be the location where incoming
direction is absent or goal arrival has already ended the run.

Before every normal solver decision:

1. If the current cell is not Right-Turn Floor, use the normal solver rule.
2. Derive the relative right direction from the incoming direction.
3. If that adjacent edge and destination are traversable, move there without consulting the solver
   rule and without consuming Random or Seeker's random generator.
4. If the right exit is closed by a boundary, Wall, fixed Wall, or future edge rule, delegate the
   complete decision to the solver as though Right-Turn Floor were absent.

The forced move consumes one ordinary movement decision and interval. It increments the shared move
count, updates last direction and Scout heading, and increments Tracker's destination visit. Seeker
does not perform its line-of-sight check on a forced decision; it resumes normal sight evaluation on
the next decision. Slow Floor cannot occupy the same cell. A forced move that lands on Slow Floor
delays the following decision normally.

The cell stays walkable for static route preservation. Its rule is treated like a visible solver
decision override, not a hidden Wall. Balance tests must reject supplies or layouts that create a
trivial guaranteed loop even when the underlying board retains a route.

Finite/infinite supply, selection, placement, replacement, removal, moving, exhaustion, protected
cells, bounds, fixed ownership, replay, retry, and cancellation follow existing semantics exactly.
Each solver derives right from its own incoming direction; there is no shared runtime cell state.

### Visual contract

The grid treatment uses a clockwise bent-arrow/quarter-turn motif around the cell perimeter, where a
90%-cell solver sprite does not cover it. A violet fill may supplement but never replace the shape.
The fixed form adds the existing top-left lock without obscuring the turn marks. Rejection feedback
still renders last. The palette icon uses the same clockwise turn shape, leaving its lower-right
corner clear for the numeric or line-drawn infinity badge.

The bounded tooltip has two lines. Its exact first-line states are `Right Turn · 3`,
`* Right Turn · 3` when selected, `Right Turn · inf` for unlimited supply, and
`Right Turn · 0 · OUT` when exhausted. The second line is always:

```text
Right Turn · 3
Turn right when open.
```

Desktop keeps the accepted 500 ms hover delay. A completed touch tap that selects the item opens the
same bubble for 2,000 ms; selecting it again restarts the timer. Starting a palette drag does not
open the bubble, and a drag-drop placement does not leave one open. A board interaction, another
selection, cancellation, phase change, or the timeout dismisses it. The normal palette remains
icon-only.

## North–South Rail Gate Contract

### Player rule

> Cross this cell vertically only.

The gate is a walkable grid position with edge restrictions:

- north/south movement from a neighboring cell into the gate is legal when the gate destination is
  otherwise traversable;
- north/south movement from the gate to a neighboring cell is legal when that destination is
  otherwise traversable;
- east/west movement into or out of the gate is illegal;
- the gate adds no wait, free movement, or move-count change.

Every solver filters candidates through the same edge rule. Seeker's clear row/column check examines
each traversed edge: a vertical sightline may cross the gate and a horizontal sightline may not.
Tracker visit state and Scout heading change only after ordinary legal moves.

Route validation must accept `(from,to)` or equivalent edge semantics and verify every solver's
matching start/goal. Treating Rail Gate as merely walkable is a correctness bug. The fixed and
player-placeable forms use the same edge rule; only ownership and supply differ. Multi-solver levels
share the static gate orientation with no runtime state.

Supply and all canonical edit operations remain cell-based and need no orientation payload because
this deliberately bounded type is always north–south. A later horizontal or rotatable form is a new
design decision, not an implicit variant.

### Visual contract

Two heavy vertical rails, open at top and bottom and capped at the horizontal edges, provide the
non-color cue. A steel-blue fill is optional. The fixed lock occupies the top-left without breaking
both rail lines, and the palette badge remains lower-right. Tooltip:

```text
N-S Rail · 2
Cross vertically only.
```

Its selected, unlimited, and exhausted first-line variants use the same `*`, `inf`, and `0 · OUT`
forms as Right Turn. The same desktop and touch bubble lifecycle applies.

## Reproducible Evidence

`GameplayAdditionalCellTypesDesignTest` contains no production enum or runtime branch. Its reference
models first reproduce production traces for Random, Scout, Tracker, and Seeker on an ordinary board,
then apply only the candidate rule.

### Right-Turn junction

On a 4x4 board, start `(3,1)`, goal `(0,1)`, Walls `(3,0)` and `(3,2)`, and Right-Turn Floor `(2,1)`:

```text
. G . .
. . . .
. R . .
W S W .
```

The first move is forced north into `R`. North, west, and east are all open at `R`, but every solver
then moves east to `(2,2)`. Random consumes one draw for the sole-neighbor entry and no draw for the
forced turn. Seeker sees its goal north from `R` but the visible cell rule takes precedence and also
turns east without a draw. Scout's heading becomes east and Tracker records one visit at `(2,2)`.
Without `R`, north/west/east remain open and Scout moves west, proving that neither a walkable Slow
Floor nor an impassable Wall reproduces the same local choice. If `(2,2)` is a Wall, the Right-Turn
board produces the exact same full trace and random-draw count as the board without `R`, proving
fallback delegates rather than partially consuming behavior state.

### Rail multi-solver junction

On a 5x5 board, Rail `(2,2)` and Walls `(4,1)`, `(4,3)`, `(3,1)`, `(3,3)`:

```text
. . V . .
. . . . .
H . | . h
. W . W .
. W v W .
```

`H -> h` is the horizontal route `(2,0)` to `(2,4)`; `v -> V` is vertical `(4,2)` to `(0,2)`.
Empty or Slow Floor gives the horizontal solver a four-move shortcut. Rail closes that shortcut but
preserves a six-move detour and the vertical four-move route. Replacing Rail with Wall traps the
vertical start and removes its route. This is the strategic state Wall and Slow Floor cannot express.

The edge-aware reference search records distances `4` empty-horizontal, `6` rail-horizontal, `4`
rail-vertical, and no wall-vertical route. All four solver rules reach both goals deterministically
without a horizontal Rail crossing. A representative Random-horizontal/Scout-vertical pair keeps
independent traces and matching routes on the same immutable board.

### Palette capacity

Two additions mean at most four visible types with Wall and Slow Floor. Four 56x44 icon items plus
three 12 px gaps require 260 px. The test-side layout reference uses the production sizing formula:

| Viewport | Available palette width | Result |
| --- | ---: | --- |
| Desktop 1280x720 | 1248 px | Four 56 px items |
| Portrait 390x844 | 358 px | Four 56 px items |
| Constrained landscape 844x286 | 550 px | Four 56 px items |
| Safe landscape 756x286 | 462 px | Four 56 px items |
| Intermediate 600x421 | 306 px | Four 56 px items |
| Minimum policy case 568x270, 7x7 | about 274 px | Four 56 px items with about 14 px spare |

Every item remains at least 44x44, stays inside the viewport, and does not overlap another item, the
grid, Back, or Start. The count reference matches production rectangles for the two existing types
at every listed viewport. No drawer, scrolling, or layout prerequisite is needed for the shortlist.
GAMEPLAY-02 must compare its three real enum values plus the four-count reference; GAMEPLAY-03 must
replace the count reference with all four real enum values across the same viewports and browsers.

## Prerequisites and Follow-Up Boundaries

### Shared requirements

- Introduce one render-independent movement-effect query with enough context for current position,
  destination edge, and incoming direction. Do not distribute cell-type switches across solvers.
- Preserve exact Random/Seeker seed use, Scout heading, Tracker visits, fixed-step timing, and first-
  solver-wins multi-solver completion.
- Generalize fixed and mutable cell-effect lookup while retaining separate ownership and inventory.
- Migrate all five released definitions to explicit finite-zero supplies for each new placeable type;
  their initial palettes and traces must remain byte-for-byte/equality compatible where applicable.
- Replace binary palette names/marks/tooltips with exhaustive data-driven descriptors. Tooltip bounds
  must support two lines, remain clamped, and have the defined transient touch path.
- Use shape primitives. ASSET-01 is required only if later visual review selects bitmap art.

### Task separation

- GAMEPLAY-02 owns Right-Turn Floor, the minimum shared movement-effect seam, supply migration,
  rendering, tooltip accessibility, fixed equivalence, and cross-platform verification.
- GAMEPLAY-03 owns edge-aware pathfinding and North–South Rail Gate after GAMEPLAY-02 establishes the
  descriptor and effect boundaries.
- GAMEPLAY-02 owns the one/two-cell Right-Turn loop classification. Finite and infinite supplies
  remain valid domain values, but every released level keeps zero supply during implementation.
- GAMEPLAY-04 owns concrete finite-supply selection, exhaustive legal-layout balancing, physical
  phone playtest, progression, and release evidence. Infinite supply is not eligible for a released
  Right-Turn level without a later finite-proof strategy. Mechanics are not balanced by silently
  adding a level during implementation.

## Independent Review Resolution

Gameplay/domain review preferred Right-Turn Floor because it creates an arrival-relative choice with
ordinary inventory and no configuration state. UI/accessibility review found Momentum easiest to
draw but confirmed a fixed-relative Turn Floor fits as a fourth tool and that four total icons fit all
supported viewports. Release review preferred Rail Gate for its topology value and recommended a
straight-motion candidate only behind prototype evidence.

The accepted pair preserves those distinct strengths: Right-Turn is the first, lower-migration
movement-choice mechanic; Rail is the static topology mechanic whose edge-aware prerequisite is
explicit. Momentum remains recorded as the reserve rather than being silently reconsidered later.
No review supported oriented arrows, paired portals, or run-mutating geometry as the first addition.

## Remaining Risks

- Right-Turn chains may create trivial deterministic loops. GAMEPLAY-02 classifies one- and two-cell
  fixtures without weakening the ordinary finite/infinite inventory contract; GAMEPLAY-04 then
  enumerates every layout through a proposed finite released supply. Infinite Right-Turn supply is
  intentionally excluded from released levels unless a later design provides a finite proof bound.
- Rail's absolute north–south identity limits reuse. That is intentional scope control; a horizontal
  or rotatable variant needs a new card and responsive interaction evidence.
- A rule icon alone is insufficient for first exposure. The transient touch tooltip and introductory
  level must be tested with users; automation proves availability and layout, not comprehension.
- Adding enum values forces exhaustive migration across supplies, rendering, layout ids, browser
  fixtures, and fixed effects. Follow-up cards treat compiler failures as migration evidence rather
  than adding nullable/string fallbacks.
