plugins {
    id("minary.android.application")
    id("minary.android.testing")
    id("minary.android.hilt")
    alias(libs.plugins.firebase)
    alias(libs.plugins.secrets)
}

android {
    namespace = "kr.co.minary"

    defaultConfig {
        applicationId = "kr.co.minary"
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
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:di"))
    implementation(project(":data"))
    implementation(project(":presentation"))
    implementation(project(":domain"))

    implementation(libs.hilt.work)

    // timber
    implementation(libs.timber)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.config)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)
}
