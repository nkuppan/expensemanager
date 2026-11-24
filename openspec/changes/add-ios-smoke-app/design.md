# Design: add-ios-smoke-app

## Context

- **Shared code today:** core:foundation, core:model, core:repository and core:domain are KMP
  modules (`naveenapps.plugin.kmp.library`: android + iosArm64 + iosSimulatorArm64). Their
  tests pass on the iOS simulator. None of them builds an iOS framework.
- **What can run on iOS now:** pure logic only. Repositories have no iOS implementation until
  Phase 2 (Room KMP, DataStore), so use cases that need data can't run, and Koin isn't started
  on iOS. The receipt parser needs neither: it takes text, the current time and a time zone,
  and returns a `ParsedReceipt`.
- **Agent limits:** the agent can't run Xcode, XcodeGen or Gradle. It writes the files, and the
  developer runs the commands on the Mac.
- **CI** (`.github/workflows/build.yml`, triggered by pull requests and pushes to `main`) has
  four Ubuntu jobs:
  - `code-style`;
  - `design-system`;
  - `bundle-release`;
  - `unit-test`, which runs `./gradlew debugCoverage`. `debugCoverage` is registered by
    `configureJacoco()` in the Android app, library and feature convention plugins only.

## Goals / Non-Goals

**Goals:**
- One Gradle task produces the `Shared` framework; Xcode links it with no manual steps beyond
  `xcodegen`.
- Swift calls real shared logic (`ReceiptParser`) on the simulator and shows the result.
- CI runs every shared module's tests, on the JVM and on the iOS simulator.

**Non-Goals:**
- The real iOS app, Compose Multiplatform UI, navigation, Koin on iOS, data storage (Phases 2–3).
- App Store signing, icons, bundle settings beyond a simulator build.
- Building the Swift app on CI (decision 25 Nov: shared tests only).
- Jacoco coverage for KMP modules. The tests run; coverage numbers for shared code can come later.

## Decisions

### D1. A `:shared` umbrella module exports the framework
`shared/build.gradle.kts`:
- applies `naveenapps.plugin.kmp.library`, so it gets the same targets and test setup;
- for each iOS target, `binaries.framework { baseName = "Shared"; isStatic = true }`;
- `commonMain` uses `implementation(project(":core:domain"))` and
  `implementation(project(":core:model"))`.

Only `:shared`'s own public API reaches Swift, because nothing is `export(...)`ed. This keeps
the generated Objective-C header small, and Phase 3 decides what the real app exports.

Static, because a static framework needs no embedding or signing at run time; Xcode links it
into the app binary. `embedAndSignAppleFrameworkForXcode` still copies it to the path Xcode
expects.

*Alternatives:*
- Build the framework from core:domain. Then every later shared module would have to be
  exported through the domain. Rejected (decision 25 Nov).
- A dynamic framework. That needs embed-and-sign at run time, with no benefit for a single app.
  Rejected.

### D2. A Swift-friendly facade, not the domain types
`ReceiptReader` (an `object` in `com.naveenapps.expensemanager.shared`):

```kotlin
object ReceiptReader {
    fun read(text: String): ReceiptSummary {
        val parsed = ReceiptParser.parse(text)
        return ReceiptSummary(
            amount = parsed.amount,
            payee = parsed.payee,
            isIncome = parsed.type == TransactionType.INCOME,
            dateEpochMillis = parsed.date?.toEpochMilliseconds(),
            accountDigits = parsed.accountDigits,
        )
    }
}
```

Why a facade:
- `ReceiptParser.parse` has default arguments (`now`, `zone`), which Objective-C/Swift export
  doesn't support.
- `ParsedReceipt.date` is a `kotlin.time.Instant`, which reaches Swift as an opaque class.
- `TransactionType` would drag the model's enum into the header.

The facade passes plain values, and Swift formats them, in line with the rule that shared
code returns data. Swift sees `ReceiptReader.shared.read(text:)`; `Double?` and `Long?` arrive
as `KotlinDouble?` and `KotlinLong?`.

*Alternative:* call `ReceiptParser` directly from Swift with every argument. That exports
`Instant`, `TimeZone` and `TransactionType`, which are awkward to build and read in Swift.
Rejected for the smoke app; Phase 3 revisits the API shape (or Swift export) for the real app.

