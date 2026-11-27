# Tasks: move-database-to-sqlite-driver

Android only: core:database stays an Android module in this change (change 8 moves it to KMP).

## 1. Pin the migration SQL (design D2)

- [ ] 1.1 `core/database/src/test/.../MigrationSqlRecorder.kt`: a recording `SupportSQLiteDatabase` (`java.lang.reflect.Proxy`) that records `execSQL` and answers the two migration-1→2 reads with fixed canned results (a count of 1, and for `PRAGMA table_info` each table's v2 column names, as a v1 table that has them all); group 2's recording connection gives the same answers, so the recorded SQL must match
- [ ] 1.2 `MigrationSqlTest` (JVM): runs `ALL_MIGRATIONS` against the recorder and compares with `src/test/resources/migration-sql.golden.txt`; a `-PrecordMigrationSql=true` run writes the golden file
- [ ] 1.3 Verify: `./gradlew :core:database:testDebugUnitTest -PrecordMigrationSql=true > build-log.txt 2>&1`, commit the golden file, then `./gradlew :core:database:testDebugUnitTest >> build-log.txt 2>&1` is green

## 2. Driver, migrations and constants (design D1–D4)

- [ ] 2.1 `gradle/libs.versions.toml`: `androidx-sqlite-framework` 2.6.2; core:database depends on it
- [ ] 2.2 `ExpenseManagerDatabase.kt`: public `DATABASE_NAME` and `DATABASE_VERSION`; `@Database(version = DATABASE_VERSION)`
- [ ] 2.3 `DatabaseMigrations.kt` and `Migration1To2.kt`: `migrate(connection: SQLiteConnection)` with `connection.execSQL`; migration 1→2's reads via `prepare`/`step` (design D2); SQL strings untouched
- [ ] 2.4 `MigrationSqlTest`: switch the recorder to a recording `SQLiteConnection` (same canned reads); the golden file is not re-recorded
- [ ] 2.5 `KoinDatabaseModule`: `setDriver(AndroidSQLiteDriver())`, `setQueryCoroutineContext(Dispatchers.IO)`, `DATABASE_NAME`; the restore check through `useWriterConnection` (design D4)
- [ ] 2.6 Verify: `./gradlew :core:database:testDebugUnitTest assembleDebug spotlessCheck > build-log.txt 2>&1` is green; `MigrationSqlTest` passes against the unchanged golden file

## 3. Backup (design D3, D5)

- [ ] 3.1 `BackupRepositoryImpl`: the version check uses `DATABASE_VERSION`, the file name `DATABASE_NAME`; `snapshotDatabase()` becomes `suspend` and uses `useWriterConnection` + `immediateTransaction`; no `openHelper` left
- [ ] 3.2 Verify: `./gradlew :core:data:testDebugUnitTest assembleDebug > build-log.txt 2>&1` is green (`PendingDatabaseRestoreTest` and the backup tests included)

## 4. Tests on the driver (design D6)

- [ ] 4.1 `core/database/src/androidTest/.../BufferedCursor.kt`: `SQLiteConnection.query(sql, args)` returning a buffered cursor with the calls the tests use
- [ ] 4.2 The 11 `Migration*Test`s: driver-based `MigrationTestHelper`, `runTest`, connection-based `createDatabase`/`runMigrationsAndValidate`; assertions unchanged
- [ ] 4.3 `MigrationFullChainTest`: builder with `setDriver(AndroidSQLiteDriver())`, checks through `useReaderConnection`
- [ ] 4.4 core:data repository tests: `setDriver(AndroidSQLiteDriver())` on every in-memory builder; `InstantStorageTest.column` through `useReaderConnection`
- [ ] 4.5 Verify: `./gradlew :core:database:connectedDebugAndroidTest > build-log.txt 2>&1` (device or emulator connected) and `./gradlew :core:data:testDebugUnitTest >> build-log.txt 2>&1` are green; the number of tests per class equals the count before
- [ ] 4.6 `grep -rn "openHelper\|SupportSQLiteDatabase" --include=*.kt core app feature | grep -v /build/` finds only `PendingDatabaseRestore` and the group-1 recorder (removed in 2.4)

## 5. Docs

- [ ] 5.1 CLAUDE.md "Database": the driver (`AndroidSQLiteDriver`), migrations on `SQLiteConnection`, the driver-based migration test pattern (`BufferedCursor`), `MigrationSqlTest` and its golden file, no `openHelper`
- [ ] 5.2 Update `claude/kmp-migration-roadmap.md`

## 6. Final verification

- [ ] 6.1 Full Android build on the Mac: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` ends with BUILD SUCCESSFUL
- [ ] 6.2 Instrumented: `./gradlew :core:database:connectedDebugAndroidTest >> build-log.txt 2>&1` passes
- [ ] 6.3 iOS regression check: `./gradlew :core:foundation:allTests :core:model:allTests :core:repository:allTests :core:domain:allTests :core:datastore:allTests :shared:allTests >> build-log.txt 2>&1` passes
- [ ] 6.4 Smoke test on an Android device, installing over the previous build: all data present; add, edit and delete a transaction (lists and dashboard update at once); make a backup, change data, restore it (data back); restore a backup made by the previous build
- [ ] 6.5 `openspec validate move-database-to-sqlite-driver --strict` passes; then archive the change
