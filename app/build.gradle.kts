plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)

    //cloud storage
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.tasky"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.tasky"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Room Database
    val room_version = "2.8.5"
    implementation("androidx.room:room-runtime:$room_version")

    // Kotlin Extensions (Allows Room to use Coroutines so the app doesn't freeze)
    implementation("androidx.room:room-ktx:$room_version")

    // The KSP compiler that checks your database code for errors
    ksp("androidx.room:room-compiler:$room_version")
    // 👈 Firebase BoM (Bill of Materials keeps all Firebase versions in sync)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // 👈 Firebase Authentication & Cloud Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
}