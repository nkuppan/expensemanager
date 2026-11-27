# Design: make-core-datastore-multiplatform

## Context

- **Module contents** (Android library):
  - nine store classes, each taking a `DataStore<Preferences>`: `ThemeDataStore`, `LocaleDataStore`,
    `CurrencyDataStore`, `ReminderTimeDataStore`, `SettingsDataStore`, `DateRangeDataStore`,
    `FeedbackDataStore`, `DeviceLocalDataStore` and `AutoBackupDataStore`;
  - `di/KoinDatastoreModule.kt`, which creates two files with `Context.preferencesDataStore`:
    - `expense_manager_app_data_store`, backed up by Android Auto Backup;
    - `expense_manager_device_local`, never backed up (`DeviceLocalDataStore` and
      `AutoBackupDataStore`).
- **Android-only code:**
  - the `Context` delegates and `androidContext()`;
  - `org.json` in `FeedbackDataStore.addInsight`/`getInsights`;
  - `System.currentTimeMillis()` in `setFeedbackDialogShown`;
  - core:common's `Long.fromLocalToUTCTimeStamp()`/`fromUTCToLocalTimeStamp()` in
    `DateRangeDataStore`.
- **Users of the two conversion functions:** core:database `DateConverter` (the "local time
  stored as UTC" converter that `DateConverterTest` pins), core:data
  `TransactionRepositoryImpl`, core:designsystem `AppDatePickerDialog`, and core:common's own
  `fromUTCToLocalDate`.
- **Consumers of the stores:** core:data repositories (with JVM tests that build stores on
  `PreferenceDataStoreFactory.create`) and core:notification.
- **Tests:** the module has none of its own.
- **AndroidX DataStore 1.3.0** publishes `datastore-preferences-core` for iOS.
  `datastore-preferences` (Android) adds the `Context` delegate on top of it.

## Goals / Non-Goals

**Goals:**
- core:datastore compiles and is tested for Android, iosArm64 and iosSimulatorArm64.
- On Android, the same files, keys, value types and defaults: every setting a user has survives
  the update unchanged.
- The insight history keeps its stored JSON format in both directions.

**Non-Goals:**
- Koin on iOS (a later Phase 2 step). iOS only gets the function that creates the file.
- iCloud backup rules for the iOS files. These come with Phase 4 (backup), together with the
  cross-platform backup format (decision 27 Nov).
- core:settings' `expense_manager_settings` store, which moves with number formatting.

## Decisions

### D1. Store classes in `commonMain`; file creation per platform
- The nine classes move to `commonMain` unchanged: same `*PreferencesKey("…")` names, types and
  defaults. They only use `DataStore`, `Preferences`, `edit` and the key builders, all from
  `datastore-preferences-core`.
- **Android** (`androidMain`): `KoinDatastoreModule` stays as it is, with the same delegates and
  file names. `preferencesDataStore(name)` keeps writing `files/datastore/<name>.preferences_pb`,
  which is what `backup_rules.xml` and `data_extraction_rules.xml` list.
- **iOS** (`iosMain`): `fun createDataStore(fileName: String): DataStore<Preferences>` calls
  `PreferenceDataStoreFactory.createWithPath { "<Application Support>/datastore/<fileName>.preferences_pb".toPath() }`.
  The Application Support directory comes from `NSFileManager.URLForDirectory(NSApplicationSupportDirectory, …, create = true)`.
  - Application Support, not Documents: it's app data the user shouldn't see in the Files app.
  - Each `DataStore` is created once per file (DataStore requires a single instance per file);
    the Koin step will hold them as singletons.

*Alternatives:*
- `expect`/`actual` for the whole DI module. Koin on iOS isn't in scope yet, so only the file
  creation needs an iOS version. Rejected for now.
- One `createWithPath` on Android too. That would move the Android files' creation away from
  the delegate the app has always used, for no gain, and risk a different path. Rejected.

### D2. Insight history with kotlinx.serialization, same JSON
- A private `@Serializable data class InsightJson(val kind: String = "", val title: String, val message: String, val postedAt: Long)`
  and a `Json { ignoreUnknownKeys = true }`.
- **Read:** decode the stored string as a `JsonArray` (a broken or missing value gives an empty
  list, as before). Decode each element separately, and skip any that fail, as the old
  per-entry `runCatching` did. A missing `kind` reads as "", like `optString`.
- **Write:** the new entry first, then up to `MAX_INSIGHTS - 1` of the old entries, then
  encode. Old entries are copied as the `JsonElement`s they were read as, so entries the code
  can't read are kept as they are, exactly as `history.get(i)` kept them.
- The output is a JSON array of objects with the same keys. Key order and number format can
  differ from org.json's, but neither reader depends on them.
- `InsightJsonCompatibilityTest` (`androidHostTest`, using the real `org.json:json` artifact,
  because Android's built-in org.json is a stub in host tests) checks:
  - org.json-written history, including one broken entry, → new reader → same `StoredInsight`s;
  - new writer → org.json reader → same values;
  - the 50-entry limit and newest-first order.

*Alternative:* a hand-written JSON string builder. Escaping the free-text title and message by
hand is error-prone. Rejected.

### D3. The date-range conversion moves to core:foundation
- `fromLocalToUTCTimeStamp` and `fromUTCToLocalTimeStamp` move to core:foundation's
  `DateKeys.kt`, with the same names and bodies (kotlinx-datetime with the system default zone).
  They gain an optional `zone: TimeZone = TimeZone.currentSystemDefault()` parameter (applied 27 Nov), so
  shared tests can check several zones. Every existing call passes nothing and computes exactly
  what it did before.
- core:common removes its copies. Callers update their imports to
  `com.naveenapps.expensemanager.core.foundation`: DateRangeDataStore, DateConverter,
  TransactionRepositoryImpl, AppDatePickerDialog and core:common's own `fromUTCToLocalDate`.
  Their bodies don't change.
- `DateConverterTest` (core:database) keeps pinning the stored format. A new `commonTest` in
  core:foundation checks the round trip in 4 zones around DST changes.

*Alternative:* leave core:common's copies delegating to foundation. That keeps two names for
one function. Rejected; the import change is mechanical.

### D4. Now
`setFeedbackDialogShown` uses `Clock.System.now().toEpochMilliseconds()` (as in change 3, D1).

### D5. Tests
- **`commonTest`:** every store's set/get round-trips through a real file-backed DataStore,
  created in a temporary directory by a small `expect fun testDataStore(name: String)`. On the
  JVM it's created under `java.io.tmpdir`, and on iOS under `NSTemporaryDirectory()`, each
  through `createWithPath`. The tests cover defaults when unset, every setter, the 50-insight
  limit and the device-local/backed-up split. The last one can only be checked by
  construction: the two stores are created from different files.
- **`androidHostTest`:** `InsightJsonCompatibilityTest` (D2).
- The `shared-ios` CI job adds `:core:datastore:allTests`.

## Risks / Trade-offs

- **The Android file path changes, and users lose their settings** → the Android DI module and
  the delegates stay byte-for-byte the same (D1). The smoke test checks that theme, currency and
  the date range survive an update over the current build (install the old APK, set them, then
  install the new one).
- **The JSON written by kotlinx differs from org.json's** → both readers ignore key order and
  formatting, and `InsightJsonCompatibilityTest` checks both directions. Unicode and quotes in
  titles go through the library's escaping on both sides.
- **`createWithPath` needs okio's `toPath()`** → `datastore-core-okio` brings okio in. If the
  compile needs it declared, add `com.squareup.okio:okio` (already in the dependency graph) to
  `iosMain` and `commonTest`.
- **DataStore's one-instance-per-file rule in tests** → each test uses its own file name in a
  fresh temporary directory.
- **Moving the conversion functions touches `DateConverter`** → the body is unchanged and only
  the import moves. `DateConverterTest` and the core:database migration tests run in
  verification.

## Migration and rollback

- No stored data changes: same files, keys and value types, and the same JSON shape.
- Groups:
  1. the conversion functions move to core:foundation, and `Clock` replaces
     `currentTimeMillis` (still an Android module);
  2. org.json → kotlinx.serialization, with the compatibility test;
  3. the module conversion (`commonMain`, `androidMain`, `iosMain`);
  4. the shared tests and the CI job;
  5. docs;
  6. final verification.
- Rollback: revert this change's commits on `feature/kmp-migration`.
