plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.main_androidswiftshare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.main_androidswiftshare"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
//  Added Dependencies as of March 12, 2025 | 1:55PM
    implementation("androidx.bluetooth:bluetooth:1.0.0-alpha02") //BLUETOOTH API - https://developer.android.com/jetpack/androidx/releases/bluetooth#kts


//  Default Dependencies!
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}