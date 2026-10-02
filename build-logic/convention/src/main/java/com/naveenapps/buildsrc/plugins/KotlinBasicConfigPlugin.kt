package com.naveenapps.buildsrc.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // AGP 9 has built-in Kotlin, so org.jetbrains.kotlin.android must not be applied any
            // more (it fails the build). What's left of "Kotlin basics" is KSP for Koin/Room.
            with(pluginManager) {
                apply("com.google.devtools.ksp")
            }
        }
    }
}
