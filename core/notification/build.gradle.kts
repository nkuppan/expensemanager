plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.hilt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.naveenapps.expensemanager.core.notification"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.work.ktx)

    implementation(libs.joda.time)

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preference)
}