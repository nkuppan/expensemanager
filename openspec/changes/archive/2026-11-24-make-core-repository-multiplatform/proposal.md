# Proposal: make-core-repository-multiplatform

## Why

core:repository holds the interfaces every data source implements, plus three small helpers:
`AnalyticsEvents`, `AnalyticsHelpers` and `AnalyticsPrivacy`. core:domain depends on it, so it
has to compile for iOS before the domain can. Its imports are already clean; the lint check has
been an error here since Phase 0. But two JVM-only calls need no import, so the lint check can't
see them, and they would fail the iOS compile:
- `FeedbackRepository.addInsight` defaults `postedAt` to `System.currentTimeMillis()`;
- `AnalyticsHelpers.logFailure` reads the error's class name with `error?.javaClass?.simpleName`.

A third one came up during apply (25 Nov): `JsonConverterRepository.fromJsonToObject(value,
Class<*>)` takes a `java.lang.Class`, and the first iOS compile rejected it. That interface is
shaped around Gson, which is JVM-only.

## What Changes

- `postedAt: Long = System.currentTimeMillis()` → `Clock.System.now().toEpochMilliseconds()`
  (`kotlin.time`): the same epoch milliseconds.
- `error?.javaClass?.simpleName?.ifBlank { null } ?: "unknown"` →
  `error?.let { it::class.simpleName }?.ifBlank { null } ?: "unknown"`: the same names for the
  exceptions the app reports. Anonymous classes still give "unknown". A JVM test compares the two.
- **Gson is replaced by kotlinx.serialization** (decision taken 25 Nov: remove the interface):
  - `JsonConverterRepository` and its implementation are deleted;
  - its only caller, `CountryRepositoryImpl`, decodes the bundled `countries.json` with a shared
    kotlinx `Json` (`ignoreUnknownKeys = true`, like Gson) into `@Serializable` DTOs that use the
    same JSON names;
  - the no-op `Uri.decode` is dropped (the file contains no `%`);
  - Gson leaves the build: its catalog entry, its core:data dependency, its Koin binding and its
    R8 rules;
  - `CountriesJsonTest` checks every field of all 246 countries against the raw JSON.
- core:repository becomes a Kotlin Multiplatform module through `naveenapps.plugin.kmp.library`:
  - sources move from `src/main/kotlin` to `src/commonMain/kotlin`, with packages unchanged;
  - the empty `AndroidManifest.xml` is removed;
  - so is the unused `core:common` dependency (no file imports it);
  - `kotlinx-coroutines-android` becomes `kotlinx-coroutines-core`, with `api`, because the
    interfaces expose `Flow`;
  - the `JvmApiInSharedLayer` lint check and its `error +=` setting are removed, because the iOS
    compile now enforces the rule.
- New tests:
  - `FailureReasonTest` in `commonTest`, with a fake `AnalyticsRepository`: the reason for a
    standard exception, an app-defined one, and no error. It runs on the Android host and the
    iOS simulator;
  - `FailureReasonJvmTest` in `androidHostTest`: the new reason equals the old
    `javaClass.simpleName` result for standard, nested, local and anonymous exception classes.

User-visible impact: none. The currency/country list is the same 246 entries. Analytics `action_failed` reasons are unchanged (the analytics rules
in CLAUDE.md are unaffected: still only class names, no money data).

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `shared-code-boundaries`: core:repository joins the shared modules; the lint check now only
  covers core:domain until that converts too.

## Impact

- **Module:** `core/repository`. Its build file changes; sources move to `src/commonMain/kotlin`;
  `AndroidManifest.xml` is deleted; `FeedbackRepository.kt` and `AnalyticsHelpers.kt` change;
  tests are added.
- **Consumers:** core:domain, core:data, core:notification and app. No source changes are
  expected for the module conversion; the build proves it.
- **Gson removal:**
  - core:data: `CountryResponseDto.kt`, `CountryRepositoryImpl.kt`, `KoinAppModule.kt`,
    `KoinRepositoryModule.kt`, `JsonConverterRepositoryImpl.kt` (deleted), the build file
    (serialization plugin), and the new test;
  - core:repository: `JsonConverterRepository.kt` (deleted);
  - `gradle/libs.versions.toml`;
  - `app/proguard-rules.pro`.
- **Dependencies:**
  - core:repository: removes `core:common` and `kotlinx-coroutines-android`, and adds
    `kotlinx-coroutines-core` (already in the app);
  - Gson leaves the app entirely;
  - core:data gains `kotlinx-serialization-json`, which the app already ships for navigation routes.

**Rollback:** revert this change's commits on `feature/kmp-migration`. No stored data or schema
changes.
