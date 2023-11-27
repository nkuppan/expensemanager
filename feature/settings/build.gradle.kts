plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.settings"
}

dependencies {
    implementation(project(":feature:export"))
    implementation(project(":feature:datefilter"))
    implementation(project(":feature:reminder"))
    implementation(project(":feature:theme"))
    implementation(project(":feature:about"))

    implementation(libs.google.android.play.review)
    implementation(libs.app.update.ktx)
}
