# Design: add-kmp-foundation-module

## Context

- **Build:** Android Gradle Plugin 9.3.3 with built-in Kotlin, Kotlin 2.4.21 and KSP 2.3.12.
  The build-logic convention plugins configure every module (`naveenapps.plugin.android.library`,
  `.kotlin.basic`, `.di`, …), and the SDK levels live once in `AndroidConfigExt.kt`.
- **No KMP yet:** no module applies the Kotlin Multiplatform plugin. Under AGP 9,
  `com.android.library` can't be combined with KMP; shared modules must use
  `com.android.kotlin.multiplatform.library`, configured as `kotlin { android { … } }`.
- **Code to move:** the code in core:common that core:domain needs is already pure Kotlin. It
  depends only on kotlinx-datetime and coroutines, both of which publish iOS artifacts:
  - `AppCoroutineDispatchers.kt`, a data class of three dispatchers;
  - `DateKeys.kt`;
  - `InstantTime.kt`.
- **What stays in core:common:** `DispatcherModule`, the Koin module that builds the
  dispatchers with `Dispatchers.Main/IO/Default`, plus `DateUtils.kt` and `InstantDisplay.kt`
  (`SimpleDateFormat`), `DateBridges.kt`, the Android resources and the `Context` helpers.
- **Tests that pin the moved code:** `DateKeysTest`, `InstantTimeTest` and `InstantDisplayTest`
  in core:common. They compare it with the old `java.util` code, so they can only run on the
  JVM.
- **Who builds:** the developer builds on a Mac. CI runs Android builds on Linux.

## Goals / Non-Goals

**Goals:**
- One convention plugin that every Phase 1 module can apply unchanged.
- core:foundation compiling for Android, iosArm64 and iosSimulatorArm64, with its tests passing
  on the Android host and the iOS simulator.
- Every existing Android module and test compiling and passing with only import changes.

