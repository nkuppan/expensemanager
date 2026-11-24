# Proposal: make-core-domain-multiplatform

## Why

core:domain holds the app's business logic: 100 files of use cases, the debt ledger, recurring
schedules and the receipt parser. It is the last shared-logic module of Phase 1. Once it compiles
for iOS, the smoke app (change 5) can call real use cases. A survey (25 Nov) found that only a few
calls block the iOS compile:

- **Android resources and display formatting:**
  - `GetBudgetsUseCase` returns the budget bar colour as an Android colour resource
    (`R.color.green_500` and so on, from core:common);
  - `GetChartDataUseCase` labels monthly chart buckets with core:common's display formatter
    `Instant.toMonthAndYear()` (`SimpleDateFormat`).
- **JVM APIs:**
  - `java.util.UUID.randomUUID()` (3 files);
  - `Math.addExact` (debt ledger) and `Math.toRadians` (receipt rows);
  - `System.currentTimeMillis()` (2 default arguments);
  - `lowercase(Locale.ROOT)` (receipt account matcher).
- **Android-only build setup:** `kotlinx-coroutines-android`, the `naveenapps.plugin.di`
  convention plugin (KSP and koin-android), and the `core:common` dependency.

## What Changes

- **Budget status instead of a colour resource.**
  - A new enum `BudgetStatus { SAFE, WATCH, RISK, OVER }` goes in core:model, using the same
    thresholds as today (≤ 35 %, ≤ 60 %, ≤ 85 %, above).
  - `BudgetUiModel.progressBarColor: Int` becomes `status: BudgetStatus`, and
    `budgetProgressColor()` becomes `budgetStatus()`.
  - The UI maps the status with a new `emColorOf(status)` in core:designsystem, to the
    `budgetSafe`/`budgetWatch`/`budgetRisk`/`budgetOver` tokens. These are exactly the colours
    today's `emColorOf(R.color.…)` resolves to: green_500 → income = budgetSafe,
    light_green_500 → budgetWatch, orange_500 → warning = budgetRisk, red_500 → expense =
    budgetOver.
- **Chart buckets are dates instead of display text.**
  - `AnalysisChartData.dates: List<String>` becomes `dates: List<LocalDate>`, plus a `groupType`.
    Each date is the start of its bucket: 1 January, the 1st of the month, or the day itself.
  - Transactions are grouped by that date rather than by the label string.
  - feature:analysis formats the labels with the same formatters as today: `toYearKey`, a new
    `LocalDate.toMonthAndYear()` in core:common (same `SimpleDateFormat`), and `toDayKey`.
- **Plain replacements** that give identical results:
  - `UUID.randomUUID().toString()` → `Uuid.random().toString()` (stable in Kotlin 2.4; same
    lowercase format);
  - `Math.addExact` → a private overflow-checked add that throws the same `ArithmeticException`;
  - `Math.toRadians(x)` → `x * (PI / 180.0)`. A run over 20 M values found no difference,
    because the JDK uses that same constant;
  - `System.currentTimeMillis()` → `Clock.System.now().toEpochMilliseconds()`;
  - `lowercase(Locale.ROOT)` → `lowercase()` (Kotlin defines it as `Locale.ROOT` on the JVM).
- **Module conversion.**
  - core:domain moves to `naveenapps.plugin.kmp.library`, and its sources to `commonMain`.
  - Its dependencies become core:model, core:repository, core:foundation, coroutines-core,
    kotlinx-datetime, koin-core (with the BOM) and the Compose runtime annotations (for `@Stable`).
  - core:common, coroutines-android, the DI convention plugin, the manifest and `lintChecks`
    go.
- **Tests** (decision 25 Nov: move the risky ones to iOS).
  - These move to `commonTest` and run on the Android host and the iOS simulator:
    `ReceiptParserTest`, `ReceiptAccountMatcherTest`, `ReceiptRowsTest`,
    `RecurringTransactionsTest`, `DebtLedgerTest` and `BudgetProgressTest`. They switch from
    Truth to `kotlin.test` and from `java.time` to kotlinx-datetime.
  - The java.time equivalence part of `RecurringTransactionsTest` needs the JVM, so it becomes
    its own `androidHostTest` class.
  - The other 24 tests (13 use Mockito) move unchanged to `androidHostTest`.
  - New tests: a `LocalDate.toMonthAndYear()` equivalence test in core:common, and a chart label
    equivalence test in feature:analysis (new labels = old labels across years, zones and
    groupings).
- **Lint check retired.** `JvmApiInSharedLayer` (lint-rules) has nothing left to check: all
  three shared-layer packages now compile for iOS, which rejects JVM and Android APIs more
  strictly. The detector, its test and its registry entry are deleted. The design-system checks
  in lint-rules stay.

User-visible impact: none. Budget bars keep the same colours in light and dark mode. Chart labels
keep the same text in every language. IDs remain random version 4 UUIDs in the same format.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `shared-code-boundaries`:
  - core:domain joins the shared modules;
  - the lint-check requirements are replaced by "the iOS compile guards shared code";
  - budget status and chart buckets cross the boundary as data.

## Impact

- **core/domain:** the build file; every source file moves to `src/commonMain/kotlin`, with
  edits in `GetBudgetsUseCase`, `GetChartDataUseCase`, `DebtUseCases`, `DebtLedger`,
  `RecurringTransactionUseCases`, `UpcomingRecurring`, `ReceiptRows`, `ReceiptAccountMatcher`,
  `GetTransactionWithFilterUseCase` and `GetFrequentTransactionsUseCase`; tests move to
  `commonTest`/`androidHostTest`; the manifest is deleted.
- **core/model:** new `BudgetStatus.kt`.
- **core/designsystem:** `emColorOf(status: BudgetStatus)`; KDoc updates in `EmColorOf.kt`,
  `EmControls.kt` and `EmColors.kt`.
- **core/common:** `LocalDate.toMonthAndYear()` and its test.
- **feature/budget:** `BudgetGauge`, `BudgetProgress`, `BudgetListScreen` and
  `BudgetDetailScreen` (the colour parameter → `BudgetStatus`; previews).
- **feature/dashboard:** two tests build `BudgetUiModel` (`progressBarColor = 0` → a status).
- **feature/analysis:** `AnalysisScreenViewModel` formats labels; a new label test.
- **lint-rules:** `SharedLayerApiDetector`, its test and the registry entry are removed.
- **Docs:** CLAUDE.md and the roadmap.
- **Dependencies:** no new libraries. koin-core, the Compose runtime annotations and
  coroutines-test are already in the catalog.

**Rollback:** revert this change's commits on `feature/kmp-migration`. No stored data or schema
changes.
