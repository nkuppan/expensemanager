# Proposal: add-kmp-foundation-module

## Why

Nothing in the build can compile for iOS yet. The first shared layer, core:domain, needs pure
Kotlin utilities - the coroutine dispatcher holder, the stored-key date functions and the
date/time edit helpers - that currently live in core:common, an Android library with string
resources and Context helpers that can't go to iOS. This change sets up the Kotlin
Multiplatform (KMP) build once, as a convention plugin, and proves it end to end on the
smallest possible module: a new core:foundation holding only that pure code. Every later
Phase 1 change (core:model, core:repository, core:domain, the iOS smoke app) reuses the plugin.

## What Changes

- New convention plugin `naveenapps.plugin.kmp.library` in build-logic. It applies
  `org.jetbrains.kotlin.multiplatform` and AGP 9's `com.android.kotlin.multiplatform.library`,
  and configures the Android target (namespace from the module path, `COMPILE_SDK`/`MIN_SDK`
  from `AndroidConfigExt.kt`, JVM 17, host tests), the iOS targets `iosArm64` and
  `iosSimulatorArm64`, and `kotlin("test")` for `commonTest`.
- Version catalog: the two plugins and `kotlinx-coroutines-core`; root `build.gradle.kts`
  declares both plugins with `apply false`.
- New module `:core:foundation` (package `com.naveenapps.expensemanager.core.foundation`),
  moved from core:common without behaviour changes:
  - `AppCoroutineDispatchers`
  - `DateKeys.kt`: stored and matched keys, period bounds, `Instant.toLocalDateIn`
  - `InstantTime.kt`: `hourAndMinute`, `withDateOf`, `withTime`, `nextTimeOfDay`,
    `nextWeekdayAt`
- core:common depends on core:foundation with `api`, keeps everything Android (resources,
  `Context` helpers, `DateUtils`/`InstantDisplay` formatters, the Koin dispatcher module), and
  loses the moved files.
- About 59 files change the package in their imports. Files that use both the `Date` and the
  `Instant` overload of `toMonthAndYearKey` import both.
- Tests:
  - new `commonTest` tests in core:foundation, which also run on the iOS simulator;
  - the existing equivalence tests (`DateKeysTest`, `InstantTimeTest`) stay JVM tests, because
    they run the old `java.util` code as the reference.
- CLAUDE.md: the new plugin and module are added to the architecture section.

User-visible impact: none. No behaviour, text or stored data changes.

## Capabilities

### New Capabilities
- `kmp-build`: how a module becomes Kotlin Multiplatform, which targets it compiles for, and
  how its tests run on Android/JVM and on the iOS simulator.

### Modified Capabilities
- `shared-code-boundaries`: adds core:foundation as a shared module, and the rule that shared
  modules may depend only on other shared modules, never on core:common or any Android-only
  module.
- `date-time-handling`: the key, period-bound and scheduling functions become shared code with
  the same results on Android and iOS.

## Impact

- **Build:** `build-logic/convention` (new plugin class and registration), `gradle/libs.versions.toml`,
  root `build.gradle.kts`, `settings.gradle.kts`.
- **Modules:** new `:core:foundation`; `:core:common` (files removed, `api` dependency added);
  import updates in `:core:domain`, `:core:data`, `:core:notification`, `:core:designsystem`,
  `:core:testing`, `:app` and the features `transaction`, `debt`, `budget`, `dashboard`,
  `analysis`, `filter`, `category`, `account`, `settings` and others that inject
  `AppCoroutineDispatchers`.
- **Dependencies:** no new libraries beyond `kotlinx-coroutines-core`, which the app already gets
  through `kotlinx-coroutines-android`. kotlinx-datetime and coroutines both publish iOS
  artifacts.
- **Tooling:** iOS compilation needs a Mac with Xcode for `iosSimulatorArm64Test`. Android-only
  builds (CI today) don't compile iOS: Kotlin skips Apple targets on non-Mac hosts with a
  warning.

**Rollback:** revert this change's commits on `feature/kmp-migration`. Nothing is stored or
migrated, and main is untouched.
