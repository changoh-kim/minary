plugins {
    id("minary.kotlin.library")
    id("minary.android.testing")
}

dependencies {
    api(libs.kotlin.result)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
