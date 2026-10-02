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
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.benchmark) apply false
    alias(libs.plugins.play.publish) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.distribution) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.dependency.analysis) apply false
}

// Spotless 7+ only drives ktlint 1.x; 0.48.x crashes with an InvocationTargetException.
// Rule tuning (Compose function names etc.) lives in the root .editorconfig.
val ktlintVersion = "1.8.0"

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
    // 0.8.7 predates Java 17 class files; keep in step with the catalog.
    toolVersion = libs.versions.jacoco.get()
}


tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("allDebugCoverage") {

    group = "Reporting"
    description = "Generate overall Jacoco coverage report for the debug build."

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // layout.buildDirectory (Project.buildDir is deprecated in Gradle 9). AGP 9's built-in Kotlin
    // writes classes under intermediates/built_in_kotlinc rather than tmp/kotlin-classes.
    val classDirs = subprojects.flatMap { proj ->
        listOf(
            "intermediates/javac/debug/compileDebugJavaWithJavac/classes",
            "intermediates/javac/debug/classes",
            "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
            "tmp/kotlin-classes/debug",
        ).map { path ->
            proj.layout.buildDirectory.dir(path).map { fileTree(it) { exclude(coverageExclusions) } }
        }
    }

    classDirectories.setFrom(classDirs)

    val sources = subprojects.map { proj ->
        listOf(
            "${proj.projectDir}/src/main/java",
            "${proj.projectDir}/src/main/kotlin",
            "${proj.projectDir}/src/debug/java",
            "${proj.projectDir}/src/debug/kotlin"
        )
    }.flatten()

    sourceDirectories.setFrom(files(sources))

    val executions = subprojects
        .map { proj -> proj.layout.buildDirectory.file("jacoco/testDebugUnitTest.exec").get().asFile }
        .filter { it.exists() }

    executionData.setFrom(files(executions))
}