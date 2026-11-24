# Spec Delta

## MODIFIED Requirements

### Requirement: Severity per module
The `JvmApiInSharedLayer` issue SHALL be an error in core:repository, so a new JVM or Android
import there fails `lintDebug`. In core:domain it SHALL be a warning until the module compiles for
iOS. Modules that already compile for iOS (core:foundation, core:model) SHALL NOT run the check,
because the iOS compile rejects JVM and Android imports in their common code.

#### Scenario: New Android import in core:repository
- **WHEN** a repository interface imports `android.net.Uri`
- **THEN** `./gradlew lintDebug` fails

#### Scenario: New JVM import in core:model
- **WHEN** a model in `core/model/src/commonMain` imports `java.math.BigDecimal`
- **THEN** `./gradlew :core:model:compileKotlinIosArm64` fails with an unresolved reference

## ADDED Requirements

### Requirement: core:model is shared code
core:model SHALL be a Kotlin Multiplatform module built with `naveenapps.plugin.kmp.library`, with
all its sources in `commonMain`. It SHALL depend only on kotlinx-datetime, the Compose runtime
annotations and the Kotlin standard library, all of which publish iOS artifacts.

#### Scenario: Compiling core:model for iOS
- **WHEN** `./gradlew :core:model:compileKotlinIosArm64 :core:model:compileKotlinIosSimulatorArm64` runs on a Mac with Xcode
- **THEN** both compilations succeed

#### Scenario: Android modules keep using it unchanged
- **WHEN** the Android app is built after the conversion
- **THEN** every module that depends on core:model compiles without source changes
