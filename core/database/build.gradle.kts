plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.hilt")
    id("naveenapps.plugin.room")
}

android {
    namespace = "com.naveenapps.expensemanager.core.database"

    defaultConfig {
        // The schemas directory contains a schema file for each version of the Room database.
        // This is required to enable Room auto migrations.
        // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.android)
}