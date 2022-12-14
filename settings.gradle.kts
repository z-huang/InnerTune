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

            version("compose-compiler", "1.3.2")
            version("compose", "1.3.0")
            version("material2", "1.0.0-alpha08")
            version("material3", "1.1.0-alpha03")

            library("compose-runtime", "androidx.compose.runtime", "runtime").versionRef("compose")
            library("compose-foundation", "androidx.compose.foundation", "foundation").versionRef("compose")
            library("compose-ui", "androidx.compose.ui", "ui").versionRef("compose")
            library("compose-ui-util", "androidx.compose.ui", "ui-util").versionRef("compose")
            library("compose-ui-tooling", "androidx.compose.ui", "ui-tooling").versionRef("compose")
            library("compose-activity", "androidx.activity", "activity-compose").version("1.5.1")
            library("compose-viewmodel", "androidx.lifecycle", "lifecycle-viewmodel-compose").version("2.5.1")
            library("compose-livedata", "androidx.compose.runtime", "runtime-livedata").versionRef("compose")
            library("compose-navigation", "androidx.navigation", "navigation-compose").version("2.5.3")
            library("compose-animation", "androidx.compose.animation", "animation-graphics").versionRef("compose")
            library("compose-animation-graphics", "androidx.compose.animation", "animation-graphics").versionRef("compose")
            library("compose-material2", "androidx.compose.material", "material").versionRef("compose")
            library("compose-material3", "androidx.compose.material3", "material3").versionRef("material3")
            library("compose-material-windowsize", "androidx.compose.material3", "material3-window-size-class").versionRef("material3")
            library("compose-material-icon-core", "androidx.compose.material", "material-icons-core").versionRef("material2")
            library("compose-material-icon-extended", "androidx.compose.material", "material-icons-extended").versionRef("material2")

            library("compose-shimmer", "com.valentinilk.shimmer", "compose-shimmer").version("1.0.3")

            library("palette", "androidx.palette", "palette").version("1.0.0")
            library("systemUiController", "com.google.accompanist", "accompanist-systemuicontroller").version("0.27.0")

            library("coil", "io.coil-kt", "coil-compose").version("2.2.2")

            library("paging-compose", "androidx.paging", "paging-compose").version("1.0.0-alpha17")
        }
    }
}

rootProject.name = "InnerTune"
include(":app")
include(":innertube")
include(":kugou")
include(":material-theme-builder")
