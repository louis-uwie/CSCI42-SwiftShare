// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
}

/**
 * Added buildscripts{} Include ALL ADDED project-level dependency repositories here.
 * Then, you are to include them in the app level build.gradle.kts via:
 *
 * dependencies{
 *      implementation( <input dependency key here> )
 *
 *      sample:
 *      implementation("androidx.bluetooth:bluetooth:1.0.0-alpha02") -> this is the bluetooth
 *      }
 */
buildscript{
    repositories {
        google()
        mavenCentral()
    }
}



