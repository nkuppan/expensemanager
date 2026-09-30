plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.transaction"
}

dependencies {
    implementation(project(":core:settings"))
    implementation(project(":core:datastore"))

    implementation(project(":feature:category"))
    implementation(project(":feature:account"))
    implementation(project(":feature:filter"))

    implementation(libs.pdfbox.android)
}
