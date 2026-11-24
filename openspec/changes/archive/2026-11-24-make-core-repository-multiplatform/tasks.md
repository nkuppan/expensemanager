# Tasks: make-core-repository-multiplatform

Each group stops at its verify task. Claude writes the code; the developer runs the command on
the Mac and shares `build-log.txt`. A task is checked off only after a green run.

## 1. JVM calls and the module conversion

- [x] 1.1 `FeedbackRepository.addInsight`: default `postedAt` to `Clock.System.now().toEpochMilliseconds()` (design D1)
- [x] 1.2 `AnalyticsHelpers.logFailure`: reason from `error?.let { it::class.simpleName }?.ifBlank { null } ?: "unknown"` (design D2)
- [x] 1.3 `core/repository/build.gradle.kts`: apply `naveenapps.plugin.kmp.library`; `commonMain` gets `implementation(project(":core:model"))` and `api(libs.kotlinx.coroutines.core)`; remove the `android { lint { … } }` block, `lintChecks` and `core:common`
- [x] 1.4 Move `core/repository/src/main/kotlin/**` to `core/repository/src/commonMain/kotlin/**` and delete `core/repository/src/main/AndroidManifest.xml`
- [x] 1.5 Verify: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` is green, with no source changes in the consuming modules (or, if one relied on a transitive dependency, that consumer declares it, as in design D3)

## 2. Replace Gson with kotlinx.serialization (added 25 Nov)

The first run of the old task 2.3 failed: `JsonConverterRepository.fromJsonToObject(value, Class<*>)` uses `java.lang.Class`, which the iOS compile rejects. Decision (25 Nov): remove the interface and parse the bundled `countries.json` with kotlinx.serialization (design D5).

- [x] 2.1 `core/data/build.gradle.kts`: apply `kotlin("plugin.serialization")` (as core:navigation does); replace `implementation(libs.gson)` with `implementation(libs.kotlinx.serialization.json)`
- [x] 2.2 `CountryResponseDto.kt`: mark `CountriesResponseDto`, `CountryResponseDto` and `CurrencyResponseDto` `@Serializable`; `@SerializedName` → `@SerialName` (same JSON names); fields and defaults unchanged
- [x] 2.3 `CountryRepositoryImpl`: decode with an injected `Json` through a new `internal fun decodeCountries(json: Json, text: String): CountriesResponseDto`, which the test can call; the `Uri.decode` step is dropped (the file has no `%`, so it did nothing); Koin passes `json = get()`
- [x] 2.4 `KoinAppModule`: `single<Gson>` → `single { Json { ignoreUnknownKeys = true } }` (Gson ignored unknown keys too)
- [x] 2.5 Delete `JsonConverterRepository` (core:repository) and `JsonConverterRepositoryImpl` (core:data) and its Koin binding; remove the `gson` entries from `gradle/libs.versions.toml`; in `app/proguard-rules.pro` remove only the Gson-specific rules (the `@SerializedName` member rule and both `TypeToken` rules), and keep the DTO keep rule and the `-keepattributes` lines
- [x] 2.6 Add `core/data/src/test/.../CountriesJsonTest.kt` (JVM): decode the real `src/main/assets/countries.json` with `decodeCountries`; check that there are 246 countries; and check every field of every country and currency against the raw JSON tree (`Json.parseToJsonElement`), with the one country without `currency` mapping to `null`
- [x] 2.7 Verify: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` is green

## 3. Tests and iOS compilation

- [x] 3.1 Add `core/repository/src/commonTest/.../FailureReasonTest.kt` (design D4): `IllegalStateException()` → "IllegalStateException", an app-defined exception → its name, `null` → "unknown"; `type` carries the action
  - Written 25 Nov, before the amendment.
- [x] 3.2 Add `core/repository/src/androidHostTest/.../FailureReasonJvmTest.kt` (design D4): the new reason equals the old `javaClass.simpleName` result for standard, nested, local and anonymous exception classes
  - Written 25 Nov, before the amendment.
- [x] 3.3 Verify on the Mac: `./gradlew :core:repository:compileKotlinIosArm64 :core:repository:allTests > build-log.txt 2>&1` - both tests pass on the Android host, and `FailureReasonTest` passes on `iosSimulatorArm64`

## 4. Documentation

- [x] 4.1 CLAUDE.md: mark core:repository as Kotlin Multiplatform (module list and KMP section); the `JvmApiInSharedLayer` lint check now covers only core:domain; JSON is kotlinx.serialization only (Gson removed)
- [x] 4.2 Update `claude/kmp-migration-roadmap.md` (status and progress log)

## 5. Final verification

- [x] 5.1 Full Android build on the Mac: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` ends with BUILD SUCCESSFUL
- [x] 5.2 iOS: `./gradlew :core:repository:allTests :core:model:allTests :core:foundation:allTests` pass on `iosSimulatorArm64`
- [x] 5.3 Smoke test on an Android device:
  - open the notification centre (insights list);
  - Settings → Currency / Country: the list shows all countries with their currencies and symbols, and search works;
  - trigger the weekly summary or a budget alert if convenient;
  - add, edit and delete a transaction
- [ ] 5.4 Release build: `./gradlew :app:bundleRelease` (or CI's release job) succeeds, so R8 is checked without the Gson rules — not run locally; left to CI's release job on the `feature/kmp-migration` push
- [x] 5.5 `openspec validate make-core-repository-multiplatform --strict` passes; then archive the change
