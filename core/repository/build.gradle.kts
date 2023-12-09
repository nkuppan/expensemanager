plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
}

android {
    namespace = "com.naveenapps.expensemanager.core.repository"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
}