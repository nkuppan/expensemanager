plugins {
    // Kotlin Multiplatform: Android + iOS (OpenSpec change add-kmp-foundation-module).
    id("naveenapps.plugin.kmp.library")
}

// Pure Kotlin shared by every platform: no Android, no java.*. Android-only helpers stay in core:common.
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Both appear in this module's public API (CoroutineDispatcher, LocalDate, TimeZone).
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
    }
}
