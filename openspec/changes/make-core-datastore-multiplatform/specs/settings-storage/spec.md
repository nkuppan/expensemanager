## ADDED Requirements

### Requirement: Settings files and keys stay stable
On Android, settings SHALL be stored in `files/datastore/expense_manager_app_data_store.preferences_pb`
(backed up) and `files/datastore/expense_manager_device_local.preferences_pb` (never backed
up), with the same preference key names, value types and defaults as before. A user's settings
SHALL survive any update.

#### Scenario: Updating over the previous build
- **WHEN** the theme, currency and a custom date range are set in the previous build and the new build is installed over it
- **THEN** the new build shows the same theme, currency and date range

#### Scenario: Backup rules still match
- **WHEN** the Android backup rules are compared with the files the stores use
- **THEN** `backup_rules.xml` and `data_extraction_rules.xml` name the backed-up file and exclude the device-local one, as before

### Requirement: Store classes are shared code
The settings store classes SHALL live in core:datastore's `commonMain` and work on any
`DataStore<Preferences>`. Each platform SHALL only decide where the file is: Android through
`Context.preferencesDataStore`, and iOS under Application Support/datastore/.

#### Scenario: Round trip on both platforms
- **WHEN** core:datastore's `commonTest` sets and reads every store value through a file-backed DataStore
- **THEN** every value reads back as set, and unset values give their defaults, under both `testAndroidHostTest` and `iosSimulatorArm64Test`

### Requirement: Insight history format is compatible
The insight history SHALL stay a JSON array of objects with `kind`, `title`, `message` and
`postedAt`, newest first, at most 50 entries. Entries that can't be read SHALL be skipped, not
fail the whole list.

#### Scenario: History written by the previous version
- **WHEN** a history string written with org.json (including one broken entry) is read by the new code
- **THEN** every readable entry comes back with the same values and order, and the broken one is skipped

#### Scenario: History read by the previous version
- **WHEN** a history written by the new code is read with the previous org.json code
- **THEN** it gets the same entries and values
