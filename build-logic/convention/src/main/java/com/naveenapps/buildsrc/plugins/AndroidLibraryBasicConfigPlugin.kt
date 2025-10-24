package com.naveenapps.buildsrc.plugins

import com.android.build.gradle.LibraryExtension
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
                apply(plugin = "org.jetbrains.kotlin.android")
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
