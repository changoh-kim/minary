plugins {
    id("minary.android.library")
    id("minary.android.compose")
    id("minary.android.hilt")
    id("minary.android.testing")
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "kr.co.presentation"

    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"${libs.versions.appVersionName.get()}\"")
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(project(":core:common"))
    implementation(project(":core:ui:common"))
    implementation(project(":core:ui:design"))
    implementation(project(":domain"))
    // hilt & navigation
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)

    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // serialization
    implementation(libs.kotlinx.serialization.json)

    // orbit
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)

    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlin.parcelize.runtime)

    // paging
    implementation(libs.androidx.paging.compose)

    // coroutines
    implementation(libs.kotlinx.coroutines.android)

    // immutable
    implementation(libs.kotlinx.collections.immutable)

    // coil
    implementation(libs.coil.compose)

    // lottie
    implementation(libs.lottie.compose)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
