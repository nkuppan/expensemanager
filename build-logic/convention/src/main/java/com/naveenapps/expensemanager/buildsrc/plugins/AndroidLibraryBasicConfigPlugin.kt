package com.naveenapps.expensemanager.buildsrc.plugins

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.naveenapps.expensemanager.buildsrc.extensions.configureAndroid
import com.naveenapps.expensemanager.buildsrc.extensions.configureBuildFeatures
import com.naveenapps.expensemanager.buildsrc.extensions.configureJVM
import com.naveenapps.expensemanager.buildsrc.extensions.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("org.gradle.jacoco")
                apply("com.android.library")
            }

            val extension = extensions.getByType<LibraryAndroidComponentsExtension>()
            configureJacoco(extension)

            extensions.configure<LibraryExtension> {
                configureJVM()
                configureAndroid()
                configureBuildFeatures()
            }
        }
    }
}
