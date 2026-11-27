## ADDED Requirements

### Requirement: core:datastore is shared code
core:datastore SHALL be a Kotlin Multiplatform module built with `naveenapps.plugin.kmp.library`.
Its `commonMain` SHALL depend only on core:model, core:foundation, `datastore-preferences-core`,
kotlinx-serialization-json and kotlinx-coroutines-core. Android-only code (the `Context`
delegates and Koin wiring) SHALL be in `androidMain`.

#### Scenario: Compiling core:datastore for iOS
- **WHEN** `./gradlew :core:datastore:compileKotlinIosArm64 :core:datastore:compileKotlinIosSimulatorArm64` runs on a Mac with Xcode
- **THEN** both compilations succeed

#### Scenario: No org.json in shared code
- **WHEN** core:datastore's sources are searched for `org.json`
- **THEN** there are no matches

### Requirement: Local-as-UTC conversion lives in core:foundation
`fromLocalToUTCTimeStamp` and `fromUTCToLocalTimeStamp` SHALL be defined once, in core:foundation,
and SHALL give the same values as before. The database's stored date format SHALL be unchanged.

#### Scenario: Stored dates unchanged
- **WHEN** `DateConverterTest` runs after the move
- **THEN** it passes unchanged

#### Scenario: Round trip across DST
- **WHEN** core:foundation's test converts local times to the stored form and back in UTC, Asia/Kolkata, America/New_York and Australia/Lord_Howe around DST changes
- **THEN** every value comes back unchanged, on the JVM and on iOS
