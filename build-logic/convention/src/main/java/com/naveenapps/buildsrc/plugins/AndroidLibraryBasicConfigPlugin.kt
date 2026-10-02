package com.naveenapps.buildsrc.plugins

import com.android.build.api.dsl.LibraryExtension
import com.naveenapps.buildsrc.extensions.configureAndroid
import com.naveenapps.buildsrc.extensions.configureJacoco
import com.naveenapps.buildsrc.extensions.configureKotlinAndroid
import com.naveenapps.buildsrc.extensions.configureTestOptions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidLibraryBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(plugin = "com.android.library")
                // No org.jetbrains.kotlin.android: AGP 9 compiles Kotlin itself (built-in Kotlin).
                apply(plugin = "jacoco")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                configureAndroid()
                configureJacoco()
                configureTestOptions(this)
            }
        }
    }
}
