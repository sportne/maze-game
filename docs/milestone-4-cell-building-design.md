# Milestone 4 Cell Building Design

## Player Goal

Milestone 4 keeps the released objective: delay the mouse beyond the level target while preserving
at least one viable path to the cheese. It changes the build phase from one implicit wall tool to a
small authored palette with per-level supplies.

The initial palette contains exactly two placeable cell types:

| Player name | Internal working name | Movement effect | Visual identity | Typical supply |
| --- | --- | --- | --- | --- |
| Wall | `NORMAL_WALL` | Blocks entry for Random and Scout | Existing solid white brick treatment | Infinite on released levels; finite or infinite on new levels |
| Slow Floor | `SLOW_FLOOR` | Walkable; entering it adds one movement interval before the next decision | Amber floor with a high-contrast crosshatch/hourglass mark | Finite on its introductory level |

Empty cells, the mouse start, and the cheese are board contents but are not palette items. No arrow,
one-way, teleport, trap, damage, mouse-specific, or player-authored start/cheese type is included.

## Released-Game Evidence

Milestone 2 physical-phone review established that direct tap-again clearing is preferable to a
separate mobile clear mode and that 44-pixel controls plus 32-pixel grid cells are the usable minimums.
Milestone 3 phone review removed verbose level/build hints and added Back from an abandoned build.
Those findings favor a persistent, compact palette with direct manipulation and short labels rather
than modal tool screens, long instructions, or hidden gestures.

The released build phase still offers only unlimited normal walls, so it cannot author scarcity or a
walkable delay. Repositioning currently requires clearing and replacing, which is needlessly costly
under time pressure and becomes error-prone once supplies are finite. Milestone 4 addresses those
specific limits while preserving the accepted tap-again, Back, minimum-target, and concise-copy rules.

## Slow Floor Timing

Slow Floor never changes which adjacent cells are legal or how either mouse ranks them. Random uses
the same seeded direction choice and Scout uses the same relative priority. After a mouse enters a
Slow Floor cell, its next movement decision is delayed by exactly one authored movement interval.
The entry itself counts as one move; the added wait does not increment the move count.

The shared timed-simulation boundary owns the delay so Random and Scout cannot interpret it
differently. Elapsed-time chunking, replay, cheese arrival, and maximum-timeout precedence remain
deterministic. If a delay reaches the maximum solve time, the existing timeout wins without another
movement decision. A Slow Floor under the mouse start or cheese is invalid because those cells are
protected and cannot contain a player item.

## Authored Supply

Every level explicitly authors one supply for each supported placeable type:

- `finite(n)` accepts any integer `n >= 0`.
- `infinite()` has no remaining count and renders with the infinity symbol plus a text fallback.
- Missing, duplicate, negative, or unknown entries are invalid authored data.
- Catalog order is also palette order unless a later accepted requirement needs separate ordering.

The three released levels migrate to infinite Wall and finite-zero Slow Floor, preserving their
exact build behavior. The new Milestone 4 level uses finite supplies of both Wall and Slow Floor, so
the release exercises infinite and finite authoring without giving its inventory challenge an
unlimited shortcut. Exact grid, mouse, counts, target, timeout, and accepted fixtures are balancing
outputs of M4-01 rather than guesses in this planning task.

Runtime inventory is derived fresh from the level definition for each attempt. Finite placement from
the palette consumes one item. Removing or replacing a finite placed item returns one. Infinite
placement and removal never change a count. Moving an existing item consumes and returns nothing.
Best-result persistence remains keyed only by stable level id; build layouts, selections, drag state,
and inventory are not persisted.

## Canonical Edit Operations

The domain exposes only the operations the accepted interactions need:

- `placeOrReplace(type, destination)` for palette drag and select-then-place.
- `remove(position)` for tap-again clearing and the existing desktop clear shortcut.
- `move(source, destination)` for repositioning a placed item.

Each operation returns the resulting immutable maze/inventory state plus an accepted or rejected
reason. It validates bounds, protected cells, occupancy rules, remaining supply, and final
start-to-cheese connectivity before publishing any mutation. There is no undo stack, command bus,
item registry, or generalized effect scripting.

### Placement equivalence

Drag-from-palette and select-then-place call the same `placeOrReplace` operation:

