# Tasks: make-core-datastore-multiplatform

Groups 1–2 change code while core:datastore is still an Android module. Group 3 converts it.

## 1. Pure helpers (design D3, D4)

- [x] 1.1 core:foundation `DateKeys.kt`: add `Long.fromLocalToUTCTimeStamp()` and `Long.fromUTCToLocalTimeStamp()` with the bodies from core:common's `DateUtils.kt`; remove them from `DateUtils.kt`
- [x] 1.2 Update the imports in `DateRangeDataStore`, `DateConverter` (core:database), `TransactionRepositoryImpl` (core:data), `AppDatePickerDialog` (core:designsystem) and `DateUtils.fromUTCToLocalDate`; no body changes
- [x] 1.3 core:foundation `commonTest`: round trip in UTC, Asia/Kolkata, America/New_York and Australia/Lord_Howe, hourly across each zone's DST changes in 2026
- [x] 1.4 `FeedbackDataStore.setFeedbackDialogShown`: `System.currentTimeMillis()` → `Clock.System.now().toEpochMilliseconds()`
- [x] 1.5 Verify: `./gradlew assembleDebug test lintDebug spotlessCheck :core:foundation:allTests > build-log.txt 2>&1` is green; `DateConverterTest` passes unchanged

## 2. Insight history without org.json (design D2)

- [x] 2.1 `FeedbackDataStore`: `addInsight`/`getInsights` with kotlinx.serialization as in design D2 (per-entry skipping, old entries kept as `JsonElement`s, 50-entry limit, newest first)
- [x] 2.2 core:datastore `build.gradle.kts`: kotlinx-serialization plugin and `kotlinx-serialization-json`; `gradle/libs.versions.toml`: `org-json` (test only)
- [x] 2.3 `src/test/.../InsightJsonCompatibilityTest.kt` (moves to `androidHostTest` in group 3): old → new, new → old, a broken entry skipped, 50-entry limit and order
- [x] 2.4 Verify: `./gradlew :core:datastore:testDebugUnitTest assembleDebug spotlessCheck > build-log.txt 2>&1` is green

## 3. Convert the module (design D1)

- [x] 3.1 `gradle/libs.versions.toml`: `androidx-dataStore-preference-core` (`androidx.datastore:datastore-preferences-core`, same version)
- [x] 3.2 `core/datastore/build.gradle.kts`: `naveenapps.plugin.kmp.library` + serialization plugin; `commonMain`: core:model, core:foundation, `datastore-preferences-core`, kotlinx-serialization-json; `androidMain`: `datastore-preferences`, koin-android (with the BOM); `androidHostTest`: `org-json`, kotlin("test"); remove core:common, coroutines-android and the Android/DI plugins
- [x] 3.3 `git mv` the 9 store classes to `src/commonMain/kotlin`, `di/KoinDatastoreModule.kt` to `src/androidMain/kotlin` (unchanged), and the test to `src/androidHostTest/kotlin`; delete the manifest if empty
- [x] 3.4 `src/iosMain/.../DataStoreFactory.kt`: `createDataStore(fileName)` under Application Support/datastore/
- [x] 3.5 Verify: `./gradlew :core:datastore:compileKotlinIosArm64 :core:datastore:compileKotlinIosSimulatorArm64 > build-log.txt 2>&1` then `./gradlew assembleDebug test lintDebug spotlessCheck >> build-log.txt 2>&1`, both green

## 4. Shared tests and CI (design D5)

- [x] 4.1 `commonTest`: `expect fun testDataStore(name: String)` with JVM (`java.io.tmpdir`) and iOS (`NSTemporaryDirectory()`) actuals
- [x] 4.2 `commonTest`: round-trip tests for all 9 stores (defaults, every setter, insight limit and order)
- [x] 4.3 `.github/workflows/build.yml` `shared-ios`: add `:core:datastore:allTests`; CLAUDE.md's KMP command likewise
- [x] 4.4 Verify: `./gradlew :core:datastore:allTests > build-log.txt 2>&1` is green under `testAndroidHostTest` and `iosSimulatorArm64Test`

## 5. Docs

- [x] 5.1 CLAUDE.md: core:datastore is Kotlin Multiplatform (store classes shared, files per platform, kotlinx.serialization only; the conversion helpers live in core:foundation)
- [x] 5.2 Update `claude/kmp-migration-roadmap.md`

## 6. Final verification

- [x] 6.1 Full Android build on the Mac: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` ends with BUILD SUCCESSFUL; `DateConverterTest` and the core:data repository tests pass
- [x] 6.2 iOS: `./gradlew :core:foundation:allTests :core:model:allTests :core:repository:allTests :core:domain:allTests :core:datastore:allTests :shared:allTests :shared:linkDebugFrameworkIosSimulatorArm64 >> build-log.txt 2>&1` passes
- [x] 6.3 Update test on an Android device: on the current build, set a theme, currency, language and custom date range, dismiss a banner and open the notification centre; install the new build over it; all of these are unchanged and the notification centre lists the same insights
- [x] 6.4 `openspec validate make-core-datastore-multiplatform --strict` passes; then archive the change
