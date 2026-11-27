# Tasks: add-ios-smoke-app

Group 1 builds the framework, group 2 the Xcode app, and group 3 the CI job. The agent writes
the files; the developer runs every command on the Mac.

## 1. The `:shared` umbrella module (design D1, D2)

- [x] 1.1 `settings.gradle.kts`: `include(":shared")`
- [x] 1.2 `shared/build.gradle.kts`: `naveenapps.plugin.kmp.library`; `iosArm64()` and `iosSimulatorArm64()` each get `binaries.framework { baseName = "Shared"; isStatic = true }`; `commonMain`: `implementation(project(":core:domain"))`, `implementation(project(":core:model"))`
- [x] 1.3 `shared/src/commonMain/kotlin/com/naveenapps/expensemanager/shared/ReceiptReader.kt`: `object ReceiptReader { fun read(text: String): ReceiptSummary }` and `data class ReceiptSummary(amount: Double?, payee: String?, isIncome: Boolean, dateEpochMillis: Long?, accountDigits: List<String>)`, delegating to `ReceiptParser.parse(text)`
- [x] 1.4 `shared/src/commonTest/.../ReceiptReaderTest.kt`: for the GPay, bank-credit and shop-bill samples, every `ReceiptSummary` field equals the matching `ReceiptParser.parse` field (`now` and `zone` fixed through a test-only overload, `internal fun read(text, now, zone)`)
- [x] 1.5 Verify: `./gradlew :shared:allTests :shared:linkDebugFrameworkIosSimulatorArm64 > build-log.txt 2>&1` is green; `shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework` exists; then `./gradlew assembleDebug spotlessCheck >> build-log.txt 2>&1` is green

## 2. The SwiftUI smoke app (design D3, D4)

- [x] 2.1 `iosApp/project.yml` as in design D3 (iOS 16, bundle ID `com.naveenapps.expensemanager.smoke`, framework search path, `-framework Shared`, user-script sandboxing off, Gradle pre-build script that sets `JAVA_HOME` when it's missing)
- [x] 2.2 `iosApp/ExpenseManagerSmoke/ExpenseManagerSmokeApp.swift` (`@main`) and `ContentView.swift` (text editor, "GPay sample" and "Shop bill sample" buttons, the parsed fields formatted with `NumberFormatter`/`DateFormatter`)
- [x] 2.3 `.gitignore`: `iosApp/*.xcodeproj/`, `iosApp/build/`, `shared/build/` is already covered by `build/`
- [x] 2.4 Verify on the Mac: `brew install xcodegen` (once); `cd iosApp && xcodegen && xcodebuild -project ExpenseManagerSmoke.xcodeproj -scheme ExpenseManagerSmoke -destination 'generic/platform=iOS Simulator' build > ../build-log.txt 2>&1` ends with `** BUILD SUCCEEDED **`; then open the project in Xcode, run it on an iPhone simulator, tap "GPay sample" and check: 250.00, SWIGGY, Expense, 7 Nov 2026 7:15 pm

## 3. macOS CI job (design D5)

- [x] 3.1 `.github/workflows/build.yml`: new job `shared-ios` on `macos-latest` (timeout 60 min): checkout, wrapper validation, JDK 17, `setup-gradle`, `actions/cache` for `~/.konan` (key on `gradle/libs.versions.toml`), then `./gradlew :core:foundation:allTests :core:model:allTests :core:repository:allTests :core:domain:allTests :shared:allTests :shared:linkDebugFrameworkIosSimulatorArm64`, and upload `**/build/reports/tests/` on failure
- [x] 3.2 Verify locally that the exact CI command is green: `./gradlew :core:foundation:allTests :core:model:allTests :core:repository:allTests :core:domain:allTests :shared:allTests :shared:linkDebugFrameworkIosSimulatorArm64 > build-log.txt 2>&1` — 26 Nov: all its tasks were already green with unchanged inputs (only the workflow YAML changed); the framework link also ran inside the Xcode build. CI confirms it in 5.3

## 4. Docs

- [x] 4.1 CLAUDE.md: the `:shared` module (Swift entry points only, plain values), `iosApp/` (XcodeGen; how to build and run), the iOS commands in "Commands", and the `shared-ios` CI job
- [x] 4.2 Update `claude/kmp-migration-roadmap.md` (Phase 1 complete, progress log)
- [x] 4.3 Verify: `./gradlew spotlessCheck > build-log.txt 2>&1` is green

## 5. Final verification

- [x] 5.1 Full Android build on the Mac: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` ends with BUILD SUCCESSFUL
- [x] 5.2 iOS: the 3.2 command passes, and the smoke app builds and parses both samples on the simulator
- [ ] 5.3 CI: open a pull request (a draft is fine) from `feature/kmp-migration` to `main`; `shared-ios`, `unit-test` and `bundle-release` pass (the last one also covers the release builds left open in changes 3 and 4) — 27 Nov: not run yet (no pull request open, 0 workflow runs on the branch); archived with this as an open follow-up in the roadmap
- [x] 5.4 `openspec validate add-ios-smoke-app --strict` passes; then archive the change
