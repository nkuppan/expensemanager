package com.naveenapps.expensemanager.buildsrc.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryBuildFeatures
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

const val TARGET_SDK = 34
const val COMPILE_SDK: Int = 34
const val MIN_SDK = 21

const val VERSION_CODE = 22
const val VERSION_NAME = "1.2.2"

const val BENCHMARK_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

fun LibraryExtension.configureAndroid() {
    namespace = "com.naveenapps.expensemanager"

    defaultConfig.targetSdk = TARGET_SDK

    defaultConfig {
        compileSdk = COMPILE_SDK
        minSdk = MIN_SDK
        testInstrumentationRunner = BENCHMARK_RUNNER
    }

    packaging {
        resources {
            excludes.add("**/attach_hotspot_windows.dll")
            excludes.add("META-INF/licenses/**")
            excludes.add("META-INF/AL2.0")
            excludes.add("META-INF/LGPL2.1")
        }
    }
}

fun ApplicationExtension.configureAndroid() {
    namespace = "com.naveenapps.expensemanager"

    defaultConfig.targetSdk = TARGET_SDK

    defaultConfig {
        compileSdk = COMPILE_SDK
        minSdk = MIN_SDK
        testInstrumentationRunner = BENCHMARK_RUNNER
    }

    packaging {
        resources {
            excludes.add("**/attach_hotspot_windows.dll")
            excludes.add("META-INF/licenses/**")
            excludes.add("META-INF/AL2.0")
            excludes.add("META-INF/LGPL2.1")
        }
    }
}

fun ApplicationExtension.configureAndroidAppVersion() {
    defaultConfig {
        applicationId = "com.naveenapps.expensemanager"
        versionCode = VERSION_CODE
        versionName = VERSION_NAME
    }
}

fun Project.configureTestOptions(extension: CommonExtension<*, *, *, *, *, *>) {
    extension.apply {
        testOptions {
            unitTests {
                // For Robolectric
                isIncludeAndroidResources = true
            }
        }
    }
}

fun Project.configureAndroidCompose(extension: CommonExtension<*, *, *, *, *, *>) {
    extension.apply {
        buildFeatures {
            compose = true
            buildConfig = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion =
                libs.findVersion("androidxComposeCompiler").get().toString()
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
        }

        testOptions {
            unitTests {
                // For Robolectric
                isIncludeAndroidResources = true
            }
        }
    }
}

fun CommonExtension<*, *, *, *, *, *>.configureBuildFeatures() {
    (buildFeatures as? LibraryBuildFeatures)?.apply {
        dataBinding = true
        viewBinding = true
    }
}
