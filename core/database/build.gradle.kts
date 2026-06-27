plugins {
    id("minary.android.library")
    id("minary.android.room")
    id("minary.android.testing")
}

android {
    namespace = "kr.co.core.database"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:storage"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.hilt.android)
}
