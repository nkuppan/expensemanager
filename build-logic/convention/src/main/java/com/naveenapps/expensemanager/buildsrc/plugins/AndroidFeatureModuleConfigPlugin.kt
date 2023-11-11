package com.naveenapps.expensemanager.buildsrc.plugins

import com.android.build.gradle.LibraryExtension
import com.naveenapps.expensemanager.buildsrc.extensions.configureAndroid
import com.naveenapps.expensemanager.buildsrc.extensions.configureBuildFeatures
import com.naveenapps.expensemanager.buildsrc.extensions.configureJVM
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

@SuppressWarnings("unused")
class AndroidFeatureModuleConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureJVM()
                configureAndroid()
                configureBuildFeatures()
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:designsystem"))

                add("testImplementation", project(":core:testing"))
                add("androidTestImplementation", project(":core:testing"))
            }
        }
    }
}
