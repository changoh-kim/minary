plugins {
    id("minary.kotlin.library")
    id("minary.android.testing")
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.paging.common)

    implementation(libs.kotlin.result.coroutines)

    // unit test
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
