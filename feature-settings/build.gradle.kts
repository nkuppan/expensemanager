plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("nkuppan.plugin.compose")
    id("androidx.navigation.safeargs")
    id("nkuppan.plugin.hilt")
}

android {
    namespace = "com.nkuppan.expensemanager.feature.settings"
}

dependencies {
    implementation(fileTree("libs"))

    implementation(project(":core-ui"))
    implementation(project(":core-common"))
    implementation(project(":data"))
    implementation(project(":core-model"))

    implementation(libs.androidx.work.ktx)
    implementation(libs.google.oss.licenses)
    implementation(libs.google.android.play.review)

    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}