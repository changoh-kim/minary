plugins {
    id("minary.android.library")
    id("minary.android.compose")
    id("minary.android.testing")
}

android {
    namespace = "kr.co.core.ui.design"
}

kotlin {
    compilerOptions {
        moduleName.set("ui-design")
    }
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.androidx.core.ktx)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
