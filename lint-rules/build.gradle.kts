import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

// Design system lint checks (see CLAUDE.md "Design system"). Plain JVM module; feature modules and
// :app consume it with lintChecks(project(":lint-rules")) through the feature convention plugin.
plugins {
    `java-library`
    alias(libs.plugins.kotlin)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // Lint loads the checks with its own bundled Kotlin stdlib, which is older than ours.
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
    }
}

dependencies {
    compileOnly(libs.lint.api)

    testImplementation(libs.lint.api)
    testImplementation(libs.lint.tests)
    testImplementation(libs.junit4)
}
