plugins {
    // Kotlin Multiplatform: Android + iOS (OpenSpec change make-core-repository-multiplatform).
    id("naveenapps.plugin.kmp.library")
}

// Repository interfaces shared by every platform. No Android, no java.*: the iOS compile enforces
// it (even calls that need no import, like System.currentTimeMillis()), so no
// shared-layer lint check is needed.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            // api: the interfaces return Flow.
            api(libs.kotlinx.coroutines.core)
        }
    }
}
