plugins {
    id("minary.android.library")
    id("minary.android.testing")
}

android {
    namespace = "kr.co.core.firebase"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.firebase.config)
    implementation(libs.firebase.storage.ktx)
}
