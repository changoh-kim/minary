package kr.co.minary.buildlogic

import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class MinaryAndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("androidx.room")

        extensions.configure<RoomExtension> {
            schemaDirectory("$projectDir/schemas")
        }

        dependencies {
            api(libs.findLibrary("androidx-room-runtime").get())
            api(libs.findLibrary("androidx-room-ktx").get())
            ksp(libs.findLibrary("androidx-room-compiler").get())
        }
    }
}
