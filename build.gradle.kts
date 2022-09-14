buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
    dependencies {
        classpath(com.nkuppan.expensemanager.buildsrc.Libs.androidGradlePlugin)
        classpath(com.nkuppan.expensemanager.buildsrc.Libs.Kotlin.gradlePlugin)
        classpath(com.nkuppan.expensemanager.buildsrc.Libs.AndroidX.Navigation.safeArgsGradlePlugin)
        classpath(com.nkuppan.expensemanager.buildsrc.Libs.Google.Hilt.androidGradlePlugin)
        classpath(com.nkuppan.expensemanager.buildsrc.Libs.Google.OssLicenses.gradlePlugin)
        classpath(com.nkuppan.expensemanager.buildsrc.Libs.Jacoco.gradle)
    }
}

val kotlinLint: Configuration by configurations.creating

dependencies {
    kotlinLint(com.nkuppan.expensemanager.buildsrc.Libs.ktlint) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

plugins {
    id("com.github.ben-manes.versions").version("0.42.0")
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {

    // optional parameters
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}

val outputDir = "${project.buildDir}/reports/ktlint/"

val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

val kotlinLintCheck by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Check Kotlin code style."
    classpath = kotlinLint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("src/**/*.kt")
}

val kotlinLintFormat by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Fix Kotlin code style deviations."
    classpath = kotlinLint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("-F", "src/**/*.kt")
}