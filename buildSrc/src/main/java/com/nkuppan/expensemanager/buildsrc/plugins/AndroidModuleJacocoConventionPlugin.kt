package com.nkuppan.expensemanager.buildsrc.plugins

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.nkuppan.expensemanager.buildsrc.extension.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidModuleJacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.gradle.jacoco")
            }

            val extension = extensions.getByType<LibraryAndroidComponentsExtension>()
            configureJacoco(extension)
        }
    }
}