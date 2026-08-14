# ASSET-01: Ship Optional Art Separately and Load It on Demand

Status: proposed

Depends on: selection of at least one optional character, cosmetic, or campaign asset set for
runtime use

## Goal

Keep startup packages and the initial browser network waterfall limited to assets required for the
startup flow and selected gameplay, while publishing and loading optional runtime atlases only when
the player reaches the feature that needs them.

## Scope

- Add an explicit asset manifest that classifies every runtime asset as startup-required or optional
  and records its stable id, release path, byte size, and owning feature.
- Keep source masters, processing metadata, unused variants, download sidecars, and orphaned assets
  out of desktop, JavaScript, WebAssembly, native, and Pages runtime artifacts.
- Stage optional browser assets under stable cacheable paths without requesting them during startup,
  settings, level selection, or released levels that do not use them.
- Add one asynchronous loader shared by JavaScript and WebAssembly that requests an optional asset
  on feature entry, reuses an in-flight or completed load, exposes bounded loading/failure state, and
  disposes textures that no longer have an owner.
- Keep desktop behavior local and deterministic: optional assets may remain packaged when referenced
  by an enabled desktop feature, but must use the same manifest ids and ownership rules without a
  network dependency.
- Prefetch only when there is a concrete next action with a high probability of requiring the asset;
  do not preload the complete optional catalog after startup.
- Define separate initial-download and optional-content budgets so adding optional art cannot silently
  consume the startup budget.
- Do not introduce a CDN, service worker, remote content-management system, downloadable code, or
  general-purpose plugin architecture in this task.

## Acceptance Criteria

- A clean JavaScript and WebAssembly startup requests no optional sprite atlas, and navigating the
  released flows requests no atlas that those flows do not render.
- Entering a feature backed by an optional atlas requests that atlas exactly once, shows a bounded
  loading state, and renders the selected art after completion on both browser targets.
- Re-entering the feature uses the cached texture without another network request or duplicate GPU
  allocation; leaving it follows the documented ownership/disposal policy.
- A missing, corrupt, or failed optional request cannot crash or strand the game and produces a
  deterministic fallback or retry action.
- Desktop, JavaScript, WebAssembly, native, and Pages artifact manifests contain every referenced
  runtime asset and no source master, unreferenced derivative, or Windows download sidecar.
- CI fails when an optional asset is fetched eagerly, omitted from its published artifact, requested
  more than once, assigned an unstable path, or causes either byte budget to be exceeded.

## Verification

- Add manifest/unit coverage for classification, stable paths, duplicate ids, ownership, disposal,
  failure, retry, and cache reuse.
- Intercept browser requests in the shared JavaScript/WebAssembly scenario and assert the exact
  startup and feature-entry asset request sets and byte totals.
- Exercise successful, delayed, missing, and corrupt optional responses without relying on external
  network services.
- Inspect assembled desktop, native, JavaScript, WebAssembly, and Pages artifacts for required,
  optional, orphaned, source-master, and sidecar files.
- Run formatting, static analysis, coverage, architecture, browser smoke, artifact-budget, Pages,
  and native packaging gates before independent review and commit.
