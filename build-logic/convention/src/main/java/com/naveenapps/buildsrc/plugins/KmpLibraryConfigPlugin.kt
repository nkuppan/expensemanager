package com.naveenapps.buildsrc.plugins

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.naveenapps.buildsrc.extensions.COMPILE_SDK
import com.naveenapps.buildsrc.extensions.MIN_SDK
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * `naveenapps.plugin.kmp.library`: a Kotlin Multiplatform library shared between the Android app
 * and iOS (OpenSpec change add-kmp-foundation-module, design D1, D2, D6).
 *
 * - Android target through AGP 9's com.android.kotlin.multiplatform.library (com.android.library
 *   can't be combined with KMP under AGP 9). Namespace from the module path, SDK levels from
 *   AndroidConfigExt.kt, JVM 17 like every other module, host (JVM) tests on.
 * - iOS: iosArm64 (devices) and iosSimulatorArm64 (Apple Silicon simulators). iPhone, iOS 16+.
 *   On hosts without Xcode (Linux CI) Kotlin skips these targets; see gradle.properties.
 * - commonTest gets kotlin("test"), so the same tests run on the JVM and on the iOS simulator.
 * - `test` runs the Android host tests, so `./gradlew test` keeps covering shared modules.
 */
class KmpLibraryConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(plugin = "org.jetbrains.kotlin.multiplatform")
                apply(plugin = "com.android.kotlin.multiplatform.library")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget(this).apply {
                    namespace = namespaceFromPath()
                    compileSdk = COMPILE_SDK
                    minSdk = MIN_SDK
                    withHostTest {}
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_17)
                    }
                }

                iosArm64()
                iosSimulatorArm64()

                compilerOptions {
                    allWarningsAsErrors.set(
                        providers.gradleProperty("warningsAsErrors").map { it.toBoolean() }.orElse(false),
                    )
                    freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
                }

                // Opt-ins per source set, not as compiler flags (the flags also reach the iOS link step).
                // Only the stdlib marker here: a module that uses experimental coroutine APIs opts in
                // itself, since a marker from a library the module doesn't depend on warns as unresolved.
                sourceSets.configureEach {
                    languageSettings.optIn("kotlin.RequiresOptIn")
                }

                sourceSets.getByName("commonTest").dependencies {
                    implementation(kotlin("test"))
                }
            }

            // Design D6: keep `./gradlew test` (and CI) running the shared module's tests.
            val hostTests = tasks.matching { it.name == "testAndroidHostTest" }
            if ("test" in tasks.names) {
                tasks.named("test") { dependsOn(hostTests) }
            } else {
                tasks.register("test") {
                    group = "verification"
                    description = "Runs the Android host (JVM) tests of this multiplatform module."
                    dependsOn(hostTests)
                }
            }
        }
    }

    /**
     * The Android target that com.android.kotlin.multiplatform.library adds to the `kotlin`
     * extension. Build scripts reach it as `kotlin { android { } }`; AGP 9.1 renamed it from
     * `androidLibrary`, which is deprecated, so look it up under either name.
     */
    private fun androidTarget(kotlin: KotlinMultiplatformExtension): KotlinMultiplatformAndroidLibraryTarget {
        val extensions = (kotlin as ExtensionAware).extensions
        val target = extensions.findByName("android") ?: extensions.findByName("androidLibrary")
        return checkNotNull(target as? KotlinMultiplatformAndroidLibraryTarget) {
            "com.android.kotlin.multiplatform.library didn't register its Android target on the kotlin extension"
        }
    }

    /** `:core:foundation` → `com.naveenapps.expensemanager.core.foundation`, like the Android modules. */
    private fun Project.namespaceFromPath(): String = "com.naveenapps.expensemanager" + path.replace(':', '.').replace('-', '_')
}
