plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs")
    id("kotlinx-serialization")
    id("dev.rikka.tools.materialthemebuilder")
}

android {
    compileSdk = 32
    buildToolsVersion = "30.0.3"
    defaultConfig {
        applicationId = "com.zionhuang.music"
        minSdk = 24
        targetSdk = 32
        versionCode = 12
        versionName = "0.4.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }
    applicationVariants.all {
        resValue("string", "app_version", versionName)
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }
    signingConfigs {
        getByName("debug") {
            // comment out the following lines to build without a signing key
            val tmpFilePath = System.getProperty("user.home") + "/work/_temp/Key/"
            val allFilesFromDir = File(tmpFilePath).listFiles()
            val keystoreFile = allFilesFromDir?.first()
            storeFile = keystoreFile ?: file(System.getenv("MUSIC_DEBUG_KEYSTORE_FILE"))
            storePassword = System.getenv("MUSIC_DEBUG_SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("MUSIC_DEBUG_SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("MUSIC_DEBUG_SIGNING_KEY_PASSWORD")
        }
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
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

materialThemeBuilder {
    themes {
        for ((name, color) in listOf(
            "Red" to "F44336",
            "Pink" to "E91E63",
            "Purple" to "9C27B0",
            "DeepPurple" to "673AB7",
            "Indigo" to "3F51B5",
            "Blue" to "2196F3",
            "LightBlue" to "03A9F4",
            "Cyan" to "00BCD4",
            "Teal" to "009688",
            "Green" to "4FAF50",
            "LightGreen" to "8BC3A4",
            "Lime" to "CDDC39",
            "Yellow" to "FFEB3B",
            "Amber" to "FFC107",
            "Orange" to "FF9800",
            "DeepOrange" to "FF5722",
            "Brown" to "795548",
            "BlueGrey" to "607D8F",
            "Sakura" to "FF9CA8"
        )) {
            create("Material$name") {
                lightThemeFormat = "ThemeOverlay.Light.%s"
                lightThemeParent = "AppTheme"
                darkThemeFormat = "ThemeOverlay.Dark.%s"
                darkThemeParent = "AppTheme"
                primaryColor = "#$color"
            }
        }
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    // AndroidX
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.5.2")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.navigation:navigation-runtime-ktx:2.5.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.5.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("androidx.transition:transition-ktx:1.4.1")
    implementation("com.google.android.material:material:1.8.0-alpha01")
    // Gson
    implementation("com.google.code.gson:gson:2.9.0")
    // ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer:2.18.1")
    implementation("com.google.android.exoplayer:extension-mediasession:2.18.1")
    implementation("com.google.android.exoplayer:extension-okhttp:2.18.1")
    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.1.1")
    implementation("androidx.test:monitor:1.5.0")
    testImplementation("androidx.paging:paging-common-ktx:3.1.1")
    implementation("androidx.paging:paging-rxjava3:3.1.1")
    // Room
    implementation("androidx.room:room-runtime:2.4.3")
    kapt("androidx.room:room-compiler:2.4.3")
    implementation("androidx.room:room-rxjava3:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")
    implementation("androidx.room:room-paging:2.4.3")
    testImplementation("androidx.room:room-testing:2.4.3")
    // YouTube API
    implementation(project(mapOf("path" to ":innertube")))
    // Apache Utils
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    // Coil
    implementation("io.coil-kt:coil:2.2.1")
    // Fast Scroll
    implementation("me.zhanghai.android.fastscroll:library:1.1.8")
    // Markdown
    implementation("org.commonmark:commonmark:0.18.2")
    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("android.arch.core:core-testing:1.1.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito:mockito-inline:4.3.1")
    testImplementation("org.mockito:mockito-android:4.3.1")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}
