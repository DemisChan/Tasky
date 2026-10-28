import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use {
            localProperties.load(it)
        }
    }

    val apiKey = System.getenv("API_KEY") ?: localProperties.getProperty("apiKey", "")
    ext["apiKey"] = apiKey
    ext["baseUrl"] = "https://tasky.pl-coding.com/"

    // true = run against in-memory fake repositories (no network / API key needed).
    // Override with USE_FAKE_BACKEND env var or `useFakeBackend=false` in local.properties.
    ext["useFakeBackend"] = System.getenv("USE_FAKE_BACKEND")
        ?: localProperties.getProperty("useFakeBackend", "true")
}
