plugins {
    `kotlin-dsl`
}

group = "kr.co.minary.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    implementation(libs.hilt.gradle.plugin)
    compileOnly(libs.kotlin.compose.compiler.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.room.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kotlinLibrary") {
            id = "minary.kotlin.library"
            implementationClass = "kr.co.minary.buildlogic.MinaryKotlinLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "minary.android.application"
            implementationClass = "kr.co.minary.buildlogic.MinaryAndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "minary.android.library"
            implementationClass = "kr.co.minary.buildlogic.MinaryAndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "minary.android.compose"
            implementationClass = "kr.co.minary.buildlogic.MinaryAndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "minary.android.hilt"
            implementationClass = "kr.co.minary.buildlogic.MinaryAndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "minary.android.room"
            implementationClass = "kr.co.minary.buildlogic.MinaryAndroidRoomConventionPlugin"
        }
        register("androidTesting") {
            id = "minary.android.testing"
            implementationClass = "kr.co.minary.buildlogic.MinaryAndroidTestingConventionPlugin"
        }
    }
}
