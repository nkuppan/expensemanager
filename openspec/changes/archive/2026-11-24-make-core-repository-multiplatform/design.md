# Design: make-core-repository-multiplatform

## Context

- **Contents:** core:repository is an Android library of 32 Kotlin files: repository interfaces,
  `ActivityComponentProvider` (a plain interface), and the analytics helpers `AnalyticsEvents`,
  `AnalyticsHelpers` and `AnalyticsPrivacy`.
- **Imports:** only `core.model`, `kotlinx.coroutines.flow` and `kotlin.time.Instant`. The
  declared `core:common` dependency is unused.
- **The lint check** `JvmApiInSharedLayer` is an error here, but it only inspects imports. Two
  JVM calls need none, so it can't see them:
  - `FeedbackRepository.addInsight(…, postedAt: Long = System.currentTimeMillis())`, a
    `java.lang.System` default argument;
  - `AnalyticsRepository.logFailure`, which reports `error?.javaClass?.simpleName`, a JVM
    reflection extension.
- **Consumers:** core:domain, core:data, core:notification and app.
- **The pattern is established:** change 2 converted core:model the same way, and nine Android
  modules consumed it with no source changes.

## Goals / Non-Goals

**Goals:**
- core:repository compiles for Android, iosArm64 and iosSimulatorArm64.
- The default `postedAt` and the analytics failure reasons stay exactly as before on Android.

**Non-Goals:**
- Moving `AnalyticsPrivacyTest` from core:data into this module. It tests through the public
  API, which doesn't change, and moving it isn't needed for iOS.
- Redesigning `ActivityComponentProvider` or other interfaces that only Android implements
  (Phase 2/3 decides their iOS counterparts).

## Decisions

### D1. `postedAt` default via `kotlin.time.Clock`
`Clock.System.now().toEpochMilliseconds()` is the same wall-clock reading in epoch milliseconds.
On the JVM it reads the same system clock as `System.currentTimeMillis()`.

*Alternative:* remove the default and pass the time at every call site (two workers). That
changes callers for no gain. Rejected.

### D2. Failure reason via `KClass.simpleName`
`error?.let { it::class.simpleName }?.ifBlank { null } ?: "unknown"` replaces the
`javaClass.simpleName` version.

On the JVM, without kotlin-reflect, `KClass.simpleName` comes from the Java class name. It differs
from `Class.simpleName` only for:
- **anonymous classes:** Kotlin gives `null` where Java gives "". Both end up "unknown" after
  `ifBlank`;
- **Java classes Kotlin maps to its own types** (for example `java.lang.Integer` → "Int"). No
  Throwable is mapped like that, except `java.lang.Throwable` itself ("Throwable" either way).

`FailureReasonJvmTest` pins this for the exceptions the app actually reports:
- standard JVM exceptions (`IllegalStateException`, `IllegalArgumentException`, `IOException`,
  `NullPointerException`, `Exception`, `Throwable`) and an app-defined subclass. Android framework
  exceptions such as `SQLiteException` are left out: host tests can't construct Android classes
  (the stubs throw "not mocked"), and they're ordinary JVM classes for this purpose anyway;
- a nested class, a local class and an anonymous object.

*Alternative:* an `expect fun Throwable.className()` with platform actuals. That's more code for
the same result. Rejected.

### D3. Converting the module (as in change 2)
- **Build:** `core/repository/build.gradle.kts` applies `naveenapps.plugin.kmp.library`.
  `commonMain` gets `implementation(project(":core:model"))` and
  `api(libs.kotlinx.coroutines.core)`; `api` because the interfaces return `Flow`.
- **Removed:** the `android { lint { error += "JvmApiInSharedLayer" } }` block, `lintChecks`,
  the `core:common` dependency and `AndroidManifest.xml`.
- **Sources:** move `src/main/kotlin/**` to `src/commonMain/kotlin/**`, with packages and
  namespace unchanged.
- **`core:model` stays `implementation`:** consumers that use model types already declare
  core:model themselves. If one only got it through core:repository, the build in group 1 shows
  it, and that consumer gets its own dependency (no `api` leak).

