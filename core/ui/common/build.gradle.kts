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
}
