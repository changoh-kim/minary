plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "kr.co.presentation"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

composeCompiler {
    // 안정성 보고서가 작성될 디렉토리 설정
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")

    // 안정성 구성 파일 등록
    stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file("stability_config.conf")
    )
}

dependencies {
    implementation(project(":domain"))

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

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

    // compose(bom)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // paging
    implementation(libs.androidx.paging.compose)

    // coroutines
    implementation(libs.kotlinx.coroutines.android)

    // immutable
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}