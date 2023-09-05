plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("nkuppan.plugin.compose")
    id("androidx.navigation.safeargs")
    id("nkuppan.plugin.hilt")
}

android {
    namespace = "com.nkuppan.expensemanager.feature.transaction"
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":core-common"))
    implementation(project(":data"))
    implementation(project(":core-model"))

    implementation(project(":feature-account"))
    implementation(project(":feature-category"))

    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}