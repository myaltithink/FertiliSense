plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.fertilisense"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fertilisense"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation("com.firebase:firebase-client-android:2.5.2")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation ("com.itextpdf:itext7-core:7.2.5")
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation("com.squareup.picasso:picasso:2.5.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.google.cloud:google-cloud-dialogflow:0.118.0-alpha")
}