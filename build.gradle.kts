// Lists all plugins used throughout the project without applying them.
buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath(libs.google.oss.licenses.plugin)
        classpath(libs.org.jacoco.core)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.navigation.safe) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.benchmark) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.play.publish) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.spotless) apply false
}

val ktlintVersion = "0.48.1"

apply<com.diffplug.gradle.spotless.SpotlessPlugin>()

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(ktlintVersion)
        //licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
    }
    format("kts") {
        target("**/*.kts")
        targetExclude("**/build/**/*.kts")
        // Look for the first line that doesn"t have a block comment (assumed to be the license)
        //licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
    }
    format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**/*.xml")
        // Look for the first XML tag that isn"t a comment (<!--) or the xml declaration (<?xml)
        //licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])")
    }
}

val coverageExclusions = listOf(
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

apply<JacocoPlugin>()

configure<JacocoPluginExtension> {
    toolVersion = "0.8.7"
}


tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.create("allDebugCoverage", JacocoReport::class) {

    group = "Reporting"
    description = "Generate overall Jacoco coverage report for the debug build."

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation = File(rootDir, "reports.xml")
    }

    val jClasses: List<String> = subprojects.map { proj ->
        "${proj.buildDir}/intermediates/javac/debug/classes"
    }
    val kClasses: List<String> = subprojects.map { proj ->
        "${proj.buildDir}/tmp/kotlin-classes/debug"
    }
    val javaClasses = jClasses.map { path ->
        fileTree(path) { exclude(coverageExclusions) }
    }
    val kotlinClasses = kClasses.map { path ->
        fileTree(path) { exclude(coverageExclusions) }
    }

    classDirectories.setFrom(files(listOf(javaClasses, kotlinClasses)))

    val sources = subprojects.map { proj ->
        listOf(
            "${proj.projectDir}/src/main/java",
            "${proj.projectDir}/src/main/kotlin",
            "${proj.projectDir}/src/debug/java",
            "${proj.projectDir}/src/debug/kotlin"
        )
    }.flatten()

    sourceDirectories.setFrom(files(sources))

    val executions = subprojects.filter { proj ->
        val path = "${proj.buildDir}/jacoco/testDebugUnitTest.exec"
        File(path).exists()
    }.map { proj ->
        "${proj.buildDir}/jacoco/testDebugUnitTest.exec"
    }

    executionData.setFrom(files(executions))
}