### D3. XcodeGen project in `iosApp/` (decision 25 Nov)
`iosApp/project.yml`:
- one `application` target, `ExpenseManagerSmoke`, iOS 16.0, sources
  `ExpenseManagerSmoke/`, bundle ID `com.naveenapps.expensemanager.smoke`, and an Info.plist
  generated from `info.properties` (`UILaunchScreen: {}`);
- settings:
  - `FRAMEWORK_SEARCH_PATHS = $(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`;
  - `OTHER_LDFLAGS = -framework Shared`;
  - `ENABLE_USER_SCRIPT_SANDBOXING = NO`, so the Gradle script can write to `shared/build`;
- `preBuildScripts`: `cd "$SRCROOT/.." && ./gradlew :shared:embedAndSignAppleFrameworkForXcode`.

The generated `iosApp/ExpenseManagerSmoke.xcodeproj` is git-ignored; `xcodegen` makes it.

*Alternatives:*
- A hand-made Xcode project. That's a large `.pbxproj` in git, and manual steps the agent can't
  check. Rejected (decision 25 Nov).
- An app packaged with SwiftPM only. Rejected: packaging a SwiftUI app through SwiftPM alone is
  awkward.

### D4. One SwiftUI screen
`ExpenseManagerSmokeApp.swift` (`@main`) shows `ContentView`:
- a `TextEditor` for receipt text, and two buttons that fill in samples: the GPay message and
  the shop bill from `ReceiptParserTest`;
- below it, the result of `ReceiptReader.shared.read(text:)` on every edit: amount
  (`NumberFormatter`, two decimals), payee, "Income"/"Expense", date (`DateFormatter`, medium
  style, device zone) and account digits.

There's no state beyond the text and no other screens.

### D5. macOS CI job `shared-ios` (decision 25 Nov: shared tests only)
The job runs on `macos-latest` with a 60-minute timeout, using the same setup steps as the
other jobs (checkout, wrapper validation, JDK 17, Gradle). It also caches `~/.konan` with
`actions/cache`, keyed on `gradle/libs.versions.toml`. It runs:

```
./gradlew :core:foundation:allTests :core:model:allTests :core:repository:allTests \
  :core:domain:allTests :shared:allTests :shared:linkDebugFrameworkIosSimulatorArm64
```

If the run fails, it uploads `**/build/reports/tests/`.

`allTests` covers `testAndroidHostTest` and `iosSimulatorArm64Test`. So this job also restores
the JVM tests the `unit-test` job stopped running when the modules became KMP.

*Alternatives:*
- Also run `testAndroidHostTest` in the Linux `unit-test` job, for cheaper JVM feedback. That
  duplicates the macOS run. Rejected for now; it's easy to add if macOS minutes become a
  concern.
- Build the Swift app on CI too. Rejected (decision 25 Nov).

### D6. CI only runs on pull requests and `main`
`build.yml` isn't triggered by pushes to `feature/kmp-migration`. That's deliberate: CI runs
when the branch is proposed for merging. So the release-build checks from changes 3 and 4, and
this new job, need a pull request from `feature/kmp-migration` (a draft is fine). The workflow
triggers are not changed.

## Risks / Trade-offs

- **Xcode's build script doesn't find Java** (Xcode runs scripts with a minimal environment) →
  the script exports `JAVA_HOME="$(/usr/libexec/java_home -v 17)"` when `JAVA_HOME` is unset.
- **The XcodeGen or Xcode version on the Mac differs from what the YAML assumes** → the YAML
  uses only long-standing keys (`targets`, `settings`, `preBuildScripts`, `info`). The verify
  task names the exact `xcodebuild` command, so failures are visible.
- **A Kotlin/Native and Xcode version mismatch** on `macos-latest` → the job pins nothing
  extra. If Kotlin 2.4.21 needs a specific Xcode, the job adds `maxim-lobanov/setup-xcode` with
  that version.
- **macOS CI minutes cost more** (10× on private repositories) → one job, with the `~/.konan`
  cache, only on pull requests and `main`.
- **Phase 3 replaces the smoke app** → `iosApp/` is throwaway by design. `:shared` and the CI
  job stay.

## Migration and rollback

- Only new modules, folders and a new CI job. No Android code, stored data or schema changes.
- Groups: 1 `:shared` and its test; 2 `iosApp/` and a simulator run; 3 the CI job; 4 docs;
  5 final verification.
- Rollback: revert this change's commits on `feature/kmp-migration`.
