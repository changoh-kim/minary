import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // ksp
    alias(libs.plugins.ksp)
    // hilt
    alias(libs.plugins.hilt)
    // serialization
    alias(libs.plugins.serialization)
}

android {
    namespace = "kr.co.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":domain"))

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // serialization
    implementation(libs.kotlinx.serialization.json)

    // datastore
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}