| Destination before edit | Selected type | Accepted result |
| --- | --- | --- |
| Empty | Available type | Place type; consume one when finite |
| Same type | Same type | Remove it and return one when finite, even when remaining supply is zero |
| Different placeable type | Available type | Replace atomically; consume selected finite item and return replaced finite item |
| Protected start or cheese | Any type | Reject; state and counts unchanged |
| Outside grid | Any type | Reject; state and counts unchanged |
| Empty or replacement would block every path | Wall | Reject; state and counts unchanged |
| Empty or different-type destination | Finite type at zero | Reject; state and counts unchanged |

Same-type removal takes precedence over availability so the last finite item can always be recovered
with the mobile tap-again interaction. Replacement availability is evaluated against the final
transaction. Replacing one finite type with another requires the selected item to be available before
the replaced item is returned.

### Existing-item movement

An existing-item drag may target only an empty, unprotected grid cell. A domain move whose source has
no placeable item is rejected with the original state and counts, even if malformed non-UI input
reaches it. Dropping a valid source item back on that same source is a no-op. Dropping on a different
occupied cell is rejected rather than introducing swap semantics. The final board with the source
cleared and destination filled is path-validated once. A successful move changes no inventory count;
an invalid or cancelled move leaves the original state untouched.

The source is visually reserved during a drag, but the canonical maze is not changed until a valid
drop commits. This makes cancellation, resize, timer expiry, and pointer loss restoration automatic
rather than reconstructing state.

## Selection and Gesture Rules

At attempt start, the first palette type with a nonzero or infinite supply is selected. If every
authored count is zero, no tool is active and the player may only start, leave, or select an exhausted
tool that cannot place anything. Clicking or tapping any palette item selects it and leaves it active
for repeated interaction. A finite item at zero is visibly exhausted and cannot place or replace,
but remains selectable so tapping an existing cell of that type can remove it and return one. The
active type remains selected when its last item is placed. Selecting a different type changes only
the active tool.

The same pointer-down sequence distinguishes taps and drags:

| Origin | Release before drag threshold | Release after drag threshold |
| --- | --- | --- |
| Palette item | Select item | Place or replace at the grid destination |
| Empty grid cell | Apply active selection | No drag begins; apply only if still a tap |
| Occupied grid cell | Apply active selection, including tap-again removal | Move the existing item to an empty destination |

The implementation will use one small CSS-pixel movement threshold, tested across desktop and touch,
rather than a time-based long press. Only the first active pointer owns a gesture; additional pointers
are ignored until it ends. Pointer capture keeps an active drag coherent outside the canvas.

| Gesture state | Entered by | Leaves by |
| --- | --- | --- |
| Idle | Gesture completion or cancellation | First eligible pointer down |
| Palette pressed | Pointer down on any palette type | Sub-threshold release selects; threshold crossing starts palette drag |
| Cell pressed | Pointer down on an empty or occupied grid cell | Sub-threshold release applies click semantics; occupied-cell threshold crossing starts move drag |
| Palette dragging | Palette threshold crossed | Grid release attempts `placeOrReplace`; any cancellation restores Idle unchanged |
| Cell dragging | Occupied-cell threshold crossed | Empty-grid release attempts `move`; any invalid/cancelled release restores Idle unchanged |

An exhausted palette drag still reaches the common `placeOrReplace` operation: dropping on that same
type removes it and returns one finite item, while an empty or different-type destination rejects.

Pointer cancel, capture loss, window blur, resize, and orientation change cancel the gesture without
a domain edit. Releasing outside the grid also cancels. When the build timer expires or Start is
pressed, the controller cancels any gesture before the session freezes the final maze and begins the
mouse run. All editing input is ignored outside the build phase.

Desktop right-click continues to remove a placed item without changing the selected palette type.
The mobile tap-again behavior remains available: with a type active, tapping a cell already containing
that same type removes it.

## Responsive Palette Contract

The palette is a bottom-screen build control, not an overlay on the grid. Each item shows a non-color
icon, short label, and `n` or infinity supply; selection uses border/shape as well as color. Interactive
targets remain at least 44 by 44 CSS pixels and grid cells remain at least 32 by 32 at the released
reference viewports.

- Desktop and portrait place the palette below the grid and above the Back/Start action row.
- Constrained and safe landscape use a single bottom strip for the palette while keeping Back/Start
  in the side panel; the grid may shrink only as far as the existing cell-size minimum.
- Safe-area insets apply to the palette and action controls.
- The initial two-type palette must fit without horizontal scrolling or a drawer.
- Drag previews clamp to the viewport, never obscure the supply label permanently, and show valid or
  rejected destination feedback without relying on color alone.

