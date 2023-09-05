plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("nkuppan.plugin.compose")
    id("androidx.navigation.safeargs")
    id("nkuppan.plugin.hilt")
}

android {
    namespace = "com.nkuppan.expensemanager.feature.category"
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":core-common"))
    implementation(project(":core-model"))
    implementation(project(":data"))

    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}