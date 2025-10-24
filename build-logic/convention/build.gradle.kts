import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.naveenapps.buildlogic"

// Configure the build-logic plugins to target JDK 19
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_19
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
                "com.naveenapps.buildsrc.plugins.AndroidLibraryBasicConfigPlugin"
        }
        create("AndroidAppBasicConfigPlugin") {
            id = "naveenapps.plugin.android.app"
            implementationClass =
                "com.naveenapps.buildsrc.plugins.AndroidAppBasicConfigPlugin"
        }
        create("AndroidFeatureModuleConfigPlugin") {
            id = "naveenapps.plugin.android.feature"
            implementationClass =
                "com.naveenapps.buildsrc.plugins.AndroidFeatureModuleConfigPlugin"
        }
        create("KotlinBasicConfigPlugin") {
            id = "naveenapps.plugin.kotlin.basic"
            implementationClass =
                "com.naveenapps.buildsrc.plugins.KotlinBasicConfigPlugin"
        }
        create("AndroidKoinPlugin") {
            id = "naveenapps.plugin.di"
            implementationClass =
                "com.naveenapps.buildsrc.plugins.AndroidKoinPlugin"
        }
        create("AndroidComposeConfigPlugin") {
            id = "naveenapps.plugin.compose"
            implementationClass =
                "com.naveenapps.buildsrc.plugins.AndroidComposeConfigPlugin"
        }
        create("AndroidRoomPlugin") {
            id = "naveenapps.plugin.room"
            implementationClass =
                "com.naveenapps.buildsrc.plugins.AndroidRoomPlugin"
        }
    }
}
