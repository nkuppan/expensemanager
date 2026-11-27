## ADDED Requirements

### Requirement: Database file and version are stable
The app's database SHALL be the SQLite file `expense_manager_database.db` at schema version
`DATABASE_VERSION` (12), with Room's exported schemas in `core/database/schemas`. Any schema change SHALL
bump the version and add a hand-written migration with an instrumented test; destructive or
automatic migrations SHALL NOT be used.

#### Scenario: Opening after an update
- **WHEN** a database created by the previous build is opened by the new build
- **THEN** it opens without migration errors and every row reads back unchanged

### Requirement: Room uses the platform SQLite through a driver
Room SHALL be built with a `SQLiteDriver`: on Android `AndroidSQLiteDriver` (the framework
SQLite). Code outside core:database SHALL NOT use `openHelper` or SupportSQLite APIs on the
Room database.

#### Scenario: No SupportSQLite on the Room database
- **WHEN** the app's main and test sources are searched for `openHelper` and `SupportSQLiteDatabase`
- **THEN** there are no matches outside `PendingDatabaseRestore` (which validates a staged backup file, not the Room database)

### Requirement: Migrations run on SQLiteConnection with unchanged SQL
Every migration SHALL implement `migrate(SQLiteConnection)`. The SQL each migration executes SHALL
stay exactly as recorded in `migration-sql.golden.txt`, unless a change deliberately updates the
golden file.

#### Scenario: Migration SQL pinned
- **WHEN** `MigrationSqlTest` runs every migration against a recording connection
- **THEN** the executed statements equal the golden file, in order

#### Scenario: Every migration still verified on a device
- **WHEN** `./gradlew :core:database:connectedDebugAndroidTest` runs
- **THEN** all 11 migration tests and the full-chain test pass with the driver-based helper

### Requirement: Backup snapshots are consistent and unchanged in format
A backup SHALL contain the database file read after a WAL checkpoint while no writer can change
it, in the same archive format as before. A backup whose schema version is newer than
`DATABASE_VERSION`, or older than the oldest restorable version, SHALL be rejected.

#### Scenario: Backup and restore on the new build
- **WHEN** a backup is made on the new build, data is changed, and the backup is restored
- **THEN** the data is back exactly as it was when the backup was made

#### Scenario: Backup from the previous build
- **WHEN** a backup made by the previous build is restored on the new build
- **THEN** it restores, and every transaction, account, category, budget and debt entry is present
