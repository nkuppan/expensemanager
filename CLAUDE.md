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
