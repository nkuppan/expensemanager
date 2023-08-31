plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("nkuppan.plugin.compose")
    id("androidx.navigation.safeargs")
    id("nkuppan.plugin.hilt")
}

android {
    namespace = "com.nkuppan.expensemanager.feature.analysis"
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":core-common"))
    implementation(project(":data"))
    implementation(project(":core-model"))

    implementation(project(":feature-transaction"))

    implementation(libs.mpcharts)

    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}