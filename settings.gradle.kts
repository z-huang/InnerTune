enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }

    versionCatalogs {
        create("libs") {
            version("kotlin", "1.7.20")
            plugin("kotlin-serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")

            library("activity", "androidx.activity", "activity-compose").version("1.5.1")
            library("navigation", "androidx.navigation", "navigation-compose").version("2.5.3")
            library("hilt-navigation", "androidx.hilt", "hilt-navigation-compose").version("1.0.0")
            library("datastore", "androidx.datastore", "datastore-preferences").version("1.0.0")

            version("compose-compiler", "1.3.2")
            version("compose", "1.3.0")
            library("compose-runtime", "androidx.compose.runtime", "runtime").versionRef("compose")
            library("compose-foundation", "androidx.compose.foundation", "foundation").versionRef("compose")
            library("compose-ui", "androidx.compose.ui", "ui").versionRef("compose")
            library("compose-ui-util", "androidx.compose.ui", "ui-util").versionRef("compose")
            library("compose-ui-tooling", "androidx.compose.ui", "ui-tooling").versionRef("compose")
            library("compose-animation", "androidx.compose.animation", "animation-graphics").versionRef("compose")
            library("compose-animation-graphics", "androidx.compose.animation", "animation-graphics").versionRef("compose")

            version("lifecycle", "2.5.1")
            library("viewmodel", "androidx.lifecycle", "lifecycle-viewmodel-ktx").versionRef("lifecycle")
            library("viewmodel-compose", "androidx.lifecycle", "lifecycle-viewmodel-compose").versionRef("lifecycle")

            version("material3", "1.1.0-alpha03")
            library("material3", "androidx.compose.material3", "material3").versionRef("material3")
            library("material3-windowsize", "androidx.compose.material3", "material3-window-size-class").versionRef("material3")

            library("coil", "io.coil-kt", "coil-compose").version("2.2.2")

            library("shimmer", "com.valentinilk.shimmer", "compose-shimmer").version("1.0.3")

            library("palette", "androidx.palette", "palette").version("1.0.0")

            version("exoplayer", "2.18.2")
            library("exoplayer", "com.google.android.exoplayer", "exoplayer").versionRef("exoplayer")
            library("exoplayer-mediasession", "com.google.android.exoplayer", "extension-mediasession").versionRef("exoplayer")
            library("exoplayer-okhttp", "com.google.android.exoplayer", "extension-okhttp").versionRef("exoplayer")

            library("paging-runtime", "androidx.paging", "paging-runtime").version("3.1.1")
            library("paging-compose", "androidx.paging", "paging-compose").version("1.0.0-alpha17")

            version("room", "2.5.0")
            library("room-runtime", "androidx.room", "room-runtime").versionRef("room")
            library("room-compiler", "androidx.room", "room-compiler").versionRef("room")
            library("room-ktx", "androidx.room", "room-ktx").versionRef("room")

            library("apache-lang3", "org.apache.commons", "commons-lang3").version("3.12.0")

            version("hilt", "2.44")
            library("hilt", "com.google.dagger", "hilt-android").versionRef("hilt")
            library("hilt-compiler", "com.google.dagger", "hilt-android-compiler").versionRef("hilt")

            version("ktor", "2.2.2")
            library("ktor-client-core", "io.ktor", "ktor-client-core").versionRef("ktor")
            library("ktor-client-okhttp", "io.ktor", "ktor-client-okhttp").versionRef("ktor")
            library("ktor-client-content-negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            library("ktor-client-encoding", "io.ktor", "ktor-client-encoding").versionRef("ktor")
            library("ktor-serialization-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")

            library("brotli", "org.brotli", "dec").version("0.1.2")

            library("opencc4j", "com.github.houbb", "opencc4j").version("1.7.2")

            library("desugaring", "com.android.tools", "desugar_jdk_libs").version("1.1.5")

            library("junit", "junit", "junit").version("4.13.2")

            library("timber", "com.jakewharton.timber", "timber").version("4.7.1")
        }
    }
}

rootProject.name = "InnerTune"
include(":app")
include(":innertube")
include(":kugou")
include(":material-color-utilities")
