plugins {
    id("minary.android.library")
    id("minary.android.hilt")
    id("minary.android.testing")
}

android {
    namespace = "kr.co.core.di"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
