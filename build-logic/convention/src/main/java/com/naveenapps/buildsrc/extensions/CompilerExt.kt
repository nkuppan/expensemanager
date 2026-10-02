package com.naveenapps.buildsrc.extensions

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val JAVA_VERSION = JavaVersion.VERSION_17

/**
 * Base Android + Kotlin configuration.
 *
 * AGP 9 compiles Kotlin itself ("built-in Kotlin"), so the org.jetbrains.kotlin.android plugin
 * is no longer applied anywhere, and Kotlin's jvmTarget follows compileOptions.targetCompatibility
 * automatically — it must not be set separately.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    // AGP 9: CommonExtension is no longer generic and its block methods moved to the concrete
    // extension types, so configure through the properties instead of `defaultConfig { }` etc.
    commonExtension.compileSdk = COMPILE_SDK
    commonExtension.defaultConfig.minSdk = MIN_SDK
    commonExtension.compileOptions.apply {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }

    configureKotlinCompilerOptions()
}

/**
 * Shared Kotlin compiler options. Configured on the compile tasks rather than through the
 * `kotlin` extension so it works the same for AGP's built-in Kotlin and plain JVM modules.
 */
internal fun Project.configureKotlinCompilerOptions() {
    // Treat all Kotlin warnings as errors (disabled by default)
    // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
    val warningsAsErrors = providers.gradleProperty("warningsAsErrors")
        .map { it.toBoolean() }
        .orElse(false)

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors.set(warningsAsErrors)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-Xconsistent-data-class-copy-visibility",
            )
        }
    }
}
