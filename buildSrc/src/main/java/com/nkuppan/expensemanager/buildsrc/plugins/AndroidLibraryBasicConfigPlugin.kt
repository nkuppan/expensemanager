package com.nkuppan.expensemanager.buildsrc.plugins

import com.android.build.gradle.LibraryExtension
import com.nkuppan.expensemanager.buildsrc.extensions.configureAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureAndroid()
            }
        }
    }
}
