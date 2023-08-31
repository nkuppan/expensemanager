plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("nkuppan.plugin.hilt")
}

android {
    namespace = "com.nkuppan.expensemanager.data"

    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation(project(":core-common"))
    implementation(project(":core-model"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preference)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    //Core testing source module it will hold the
    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}