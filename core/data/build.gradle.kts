plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.hilt")
    id("naveenapps.plugin.room")
}

android {
    namespace = "com.naveenapps.expensemanager.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(project(":core:database"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.joda.time)

    implementation(libs.opencsv)

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preference)

    androidTestImplementation(project(":core:testing"))
    testImplementation(project(":core:testing"))
}