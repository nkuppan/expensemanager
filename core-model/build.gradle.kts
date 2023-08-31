plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
}

android {
    namespace = "com.nkuppan.expensemanager.core.model"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}