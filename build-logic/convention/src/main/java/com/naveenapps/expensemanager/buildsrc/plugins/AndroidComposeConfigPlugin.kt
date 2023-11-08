package com.naveenapps.expensemanager.buildsrc.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.naveenapps.expensemanager.buildsrc.extensions.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidComposeConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            when {
                pluginManager.hasPlugin("com.android.application") ->
                    configure<ApplicationExtension> {
                        configureAndroidCompose(this)
                    }

                pluginManager.hasPlugin("com.android.library") ->
                    configure<com.android.build.api.dsl.LibraryExtension> {
                        configureAndroidCompose(this)
                    }

                else -> Unit
            }
        }
    }
}
