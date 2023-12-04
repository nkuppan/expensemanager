package com.naveenapps.expensemanager.buildsrc.plugins

import com.naveenapps.expensemanager.buildsrc.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("dagger.hilt.android.plugin")
                apply("com.google.devtools.ksp")
            }

            dependencies {
                "implementation"(libs.findLibrary("hilt.android").get())
                "ksp"(libs.findLibrary("hilt.compiler").get())
                "ksp"(libs.findLibrary("hilt.ext.compiler").get())
                "kspAndroidTest"(libs.findLibrary("hilt.compiler").get())
                "kspTest"(libs.findLibrary("hilt.compiler").get())
            }
        }
    }
}
