import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.nkuppan.expensemanager.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
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
            id = "nkuppan.plugin.android.library"
            implementationClass =
                "com.nkuppan.expensemanager.buildsrc.plugins.AndroidLibraryBasicConfigPlugin"
        }
        create("AndroidAppBasicConfigPlugin") {
            id = "nkuppan.plugin.android.app"
            implementationClass =
                "com.nkuppan.expensemanager.buildsrc.plugins.AndroidAppBasicConfigPlugin"
        }
        create("KotlinBasicConfigPlugin") {
            id = "nkuppan.plugin.kotlin.basic"
            implementationClass =
                "com.nkuppan.expensemanager.buildsrc.plugins.KotlinBasicConfigPlugin"
        }
        create("AndroidHiltPlugin") {
            id = "nkuppan.plugin.hilt"
            implementationClass =
                "com.nkuppan.expensemanager.buildsrc.plugins.AndroidHiltPlugin"
        }
        create("AndroidComposeConfigPlugin") {
            id = "nkuppan.plugin.compose"
            implementationClass =
                "com.nkuppan.expensemanager.buildsrc.plugins.AndroidComposeConfigPlugin"
        }
    }
}
