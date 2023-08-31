package com.nkuppan.expensemanager.buildsrc.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.Lint
import com.android.build.gradle.LibraryExtension
import com.nkuppan.expensemanager.buildsrc.extensions.configureAndroid
import com.nkuppan.expensemanager.buildsrc.extensions.configureAndroidCompose
import com.nkuppan.expensemanager.buildsrc.extensions.configureBuildFeatures
import com.nkuppan.expensemanager.buildsrc.extensions.configureJVM
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