### D4. Tests
- **`FailureReasonTest` (`commonTest`):** a recording fake `AnalyticsRepository` (five methods)
  checks that `logFailure` logs `action_failed` with `type` and `reason`, for
  `IllegalStateException()`, an app-defined `class SaveFailed : Exception()` and `null`. It runs on
  the Android host and the iOS simulator.
- **`FailureReasonJvmTest` (`androidHostTest`):** for each exception in D2, the logged reason
  equals `error.javaClass.simpleName.ifBlank { null } ?: "unknown"`, the old code verbatim.

### D5. Gson → kotlinx.serialization, without `JsonConverterRepository` (amendment, 25 Nov)
`fromJsonToObject(value: String, classValue: Class<*>): Any?` is Gson's reflection API wrapped in
an interface. It needs `java.lang.Class`, so it can't be shared code. It has one caller:
`CountryRepositoryImpl`, which reads the bundled `countries.json` (246 countries, 154 KB).
`fromObjectToJson` is never called.

**The replacement:**
- `CountryResponseDto.kt` is `@Serializable`, with `@SerialName` carrying the same JSON names as
  the old `@SerializedName`;
- `CountryRepositoryImpl` gets the `Json` from Koin and decodes through
  `internal fun decodeCountries(json: Json, text: String): CountriesResponseDto`;
- the interface, the implementation and Gson are removed.

**Why the results are identical:**
- the file has the same 8 country keys in every entry and 7 currency keys, all matching the DTO
  fields and types;
- one country has no `currency`, and the DTO defaults it to `null`, as Gson left it;
- there are no unknown keys, but `ignoreUnknownKeys = true` keeps Gson's tolerance if the file
  gains one;
- `Uri.decode` only rewrites `%XX` sequences and the file has none, so dropping it changes nothing.

`CountriesJsonTest` checks the decoded DTOs field by field against the raw JSON tree, using
kotlinx's own `parseToJsonElement`. That's independent of the typed mapping, so the test proves the
mapping itself.

**R8:** the kotlinx.serialization plugin generates serializers, and the library ships its own R8
rules, so no reflection keep rules are needed. Only the Gson-specific rules go (the
`@SerializedName` member rule and the two `TypeToken` rules). The DTO keep rule and the global
`-keepattributes` lines stay, because other code may rely on them and they cost nothing. Task 5.4
builds the release bundle to prove it.

*Alternatives:*
- **Keep the interface, typed with `KSerializer<T>`:** a JSON seam for future callers, but
  core:repository would then depend on kotlinx-serialization for one caller. Rejected by the
  developer's choice.
- **Keep Gson in core:data only:** that would work for Android, since core:data isn't shared yet,
  but Phase 2 would have to redo it, and CLAUDE.md already says new JSON uses
  kotlinx.serialization. Rejected.

## Risks / Trade-offs

- **kotlinx is stricter than Gson about types** (a string where an Int is expected fails instead
  of being coerced) → the Python audit showed every value has the DTO's type, and
  `CountriesJsonTest` runs on the real file, so a mismatch fails the build, not the app.
- **Release build without the Gson rules** → task 5.4 runs `bundleRelease`; kotlinx's bundled R8
  rules cover the generated serializers.

- **A consumer used core:common transitively through core:repository** → the full Android build
  in group 1 proves it either way. If something breaks, that consumer declares core:common itself
  (most already do).
- **A consumer used `kotlinx-coroutines-android` transitively** (for example `Dispatchers.Main`)
  → core:domain and the other consumers declare coroutines themselves, and the app module
  declares `kotlinx-coroutines-android`. The group 1 build proves it.
- **iOS `simpleName` for Kotlin standard exceptions:** Kotlin/Native gives the Kotlin class name
  ("IllegalStateException") → `FailureReasonTest` asserts it on the iOS simulator.

## Migration Plan

1. Fix the two JVM calls, convert the module (build file, source move, manifest and dependency
   clean-up), and verify the full Android build. Done 25 Nov.
2. Replace Gson with kotlinx.serialization and remove `JsonConverterRepository` (D5), then verify
   the full Android build with `CountriesJsonTest`.
3. Add the failure-reason tests and run `:core:repository:allTests` on the Mac.
4. Update the docs, run the final verification (including a release build) and archive.

**Rollback:** revert the change's commits. No data or schema impact.

## Open Questions

- None.
