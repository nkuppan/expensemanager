package com.naveenapps.buildsrc.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.naveenapps.buildsrc.extensions.configureAndroid
import com.naveenapps.buildsrc.extensions.configureAndroidAppVersion
import com.naveenapps.buildsrc.extensions.configureJacoco
import com.naveenapps.buildsrc.extensions.configureKotlinAndroid
import com.naveenapps.buildsrc.extensions.configureTestOptions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidAppBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(plugin = "com.android.application")
                apply(plugin = "org.jetbrains.kotlin.android")
                apply(plugin = "jacoco")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureAndroid()
                configureAndroidAppVersion()
                configureTestOptions(this)
                configureJacoco()
            }
        }
    }
}
