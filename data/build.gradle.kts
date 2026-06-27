plugins {
    id("minary.android.library")
    id("minary.android.testing")
    id("minary.android.hilt")
    alias(libs.plugins.serialization)
}

android {
    namespace = "kr.co.data"

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
    implementation(project(":core:di"))
    implementation(project(":core:storage"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:firebase"))
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)

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

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
