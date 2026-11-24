# Tasks: add-kmp-foundation-module

Each group stops at its verify task. Claude writes the code; the developer runs the command on
the Mac and shares `build-log.txt`. A task is checked off only after a green run.

## 1. Build conventions (nothing applies them yet)

- [x] 1.1 Add the plugins `kotlin-multiplatform` (`org.jetbrains.kotlin.multiplatform`, version `kotlin`) and `android-kotlin-multiplatform-library` (`com.android.kotlin.multiplatform.library`, version `androidGradlePlugin`), and the library `kotlinx-coroutines-core`, to `gradle/libs.versions.toml`; declare both plugins `apply false` in the root `build.gradle.kts`
- [x] 1.2 Confirm the KMP Android target's DSL accessor in AGP 9.3.3 (`kotlin { android { } }`, type `KotlinMultiplatformAndroidLibraryTarget`) and that build-logic's `compileOnly` AGP and Kotlin Gradle plugin dependencies expose it; add a `compileOnly` dependency only if one is missing
  - Note (24 Nov): Android's docs confirm the type `com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget` and the `android {}` block (`androidLibrary {}` deprecated since AGP 9.1). They don't document access from a binary plugin, so `KmpLibraryConfigPlugin` looks the target up as the `android` extension on `kotlin`, falling back to `androidLibrary`. The existing `compileOnly` AGP and Kotlin Gradle plugin dependencies contain both APIs, so no new build-logic dependency was added. Task 1.5 confirmed it: build-logic compiled against AGP 9.3.3 and the full build was green (24 Nov).
- [x] 1.3 Write `KmpLibraryConfigPlugin` (id `naveenapps.plugin.kmp.library`) as in design D1, D2 and D6: Android target with namespace from the module path, `COMPILE_SDK`/`MIN_SDK`, host tests and JVM 17; `iosArm64()` and `iosSimulatorArm64()`; `kotlin("test")` in `commonTest`; `test` depends on `testAndroidHostTest`
- [x] 1.4 Register the plugin in `build-logic/convention/build.gradle.kts`; add `kotlin.native.ignoreDisabledTargets=true` to `gradle.properties` (design D7)
- [x] 1.5 Verify: `./gradlew assembleDebug test lintDebug > build-log.txt 2>&1` is green with no module using the plugin yet (it proves the build logic compiles and nothing else changed)

## 2. core:foundation module and the move

- [x] 2.1 Create `core/foundation/build.gradle.kts` (plugin `naveenapps.plugin.kmp.library`; `commonMain`: `kotlinx-coroutines-core`, `kotlinx-datetime`) and `include(":core:foundation")` in `settings.gradle.kts`
- [x] 2.2 Move `AppCoroutineDispatchers.kt`, `DateKeys.kt` and `InstantTime.kt` from `core/common/src/main/kotlin/.../core/common/utils/` to `core/foundation/src/commonMain/kotlin/com/naveenapps/expensemanager/core/foundation/`, changing only the `package` line
- [x] 2.3 In `core/common/build.gradle.kts` add `api(project(":core:foundation"))`; keep `DispatcherModule` in core:common, importing `AppCoroutineDispatchers` from the new package
- [x] 2.4 Update imports in every module (about 59 files) with a script: each moved symbol is imported from `…core.foundation`; where a file also calls the `java.util.Date` overload of `toMonthAndYearKey` (from `DateUtils.kt`), keep that import too (design D3)
- [x] 2.5 Add imports to the core:common equivalence tests (`DateKeysTest`, `InstantTimeTest`, `InstantDisplayTest`), which used the moved functions from their own package
- [x] 2.6 Check with the leftover scan that no file uses a moved symbol without the new import, and that no remaining `core.common.utils` import is unused
- [x] 2.7 Verify: `./gradlew assembleDebug test lintDebug > build-log.txt 2>&1` is green, all existing tests included

## 3. Shared tests and iOS compilation

- [x] 3.1 Add `core/foundation/src/commonTest/.../DateKeysCommonTest.kt`:
  - month, year and day keys for fixed dates (including "October 2026");
  - parsers accept the 3-letter month form and reject malformed keys;
  - month and year bounds in `TimeZone.of("Asia/Kolkata")` and `TimeZone.UTC` against fixed epoch values
- [x] 3.2 Add `core/foundation/src/commonTest/.../InstantTimeCommonTest.kt`:
  - `hourAndMinute`;
  - `withTime` (seconds kept and reset);
  - `withDateOf`;
  - `nextTimeOfDay` later today, tomorrow, and on the 8 March 2026 America/New_York gap night (expect 03:00 EDT);
  - `nextWeekdayAt` from a Sunday before and after 19:00
- [x] 3.3 Verify on the Mac: `./gradlew :core:foundation:compileKotlinIosArm64 :core:foundation:allTests > build-log.txt 2>&1` - the Android host and `iosSimulatorArm64` test runs both pass (the first run downloads the Kotlin/Native toolchain)

## 4. Documentation

- [x] 4.1 CLAUDE.md:
  - add core:foundation to the module list (pure Kotlin, shared, no Android);
  - add `naveenapps.plugin.kmp.library` to the convention-plugin ids;
  - update the KMP-readiness section: helpers now import from `core.foundation`, and shared modules may not depend on core:common
- [x] 4.2 Update the project's `claude/kmp-migration-roadmap.md` status for this change

## 5. Final verification

- [x] 5.1 Full Android build on the Mac: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` ends with BUILD SUCCESSFUL
- [x] 5.2 iOS: `./gradlew :core:foundation:allTests` passes on `iosSimulatorArm64`
- [x] 5.3 Smoke test on an Android device (only imports changed, so a quick pass):
  - add and edit a transaction (date and time pickers);
  - open a monthly budget;
  - check that the Home "Upcoming" card and the reminder still show
- [x] 5.4 `openspec validate add-kmp-foundation-module --strict` passes; then archive the change (`openspec archive add-kmp-foundation-module`), which merges the delta specs into `openspec/specs/`
