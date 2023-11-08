package com.naveenapps.expensemanager.buildsrc.plugins

import com.naveenapps.expensemanager.buildsrc.extensions.configureKotlinJVM
import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("org.jetbrains.kotlin.android")
                apply("com.google.devtools.ksp")
            }

            configureKotlinJVM()
        }
    }
}
