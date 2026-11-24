# kmp-build Specification

## Purpose

How a module becomes Kotlin Multiplatform in this project: one convention plugin decides the
targets, SDK levels and test setup, so every shared module builds and tests the same way on
Android and iOS.

## Requirements

### Requirement: One convention plugin for shared modules
A module that applies `naveenapps.plugin.kmp.library` SHALL be a Kotlin Multiplatform library
with an Android target and the iOS targets `iosArm64` and `iosSimulatorArm64`. Its Android
compileSdk and minSdk SHALL come from `AndroidConfigExt.kt`, its Android namespace SHALL be
derived from the module path, and its JVM target SHALL match the Android modules (17). Modules
SHALL NOT set these values themselves.

#### Scenario: Compiling core:foundation for every target
- **WHEN** `./gradlew :core:foundation:assemble :core:foundation:compileKotlinIosArm64 :core:foundation:compileKotlinIosSimulatorArm64` runs on a Mac with Xcode
- **THEN** the Android library and both iOS compilations succeed

#### Scenario: Namespace from the module path
- **WHEN** the plugin is applied to `:core:foundation`
- **THEN** the Android namespace is `com.naveenapps.expensemanager.core.foundation`

### Requirement: Shared code compiles without JVM or Android APIs
Code in a shared module's `commonMain` source set SHALL compile against Kotlin common libraries
only. A `java.*` or `android.*` import in `commonMain` SHALL fail compilation.

#### Scenario: A JVM import slips into common code
- **WHEN** a file in `core/foundation/src/commonMain` imports `java.util.Locale`
- **THEN** `compileKotlinIosArm64` fails with an unresolved reference

### Requirement: Common tests run on Android and on the iOS simulator
Tests in a shared module's `commonTest` SHALL run both as Android host (JVM) tests and on the
iOS simulator. Tests that need JVM-only reference code SHALL live in the Android host test
source set (`androidHostTest`).

#### Scenario: Running core:foundation's tests
- **WHEN** `./gradlew :core:foundation:allTests` runs on a Mac with Xcode
- **THEN** the `commonTest` tests run for the Android host target and for `iosSimulatorArm64`,
  and all pass

### Requirement: Android builds don't need a Mac
Android builds, unit tests and lint SHALL keep working on hosts without Xcode (Linux CI). Apple
targets SHALL be skipped there, not fail the build.

#### Scenario: CI on Linux
- **WHEN** `./gradlew assembleDebug test lintDebug` runs on Linux
- **THEN** it succeeds, and the Apple targets of shared modules are skipped
