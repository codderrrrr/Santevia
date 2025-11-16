// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // Add JitPack for top-level too
    }
    dependencies {
        // No extra dependencies needed here if using version catalogs
    }
}
