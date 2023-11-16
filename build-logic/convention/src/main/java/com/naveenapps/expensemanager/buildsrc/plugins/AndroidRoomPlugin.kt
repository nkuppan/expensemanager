package com.naveenapps.expensemanager.buildsrc.plugins

import com.naveenapps.expensemanager.buildsrc.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
            }
            dependencies {
                add("implementation", libs.findLibrary("room.runtime").get())
                add("implementation", libs.findLibrary("room.ktx").get())
                add("ksp", libs.findLibrary("room.compiler").get())
            }
        }
    }
}