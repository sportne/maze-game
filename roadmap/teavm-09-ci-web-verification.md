# WEB-09: Add Continuous-Integration Web Verification

Status: pending

Depends on: WEB-07, WEB-08

## Goal

Require the web build and browser smoke suite on pull requests without granting deployment
permissions to untrusted builds.

## Scope

- Extend CI to build the optimized JavaScript webapp after the normal quality gate.
- Run the automated browser smoke suite.
- Upload failure diagnostics and the built site as ordinary workflow artifacts.
- Cache Gradle and browser runtime downloads without caching generated production output.
- Keep the verification job read-only with `contents: read` permissions.

## Acceptance Criteria

- Pull requests cannot pass CI when TeaVM compilation or the browser smoke suite fails.
- The built site can be downloaded from successful CI runs for review.
- CI still runs the existing quality gate and Native Image build.
- The verification job cannot deploy Pages.

## Verification

- Validate the workflow syntax locally where tooling permits.
- Exercise both a successful run and an intentional browser-test failure on a temporary branch.
- Confirm uploaded artifacts contain no credentials or machine-local paths.
