package com.naveenapps.expensemanager.buildsrc.extensions

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val JAVA_VERSION = JavaVersion.VERSION_17

fun CommonExtension<*, *, *, *, *>.configureJVM() {
    this.compileOptions {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
}

fun Project.configureKotlinJVM() {

    tasks.withType<KotlinCompile>().configureEach {

        kotlinOptions {
            jvmTarget = JAVA_VERSION.toString()

            val warningsAsErrors: String? by project
            allWarningsAsErrors = warningsAsErrors.toBoolean()
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                // Enable experimental coroutines APIs, including Flow
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.Experimental",
                // Enable experimental kotlinx serialization APIs
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }
}