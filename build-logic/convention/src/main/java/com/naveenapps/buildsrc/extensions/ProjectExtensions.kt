package com.naveenapps.buildsrc.extensions

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

private val coverageExclusions = listOf(
    "**/databinding/*Binding.*",
    "**/*DataBinding*",
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    // butterKnife
    "**/*\$ViewInjector*.*",
    "**/*\$ViewBinder*.*",
    "**/Lambda$*.class",
    "**/Lambda.class",
    "**/*Lambda.class",
    "**/*Lambda*.class",
    "**/*_MembersInjector.class",
    "**/Dagger*Component*.*",
    "**/*Module_*Factory.class",
    "**/di/module/*",
    "**/*_Factory*.*",
    "**/*Module*.*",
    "**/*Dagger*.*",
    "**/*Hilt*.*",
    // kotlin
    "**/*MapperImpl*.*",
    "**/*\$ViewInjector*.*",
    "**/*\$ViewBinder*.*",
    "**/BuildConfig.*",
    "**/*Component*.*",
    "**/*BR*.*",
    "**/Manifest*.*",
    "**/*\$Lambda$*.*",
    "**/*Companion*.*",
    "**/*Module*.*",
    "**/*Dagger*.*",
    "**/*Hilt*.*",
    "**/*hilt*",
    "**/*MembersInjector*.*",
    "**/*_MembersInjector.class",
    "**/*_Factory*.*",
    "**/*_Provide*Factory*.*",
    "**/*Extensions*.*",
    "**/*_Impl*.*",
    "**/*.new*",
)

internal fun Project.configureJacoco() {

    configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
    }

    tasks.register<JacocoReport>("debugCoverage") {

        dependsOn("testDebugUnitTest")

        group = "Reporting"

        description = "Generate Jacoco coverage reports for the debug build."

        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        val jClasses = "${buildDir}/intermediates/javac/debug/classes"
        val kClasses = "${buildDir}/tmp/kotlin-classes/debug"
        val javaClasses = fileTree(jClasses) { exclude(coverageExclusions) }
        val kotlinClasses = fileTree(kClasses) { exclude(coverageExclusions) }

        classDirectories.setFrom(files(javaClasses, kotlinClasses))

        val sourceDirs = listOf(
            "${projectDir}/src/main/java",
            "${projectDir}/src/main/kotlin",
            "${projectDir}/src/debug/java",
            "${projectDir}/src/debug/kotlin",
        )

        sourceDirectories.setFrom(files(sourceDirs))

        executionData.setFrom(
            files(
                listOf("${buildDir}/jacoco/testDebugUnitTest.exec"),
            ),
        )
    }

    tasks.withType<Test>().configureEach {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}
