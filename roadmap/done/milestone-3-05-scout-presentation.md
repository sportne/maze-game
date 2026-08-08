# M3-05: Give Scout a Unique Visual and UI Identity

Status: complete

Archived: yes

Depends on: M3-04

## Goal

Make the active mouse behavior immediately recognizable without adding a tutorial system or relying
on color alone.

## Scope

- Add a release-ready transparent Scout sprite compatible with the existing pixel-art presentation.
- Use a blue cap with a high-contrast star badge so silhouette/detail as well as color distinguishes
  Scout from the red-scarf random mouse without revealing its turning preference.
- Keep the current random mouse sprite and cheese presentation unchanged.
- Select the sprite from immutable level mouse behavior rather than level id or screen-specific
  conditionals.
- Show the player-facing name `Scout` and only “Scout follows a consistent search pattern” on level
  selection and build presentation.
- Permit result feedback to suggest watching turns at intersections, but never disclose the exact
  left, straight, right, back order in player-facing text.
- Identify the active mouse consistently during running and results without overcrowding compact
  portrait or constrained-landscape layouts.
- Preserve asset attribution, artifact budgets, loading behavior, and WebAssembly packaging.

## Acceptance Criteria

- Players can distinguish Scout in grayscale or without reading its color.
- Initial presentation does not reveal or visually encode Scout's left-first preference.
- The correct sprite and name follow the selected level through build, run, result, retry, replay, and
  next-level navigation.
- The explanation fits supported desktop, 390x844 portrait, 844x286 constrained-landscape, and
  756x286 safe-content layouts without overlap or undersized controls.
- Missing or invalid sprite configuration fails visibly during development rather than silently using
  the wrong mouse.
- Existing level presentation and release artifact budgets remain valid.

## Verification

- Add asset-region/loading and behavior-to-presentation selection tests.
- Add renderer assertions for both mouse names, concise explanation, and correct sprite selection.
- Extend responsive layout tests across every phase and all three level definitions.
- Visually inspect desktop, portrait, constrained-landscape, and safe-content captures.
- Run formatting, static analysis, tests, coverage, web linting, both TeaVM builds, artifact-budget
  checks, Pages assembly, and native-image packaging before commit.
