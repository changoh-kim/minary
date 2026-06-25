plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "kr.co.core.datastore"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin")
            }
        }
    }
}

dependencies {
    implementation(project(":core:storage"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.hilt.android)

    api(libs.androidx.datastore)
    api(libs.androidx.datastore.preferences)
    api(libs.protobuf.kotlin.lite)
}
