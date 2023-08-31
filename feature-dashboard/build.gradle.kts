plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("nkuppan.plugin.compose")
    id("androidx.navigation.safeargs")
    id("nkuppan.plugin.hilt")
}

android {
    namespace = "com.nkuppan.expensemanager.feature.dashboard"
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":core-model"))
    implementation(project(":data"))

    implementation(project(":feature-account"))
    implementation(project(":feature-analysis"))
    implementation(project(":feature-category"))
    implementation(project(":feature-transaction"))
    implementation(project(":feature-settings"))

    implementation(libs.mpcharts)

    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}