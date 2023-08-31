package com.nkuppan.expensemanager.buildsrc.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.nkuppan.expensemanager.buildsrc.extensions.configureAndroid
import com.nkuppan.expensemanager.buildsrc.extensions.configureAndroidAppVersion
import com.nkuppan.expensemanager.buildsrc.extensions.configureAndroidCompose
import com.nkuppan.expensemanager.buildsrc.extensions.configureBuildFeatures
import com.nkuppan.expensemanager.buildsrc.extensions.configureJVM
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidAppBasicConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                configureJVM()
                configureAndroid()
                configureAndroidAppVersion()
                configureBuildFeatures()
            }
        }
    }
}
