# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build the debug APK
./gradlew assembleDebug

# Install debug build on a connected device/emulator
./gradlew installDebug

# Build the release bundle (what CI runs)
./gradlew --no-configuration-cache bundleRelease

# Run all unit tests across every module
./gradlew test

# Run unit tests for a single module (e.g. core:data)
./gradlew :core:data:testDebugUnitTest

# Run a single test class/method within a module
./gradlew :core:data:testDebugUnitTest --tests "com.naveenapps.expensemanager.core.data.repository.CategoryRepositoryImplTest"
./gradlew :core:data:testDebugUnitTest --tests "*.CategoryRepositoryImplTest.someTestMethod"

# Run instrumented tests (needed for Room migration tests, which live in androidTest)
./gradlew :core:database:connectedDebugAndroidTest

# Unit test coverage (Jacoco) — per module, this is the task CI runs (`unit-test` job)
./gradlew debugCoverage
# Aggregated report across all modules, defined in the root build.gradle.kts
./gradlew allDebugCoverage

# Lint / format (Spotless + ktlint, applied to every .kt/.kts/.xml file)
./gradlew spotlessCheck
./gradlew spotlessApply
```

Room schema changes are hand-written migrations (see "Database" below) validated by instrumented tests under `core/database/src/androidTest/.../database/Migration<N>To<N+1>Test.kt` — these require `connectedDebugAndroidTest`, not the plain `test` task.

## Git hooks

`.githooks/pre-push` runs `./gradlew spotlessApply` before every push. If it reformats files, it commits them as "Apply spotless formatting" and stops the push; run `git push` again. Files with uncommitted changes are formatted but not committed. Gradle sets `core.hooksPath` to `.githooks` on sync (not on CI). Skip once with `SKIP_SPOTLESS_HOOK=1 git push`.

## Architecture

This is a multi-module Clean Architecture Android app (single-Activity, 100% Kotlin/Compose). The module graph enforces a strict one-way dependency direction; when adding code, respect these boundaries rather than reaching "up" a layer:

- **`app`** — application entry point, single Activity (`MainActivity`), Koin bootstrap, the root Compose `NavHost`, app-lock/biometric gating, in-app update check.
- **`core:model`** — plain domain models shared everywhere (no Android framework dependency).
- **`core:repository`** — repository *interfaces* only (e.g. `CategoryRepository`, `SettingsRepository`, `FeedbackRepository`). Feature and domain code depends on these, never on `core:data` directly.
- **`core:domain`** — use cases (one class per operation, `operator fun invoke()`), grouped by feature under `usecase/<feature>/`, each with a matching `di/<Feature>UseCaseModule.kt`.
- **`core:data`** — repository *implementations*, Koin DI module definitions (`di/KoinRepositoryModule.kt`, `di/KoinActivityModule.kt`), mappers between Room entities and domain models (`mappers/*Mappers.kt`).
- **`core:database`** — Room database, DAOs, entities, and all schema migrations (`DatabaseMigrations.kt`, `exportSchema = true`).
- **`core:datastore`** — Jetpack DataStore Preferences wrappers, one class per concern (e.g. `FeedbackDataStore`, `ThemeDataStore`), all backed by a single shared `DataStore<Preferences>` instance.
- **`core:navigation`** — the navigation abstraction described below (`AppComposeNavigator`, `ExpenseManagerScreens`).
- **`core:designsystem`** — shared Compose components/theme (`SettingRow`, `AppCardView`, `DeleteDialogItem`, `SettingsSection`, etc.) used by every feature module instead of hand-rolled UI.
- **`core:common`**, **`core:testing`**, **`core:notification`**, **`core:settings`** — shared utilities/extensions, test fixtures (`FakeConstants.kt`), notification scheduling, and a small settings-domain module (number formatting) respectively.
- **`feature:*`** (account, analysis, budget, category, country, currency, dashboard, export, filter, language, onboarding, reminder, settings, theme, transaction, about) — one module per user-facing feature. Each follows the same internal shape: `<Screen>.kt` (Compose), `<Screen>ViewModel.kt`, `<Screen>State.kt`, `<Screen>Action.kt`, optionally `<Screen>Event.kt`, and `di/<Feature>ViewModelModule.kt`.

Module wiring for a new feature module always applies the same convention plugins (defined in `build-logic/convention`): `naveenapps.plugin.android.feature`, `naveenapps.plugin.kotlin.basic`, `naveenapps.plugin.compose`, `naveenapps.plugin.di`. Other convention plugin ids: `naveenapps.plugin.android.library`, `naveenapps.plugin.android.app`, `naveenapps.plugin.room`. `MIN_SDK`/`TARGET_SDK`/`COMPILE_SDK` are defined once in `AndroidConfigExt.kt` and applied everywhere via these plugins — don't hardcode SDK versions in a module's own `build.gradle.kts`.

### Dependency injection (Koin)

DI is Koin, organized **per layer and per feature**, never one giant module:

- Each feature module exposes `di/<Feature>ViewModelModule.kt`; all of them are aggregated into one `ViewModelModule` in `app/.../di/ViewModelModule.kt` via `includes(...)`.
- Core layers each expose their own module (`RepositoryModule`, `UseCaseModule`, `DatabaseModule`, `DatastoreModule`, `NavigationModule`, `NotificationModule`, `AppModule`, `ActivityModule`).
- Koin itself is started from a Jetpack **App Startup** `Initializer` (`app/.../initializer/KoinInitializer.kt`), not from `Application.onCreate()`.
- Most repositories are app-scoped singletons (`single<XxxRepository> { XxxRepositoryImpl(...) }` in `KoinRepositoryModule.kt`). Anything that genuinely needs an `Activity` (sharing, in-app review/update, biometric-adjacent flows) is registered in `KoinActivityModule.kt` using `activityScope { scoped<XxxRepository> { ... } }` and reached from Compose via `ActivityComponentProvider` (see `MainActivity`'s `AndroidScopeComponent` implementation). Don't add Activity-dependent logic to an app-scoped singleton — it won't have a live `Activity` reference.

### Navigation (Compose Navigation, single-Activity)

There is exactly one `NavHost`, created once in `MainScreen.kt` and populated by `HomePageNavHostContainer` / `expenseManagerNavigation()` in `app/.../ui/HomeScreen.kt`. Routes are type-safe: `ExpenseManagerScreens` is a sealed class of `@Serializable` `data object`/`data class` routes (in `core:navigation`), matched with `composable<ExpenseManagerScreens.Xxx> { ... }` — there is no string-based route table to keep in sync.

ViewModels never touch `NavController` directly. They depend on the abstract `AppComposeNavigator` (`navigate(route)`, `popBackStack()`, `navigateBackWithResult(...)`, etc.), which emits `NavigationCommand`s onto a `SharedFlow`. A single `LaunchedEffect` in `MainScreen` (`composeNavigator.handleNavigationCommands(navHostController)`) is the only place that actually calls into `NavController`. This keeps navigation testable and keeps feature modules decoupled from `core:navigation`'s Android Navigation dependency specifics.

Two distinct navigation patterns coexist, and it matters which one a given screen should use:
- **Multi-page (back-stack) navigation** — the default. Every screen except Home (account/category/budget/transaction create-edit screens, Settings, Export, etc.) is its own `composable<Route>` destination, pushed/popped via `AppComposeNavigator`. Each gets its own back-stack entry, system back button, and (if edited) `SavedStateHandle`-based args.
- **Single-page tab navigation** — `ExpenseManagerScreens.Home` is *one* back-stack entry. Inside it, `HomeScreen()` (in `app/.../ui/HomeScreen.kt`) owns a `HomeViewModel`-held `HomeScreenBottomBarItems` enum and switches between `DashboardScreen`/`AnalysisScreen`/`TransactionListScreen`/`CategoryTransactionTabScreen` via a plain `when` over local state — no new back-stack entry is created when switching bottom-nav tabs, and the custom `BackHandler` returns to the Home tab before falling through to `activity.finish()`. Use this pattern for tabs that should feel like siblings of one screen, not the stack pattern for anything that should be independently back-navigable.

### MVI-flavored MVVM (used by every screen, no exceptions)

Every screen follows the same State/Action/ViewModel triad:
- `<Screen>State.kt` — an immutable `data class` (often `@Stable`) holding everything the Composable needs to render; no logic.
- `<Screen>Action.kt` — a `sealed class` of user intents (`data object`s for no-arg actions, `data class`es for parameterized ones), e.g. `ShowDeleteDialog`, `SelectAccount(val account: Account)`.
- `<Screen>ViewModel.kt` — holds `MutableStateFlow<State>` privately, exposes it as `val state = _state.asStateFlow()`, and has a single public entry point `fun processAction(action: Action)` that `when`-matches every action to a private handler function. State is only ever changed via `_state.update { it.copy(...) }`.
- One-off, non-persistent effects (navigation is the exception — that goes through `AppComposeNavigator` directly; this is for things like "trigger the Play Store rate flow" or "show a snackbar") use a `Channel<Event>` + `receiveAsFlow()`, consumed in the Composable via `ObserveAsEvents(viewModel.event) { ... }` (see `SettingsScreen`/`SettingEvent` for the canonical example).
- Composables are split into a public entry point (`fun XScreen(viewModel: XViewModel = koinViewModel())`) that collects state and wires `viewModel::processAction`, and a private, stateless `XScreenContent(state, onAction)` that does the actual rendering and is what gets previewed/tested. Keep new screens split the same way rather than mixing ViewModel access into the rendering composable.

When a screen needs an Activity-scoped dependency (e.g. `ShareRepository`, `BackupRepository`), it's passed as a constructor parameter to the public composable, sourced from `ActivityComponentProvider` at the `NavGraphBuilder.expenseManagerNavigation()` call site — not injected into the ViewModel via `koinViewModel()`, since Activity-scoped instances aren't visible in the app-level Koin graph ViewModels are resolved from.

### Database

Room, with hand-written `Migration` objects in `core/database/.../DatabaseMigrations.kt` (never destructive/auto migrations) and `exportSchema = true`. Every schema change needs: a new `MIGRATION_N_N+1` object, a version bump in `ExpenseManagerDatabase.kt`, registering the migration in `KoinDatabaseModule.kt`'s `.addMigrations(...)`, and an instrumented test in `core/database/src/androidTest/.../Migration<N>To<N+1>Test.kt` using `MigrationTestHelper` (see `Migration4To5Test.kt` for the pattern: schema-shape assertions, backfill assertions, and an "other tables untouched" assertion).

### Locale-sensitive code — read before touching `DateUtils.kt` or anything persisted as text

The app supports switching its display language at runtime (`AppCompatDelegate.setApplicationLocales`, wired in `feature:language` + `LocaleRepositoryImpl`), which means `Locale.getDefault()` is no longer stable across the app's lifetime. `core/common/.../utils/DateUtils.kt` distinguishes two kinds of formatters and documents this at the top of the file: formatters used only for one-way **display** text may follow `Locale.getDefault()`; formatters for values that get **persisted and later parsed back or used as matching keys** (e.g. `Budget.selectedMonth`) must be pinned to a fixed `Locale` (`Locale.ENGLISH`/`Locale.US`), or a locale switch can make previously-saved data unparseable. The same display-vs-storage-key split applies to category names: built-in categories carry a `titleResId` (resolved via `stringResource` for display) alongside the original English `name` (the immutable stored/matching value); only categories with a null `titleResId` are user-created/renamed and safe to treat as arbitrary free text.

## Release notes ("What's new") — keep them current

Every release that changes something users notice gets "What's new" notes, written as soon as the previous version ships:

- The version in `version.properties` is the one live on Play. The **next** release is that version + 1 patch (the "Release to Beta" workflow bumps it). After a release goes out, add the notes for the next version while making the first user-visible change for it.
- Add a `whats_new_<x_y_z>` string-array to `feature/dashboard/src/main/res/values*/strings.xml` in all 16 languages, newest array first, and put a `WhatsNewRelease(version = "x.y.z", releasedOn = …)` entry at the top of `feature/dashboard/.../notifications/WhatsNewReleases.kt`. The bell then shows a dot for everyone who updates.
- Use the same lines, prefixed with "• ", in `app/src/main/play/release-notes/<locale>/default.txt` (17 folders, en-US and en-IN share English; Play's limit is 500 characters per language).
- Keep it to 3–5 short, user-facing highlights (features and visible fixes, not refactors). Update the list as more big changes land before the release.
- `./gradlew checkReleaseNotes` (also run by `bumpVersion` in CI) warns when the version has no notes or the Play notes don't match.

## Design system — features never use Material directly

Plan and rationale: the "Expense Manager — Design System Plan" doc (Claude project). All 5 phases are done: every feature module and `:app` use only `Em*` components (exceptions: `ExperimentalMaterial3Api` opt-ins and the Glance widget, which suppresses the design-system lint checks). The four lint checks are **errors**, and CI's `design-system` job runs `:lint-rules:test`, `lintDebug` and the screenshot verify.

Components (package `core.designsystem.em`): `EmText`/`EmAmountText`, `EmSectionHeader`, `EmCard`/`EmGroupedList`/`emGroupedItems`, `EmListItem`/`EmSettingItem`/`EmIconTile`/`EmDivider`, `EmButton`/`EmIconButton`, `EmTopBar`/`EmScaffold`, `EmBottomSheet`/`EmConfirm`/`EmSelectionSheet`/`EmSelectableItem`, `EmEmptyState`, `EmIcon`, `EmChip`/`EmChipRow`/`EmSegmented`, `EmTextField`, `EmSwitch`/`EmCheckbox`/`EmRadio`, `EmLoading`/`EmProgress`, `EmFab` (Add only), `EmBottomActionBar` (form Save), `EmFitText`, `EmPreviews` (use instead of the library's AppPreviewsLightAndDarkMode), `emColorOf` (legacy colour-resource bridge), and `EmSnackbarHostState`/`EmSnackbarResult`/`EmSnackbarDuration` type aliases. Pick-one-of-few = a row with `EmSettingTrailing.Value` opening `EmSelectionSheet`, not a dropdown. Fields: `EmTextField` (`selectAllOnFocus` for pre-filled values), `EmAmountField` (decimal), `EmPickerField` (read-only, opens a picker), `EmSearchField` + `EmNoSearchResults`. `EmSegmented` is the animated pill (index or generic `options/selected/label` form); `EmModalSheet` is the bare sheet for pickers that lay out their own lists. Light-theme primary/income is #0B7A46 (AA contrast); the brand #0F9D5B is only for decoration. All are shown in `gallery/ComponentGallery.kt`. The old `AppCardView`, `SettingRow`, `SettingsSection`, `DashboardWidgetTitle`, `DeleteDialogItem`, `EmptyItem` and `ExpenseManagerTopAppBar` are deleted; their replacements are `EmCard` (`position = EmCardPosition.of(index, items)` for grouped lists, `selected` for pickers), `EmSettingItem`, `EmSection`, `EmSectionHeader(onViewAllClick)`, `EmDeleteConfirm`, `EmEmptyState` (keeps the "EmptyItem" test tag) and `EmTopBar`. Also: `EmTabs`, `EmNavigationBar`/`EmNavigationBarItem`, `EmOverflowMenu`/`EmMenuItem`, `EmText(role = EmTextRole.Code)` for monospace.

1. **Tokens come from `EmTheme`** (`core/designsystem/.../theme`): `EmTheme.colors` (Material scheme + income/expense/transfer/debt/warning/budget*), `EmTheme.type` (roles: display, screenTitle, sectionTitle, itemTitle, body, supporting, label, overline, amountLarge/Medium/Small), `EmTheme.spacing` (xxs 2, xs 4, sm 8, md 12, lg 16, xl 24, xxl 32, xxxl 48), `EmTheme.shapes` (xs 4, sm 8, md 12, lg 16, xl 28, pill, sheet), `EmTheme.icons` (sm 16, md 20, lg 24, tile 40, hero 72), `EmTheme.elevation`, `EmTheme.motion`, `EmTheme.alpha`.
2. **Features and `:app` use `Em*` components from `core:designsystem` only.** No `androidx.compose.material3.*` or `com.naveenapps.designsystem.*` imports outside `core:designsystem` (allowed: `foundation.layout`, `Modifier`, state APIs, `Icons`, `ExperimentalMaterial3Api`). If a component is missing, add it to `core:designsystem` with light+dark previews; never a local copy in the feature.
3. **No raw style values in features:** no `.sp`, `FontWeight`, `Color(0x…)`, `Color.White/Black`, `colorResource`, `RoundedCornerShape`/`CircleShape`, ad-hoc `.copy(alpha = …)`; spacing through `EmTheme.spacing`. Exception: the Glance widget (`app/.../widget`), which can't read Compose themes.
4. **One pattern per job:** destructive confirm = bottom sheet; form save = sticky bottom action bar (FAB only for "Add"); one section header; one empty state.
4b. **Text scale (keep screens consistent):**
   - Full-screen list rows (transactions, accounts, categories, budgets, debts, recurring): title `ItemTitle` (16 Medium), secondary `Supporting` (12), amount `EmAmountText` (Medium, 16).
   - Compact rows inside Home cards (accounts strip, category legend, upcoming): title `Body` (14), amount `EmAmountText(size = Small)` (14).
   - Card figures: one `EmAmountText(size = Large)` (28) for a card's main number; `Medium` (16) for figures in tiles. Never 22 or 14 for an amount.
   - Section headers only via `EmSectionHeader` (11 sp caps); its "View all" / "Add new" action uses the same style. Empty states only via `EmEmptyState` (title 16 Medium).
5. Enforced by `:lint-rules` (`RawMaterialUsage`, `HardcodedTextStyle`, `HardcodedColor`, `HardcodedShape`), all errors. Never add design-system entries to a `lint-baseline.xml`; fix the code instead.
6. Screenshot tests (Roborazzi, `core:designsystem`): `./gradlew :core:designsystem:testDebugUnitTest -Proborazzi.test.record=true` to record, `-Proborazzi.test.verify=true` to check. Re-record and commit the images when a token or component changes on purpose.

## Kotlin Multiplatform readiness — keep shared layers platform-neutral

An iOS app is planned with Kotlin Multiplatform + Compose Multiplatform. Plan and progress: the "KMP groundwork checklist" doc (Claude project). Until the modules are converted, new code must not add work to that migration:

- **`core:model`, `core:repository`, `core:domain` use no JVM/Android APIs**: no `java.*`, `javax.*`, `android.*`, `androidx.*` (except the Compose `@Stable`/`@Immutable` annotations, `androidx.annotation`, `androidx.collection`), Gson, `org.json` or Firebase. Allowed: Kotlin stdlib, kotlinx (coroutines, serialization, datetime), Koin. Enforced by the `JvmApiInSharedLayer` lint check (a warning while existing usages are migrated; never baseline it).
- **Platform types cross layer boundaries as plain values**: a Uri as its `toString()`, a file as its absolute path `String`. Convert back only in Android-only code (`core:data`, composables, `MainActivity`).
- **New dates and times** use `kotlin.time.Instant` / `kotlinx.datetime`, not `java.util.Date`, `Calendar` or `java.time`. Stored values must stay bit-identical to what `DateConverter` writes today.
- **New IDs** use `kotlin.uuid.Uuid`; **new JSON** uses kotlinx.serialization.
- **Anything needing `Context`, Firebase, Play services or ML Kit** sits behind an interface in `core:repository` with the implementation in `core:data` (or an Activity-scoped one, as above).
- **Room**: DAO functions are `suspend` or return `Flow`; migrations use raw SQL only (`execSQL`), so they can later move to `SQLiteConnection`.

## Analytics — never send money data

Users' money is private. No analytics event parameter or user property may carry anything about money:

- No amounts, totals, balances, prices, budget values, thresholds or percentages of a budget.
- Not even whether an amount was entered or found (no `has_amount` / `found_amount` style flags, no "amount" in field lists).
- No free text either: notes, payee names, account / category names, search text.

Allowed: counts and buckets of things (transactions, accounts, categories), choices (type, period, setting values), timings, booleans about features (reminder on, has a budget), error class names.

`AnalyticsPrivacy` (core:repository) drops any parameter or user property whose name looks like money, as a safety net. Don't work around it by renaming; if it blocks something, the data shouldn't be sent. Keep `claude/analytics-tracking-plan.md` in the Expense Manager project in step when events change.
