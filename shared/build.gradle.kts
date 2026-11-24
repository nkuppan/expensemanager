plugins {
    // Kotlin Multiplatform: Android + iOS (OpenSpec change add-ios-smoke-app).
    id("naveenapps.plugin.kmp.library")
}

// The one framework the iOS app links. It holds only Swift-facing entry points that delegate to
// the shared modules (plain values in and out, no default arguments); no business logic here.
// No Android module depends on it.
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            // Linked into the app binary: nothing to embed or sign at run time.
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:model"))
        }
    }
}
