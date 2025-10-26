package com.naveenapps.buildsrc.plugins

import com.android.build.gradle.LibraryExtension
import com.naveenapps.buildsrc.extensions.configureAndroid
import com.naveenapps.buildsrc.extensions.configureJacoco
import com.naveenapps.buildsrc.extensions.configureKotlinAndroid
import com.naveenapps.buildsrc.extensions.configureTestOptions
import com.naveenapps.buildsrc.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import kotlin.jvm.optionals.getOrNull

@SuppressWarnings("unused")
class AndroidFeatureModuleConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(plugin = "com.android.library")
                apply(plugin = "org.jetbrains.kotlin.android")
                apply(plugin = "jacoco")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                configureAndroid()
                configureJacoco()
                configureTestOptions(this)
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:repository"))
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:navigation"))

                libs.findLibrary("kotlinx.coroutines.android").getOrNull()?.let {
                    add("implementation", it)
                }

                add("testImplementation", project(":core:testing"))
                add("androidTestImplementation", project(":core:testing"))
            }
        }
    }
}
