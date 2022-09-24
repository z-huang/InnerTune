// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    val kotlin_version = "1.6.20"
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlin_version")
        classpath("dev.rikka.tools.materialthemebuilder:gradle-plugin:1.3.2")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

tasks.register<Delete>("Clean") {
    delete(rootProject.buildDir)
}
