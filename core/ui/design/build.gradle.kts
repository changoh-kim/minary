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
}
