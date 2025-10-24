package com.naveenapps.buildsrc.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.android")
                apply("com.google.devtools.ksp")
            }
        }
    }
}
