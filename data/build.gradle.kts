plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.serialization)
}

android {
    namespace = "kr.co.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

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

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    api(project(":core:common"))
    implementation(project(":core:di"))
    implementation(project(":core:storage"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:firebase"))
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)

    // serialization
    implementation(libs.kotlinx.serialization.json)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.firebase.config)

    // paging
    implementation(libs.androidx.paging.runtime)
    testImplementation(libs.androidx.paging.common)

    // coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlin.result.coroutines)

    // generative ai
    implementation(libs.generative.ai)

    // work
    implementation(libs.androidx.work.runtime.ktx)

    // true-time
    implementation(libs.truetime)

    // exifinterface (image processor)
    implementation(libs.androidx.exifinterface)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
