package com.naveenapps.expensemanager.buildsrc.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.naveenapps.expensemanager.buildsrc.extensions.configureAndroid
import com.naveenapps.expensemanager.buildsrc.extensions.configureAndroidAppVersion
import com.naveenapps.expensemanager.buildsrc.extensions.configureBuildFeatures
import com.naveenapps.expensemanager.buildsrc.extensions.configureJVM
import com.naveenapps.expensemanager.buildsrc.extensions.configureJacoco
import com.naveenapps.expensemanager.buildsrc.extensions.configureTestOptions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidAppBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("com.android.application")
                apply("jacoco")
            }

            extensions.configure<ApplicationExtension> {
                configureJVM()
                configureAndroid()
                configureAndroidAppVersion()
                configureBuildFeatures()
                configureTestOptions(this)
                configureJacoco()
            }
        }
    }
}
