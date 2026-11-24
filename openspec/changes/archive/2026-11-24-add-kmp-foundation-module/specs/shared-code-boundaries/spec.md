# Spec Delta

## ADDED Requirements

### Requirement: Shared modules depend only on shared code
A Kotlin Multiplatform module (core:foundation now; core:model, core:repository and
core:domain as each one converts) SHALL depend only on other shared modules and on libraries
that publish iOS artifacts. It SHALL NOT depend on core:common or any other Android-only module.

#### Scenario: core:foundation's dependencies
- **WHEN** the dependencies of `:core:foundation` are listed
- **THEN** they are kotlinx-coroutines-core, kotlinx-datetime and the Kotlin standard library
  only

#### Scenario: Android code still reaches the moved utilities
- **WHEN** an Android module that depends on core:common uses `AppCoroutineDispatchers`
- **THEN** it compiles through core:common's `api` dependency on core:foundation, importing
  the class from `com.naveenapps.expensemanager.core.foundation`

### Requirement: Pure utilities live in core:foundation
The coroutine dispatcher holder (`AppCoroutineDispatchers`), the stored-key and period-bound
date functions (`DateKeys.kt`), and the date/time edit and scheduling helpers (`InstantTime.kt`)
SHALL live in core:foundation's `commonMain`. core:common SHALL keep only Android-dependent code:
resources, `Context` helpers, display formatters and Koin wiring.

#### Scenario: Looking for a date key function
- **WHEN** code needs `toMonthAndYearKey()` for an `Instant` or `LocalDate`
- **THEN** it imports it from `com.naveenapps.expensemanager.core.foundation`
