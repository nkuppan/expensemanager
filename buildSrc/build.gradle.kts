repositories {
    google()
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

group = "com.nkuppan.expensemanager.buildsrc"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("com.android.tools.build:gradle:7.3.0-rc01")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
    implementation("com.squareup:javapoet:1.13.0")
}

gradlePlugin {
    plugins {
        create("AndroidModuleJacocoConventionPlugin") {
            id = "com.nkuppan.expensemanager.library.jacoco"
            implementationClass = "com.nkuppan.expensemanager.buildsrc.plugins.AndroidModuleJacocoConventionPlugin"
        }
        create("AndroidAppJacocoConventionPlugin") {
            id = "com.nkuppan.expensemanager.app.jacoco"
            implementationClass = "com.nkuppan.expensemanager.buildsrc.plugins.AndroidAppJacocoConventionPlugin"
        }
    }
}