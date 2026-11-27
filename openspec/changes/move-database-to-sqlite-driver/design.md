# Design: move-database-to-sqlite-driver

## Context

- **core:database** (Android library, Room 2.8.5, KSP): 12 entities, 6 DAOs, schema version 12
  with exported schemas 2–12, and 11 hand-written migrations.
  - Migrations 2→3 … 11→12 are 27 `db.execSQL` calls. Migration 1→2 rebuilds the v1 tables and
    reads twice: `SELECT COUNT(*) …` and `PRAGMA table_info`.
  - Every DAO function is already `suspend` or returns `Flow`.
- **`KoinDatabaseModule`:**
  - `Room.databaseBuilder(context, ExpenseManagerDatabase::class.java, "expense_manager_database.db").addMigrations(*ALL_MIGRATIONS)`,
    with no driver (Room's SupportSQLite compatibility mode);
  - after a staged restore, it forces the open with `restored.openHelper.writableDatabase`, so
    an unopenable backup rolls back instead of crashing every launch.
- **`BackupRepositoryImpl`** (core:data):
  - restore rejects a backup whose `user_version` is above `openHelper.readableDatabase.version`;
  - `snapshotDatabase()` runs `PRAGMA wal_checkpoint(TRUNCATE)`, then reads the database file
    inside `beginTransaction()` so no writer can checkpoint into it meanwhile;
  - `databaseName()` reads `openHelper.databaseName`.
- **Tests:**
  - 11 instrumented `Migration*Test`s with the SupportSQLite `MigrationTestHelper`, reading
    through `Cursor` (about 290 data assertions);
  - core:data's Robolectric tests build `Room.inMemoryDatabaseBuilder` without a driver, and
    `InstantStorageTest` reads columns through `openHelper`.
- **Room's rules:** once `setDriver` is called, `openHelper` is unavailable and migrations must
  override `migrate(SQLiteConnection)`. Room 2.8 provides `useReaderConnection` and
  `useWriterConnection`, and a driver-based `MigrationTestHelper(instrumentation, file, driver, databaseClass: KClass)`.

## Goals / Non-Goals

**Goals:**
- Android uses the `SQLiteDriver` APIs throughout, so change 8 can move the code to `commonMain`
  unchanged.
- The same file, schema, data, SQL, backup format and SQLite engine: users notice nothing.
- Every existing migration assertion still runs.

**Non-Goals:**
- iOS, KMP or Room 3 (change 8).
- `PendingDatabaseRestore`, which uses Android's `SQLiteDatabase` on the staged file. It belongs
  to the cross-platform backup work.
- Changing queries, indexes or the schema.

## Decisions

### D1. AndroidSQLiteDriver, with Room's I/O context
- The builder becomes `.setDriver(AndroidSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).addMigrations(*ALL_MIGRATIONS)`.
- `AndroidSQLiteDriver` (`androidx.sqlite:sqlite-framework`, pinned at 2.6.2, which fixed the
  WAL connection-pool bug) uses the framework SQLite, so the engine, collations (`COLLATE NOCASE`
  in `DebtDao`) and file format are the same.
- Room keeps WAL journaling and manages the connection pool itself.

*Alternatives:*
- `BundledSQLiteDriver`. Rejected on 27 Nov: it changes the engine and adds 1–2 MB per ABI.
- No driver. Then the migrations would need to exist in two forms until change 8. Rejected.

### D2. Migrations on SQLiteConnection, same SQL
- Each `object : Migration(n, n+1)` overrides `migrate(connection: SQLiteConnection)` and calls
  `connection.execSQL(sql)` (`androidx.sqlite.execSQL`). The SQL strings aren't touched.
- Migration 1→2's helpers take a `SQLiteConnection`:
  - `needsFallback`: `connection.prepare("SELECT COUNT(*) …").use { it.step() && it.getLong(0) > 0 }`;
  - `columnsOf`: `prepare("PRAGMA table_info(`$table`)")`, find the `name` column through
    `getColumnNames()`, then `while (step()) add(getText(index))`.
- **SQL golden test** (`MigrationSqlTest`, JVM, `src/test`): runs every migration against a
  recording `SQLiteConnection` fake (`execSQL` records, `prepare` returns canned results for the
  two reads), and compares the recorded SQL with `migration-sql.golden.txt`.
  - The golden file is recorded once, in group 1, from the current SupportSQLite migrations,
    through a recording `SupportSQLiteDatabase` (a `java.lang.reflect.Proxy`).
  - It's committed before any migration changes, so the rewrite can't silently change a
    statement.

*Alternative:* rely on the instrumented tests alone. They validate the schema and data, but not
that statements we didn't intend to change stayed the same (for example a reordered `DROP`).
The golden file is cheap insurance. Rejected as the only check.

### D3. Constants instead of openHelper
- `const val DATABASE_NAME = "expense_manager_database.db"` and `const val DATABASE_VERSION = 12`,
  public in core:database; `@Database(version = DATABASE_VERSION)`.
- Backup uses `DATABASE_VERSION` for the "newer backup" check. An open Room database is always
  at the compiled version, so this is the value `openHelper.readableDatabase.version` returned.
- Backup uses `DATABASE_NAME` instead of `openHelper.databaseName`.

### D4. Restore check through the writer connection
`restored.openHelper.writableDatabase` becomes
`runBlocking { restored.useWriterConnection { it.usePrepared("PRAGMA user_version") { stmt -> stmt.step() } } }`.
The first connection makes Room open the file and run the migrations and schema validation,
exactly what the old call triggered. The Koin `single` already blocks on this today, so
`runBlocking` keeps the same timing. The rollback path is unchanged.

### D5. Backup snapshot through the writer connection
`snapshotDatabase()` becomes `suspend` (it's already called inside `withContext(dispatchers.io)`):

```kotlin
database.useWriterConnection { transactor ->
    transactor.usePrepared("PRAGMA wal_checkpoint(TRUNCATE)") { it.step() }
    transactor.immediateTransaction { context.getDatabasePath(DATABASE_NAME).readBytes() }
}
```

- The writer connection is exclusive in Room's pool, and the immediate transaction holds the
  write lock while the file is read. That's the same protection `beginTransaction()` gave: no
  writer, and so no checkpoint, until the read finishes.
- The archive format and encryption are unchanged.

### D6. Tests keep their assertions
- **Instrumented migration tests:**
  - `MigrationTestHelper(instrumentation, file = targetContext.getDatabasePath(TEST_DB), driver = AndroidSQLiteDriver(), databaseClass = ExpenseManagerDatabase::class)`;
  - `createDatabase(n)` and `runMigrationsAndValidate(n, listOf(...))` return a
    `SQLiteConnection`; tests run in `runTest`;
  - a test-only `SQLiteConnection.query(sql, args)` returns a `BufferedCursor`. It reads all rows
    up front through `prepare`/`step`/`getColumnType` and offers the `Cursor` calls the tests
    use (`moveToFirst`, `moveToNext`, `count`, `getString`, `getLong`, `getInt`, `getDouble`,
    `isNull`, `getColumnIndexOrThrow`, `use`). The roughly 290 assertions don't change.
- **`MigrationFullChainTest`:** builds through `Room.databaseBuilder(...).setDriver(AndroidSQLiteDriver())`
  and checks through `useReaderConnection` with the same cursor helper.
- **core:data Robolectric tests:** `.setDriver(AndroidSQLiteDriver())` on the in-memory builder
  (Robolectric's SQLite backs the framework driver). `InstantStorageTest.column` reads with
  `useReaderConnection`.

*Alternative:* rewrite each assertion against `SQLiteStatement`. That's about 290 edits, with
room for mistakes and nothing gained. Rejected.

## Risks / Trade-offs

- **A migration statement changes by accident** → `MigrationSqlTest` against the golden file
  recorded from the current code. The instrumented tests check the schema and data on a
  device.
- **Room's driver mode behaves differently from compatibility mode** (connection pool,
  `Flow` invalidation timing) → all DAO tests in core:data run with the driver, and the smoke
  test covers adding, editing and deleting transactions and the screens that update live.
- **Backup or restore regresses** → the smoke test makes a backup on the new build, restores it,
  and restores a backup made by the previous build. The format is unchanged and
  `PendingDatabaseRestore` isn't touched.
- **Robolectric doesn't support `AndroidSQLiteDriver`** → fall back to `BundledSQLiteDriver` in
  host tests only (`androidx.sqlite:sqlite-bundled` as `testImplementation`), so the app keeps
  the framework driver.
- **`useWriterConnection`, `usePrepared` or `immediateTransaction` have different names in
  Room 2.8.5** → the group 2 compile shows it at once. The `RoomDatabase` connection APIs for
  2.8 are checked then, and only the call sites in D4 and D5 change.

## Migration and rollback

- The database file, schema and backup format are unchanged.
- Groups:
  1. record the golden SQL from the current migrations;
  2. the driver, the migrations, the constants and the restore check;
  3. backup;
  4. the instrumented and core:data tests;
  5. docs;
  6. final verification on a device (migration tests, backup and restore, the update test).
- Rollback: revert this change's commits on `feature/kmp-migration`.
