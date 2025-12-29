plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    // dependencies injection
    implementation(libs.javax.inject)
    // coroutines
    implementation(libs.kotlinx.coroutines.core)
    // paging
    implementation(libs.androidx.paging.common)
}
