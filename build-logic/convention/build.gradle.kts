import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.naveenapps.expensemanager.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_19.toString()
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.firebase.crashlytics.gradlePlugin)
    compileOnly(libs.firebase.performance.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        create("AndroidLibraryBasicConfigPlugin") {
            id = "naveenapps.plugin.android.library"
            implementationClass =
                "com.naveenapps.expensemanager.buildsrc.plugins.AndroidLibraryBasicConfigPlugin"
        }
        create("AndroidAppBasicConfigPlugin") {
            id = "naveenapps.plugin.android.app"
            implementationClass =
                "com.naveenapps.expensemanager.buildsrc.plugins.AndroidAppBasicConfigPlugin"
        }
        create("AndroidFeatureModuleConfigPlugin") {
            id = "naveenapps.plugin.android.feature"
            implementationClass =
                "com.naveenapps.expensemanager.buildsrc.plugins.AndroidFeatureModuleConfigPlugin"
        }
        create("KotlinBasicConfigPlugin") {
            id = "naveenapps.plugin.kotlin.basic"
            implementationClass =
                "com.naveenapps.expensemanager.buildsrc.plugins.KotlinBasicConfigPlugin"
        }
        create("AndroidHiltPlugin") {
            id = "naveenapps.plugin.hilt"
            implementationClass =
                "com.naveenapps.expensemanager.buildsrc.plugins.AndroidHiltPlugin"
        }
        create("AndroidComposeConfigPlugin") {
            id = "naveenapps.plugin.compose"
            implementationClass =
                "com.naveenapps.expensemanager.buildsrc.plugins.AndroidComposeConfigPlugin"
        }
        create("AndroidRoomPlugin") {
            id = "naveenapps.plugin.room"
            implementationClass =
                "com.naveenapps.expensemanager.buildsrc.plugins.AndroidRoomPlugin"
        }
    }
}
