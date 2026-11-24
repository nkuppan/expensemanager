# Tasks: make-core-domain-multiplatform

Groups 1–3 change code while core:domain is still an Android module, so the full Android build
checks each one on its own. Group 4 converts the module. Group 5 moves the risky tests to
`commonTest`. Group 6 retires the lint check and updates the docs.

## 1. Plain JVM replacements (design D3)

- [x] 1.1 `UUID.randomUUID().toString()` → `Uuid.random().toString()` (`kotlin.uuid.Uuid`) in `DebtUseCases.kt`, `RecurringTransactionUseCases.kt` and `UpcomingRecurring.kt`; remove the `java.util.UUID` imports
- [x] 1.2 `DebtLedger`: replace `Math.addExact` with a private `addExact(a: Long, b: Long)` that throws `ArithmeticException("long overflow")` exactly as the JDK does
- [x] 1.3 `ReceiptRows`: `Math.toRadians(x)` → `x * (PI / 180.0)` (`kotlin.math.PI`)
- [x] 1.4 `GetTransactionWithFilterUseCase` and `GetFrequentTransactionsUseCase`: default `System.currentTimeMillis()` → `Clock.System.now().toEpochMilliseconds()`
- [x] 1.5 `ReceiptAccountMatcher`: `lowercase(Locale.ROOT)` → `lowercase()`; remove the `java.util.Locale` import
- [x] 1.6 Verify: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` is green (the existing `DebtLedgerTest`, `ReceiptRowsTest`, `ReceiptAccountMatcherTest`, `RecurringTransactionsTest` and `GetTransactionWithFilterUseCaseTest` pass unchanged)

## 2. Budget status instead of a colour resource (design D1)

- [x] 2.1 core:model: add `BudgetStatus.kt` with `enum class BudgetStatus { SAFE, WATCH, RISK, OVER }` and KDoc for the thresholds
- [x] 2.2 core:domain `GetBudgetsUseCase.kt`: `budgetProgressColor(percent): Int` → `budgetStatus(percent): BudgetStatus` (same `<=` edges); `BudgetUiModel.progressBarColor: Int` → `status: BudgetStatus`; remove the `core.common.R` import
- [x] 2.3 core:designsystem: add `@Composable fun emColorOf(status: BudgetStatus): Color` returning `budgetSafe`/`budgetWatch`/`budgetRisk`/`budgetOver`; update the KDoc in `EmColorOf.kt`, `EmControls.kt` (`EmProgressStatus`) and `EmColors.kt` that mention `budgetProgressColor`
- [x] 2.4 feature:budget: `BudgetGauge`, `BudgetProgress`, `BudgetListScreen` and `BudgetDetailScreen` take and pass `BudgetStatus` instead of `@ColorRes Int`, and colour through `emColorOf(status)`; the previews use statuses (orange_500 → `RISK`, green_500 → `SAFE`)
- [x] 2.5 feature:dashboard `BudgetProgressPeriodTest` and `HomeBudgetTest`: `progressBarColor = 0` → `status = BudgetStatus.SAFE`
- [x] 2.6 core:domain tests: `BudgetProgressTest` asserts statuses (same 10 inputs, including 35.5 → `WATCH` and 60.4 → `RISK`); `GetBudgetDetailUseCaseTest` expects `BudgetStatus.WATCH` instead of `R.color.light_green_500`
- [x] 2.7 Verify: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` is green; `grep -rn "core.common.R" core/domain` finds nothing

## 3. Chart buckets as dates (design D2)

