plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // ksp
    alias(libs.plugins.ksp)
    // hilt
    alias(libs.plugins.hilt)
    // firebase
    alias(libs.plugins.firebase)
    // secrets
    alias(libs.plugins.secrets)
}

android {
    namespace = "kr.co.minary"
    compileSdk = 35

    defaultConfig {
        applicationId = "kr.co.minary"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            // 코드 앱 최적화.
            isMinifyEnabled = true

            // 리소스 최적화.
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // 릴리즈 빌드용 debug 사인키
            signingConfig = signingConfigs.getByName("debug")
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
    implementation(project(":data"))
    implementation(project(":presentation"))
    implementation(project(":domain"))

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.work)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
}