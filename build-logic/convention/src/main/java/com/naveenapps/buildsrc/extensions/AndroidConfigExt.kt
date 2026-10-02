package com.naveenapps.buildsrc.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

const val TARGET_SDK = 37
const val COMPILE_SDK: Int = 37
const val MIN_SDK = 26

const val VERSION_NAME = "1.4.15"
val versions = VERSION_NAME.split(".")
val VERSION_CODE = 1000000 * versions[0].toInt() + 1000 * versions[1].toInt() + versions[2].toInt()

const val BENCHMARK_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

fun LibraryExtension.configureAndroid() {
    // Fallback only — every module sets its own namespace (AGP 9 enforces unique ones).
    namespace = "com.naveenapps.expensemanager"

    compileSdk = COMPILE_SDK

    // AGP 9 removed targetSdk from library defaultConfig; a library has no targetSdk of its own.
    // It only matters for what its tests and lint run against.
    testOptions.targetSdk = TARGET_SDK
    lint.targetSdk = TARGET_SDK

    defaultConfig {
        minSdk = MIN_SDK
        testInstrumentationRunner = BENCHMARK_RUNNER
    }

    packaging {
        resources {
            excludes.add("**/attach_hotspot_windows.dll")
            excludes.add("META-INF/licenses/**")
            excludes.add("META-INF/AL2.0")
            excludes.add("META-INF/LGPL2.1")
            excludes.add("**/com/itextpdf/io/font/cmap_info.txt")
            excludes.add("**/com/itextpdf/io/font/cmap/*")
        }
    }
}

fun ApplicationExtension.configureAndroid() {
    namespace = "com.naveenapps.expensemanager"

    compileSdk = COMPILE_SDK

    defaultConfig {
        targetSdk = TARGET_SDK
        minSdk = MIN_SDK
        testInstrumentationRunner = BENCHMARK_RUNNER
    }

    packaging {
        resources {
            excludes.add("**/attach_hotspot_windows.dll")
            excludes.add("META-INF/licenses/**")
            excludes.add("META-INF/AL2.0")
            excludes.add("META-INF/LGPL2.1")
            excludes.add("**/com/itextpdf/io/font/cmap_info.txt")
            excludes.add("**/com/itextpdf/io/font/cmap/*")
        }
    }
}

fun ApplicationExtension.configureAndroidAppVersion() {
    namespace = "com.naveenapps.expensemanager"
    defaultConfig {
        versionCode = VERSION_CODE
        versionName = VERSION_NAME
    }
}

fun configureTestOptions(extension: CommonExtension) {
    // For Robolectric
    extension.testOptions.unitTests.isIncludeAndroidResources = true
}

fun Project.configureAndroidCompose(extension: CommonExtension) {
    extension.buildFeatures.apply {
        compose = true
        buildConfig = true
    }

    // For Robolectric
    extension.testOptions.unitTests.isIncludeAndroidResources = true

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))
    }
}
