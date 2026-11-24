# date-time-handling Specification

## Purpose
How the app represents, stores, keys and compares dates and times, so the same rules hold on
Android and iOS. Established by the Phase 0 groundwork (C1–C6, PRs 3–12).

## Requirements

### Requirement: Date types
Models, Room entities, repository interfaces, use cases and screen state SHALL represent a
moment as `kotlin.time.Instant` and calendar values as kotlinx-datetime types (`LocalDate`,
`LocalDateTime`, `TimeZone`, `DayOfWeek`). Production code SHALL NOT use `java.util.Date` or
`java.util.Calendar`, except inside the Android display formatters (`DateUtils.kt`,
`InstantDisplay.kt`) and the Android week counter (`CalendarWeeksRepositoryImpl`).

#### Scenario: A transaction's date
- **WHEN** a transaction is created, saved and loaded again
- **THEN** its `createdOn` is an `Instant` at every layer, from the screen state to the entity

### Requirement: Stored date format
`DateConverter` SHALL store an `Instant` as the device's local wall-clock time written as if it
were UTC, in epoch milliseconds, and SHALL read a stored value back as that wall-clock time in
the device's current zone. The stored values SHALL be identical to those written by the earlier
`java.util.Date` converter, so no database migration is needed.

#### Scenario: Saving in India
- **WHEN** a date of 23 Nov 2026 10:15:30.123 in Asia/Kolkata is saved
- **THEN** the column holds the epoch milliseconds of 2026-11-23T10:15:30.123Z

#### Scenario: Phone changes time zone
- **WHEN** a row saved at 10:15 in Asia/Kolkata is read after the phone moves to America/New_York
- **THEN** it reads as 10:15 on the same day in New York

### Requirement: Millisecond precision across the database
The database SHALL keep millisecond precision. Code that compares a date held in memory with one
loaded from the database SHALL compare `toEpochMilliseconds()`, not `Instant` equality.

#### Scenario: Unsaved-changes check after a reload
- **WHEN** an edited transaction is compared with the copy loaded from the database
- **THEN** the dates are compared in epoch milliseconds, so sub-millisecond precision from
  `Clock.System.now()` never counts as a change

### Requirement: Locale-independent keys
Values that are stored or matched SHALL use fixed, locale-independent formats: the month key
"<English month name> <yyyy>" (for example "October 2026"), the year key "<yyyy>", and the day
key "dd/MM/yyyy". They SHALL produce the same strings whatever the app language or the device's
calendar system.

#### Scenario: Budget month key in Thai with the Buddhist calendar
- **WHEN** a monthly budget for October 2026 is saved with the app in Thai
- **THEN** its `selectedMonth` is "October 2026"

### Requirement: Display text follows the app language
Dates shown to the user (day and month names, times, ranges) SHALL be formatted in the app's
selected language, through the Android formatters in core:common (`DateUtils.kt` and the
`Instant` overloads in `InstantDisplay.kt`), which give the same text for `Instant` and `Date`.

#### Scenario: Month and year in the budget title
- **WHEN** a budget for a past month is shown with the app in German
- **THEN** the month name is German

#### Scenario: Same text after the move to Instant
- **WHEN** any `Instant` display formatter formats a moment
- **THEN** the text equals what its `java.util.Date` twin gives for the same moment, zone and
  language

### Requirement: Same keys and schedules on every platform
The stored-key functions (month, year and day keys and their parsers), the period bounds and the
scheduling helpers (`nextTimeOfDay`, `nextWeekdayAt`) SHALL be shared code and SHALL give the
same results on Android and iOS for the same inputs and time zone.

#### Scenario: Month key on both platforms
- **WHEN** `LocalDate(2026, 10, 15).toMonthAndYearKey()` runs in the common tests
- **THEN** it returns "October 2026" on the Android host target and on `iosSimulatorArm64`

#### Scenario: Next reminder on a daylight-saving night, both platforms
- **WHEN** `nextTimeOfDay(2, 0, America/New_York)` is asked at 14:00 on 7 March 2026
- **THEN** both targets return 03:00 EDT on 8 March, the first moment after the skipped hour
