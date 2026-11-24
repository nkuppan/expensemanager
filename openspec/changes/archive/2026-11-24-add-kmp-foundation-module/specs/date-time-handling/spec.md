# Spec Delta

## ADDED Requirements

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
