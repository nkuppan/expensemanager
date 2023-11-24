plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.hilt")
}

android {
    namespace = "com.naveenapps.expensemanager.core.notification"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:repository"))
    implementation(project(":core:datastore"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.work.ktx)

    implementation(libs.joda.time)

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preference)
    implementation(libs.androidx.hilt.common)
    implementation(libs.hilt.ext.work)
}