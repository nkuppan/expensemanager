package com.nkuppan.expensemanager.buildsrc.extensions

import com.nkuppan.expensemanager.buildsrc.extensions.configureJVM
import com.nkuppan.expensemanager.buildsrc.extensions.configureKotlinJVM
import com.nkuppan.expensemanager.buildsrc.extensions.configureAndroidAndKotlinJVM

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import com.nkuppan.expensemanager.buildsrc.Versions
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

const val BENCHMARK_RUNNER = "androidx.benchmark.junit4.AndroidBenchmarkRunner"

public fun LibraryExtension.configureAndroid() {

    defaultConfig.targetSdk = Versions.targetSdk

    defaultConfig {
        compileSdk = Versions.compileSdk
        minSdk = Versions.minSdk
        testInstrumentationRunner = BENCHMARK_RUNNER
    }
}

public fun ApplicationExtension.configureAndroid() {

    defaultConfig.targetSdk = Versions.targetSdk

    defaultConfig {
        compileSdk = Versions.compileSdk
        minSdk = Versions.minSdk
        testInstrumentationRunner = BENCHMARK_RUNNER
    }
}