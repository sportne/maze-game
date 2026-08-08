# M3-02: Extract the Minimal Mouse-Simulation Contract

Status: pending

Depends on: M3-01

## Goal

Extract the small runtime boundary needed by a second mouse without yet adding a behavior value that
the session cannot honor or changing released random behavior.

## Scope

- Introduce the smallest simulation contract needed by `GameSession`: current result and timed
  update.
- Make the existing random simulator implement the contract and type the session field to that
  contract while it continues to construct the random implementation directly.
- Leave `LevelDefinition` and its random seed unchanged in this card. M3-03 adds authored behavior
  selection only when both real implementations and the exhaustive construction switch exist.
- Preserve the existing random simulator as a concrete implementation and avoid registries,
  reflection, service loading, dependency-injection containers, or public extension APIs.

## Acceptance Criteria

- The session depends on the minimal shared simulation contract rather than the random concrete type.
- No mouse-behavior field, placeholder Scout, ignored enum value, or unsupported runtime branch is
  introduced before M3-03.
- Milestone 1 and 2 produce byte-for-byte-equivalent logical run results for their existing fixtures.
- Level ids, unlock derivation, result keys, serialized best results, and catalog order are unchanged.
- Architecture rules keep simulation code independent of rendering and platform launchers.

## Verification

- Re-run all existing random simulation timing, replay, session, persistence, and result tests.
- Prove existing session behavior is unchanged when the random implementation is referenced through
  the new contract.
- Run architecture tests and dependency analysis for the new contract.
- Run formatting, static analysis, tests, coverage, browser builds, Pages assembly, and native-image
  packaging before commit.
