plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.di")
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

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    // AndroidSQLiteDriver: Room on the driver APIs (OpenSpec change move-database-to-sqlite-driver).
    api(libs.androidx.sqlite.framework)

    // DateConverterTest: plain JVM test pinning the stored date format (KMP groundwork C1).
    testImplementation(libs.junit4)

    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.truth)
}

// MigrationSqlTest (OpenSpec change move-database-to-sqlite-driver): the golden file it compares
// with, and -PrecordMigrationSql=true to re-record it after a deliberate migration change.
tasks.withType<Test>().configureEach {
    systemProperty("migrationSqlGolden", file("src/test/resources/migration-sql.golden.txt").absolutePath)
    systemProperty("recordMigrationSql", providers.gradleProperty("recordMigrationSql").getOrElse("false"))
}
