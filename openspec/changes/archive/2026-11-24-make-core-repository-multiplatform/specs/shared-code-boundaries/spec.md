# Spec Delta

## MODIFIED Requirements

### Requirement: Severity per module
The `JvmApiInSharedLayer` issue SHALL be a warning in core:domain until that module compiles for
iOS. Modules that already compile for iOS (core:foundation, core:model, core:repository) SHALL
NOT run the check, because the iOS compile rejects JVM and Android APIs in their common code,
including calls that need no import, such as `System.currentTimeMillis()`.

#### Scenario: New Android import in core:repository
- **WHEN** a repository interface in `core/repository/src/commonMain` imports `android.net.Uri`
- **THEN** `./gradlew :core:repository:compileKotlinIosArm64` fails with an unresolved reference

#### Scenario: JVM call without an import
- **WHEN** code in `core/repository/src/commonMain` calls `System.currentTimeMillis()`
- **THEN** the iOS compile fails, although no lint check would have reported it

#### Scenario: New JVM import in core:model
- **WHEN** a model in `core/model/src/commonMain` imports `java.math.BigDecimal`
- **THEN** `./gradlew :core:model:compileKotlinIosArm64` fails with an unresolved reference

## ADDED Requirements

### Requirement: core:repository is shared code
core:repository SHALL be a Kotlin Multiplatform module built with `naveenapps.plugin.kmp.library`,
with all its sources in `commonMain`. It SHALL depend only on core:model and kotlinx-coroutines-core.

#### Scenario: Compiling core:repository for iOS
- **WHEN** `./gradlew :core:repository:compileKotlinIosArm64 :core:repository:compileKotlinIosSimulatorArm64` runs on a Mac with Xcode
- **THEN** both compilations succeed

#### Scenario: Android modules keep using it unchanged
- **WHEN** the Android app is built after the conversion
- **THEN** core:domain, core:data, core:notification and app compile without source changes

### Requirement: Failure reasons are class names on every platform
`logFailure(action, error)` SHALL report the error's simple class name as the `reason`
parameter, or "unknown" when there is no error or the class has no name (anonymous classes). On
the JVM, the name SHALL equal the one the earlier `javaClass.simpleName` code reported.

#### Scenario: A standard exception
- **WHEN** `logFailure("category_save", IllegalStateException())` is called
- **THEN** the logged `reason` is "IllegalStateException" on Android and iOS

#### Scenario: No error
- **WHEN** `logFailure("export", null)` is called
- **THEN** the logged `reason` is "unknown"

#### Scenario: Same names as before on Android
- **WHEN** `FailureReasonJvmTest` compares the new reason with `javaClass.simpleName` for standard,
  nested, local and anonymous exception classes
- **THEN** they are equal (anonymous classes: "unknown" in both)

### Requirement: JSON uses kotlinx.serialization
The app SHALL parse and write JSON with kotlinx.serialization only, using `@Serializable` types;
Gson and other reflection-based JSON libraries SHALL NOT be used. The bundled `countries.json`
SHALL decode to the same 246 countries, with every field and currency as in the file.

#### Scenario: Bundled country list
- **WHEN** `CountriesJsonTest` decodes `core/data/src/main/assets/countries.json`
- **THEN** it yields 246 countries, every field equals the value in the raw JSON, and the one
  country without a `currency` object has `null` currency

#### Scenario: No Gson in the build
- **WHEN** the dependencies of every module are listed
- **THEN** none contains `com.google.code.gson:gson`
