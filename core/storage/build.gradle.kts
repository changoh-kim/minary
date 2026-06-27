plugins {
    id("minary.android.library")
    id("minary.android.testing")
}

android {
    namespace = "kr.co.core.storage"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.hilt.android)
}
