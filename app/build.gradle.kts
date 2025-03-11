plugins {
    id("com.android.application")
    kotlin("android") version "2.0.0" // Укажите актуальную версию Kotlin
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" // Совместимая версия Compose Compiler
}

android {
    namespace = "com.example.colormatchinggame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.colormatchinggame"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
    // Удалите блок composeOptions, так как kotlinCompilerExtensionVersion больше не требуется
}

dependencies {
    // Другие зависимости вашего проекта
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("com.airbnb.android:lottie:6.0.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.1")
    testImplementation("junit:junit:4.13.2")


    // Зависимости для Android Instrumented Tests:
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
}

