plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.zionhuang.music"
    compileSdk = 33
    buildToolsVersion = "30.0.3"
    defaultConfig {
        applicationId = "com.zionhuang.music"
        minSdk = 24
        targetSdk = 32
        versionCode = 14
        versionName = "0.4.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "app_name", "InnerTune")
        }
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "InnerTune Debug")
        }
    }
    signingConfigs {
        getByName("debug") {
            if (System.getenv("MUSIC_DEBUG_SIGNING_STORE_PASSWORD") != null) {
                val tmpFilePath = System.getProperty("user.home") + "/work/_temp/Key/"
                val allFilesFromDir = File(tmpFilePath).listFiles()
                val keystoreFile = allFilesFromDir?.first()
                storeFile = keystoreFile ?: file(System.getenv("MUSIC_DEBUG_KEYSTORE_FILE"))
                storePassword = System.getenv("MUSIC_DEBUG_SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("MUSIC_DEBUG_SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("MUSIC_DEBUG_SIGNING_KEY_PASSWORD")
            }
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packagingOptions {
        resources {
            excludes += listOf("META-INF/proguard/androidx-annotations.pro", "META-INF/DEPENDENCIES")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=kotlin.RequiresOptIn")
    }
    configurations.all {
        resolutionStrategy {
            exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.activity)
    implementation(libs.navigation)

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.animation)
    implementation(libs.compose.animation.graphics)

    implementation(libs.viewmodel)
    implementation(libs.viewmodel.compose)

    implementation(libs.material3)
    implementation(projects.materialColorUtilities)

    implementation(libs.coil)

    implementation(libs.shimmer)

    implementation(libs.palette)

    implementation(libs.exoplayer)
    implementation(libs.exoplayer.mediasession)
    implementation(libs.exoplayer.okhttp)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(projects.innertube)
    implementation(projects.kugou)

    implementation(libs.apache.lang3)

    coreLibraryDesugaring(libs.desugaring)
}
