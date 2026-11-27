# Proposal: make-core-datastore-multiplatform

## Why

Phase 2 (data) starts here (decision 27 Nov: core:datastore first). core:datastore holds the
app's settings and small bits of state (theme, locale, currency, reminders, date range,
feedback and insight history, auto-backup folder, device-local flags). It's 10 files (about 800
lines), each a thin wrapper around a `DataStore<Preferences>`.

AndroidX DataStore Preferences publishes iOS artifacts, so the wrappers can become shared code
almost unchanged. Only four things tie the module to Android:
- the `Context.preferencesDataStore(...)` delegates in `KoinDatastoreModule` that create the two
  files;
- `org.json` (`JSONArray`/`JSONObject`) for the insight history in `FeedbackDataStore`;
- `System.currentTimeMillis()` in `FeedbackDataStore.setFeedbackDialogShown`;
- core:common's `fromLocalToUTCTimeStamp`/`fromUTCToLocalTimeStamp` in `DateRangeDataStore`
  (pure kotlinx-datetime, in an Android module).

This step also sets up the platform-file pattern (where each platform keeps a file) that
core:database follows next.

## What Changes

- **core:datastore becomes Kotlin Multiplatform** (`naveenapps.plugin.kmp.library`).
  - The 9 store classes move to `commonMain`, with the same key names, key types and defaults,
    so stored settings are read exactly as before.
  - `commonMain` depends on core:model, core:foundation and `androidx.datastore:datastore-preferences-core`
    (the multiplatform artifact), plus kotlinx-serialization-json for D2.
  - `androidMain` keeps the Koin module with the two `Context.preferencesDataStore` delegates
    (`expense_manager_app_data_store` and `expense_manager_device_local`). The Android files stay
    at `files/datastore/<name>.preferences_pb`, so the backup rules in `app/src/main/res/xml/`
    still match.
  - `iosMain` gets `createDataStore(fileName)`, which builds the file with
    `PreferenceDataStoreFactory.createWithPath` under Application Support/datastore/. No Koin
    on iOS yet (that's a later Phase 2 step).
- **Insight history: `org.json` → kotlinx.serialization.** The stored string keeps the same JSON
  shape (an array of `{kind, title, message, postedAt}`), and entries that can't be read are
  still skipped.
  - A JVM test checks that both directions are compatible: what the old org.json code wrote is
    read by the new code, and what the new code writes is read by the old code.
- **`System.currentTimeMillis()` → `Clock.System.now().toEpochMilliseconds()`.**
- **The date-range conversion moves to core:foundation.** `fromLocalToUTCTimeStamp` and
  `fromUTCToLocalTimeStamp` are already pure kotlinx-datetime. They move to `DateKeys.kt` with
  the same names and code, and core:common's copies are removed. core:common re-exports
  core:foundation through its `api` dependency, so existing callers change only their imports.
- **Tests (new; the module has none today):**
  - `commonTest`: each store's values round-trip through a real DataStore on a temporary file
    (Android host and iOS simulator), including the insight history's 50-entry limit and
    newest-first order;
  - `androidHostTest`: the org.json compatibility test.

User-visible impact: none. Same files, keys and values on Android; settings survive the update.

## Capabilities

### New Capabilities
- `settings-storage`: how settings are stored (files, compatibility of stored values, the
  shared store classes).

### Modified Capabilities
- `shared-code-boundaries`: core:datastore joins the shared modules; the date-range conversion
  lives in core:foundation.

## Impact

- **core/datastore:**
  - the build file;
  - sources move to `src/commonMain`, `src/androidMain` (DI) and `src/iosMain` (file creation);
  - `FeedbackDataStore` and `DateRangeDataStore` change;
  - new tests.
- **core/foundation:** `DateKeys.kt` gets the two conversion functions, plus a test.
- **core/common:** the two functions are removed from `DateUtils.kt`; their callers' imports are
  updated (expected: `DateRangeDataStore` and a few core:data/feature files; the build lists
  any others).
- **gradle/libs.versions.toml:** `androidx-dataStore-preference-core` (same version, 1.3.0).
  Also `org-json` for the JVM compatibility test only, because Android's built-in org.json is a
  stub in host tests.
- **CI:** `:core:datastore:allTests` is added to the `shared-ios` job.
- **Docs:** CLAUDE.md and the roadmap.
- **Not in scope:** core:settings' own DataStore (`expense_manager_settings`, number formatting).
  It moves with the number-formatting step.

**Rollback:** revert this change's commits on `feature/kmp-migration`. Stored files are
untouched, so no data migration is needed either way.
