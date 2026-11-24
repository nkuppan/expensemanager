# Proposal: add-ios-smoke-app

## Why

Changes 1–4 made all shared business logic (core:foundation, core:model, core:repository,
core:domain) compile and pass its tests on the iOS simulator. Nothing yet proves that this code
can be **linked into an iOS app and called from Swift**. Phase 1 ends with that proof: a
throwaway SwiftUI app that runs real shared logic on an iPhone simulator. It shakes out the
framework packaging, the Xcode build step and the Kotlin-to-Swift API shape before Phase 3
builds the real app on the same setup.

Two CI gaps came to light while preparing this change (25 Nov):
- **The shared modules' tests no longer run on CI.** The `unit-test` job runs `debugCoverage`,
  which only the Android convention plugins register. Since changes 1–4 moved core:foundation,
  core:model, core:repository and core:domain to the KMP plugin, their tests (338 in
  core:domain alone) are skipped on CI.
- **Nothing on CI compiles or tests the iOS targets.** All jobs run on Ubuntu, where Apple
  targets are skipped (`kotlin.native.ignoreDisabledTargets`).

## What Changes

- **New `:shared` module (umbrella framework).** Decision 25 Nov: a dedicated module.
  - It uses `naveenapps.plugin.kmp.library` and depends on core:domain and core:model.
  - It exports one static framework named `Shared` for `iosArm64` and `iosSimulatorArm64`
    (iOS 16).
  - Its only public API is a small, Swift-friendly entry point: `ReceiptReader.read(text)`.
    It returns a `ReceiptSummary` with plain fields:
    - `amount: Double?`;
    - `payee: String?`;
    - `isIncome: Boolean`;
    - `dateEpochMillis: Long?`;
    - `accountDigits: List<String>`.

    It calls the shared `ReceiptParser`, and ships no logic of its own.
  - A `commonTest` checks that `read` returns exactly what `ReceiptParser.parse` returns for
    sample receipts. It runs on the Android host and the iOS simulator.
- **New `iosApp/` folder (smoke app).** Decision 25 Nov: XcodeGen.
  - `project.yml` describes one iOS 16 SwiftUI app target, `ExpenseManagerSmoke`.
  - A pre-build script runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode`, so Xcode
    always links a freshly built framework.
  - One screen: a text field to paste receipt text (with two sample buttons: a UPI message and a
    shop bill), and the parsed amount, payee, type, date and account digits. Swift does the
    formatting (`DateFormatter`, `NumberFormatter`), because shared code returns data.
  - The generated `.xcodeproj` is git-ignored; `xcodegen` recreates it from `project.yml`.
- **New macOS CI job `shared-ios`** in `.github/workflows/build.yml`. Decision 25 Nov: shared
  tests only.
  - It runs on `macos-latest`: `allTests` for the four shared modules and `:shared`, plus the
    simulator framework link.
  - `allTests` runs both the Android host tests and the iOS simulator tests, so this one job
    closes both CI gaps above.
  - It caches `~/.konan`, the Kotlin/Native toolchain.
- **Docs:** CLAUDE.md (the `:shared` module, `iosApp/`, the iOS commands and the new CI job)
  and the roadmap.

User-visible impact: none. The Android app doesn't depend on `:shared` or `iosApp/`.

## Capabilities

### New Capabilities
- `ios-app`: how the iOS app links and calls shared code (the umbrella framework, the Xcode
  project and the smoke screen).

### Modified Capabilities
- `kmp-build`: CI runs the shared modules' tests on the Android host and the iOS simulator.

## Impact

- **New:** `shared/` (the build file, `ReceiptReader.kt` and its test) and `iosApp/`
  (`project.yml`, `ExpenseManagerSmoke/ExpenseManagerSmokeApp.swift` and `ContentView.swift`).
- **Changed:**
  - `settings.gradle.kts` (`include(":shared")`);
  - `.gitignore` (`iosApp/*.xcodeproj`, `iosApp/build/`);
  - `.github/workflows/build.yml` (the new job);
  - `CLAUDE.md`.
- **Untouched:** every existing module. Android code doesn't change.
- **Tools on the Mac:** Xcode with an iOS 16+ simulator, already used since change 1, and XcodeGen
  (`brew install xcodegen`).
- **CI cost:** one macOS job per pull request and main push. On private repositories, macOS
  minutes cost 10× Linux minutes. The Kotlin/Native cache keeps the job to roughly 10–15
  minutes.

**Rollback:** revert this change's commits on `feature/kmp-migration`. Nothing else depends on
`:shared` or `iosApp/`.
