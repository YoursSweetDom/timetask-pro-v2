import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

// ---------------------------------------------------------
// AUTO-INCREMENT VERSION LOGIC
// ---------------------------------------------------------
val versionPropsFile = file("version.properties")
val versionProps = Properties()

if (versionPropsFile.canRead()) {
    versionProps.load(FileInputStream(versionPropsFile))
} else {
    versionProps["VERSION_CODE"] = "1"
    versionProps["VERSION_NAME"] = "1.0.0"
}

// Increment versionCode
val currentVersionCode = versionProps["VERSION_CODE"].toString().toInt()
val nextVersionCode = currentVersionCode + 1

// Extract and increment versionName patches (e.g., 1.0.5 -> 1.0.6)
val currentVersionName = versionProps["VERSION_NAME"].toString()
val versionNameParts = currentVersionName.split(".")
val nextVersionName = if (versionNameParts.size == 3) {
    val major = versionNameParts[0]
    val minor = versionNameParts[1]
    val patch = versionNameParts[2].toInt() + 1
    "$major.$minor.$patch"
} else {
    currentVersionName // Fallback
}

// Write back to file on every sync/build so the next time it's higher
versionProps["VERSION_CODE"] = nextVersionCode.toString()
versionProps["VERSION_NAME"] = nextVersionName
versionProps.store(FileOutputStream(versionPropsFile), "Auto-Incremented App Version Info")
// ---------------------------------------------------------

android {
    namespace = "com.timetask.pro.v2"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.timetask.pro.v2"
        minSdk = 26
        targetSdk = 33
        versionCode = nextVersionCode
        versionName = nextVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(21)
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Compose Compiler Metrics — включается через Gradle property:
// ./gradlew assembleRelease -Pkotlin.compose.metrics=true
// Результаты: app/build/compose-metrics/


dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    
    // Drag and Drop
    implementation(libs.reorderable)

    // Core
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)

    // Coil
    implementation(libs.coil.compose)

    // WorkManager
    implementation(libs.work.runtime)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Immutable Collections (Compose stability)
    implementation(libs.kotlinx.collections.immutable)

    // Window Size Class (adaptiveness)
    implementation(libs.material3.windowsizeclass)

    // Splash Screen
    implementation(libs.core.splashscreen)

    // Baseline Profile
    implementation(libs.androidx.profileinstaller)

    // Testing
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    debugImplementation(libs.compose.ui.test.manifest)
}