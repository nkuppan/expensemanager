package com.naveenapps.expensemanager.buildsrc.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.naveenapps.expensemanager.buildsrc.extensions.configureAndroid
import com.naveenapps.expensemanager.buildsrc.extensions.configureAndroidAppVersion
import com.naveenapps.expensemanager.buildsrc.extensions.configureBuildFeatures
import com.naveenapps.expensemanager.buildsrc.extensions.configureJVM
import com.naveenapps.expensemanager.buildsrc.extensions.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class AndroidAppBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("org.gradle.jacoco")
                apply("com.android.application")
            }

            val extension = extensions.getByType<ApplicationAndroidComponentsExtension>()
            configureJacoco(extension)

            extensions.configure<ApplicationExtension> {
                configureJVM()
                configureAndroid()
                configureAndroidAppVersion()
                configureBuildFeatures()
            }
        }
    }
}
