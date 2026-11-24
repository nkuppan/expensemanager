# Design: make-core-domain-multiplatform

## Context

- **Contents:** 100 main files and 30 test files. Use cases (one class per operation), their
  Koin modules (`org.koin.dsl.module` only, no annotations or KSP), and pure helpers: the
  `DebtLedger`, `RecurringSchedule`, `UpcomingRecurring`, the receipt parser
  (`ReceiptParser`, `ReceiptRows`, `ReceiptAccountMatcher`), `BudgetTitles` and
  `ComparisonWindows`.
- **Already done in Phase 0:** dates are `kotlin.time.Instant`/kotlinx-datetime, and the date
  keys and dispatchers come from core:foundation.
- **What still blocks iOS** (import scan plus a search for JVM calls that need no import):

  | Where | JVM/Android API | Fix |
  |---|---|---|
  | `GetBudgetsUseCase` | `core.common.R.color.*`, `androidx.compose.runtime.Stable` | D1; `@Stable` stays (KMP annotation artifact, as in core:model) |
  | `GetChartDataUseCase` | `core.common.utils.toMonthAndYear` (`SimpleDateFormat`) | D2 |
  | `DebtUseCases`, `RecurringTransactionUseCases`, `UpcomingRecurring` | `java.util.UUID` | D3 |
  | `DebtLedger` | `Math.addExact` | D3 |
  | `ReceiptRows` | `Math.toRadians` | D3 |
  | `GetTransactionWithFilterUseCase`, `GetFrequentTransactionsUseCase` | `System.currentTimeMillis()` | D3 |
  | `ReceiptAccountMatcher` | `java.util.Locale.ROOT` | D3 |

- **Consumers:** core:data, core:notification, app and every feature module. Only feature:budget
  (budget colour), feature:analysis (chart labels) and two feature:dashboard tests use the
  changed types.

## Goals / Non-Goals

**Goals:**
- core:domain compiles for Android, iosArm64 and iosSimulatorArm64.
- The Android app looks and behaves exactly as before: budget colours, chart labels, IDs,
  overflow behaviour and receipt parsing.
- The logic most likely to differ on iOS (regexes, time-zone schedules, ledger arithmetic) is
  tested on the iOS simulator.

**Non-Goals:**
- Replacing Mockito. The 13 Mockito tests stay JVM tests (`androidHostTest`). Moving them to
  fakes or Mokkery is a separate decision.
- Formatting on iOS: the smoke app shows raw values. iOS display formatting is Phase 3.
- Koin start-up on iOS (Phase 2). The use-case modules just need to compile there.

## Decisions

### D1. Budget bar colour → `BudgetStatus` enum, mapped to the existing design tokens
- A new `enum class BudgetStatus { SAFE, WATCH, RISK, OVER }` lives in core:model, next to
  `AmountTone`, which set the pattern for "meaning, not colour". It goes there because
  core:designsystem, which maps it, depends on core:model but not on core:domain.
- `budgetStatus(percent)` keeps today's thresholds and their inclusive edges: ≤ 35 → SAFE,
  ≤ 60 → WATCH, ≤ 85 → RISK, otherwise OVER.
- `BudgetUiModel.progressBarColor: Int` becomes `status: BudgetStatus`.
- core:designsystem gains `@Composable fun emColorOf(status: BudgetStatus): Color`, returning
  `EmTheme.colors.budgetSafe/budgetWatch/budgetRisk/budgetOver`.

Pixel identity: today the UI calls `emColorOf(R.color.x)`, which resolves:

| Resource | Today's token | Token for the new status | Same value? |
|---|---|---|---|
| green_500 | `income` | `budgetSafe` = `extended.income` | yes |
| light_green_500 | `budgetWatch` | `budgetWatch` | yes |
| orange_500 | `warning` | `budgetRisk` = `extended.warning` | yes |
| red_500 | `expense` | `budgetOver` = `extended.expense` | yes |

The same holds in dark mode: both paths read the same `EmColors` instance.

*Alternatives:*
- Keep an `Int` and map it in core:data. That's still a resource ID in shared code. Rejected.
- Reuse the design system's `EmProgressStatus`. Shared logic can't depend on a Compose UI
  module. Rejected.
- Put the enum in core:domain. Then core:designsystem would need to depend on the domain to
  map it. Rejected.

