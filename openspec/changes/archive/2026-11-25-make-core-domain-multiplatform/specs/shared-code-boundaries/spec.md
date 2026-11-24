## ADDED Requirements

### Requirement: The iOS compile guards shared code
Every shared module (core:foundation, core:model, core:repository, core:domain) SHALL keep its
production code in `commonMain` and compile for `iosArm64` and `iosSimulatorArm64`. That compile
is the check that no JVM or Android API is used, whether through an import or through a call
that needs none.

#### Scenario: JVM import in core:domain
- **WHEN** a use case in `core/domain/src/commonMain` imports `java.util.UUID`
- **THEN** `./gradlew :core:domain:compileKotlinIosArm64` fails with an unresolved reference

#### Scenario: JVM call without an import
- **WHEN** code in `core/domain/src/commonMain` calls `System.currentTimeMillis()`
- **THEN** the iOS compile fails

### Requirement: core:domain is shared code
core:domain SHALL be a Kotlin Multiplatform module built with `naveenapps.plugin.kmp.library`, with
all its production sources in `commonMain`. It SHALL depend only on core:model, core:repository,
core:foundation, kotlinx-coroutines-core, kotlinx-datetime, koin-core and the Compose runtime
annotations. It SHALL NOT depend on core:common.

#### Scenario: Compiling core:domain for iOS
- **WHEN** `./gradlew :core:domain:compileKotlinIosArm64 :core:domain:compileKotlinIosSimulatorArm64` runs on a Mac with Xcode
- **THEN** both compilations succeed

#### Scenario: New IDs keep their format
- **WHEN** a debt person, debt entry or recurring rule is created
- **THEN** its ID is a random version 4 UUID string in lowercase 8-4-4-4-12 form, as before

#### Scenario: Ledger overflow
- **WHEN** a debt balance would exceed `Long.MAX_VALUE` milli-units
- **THEN** `DebtLedger.balanceOf` throws `ArithmeticException` on Android and iOS, as
  `Math.addExact` did

### Requirement: Budget progress is a status
The domain SHALL report how much of a budget is spent as `BudgetStatus`: `SAFE` up to and
including 35 %, `WATCH` up to and including 60 %, `RISK` up to and including 85 %, and `OVER`
above. The Android UI SHALL show `SAFE`, `WATCH`, `RISK` and `OVER` in the design tokens
`budgetSafe`, `budgetWatch`, `budgetRisk` and `budgetOver`, which are the colours previously
shown for green_500, light_green_500, orange_500 and red_500.

#### Scenario: Thresholds
- **WHEN** `budgetStatus` is called with 35, 35.5, 60, 60.4, 85 and 85.1
- **THEN** it returns `SAFE`, `WATCH`, `WATCH`, `RISK`, `RISK` and `OVER` (`BudgetProgressTest`,
  Android host and iOS simulator)

#### Scenario: Same bar colour as before
- **WHEN** a budget is 50 % spent
- **THEN** its bar is drawn in `budgetWatch`, the colour `emColorOf(R.color.light_green_500)`
  gave before

### Requirement: Chart buckets are dates
`GetChartDataUseCase` SHALL return each chart bucket as the `LocalDate` it starts on (1 January
for yearly, the 1st for monthly, the day itself for daily buckets, in the device's time zone)
together with the `GroupType`. Platform UI code SHALL format the labels. On Android the labels
SHALL be the same text as before: `toYearKey`, the app-locale month-and-year format, and
`toDayKey`.

#### Scenario: Monthly label in another language
- **WHEN** the app language is German and the chart groups by month
- **THEN** the label for March 2026 is the same German `MonthAndYearFormat` text as before,
  produced by feature:analysis, not by the domain

#### Scenario: Labels unchanged
- **WHEN** `ChartLabelsEquivalenceTest` formats the buckets of instants across several years,
  DST changes, four time zones and all three group types
- **THEN** every label equals the one the previous string-keyed code produced

### Requirement: Logic at risk of platform differences is tested on iOS
The tests for the receipt parser, receipt rows, receipt account matching, recurring schedules,
the debt ledger and budget status SHALL live in core:domain's `commonTest`, and SHALL run on the
Android host and on `iosSimulatorArm64`.

#### Scenario: Running the shared tests
- **WHEN** `./gradlew :core:domain:allTests` runs on a Mac with Xcode
- **THEN** `ReceiptParserTest`, `ReceiptRowsTest`, `ReceiptAccountMatcherTest`,
  `RecurringTransactionsTest`, `DebtLedgerTest` and `BudgetProgressTest` pass under both
  `testAndroidHostTest` and `iosSimulatorArm64Test`

## MODIFIED Requirements

### Requirement: Platform values cross layer boundaries as plain types
Repository interfaces and domain models SHALL represent files and content URIs as `String`
paths/URIs, dates as `kotlin.time.Instant` or kotlinx-datetime types, amount colours as the
`AmountTone` enum, budget progress as the `BudgetStatus` enum, and chart buckets as `LocalDate`.
Android types (`Uri`, `File`, `java.util.Date`, colour resource IDs) and display-formatted text
SHALL appear only in Android-only modules.

#### Scenario: Receipt image from the camera
- **WHEN** the image picker returns a captured photo
- **THEN** the view model and `ImageStorageRepository` receive it as a `String`, and only the
  Android implementation parses it back into a `Uri`

#### Scenario: Negative balance colour
- **WHEN** an account balance is negative
- **THEN** its model carries `AmountTone.NEGATIVE` and the UI maps it to the expense colour

#### Scenario: Over-budget colour
- **WHEN** a budget is 120 % spent
- **THEN** its `BudgetUiModel` carries `BudgetStatus.OVER` and the UI maps it to `budgetOver`

## REMOVED Requirements

### Requirement: Shared-layer API check
**Reason**: Every package the `JvmApiInSharedLayer` check covered now compiles for iOS, and that
compile rejects JVM and Android APIs, including calls the import-based check couldn't see. No
module would run the check any more.
**Migration**: See "The iOS compile guards shared code". The detector, its test and its registry
entry are deleted from lint-rules.

### Requirement: Allowed imports and scope of the check
**Reason**: The check is removed (see above). The iOS compile allows exactly the libraries that
publish iOS artifacts, including the Compose runtime annotations.
**Migration**: None needed.

### Requirement: Severity per module
**Reason**: There is no lint check left to set a severity for. All four shared modules are
guarded by their iOS compile.
**Migration**: See "The iOS compile guards shared code".