- [x] 3.1 core:domain `GetChartDataUseCase`: add a public `fun chartBucketStart(groupType: GroupType, at: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate`; key `bucketTotals` by it; `AnalysisChartData(chartData, dates: List<LocalDate>, groupType: GroupType, title)`; remove the `core.common.utils.toMonthAndYear` import
- [x] 3.2 core:common `InstantDisplay.kt`: add `fun LocalDate.toMonthAndYear(): String`, which formats the date's start of day in the system zone with the existing `MonthAndYearFormat`; add `core/common/src/test/.../LocalDateDisplayTest.kt`: for every month 1990–2040 in 4 zones (UTC, Asia/Kolkata, America/New_York, Pacific/Chatham), `LocalDate.toMonthAndYear()` equals `Instant.toMonthAndYear()` for the instants of that month's first and last day
- [x] 3.3 feature:analysis `AnalysisScreenViewModel`: format `chart.dates` with a new `internal fun chartLabel(groupType, date)` (YEAR → `toYearKey()`, MONTH → `toMonthAndYear()`, DATE → `toDayKey()`); `AnalysisUiChartData.dates` stays `List<String>`
- [x] 3.4 feature:analysis `src/test/.../ChartLabelsEquivalenceTest.kt`: a copy of the old `groupValue` code as the reference; for instants across 2019–2027 (hourly around each DST change, daily otherwise), 4 zones and all 3 group types, `chartLabel(g, chartBucketStart(g, i))` equals the old `groupValue(g, i)`, and two instants share a bucket exactly when they shared a key
- [x] 3.5 Verify: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` is green; `grep -rn "core.common" core/domain/src/main` finds nothing

## 4. Convert the module (design D4)

- [x] 4.1 `core/domain/build.gradle.kts`: `naveenapps.plugin.kmp.library` only; `commonMain` dependencies `implementation(project(":core:model"))`, `implementation(project(":core:repository"))`, `implementation(project(":core:foundation"))`, `implementation(libs.kotlinx.datetime)`, `implementation(project.dependencies.platform(libs.koin.bom))`, `implementation(libs.koin.core)`, `implementation(libs.androidx.compose.runtime.annotation)`; `commonTest`: `implementation(libs.kotlinx.coroutines.test)`; `androidHostTest`: `implementation(project(":core:testing"))`; remove the `android {}` block, `core:common`, `coroutines-android`, `naveenapps.plugin.di`, `naveenapps.plugin.kotlin.basic` and `lintChecks`
- [x] 4.2 `git mv core/domain/src/main/kotlin core/domain/src/commonMain/kotlin`; delete `core/domain/src/main/AndroidManifest.xml`
- [x] 4.3 `git mv core/domain/src/test/kotlin core/domain/src/androidHostTest/kotlin` (all 30 test files for now)
- [x] 4.4 Verify: `./gradlew :core:domain:compileKotlinIosArm64 :core:domain:compileKotlinIosSimulatorArm64 > build-log.txt 2>&1` then `./gradlew assembleDebug test lintDebug spotlessCheck >> build-log.txt 2>&1`: both green; all 30 core:domain test classes run under `testAndroidHostTest`, with the same test counts as in group 3's run

## 5. Risky tests on iOS (design D5)

- [x] 5.1 Add `core/domain/src/commonTest/.../TestAssertions.kt` with `assertContainsExactly(expected: Collection<T>, actual: Collection<T>)` (same elements and counts, any order)
- [x] 5.2 Move `ReceiptParserTest`, `ReceiptAccountMatcherTest`, `ReceiptRowsTest`, `DebtLedgerTest` and `BudgetProgressTest` to `commonTest`: Truth → `kotlin.test` per design D5, `org.junit.Test` → `kotlin.test.Test`, the `java.time` helper in `ReceiptParserTest` → `toLocalDateTime(zone)`
- [x] 5.3 Split `RecurringTransactionsTest`: its `legacyOccurrenceDate` region (java.time/`java.util.Date`) → `androidHostTest/.../RecurringScheduleLegacyEquivalenceTest.kt` unchanged; the rest → `commonTest` with `kotlin.test`, `TimeZone.of(…)` and `toLocalDateTime`
- [x] 5.4 Verify: `./gradlew :core:domain:allTests > build-log.txt 2>&1` is green; the six classes appear under both `testAndroidHostTest` and `iosSimulatorArm64Test`; the `@Test` count of every moved file (plus the legacy split) equals its count before the move

## 6. Retire the lint check and update the docs (design D6)

- [x] 6.1 lint-rules: delete `SharedLayerApiDetector.kt` and `SharedLayerApiDetectorTest.kt`, and remove `JvmApiInSharedLayer` from the issue registry; `grep -rn "JvmApiInSharedLayer\|SharedLayerApi" --include=*.kt --include=*.kts .` finds nothing outside `build/`
- [x] 6.2 CLAUDE.md: core:domain is Kotlin Multiplatform; the four shared modules are guarded by the iOS compile (no lint check); `BudgetStatus` and the chart's `LocalDate` buckets are formatted by the UI
- [x] 6.3 Update `claude/kmp-migration-roadmap.md` (status and progress log)
- [x] 6.4 Verify: `./gradlew :lint-rules:test lintDebug spotlessCheck > build-log.txt 2>&1` is green

## 7. Final verification

- [x] 7.1 Full Android build on the Mac: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` ends with BUILD SUCCESSFUL
- [x] 7.2 iOS: `./gradlew :core:domain:allTests :core:repository:allTests :core:model:allTests :core:foundation:allTests >> build-log.txt 2>&1` passes on `iosSimulatorArm64`
- [x] 7.3 Smoke test on an Android device:
  - Budgets list and a budget's details: bars under 35 %, around 50 %, around 80 % and over 100 % show green, light green, orange and red, in light and dark mode
  - Analysis chart grouped by day, by month and by year: the same labels as before; with the app language switched (e.g. to German), the month labels are translated
  - Add a debt entry and a recurring transaction (new IDs); scan a receipt (account match and amounts)
- [ ] 7.4 Release build: `./gradlew :app:bundleRelease` or CI's release job succeeds — not run locally; left to CI's release job on the `feature/kmp-migration` push
- [x] 7.5 `openspec validate make-core-domain-multiplatform --strict` passes; then archive the change
