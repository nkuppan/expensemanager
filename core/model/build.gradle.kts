plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
}

android {
    namespace = "com.naveenapps.expensemanager.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    // api: model classes expose kotlinx-datetime types (LocalDate, ...) to every consumer.
    api(libs.kotlinx.datetime)
    implementation(libs.androidx.compose.runtime.annotation)

    // JvmApiInSharedLayer: keeps this module free of JVM/Android-only APIs ahead of KMP.
    lintChecks(project(":lint-rules"))
}