If the two types cannot fit a supported viewport with these minimums, the layout fails validation
rather than silently shrinking targets.

## Compatibility Boundaries

- Released level ids, unlock order, saved best results, random seeds, Scout behavior, and accepted
  replays remain unchanged.
- Released levels retain infinite Wall placement and have no usable Slow Floor supply.
- `CellContent` may gain the new rendered content, while player-placeable identity remains a small
  closed type separate from fixed mouse/cheese contents.
- `MazeState` generalizes from a wall set to immutable placed-cell data and remaining inventory. The
  normal-only `WallType` is replaced by the closed placeable type; old wall-specific methods may
  remain temporarily only as tested compatibility adapters during the ordered migration and must be
  removed when no caller needs them.
- Mouse simulations depend on traversability and entry delay, not UI types or level ids.
- Best-result storage needs compatibility tests but no schema change.
- JavaScript remains the production default and WebAssembly remains the equivalent opt-in preview.

## Required Design Evidence

M4-01 must establish a new level through deterministic fixtures before production authoring. Evidence
must include empty, deliberate pass, insufficient-inventory, replacement, Slow Floor timing, timeout,
Random/Scout comparison, whole-duration/chunked updates, and at least one solution using both types.
It must also confirm that the new level is not solvable merely by recreating an earlier unlimited-wall
strategy.

## Accepted Level 4 Parameters

M4-01 accepts a 7x7 fourth level with Scout, bottom-center start `(6,3)`, top-center cheese `(0,3)`,
a 25-second build time, 250-millisecond movement interval, 5.5-second target, 6.5-second timeout,
three Walls, and three Slow Floors. The stable production id will be `milestone-4`; production
authoring remains deferred to M4-08.

The accepted passing edit uses Walls at `(0,0)`, `(1,1)`, and `(2,2)`, with Slow Floors at `(6,2)`,
`(6,1)`, and `(6,0)`:

```text
W . . C . . .
. W . . . . .
. . W . . . .
. . . . . . .
. . . . . . .
. . . . . . .
. S S M . . .
```

`M` and `C` are protected, `W` is Wall, and `S` is Slow Floor. Scout follows this literal route:

```text
(6,3) (6,2) (6,1) (6,0) (5,0) (4,0) (3,0) (2,0) (1,0) (2,0)
(2,1) (3,1) (3,2) (3,3) (2,3) (1,3) (1,2) (0,2) (0,1) (0,2) (0,3)
```

The 20 moves occur at 0.25, 0.75, 1.25, 1.75, then every 0.25 seconds through 5.75 seconds.
The three Slow Floor waits add 0.75 seconds without changing the route or move count. Empty finishes
in 3.0 seconds, Slow-Floor-only in 3.75 seconds, and the same Walls without Slow Floor in 5.0
seconds, so each fails. Exhaustively evaluating every legal layout with zero through three Walls
finds 5.5 seconds as the maximum and no timeout, which does not exceed the target; the finite Wall
supply therefore cannot recreate an earlier unlimited-Wall pass. Test-side editing evidence also
exercises infinite-Wall/zero-Slow-Floor authoring as the released-level compatibility case.

A timeout fixture uses Walls at `(0,1)`, `(1,2)`, `(2,1)` and Slow Floors at `(1,0)`, `(2,0)`,
`(1,3)`. Scout enters the final Slow Floor at 6.25 seconds, then its pending extra wait reaches the
6.5-second timeout at `(1,3)` after 19 moves, with no post-timeout decision. Whole-duration and
100-millisecond chunked runs produce identical traces, decision timestamps, counts, and results.
Parallel seeded-Random fixtures
prove the same Slow Floor timing rule preserves its route and move count. Random seed 53 times out
even on the empty board at `(4,3)` after 26 moves; the combined board also times out, at `(4,4)`
after 25 moves. Because its do-nothing and combined layouts both pass, it does not provide Scout's
deliberate, teachable combined-type threshold; Scout is therefore retained for Level 4. The
test-side wall-only traces for both mouse behaviors are cross-checked against the existing production
simulation implementations so the reference timing model cannot silently redefine route choice.

Every implementation card retains formatting, static analysis, coverage, architecture, desktop,
JavaScript, WebAssembly, responsive touch, Pages, Safari, and native-image gates in proportion to its
scope. The release card requires a physical-phone playtest in portrait and landscape and records any
qualitative criteria that were not actually observed rather than treating automation as playtest.
