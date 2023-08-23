package com.nkuppan.expensemanager.buildsrc.plugins

import com.nkuppan.expensemanager.buildsrc.extensions.configureJVM
import com.nkuppan.expensemanager.buildsrc.extensions.configureKotlinJVM

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.kapt")
            }

            configureJVM()

            configureKotlinJVM()
        }
    }
}
