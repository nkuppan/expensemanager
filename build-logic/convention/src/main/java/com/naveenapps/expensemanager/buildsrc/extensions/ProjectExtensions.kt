package com.naveenapps.expensemanager.buildsrc.extensions

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Locale

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

private val coverageExclusions = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    "androidx/**/*.*",
    "**/*\$ViewInjector*.*",
    "**/*Dagger*.*",
    "**/*MembersInjector*.*",
    "**/*_Factory.*",
    "**/*_Provide*Factory*.*",
    "**/*_ViewBinding*.*",
    "**/AutoValue_*.*",
    "**/R2.class",
    "**/R2$*.class",
    "**/*Directions$*",
    "**/*Directions.*",
    "**/*Binding.*"
)

private fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

internal fun Project.configureJacoco() {

    configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
    }

    tasks.create("debugCoverage", JacocoReport::class.java) {
        dependsOn("testDebugUnitTest")
        group = "Reporting"
        description = "Generate Jacoco coverage reports for the debug build."

        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        val jClasses = "${project.buildDir}/intermediates/javac/debug/classes"
        val kClasses = "${project.buildDir}/tmp/kotlin-classes/debug"
        val javaClasses = fileTree(jClasses) { exclude(coverageExclusions) }
        val kotlinClasses = fileTree(kClasses) { exclude(coverageExclusions) }

        classDirectories.setFrom(files(javaClasses, kotlinClasses))

        val sourceDirs = listOf(
            "${project.projectDir}/src/main/java",
            "${project.projectDir}/src/main/kotlin",
            "${project.projectDir}/src/debug/java",
            "${project.projectDir}/src/debug/kotlin"
        )

        sourceDirectories.setFrom(files(sourceDirs))

        executionData.setFrom(
            files(
                listOf("${project.buildDir}/jacoco/testDebugUnitTest.exec")
            )
        )
    }

    tasks.withType<Test>().configureEach {
        configure<JacocoTaskExtension> {
            // Required for JaCoCo + Robolectric
            // https://github.com/robolectric/robolectric/issues/2230
            // TODO: Consider removing if not we don't add Robolectric
            isIncludeNoLocationClasses = true

            // Required for JDK 11 with the above
            // https://github.com/gradle/gradle/issues/5184#issuecomment-391982009
            excludes = listOf("jdk.internal.*")
        }
    }
}
