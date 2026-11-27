## ADDED Requirements

### Requirement: CI tests shared code on the JVM and on iOS
CI SHALL run `allTests` for every shared module (core:foundation, core:model, core:repository,
core:domain, :shared) on a macOS runner for every pull request and push to `main`, so their
`commonTest` and `androidHostTest` tests run on the JVM and their `commonTest` tests run on the
iOS simulator. It SHALL also link the simulator framework.

#### Scenario: A shared test breaks on iOS only
- **WHEN** a pull request makes a `commonTest` test fail on `iosSimulatorArm64` but not on the JVM
- **THEN** the `shared-ios` job fails

#### Scenario: Domain JVM tests run on CI again
- **WHEN** the `shared-ios` job runs
- **THEN** core:domain's `androidHostTest` tests (Mockito-based) run and must pass, although the
  Linux `unit-test` job's `debugCoverage` doesn't include KMP modules
