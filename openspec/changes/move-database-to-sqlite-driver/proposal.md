# Proposal: move-database-to-sqlite-driver

## Why

core:database is the next Phase 2 step (decision 27 Nov). Room only runs on iOS through the
`SQLiteDriver` APIs (`SQLiteConnection`, `SQLiteStatement`), but the Android app still uses the
older SupportSQLite APIs in five places:
- the 11 migrations (`Migration.migrate(SupportSQLiteDatabase)`; migration 1→2 also reads with
  cursors);
- the database builder (no driver, so Room runs in compatibility mode);
- the restore check in `KoinDatabaseModule` (`openHelper.writableDatabase`);
- backup (`BackupRepositoryImpl` reads `openHelper.readableDatabase.version`, runs the WAL
  checkpoint and reads the file through `openHelper`, and takes the file name from
  `openHelper.databaseName`);
- the tests (the 11 instrumented migration tests use the SupportSQLite `MigrationTestHelper`, and
  `InstantStorageTest` reads columns through `openHelper`).

Decisions (27 Nov):
- **Each platform's own SQLite.** Android uses `AndroidSQLiteDriver`, the same framework SQLite
  as today, so there's no engine change and no APK growth. iOS will use `NativeSQLiteDriver`.
- **Two changes.** This one (change 7) moves Android to the driver APIs inside the existing
  Android module, where the existing tests can check it. Change 8 moves the module to KMP and
  runs it on iOS.

This is also the path Google recommends towards Room 3 (`androidx.room3`, stable 3.0.3): do the
driver modernisation on Room 2.8 first. Whether change 8 lands on Room 2.8 KMP or Room 3 is
decided when it's proposed.

## What Changes

- **Driver:** the database is built with `.setDriver(AndroidSQLiteDriver())` and
  `.setQueryCoroutineContext(Dispatchers.IO)`, the same file (`expense_manager_database.db`)
  and the same migrations. `androidx.sqlite:sqlite-framework` 2.6.2 is added to the catalog.
- **Migrations:** all 11 override `migrate(connection: SQLiteConnection)` and use
  `connection.execSQL`. The SQL text is unchanged, character for character.
  - Migration 1→2's two reads (the count of account-less transactions and `PRAGMA table_info`)
    use `connection.prepare(…).use { step() … }`.
- **Constants:** `DATABASE_NAME` and `DATABASE_VERSION` (12) become public constants in
  core:database. `@Database(version = DATABASE_VERSION)`.
- **Restore check (`KoinDatabaseModule`):** opening the restored file through
  `openHelper.writableDatabase` becomes a first query on Room's writer connection. Room runs the
  migrations and validation on that first use. A failure still rolls back to the previous
  database, as today.
- **Backup (`BackupRepositoryImpl`):**
  - the "newer version" check compares with `DATABASE_VERSION`;
  - the file name comes from `DATABASE_NAME`;
  - the snapshot runs `PRAGMA wal_checkpoint(TRUNCATE)` and reads the file inside an immediate
    transaction on Room's writer connection (`useWriterConnection`). That gives the same
    guarantee as today: no writer can change the file while it's read.
  - The backup file format is unchanged.
- **Tests:**
  - the 11 instrumented migration tests use the driver-based `MigrationTestHelper`
    (`AndroidSQLiteDriver`, `KClass`). A small test-only `connection.query(sql)` returns a
    buffered cursor with the same `moveToFirst`/`getString`/`isNull`/… calls, so the roughly 290
    data assertions stay as they are; only how each test opens the database changes;
  - `MigrationFullChainTest` opens the final database through the builder with the driver;
  - the core:data Robolectric tests build their in-memory databases with the same driver, and
    `InstantStorageTest` reads columns through `useReaderConnection`;
  - a new JVM test pins the migration SQL: the statement each migration executes equals what it
    executed before.

User-visible impact: none. Same database file, schema, data, backup format and SQLite engine.

## Capabilities

### New Capabilities
- `database-storage`: how the app's database is opened, migrated, backed up and restored (file,
  version, driver, migration rules).

### Modified Capabilities
- (none; `date-time-handling` keeps pinning the stored date format, unchanged)

## Impact

- **core/database:**
  - `ExpenseManagerDatabase.kt` (the constants), `DatabaseMigrations.kt`, `Migration1To2.kt`
    and `di/KoinDatabaseModule.kt`;
  - the build file (`sqlite-framework`);
  - the 11 tests in `src/androidTest`, plus a test-only cursor helper;
  - a new JVM test for the migration SQL.
- **core/data:** `BackupRepositoryImpl.kt`, the Robolectric repository tests (driver on the
  in-memory builder) and `InstantStorageTest`.
- **app:** none expected. `DemoDataSeeder.clearAllTables()` works with a driver.
- **gradle/libs.versions.toml:** `androidx-sqlite-framework` (2.6.2).
- **Not in scope:** `PendingDatabaseRestore`. It validates a backup file with Android's own
  `SQLiteDatabase` before Room opens it, and stays Android code. It's revisited with the
  cross-platform backup work.

**Rollback:** revert this change's commits on `feature/kmp-migration`. The database file and
schema are untouched, so a downgrade opens it as before.
