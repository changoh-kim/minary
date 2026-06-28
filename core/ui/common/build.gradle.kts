plugins {
    id("minary.android.library")
    id("minary.android.compose")
    id("minary.android.testing")
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "kr.co.core.ui.common"
}

kotlin {
    compilerOptions {
        moduleName.set("ui-common")
    }
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.kotlin.result)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.ui.test.manifest)
}
