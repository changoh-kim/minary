plugins {
    id("minary.android.library")
    id("minary.android.testing")
    alias(libs.plugins.protobuf)
}

android {
    namespace = "kr.co.core.datastore"
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

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
}