### D2. Chart buckets → `LocalDate` + `GroupType`; feature:analysis formats the labels
- `GetChartDataUseCase` keys buckets by `chartBucketStart(groupType, instant): LocalDate` (public, so feature:analysis's test can call it). That's the
  instant's date in the device's zone (as today's `toLocalDateIn()`), truncated to 1 January
  (YEAR), the 1st (MONTH) or left as is (DATE).
- `AnalysisChartData` becomes `(chartData, dates: List<LocalDate>, groupType: GroupType, title)`.
- feature:analysis's view model formats each date:
  - YEAR → `toYearKey()`;
  - MONTH → a new `LocalDate.toMonthAndYear()` in core:common, which formats the date's local
    midnight with the existing `MonthAndYearFormat` (`SimpleDateFormat`, app locale);
  - DATE → `toDayKey()`.

Equivalence: today's key is a pure function of the year, of (month, year), or of (day, month,
year) of the instant's local date. Those triples map one-to-one to the truncated `LocalDate`, so
the grouping is the same. The labels are the same because both paths format the same local date
with the same formatter. `ChartLabelsEquivalenceTest` (feature:analysis, JVM) checks this
against the old code, for instants across several years, around DST changes, in 4 zones and for
all 3 group types.

Side benefit: buckets no longer depend on a locale-sensitive display string (the CLAUDE.md
"display vs storage key" rule).

*Alternatives:*
- Keep `List<String>` and use the English `toMonthAndYearKey()` for MONTH. That changes the
  label text in 15 languages. Rejected.
- `expect`/`actual` month formatting in the domain. It puts display text back into shared code,
  against the decided "shared code returns data". Rejected.

### D3. Plain JVM replacements
- **IDs:** `Uuid.random().toString()` is a random version 4 UUID in the same lowercase
  8-4-4-4-12 form as `UUID.randomUUID().toString()`. `Uuid` is stable since Kotlin 2.4, so no
  opt-in is needed.
- **Overflow-checked add:** a private `addExact(a, b)` in `DebtLedger`:
  `val r = a + b; if ((a xor r) and (b xor r) < 0) throw ArithmeticException("long overflow")`.
  That's the JDK's own algorithm and message (checked on the JDK: same results and message for
  the MAX+1, MIN-1, MIN+MIN and normal cases).
- **Degrees to radians:** `x * (PI / 180.0)`. JDK 21's `toRadians` is
  `angdeg * DEGREES_TO_RADIANS`, and that constant is `PI / 180.0` (0.017453292519943295). On the
  JDK, 20 M random angles plus every tenth of a degree in ±360 gave 0 differences.
- **Now:** `Clock.System.now().toEpochMilliseconds()` (as in change 3, D1).
- **`lowercase(Locale.ROOT)` → `lowercase()`:** Kotlin's JVM implementation is literally
  `toLowerCase(Locale.ROOT)`.

### D3a. Found by the first iOS compile (25 Nov)
`FindTransactionByIdUseCase` returned `Resource.Error(KotlinNullPointerException(…))` for a
missing ID. `KotlinNullPointerException` exists only on the JVM, and the import scan couldn't see
it (it needs no import). It becomes `IllegalArgumentException("Id shouldn't be null")`, which is
also the accurate type. No behaviour changes: the only caller (`TransactionCreateViewModel`)
ignores the error, and it's not logged to analytics. core:data has 5 more uses; they are
Android-only for now and are a Phase 2 item.

The first host-test compile also found two moved tests (`GetBudgetTransactionsUseCaseTest`,
`GetBudgetTransactionsUseCaseTimeZoneLocaleTest`) building budget keys with core:common's
`Date.toMonthAndYearKey()`. They now use core:foundation's `Instant.toMonthAndYearKey()`, the
function the app itself writes keys with. core:common's `DateKeysTest` keeps pinning the two as
equal, so core:domain's tests don't need core:common.

### D4. Module setup
- `naveenapps.plugin.kmp.library`.
- `commonMain`: `implementation` of core:model, core:repository and core:foundation (as today:
  consumers already declare the modules they use, so nothing becomes `api`); `kotlinx-datetime`;
  `platform(koin-bom)` + `koin-core`; and `androidx.compose.runtime.annotation`.
- `androidHostTest`: `project(":core:testing")` for Mockito, Truth, JUnit, coroutines-test and
  Turbine, so the moved JVM tests compile unchanged.
- `commonTest`: `kotlinx-coroutines-test` (for `runTest` in `RecurringTransactionsTest`).
- `GetTransactionWithFilterUseCase` already opts in to `ExperimentalCoroutinesApi` at the call
  site, so the module needs no extra opt-ins.

*Alternative:* keep `naveenapps.plugin.di`. It applies KSP and koin-android to the Android
compilation only; the domain uses neither. Rejected.

### D5. Which tests run on iOS
Decision 25 Nov ("move the risky ones"): tests whose logic could behave differently on
Kotlin/Native move to `commonTest`.
- **Regexes:** `\p{L}`, `\p{N}`, `\b`, and `IGNORE_CASE` with Unicode text. Kotlin/Native has
  its own regex engine. Tests: `ReceiptParserTest`, `ReceiptAccountMatcherTest`.
- **Trigonometry and floating point:** `ReceiptRowsTest`.
- **Time-zone gaps and overlaps:** `RecurringTransactionsTest` (Native uses its own tz
  database).
- **Overflow:** `DebtLedgerTest`.
- **New enum thresholds:** `BudgetProgressTest`.

Conversion rules:
- `assertThat(x).isEqualTo(y)` → `assertEquals(y, x)`;
- `containsExactly(…).inOrder()` → `assertEquals(listOf(…), x)`;
- `containsExactly(…)` without `inOrder` → a small `assertContainsExactly` helper in commonTest
  that compares counts per element;
- `isWithin(t).of(v)` → `assertEquals(v, x, t)`;
- `isInstanceOf` → `assertIs`;
- `assertWithMessage(m)` → the `message` argument;
- `java.time` conversions in the helpers → `toLocalDateTime(zone)`.

The `legacyOccurrenceDate` region of `RecurringTransactionsTest` compares against the old
`java.util.Date` code, so it moves unchanged to `androidHostTest` as
`RecurringScheduleLegacyEquivalenceTest`. Kotlin/Native rejects commas in function names, so four backtick test names lost their commas (25 Nov); the tests themselves are unchanged. The number of test methods is kept: each test file's
`@Test` count before and after is listed in the verify task.

### D6. Retire `JvmApiInSharedLayer`
After this change, every package the check covers (`core.model`, `core.repository`,
`core.domain`) lives in modules that compile for iOS, where JVM and Android APIs don't resolve.
That covers imports and calls alike, which the lint check never could. The check would run
nowhere (no module would apply it to those packages). So it's deleted:
- `SharedLayerApiDetector`, its test, and its entry in the issue registry;
- the spec's two lint requirements give way to "the iOS compile guards shared code".

*Alternative:* keep it dormant for future shared modules. Every future shared module is KMP,
where the compiler does the job. Rejected.

## Risks / Trade-offs

- **Kotlin/Native's regex engine may differ** (e.g. `\b` next to non-ASCII letters, `\p{L}`) →
  that's what moving `ReceiptParserTest`/`ReceiptAccountMatcherTest` to commonTest catches. If
  one fails only on iOS, the iOS behaviour is fixed in shared code without changing Android
  results, and the case is noted here.
- **The time-zone database on iOS differs from the JVM's** → the recurring schedule tests run
  in named zones on the simulator. A failure there is a real iOS bug to fix before Phase 3, not
  a reason to weaken the test.
- **The KMP Android host test may fail to resolve the Android library `core:testing`** (variant
  matching) → fall back to listing its libraries directly in `androidHostTest`.
  *Happened (25 Nov):* core:testing's Compose test libraries have no version of their own (the
  Compose BOM is only `implementation` there), so `androidHostTestCompileClasspath` failed on
  `ui-test-junit4:`. Fixed by adding `platform(libs.androidx.compose.bom)` to `androidHostTest`;
  core:testing stays, because the tests need its `BaseCoroutineTest` and `FAKE_*` fixtures.
- **Chart grouping by `LocalDate` instead of strings** could change bucket boundaries if
  `chartBucketStart` and the label used different zones → both use the device's zone, as today, and
  `ChartLabelsEquivalenceTest` covers DST changes.
- **Large mechanical diff** (100 file moves) → `git mv` keeps the history. The task groups
  separate the behavioural edits (groups 1–3, still on the Android module) from the move
  (group 4), so each diff can be reviewed alone.

## Migration and rollback

- No stored data or schema changes. `BudgetStatus` and the chart dates exist only in memory.
- Groups 1–3 change behaviour-neutral code while core:domain is still an Android module, and
  each is verified by the full Android build. Group 4 converts the module and moves files.
  Group 5 moves the tests. Group 6 retires the lint check and updates the docs.
- Rollback: revert this change's commits on `feature/kmp-migration`.
