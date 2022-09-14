package com.nkuppan.expensemanager.buildsrc.plugins

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.nkuppan.expensemanager.buildsrc.extension.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidAppJacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.gradle.jacoco")
            }
            val extension = extensions.getByType<ApplicationAndroidComponentsExtension>()
            configureJacoco(extension)
        }
    }
}