**Non-Goals:**
- Converting core:model, core:repository or core:domain (the next changes).
- Koin for iOS, an iOS framework or an Xcode project (the iOS smoke app change).
- A macOS CI job (added with the iOS smoke app, when there's an app to build).
- Moving the equivalence tests to common code. They need the old JVM code.

## Decisions

### D1. Shared modules use AGP's KMP library plugin through one convention plugin
`KmpLibraryConfigPlugin` (id `naveenapps.plugin.kmp.library`) does the following:
- applies `org.jetbrains.kotlin.multiplatform` and `com.android.kotlin.multiplatform.library`;
- configures `kotlin { android { namespace; compileSdk; minSdk; withHostTest {} } }` from
  `AndroidConfigExt.kt`, plus JVM target 17;
- adds `iosArm64()` and `iosSimulatorArm64()`;
- adds `kotlin("test")` to `commonTest`.

The plugin reads the module path to build the namespace, the same convention the Android
modules follow by hand.

*Alternatives:*
- `com.android.library` + KMP: rejected, because it doesn't work under AGP 9.
- A JVM-only target (`jvm()`) instead of an Android target: Android could consume it, but Phase
  2 needs `androidMain` for Room and `Context`-based code, so every module would be converted
  twice. Rejected.
- Configuring each module by hand: repeats SDK levels and targets in five or more modules. That
  goes against the existing convention-plugin rule in CLAUDE.md, so it was rejected.

### D2. Targets: android, iosArm64, iosSimulatorArm64
This matches the decision taken for the project: iPhone only, iOS 16+, Apple Silicon. There is no
`iosX64`, which only matters on Intel Macs and Intel CI runners. The iOS deployment target
(16.0) is set when the framework and Xcode project arrive in the smoke-app change; a klib has no
deployment target.

### D3. A new package, `com.naveenapps.expensemanager.core.foundation`
The moved files get the new module's package, and about 59 imports are updated by script.

*Alternative:* keep `…core.common.utils` in both modules (a split package). That avoids the
import edits, but one package would then live in two modules, one shared and one Android-only.
"Which module is this from?" becomes unanswerable from an import, and the split would leak into
the iOS framework's names. Rejected.

One subtlety: `toMonthAndYearKey` has an `Instant`/`LocalDate` version (moving) and a
`java.util.Date` version in `DateUtils.kt` (staying). A file that calls both needs both imports.
The import script detects `Date` receivers and keeps the old import alongside the new one.

### D4. Koin wiring and display formatters stay in core:common
`DispatcherModule` stays put. iOS needs its own wiring: `Dispatchers.Main` runs on the main
queue there, and `Dispatchers.IO` comes from a separate `kotlinx.coroutines.IO` import on
Native. That belongs with the iOS DI setup in Phase 2. The formatters stay because they use
`SimpleDateFormat`.

*Alternative:* add koin-core to core:foundation now. Rejected, because nothing on iOS would use
it yet.

### D5. Test layout
Plain behaviour tests go in core:foundation's `commonTest`, with fixed expected strings and
instants, so the same assertions run on the JVM and on the iOS simulator. They cover:
- the keys and their parsers;
- period bounds;
- `withTime`/`withDateOf`;
- `nextTimeOfDay`/`nextWeekdayAt`, including a gap night in America/New_York.

The equivalence tests that run the old `java.util` code stay in core:common's `src/test`, with
updated imports. core:common depends on core:foundation, so they still reach both versions.

### D6. Existing test commands keep covering the new module
The KMP Android host test task is `testAndroidHostTest`, so `./gradlew test` and CI's per-module
`debugCoverage` wouldn't run it on their own. The convention plugin therefore makes the
module's `test` lifecycle task depend on `testAndroidHostTest`, reusing a `test` task if one
already exists. The usual Android command then runs the shared tests too, and the iOS tests run
through `allTests` on a Mac.

*Alternative:* add `allTests` to CI. Rejected, because CI runs on Linux, where `allTests` skips
iOS anyway, and every developer command would change.

### D7. Quiet, non-failing Apple targets on Linux
`gradle.properties` gets `kotlin.native.ignoreDisabledTargets=true`. On Linux CI, the Kotlin
Gradle plugin then skips Apple targets without warning, and Android-only builds keep working
unchanged.

## Risks / Trade-offs

- **First iOS compile downloads the Kotlin/Native toolchain** (about 1 GB, several minutes) →
  expected once per machine. Run `:core:foundation:compileKotlinIosArm64` once before the full
  verification.
- **The convention plugin's DSL accessors for the KMP Android target differ between AGP
  versions** (`androidLibrary {}` was renamed to `android {}`) → task 1.2 confirms the accessor
  against AGP 9.3.3's API before writing the plugin. If it differs, the plugin uses whatever
  AGP 9.3.3 exposes, with no design change.
- **The import update misses a file, or adds a wrong second import** → after the script runs,
  scan every changed file:
  - each moved symbol is imported from the new package;
  - each remaining `core.common.utils` import is still used;
  - the build is the final check.
- **CI coverage (Jacoco `debugCoverage`) doesn't know KMP modules**, so core:foundation's
  coverage won't appear in the aggregate report. That's acceptable for now: its tests still
  run under `test` (D6). Revisit when core:domain converts, because its coverage matters.
- **`api(project(":core:foundation"))` in core:common** exposes the foundation code to every
  Android module. That's intended for the migration period, since today everything already
  sees these functions through core:common.

## Migration Plan

1. On `feature/kmp-migration`, land the build conventions first. Nothing applies them yet, so the
   Android build must stay green.
2. Create core:foundation with the moved files, the `api` dependency and the import updates in
   one commit. The build is broken between "move" and "imports", so they ship together.
3. Add the common tests and the CLAUDE.md update.
4. Verify: the full Android build, unit tests and lint, plus `:core:foundation:allTests` on the
   Mac.

**Rollback:** revert the change's commits. There's no data or schema impact, and `main` is
unaffected until the branch merges.

## Open Questions

- None blocking. The macOS CI job's timing is decided in the iOS smoke-app change.
