package kr.co.minary.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class MinaryAndroidTestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        configureTestingDependenciesWhenPluginsAreReady()

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            jvmArgs(
                "-XX:+EnableDynamicAgentLoading",
                "-Xshare:off",
            )
        }
    }
}

private fun Project.configureTestingDependenciesWhenPluginsAreReady() {
    pluginManager.withPlugin("java") { configureUnitTestDependenciesOnce() }
    pluginManager.withPlugin("java-library") { configureUnitTestDependenciesOnce() }
    pluginManager.withPlugin("com.android.library") {
        configureAndroidTestingPackagingForLibrary()
        configureUnitTestDependenciesOnce()
        configureAndroidTestingDependenciesOnce()
    }
    pluginManager.withPlugin("com.android.application") {
        configureAndroidTestingPackagingForApplication()
        configureUnitTestDependenciesOnce()
        configureAndroidTestingDependenciesOnce()
    }
}

private fun Project.configureUnitTestDependenciesOnce() {
    if (extensions.extraProperties.has("minary.unitTestDependenciesConfigured")) return

    extensions.extraProperties.set("minary.unitTestDependenciesConfigured", true)
    dependencies {
        testImplementation(libs.findLibrary("junit-jupiter").get())
        testImplementation(libs.findLibrary("kotlinx-coroutines-test").get())
        testImplementation(libs.findLibrary("mockk").get())
        testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
    }
}

private fun Project.configureAndroidTestingDependenciesOnce() {
    if (extensions.extraProperties.has("minary.androidTestDependenciesConfigured")) return

    extensions.extraProperties.set("minary.androidTestDependenciesConfigured", true)
    configureAndroidTestDependencies()
}

private fun Project.configureAndroidTestDependencies() {
    dependencies {
        "androidTestImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
        "androidTestImplementation"(libs.findLibrary("mockk-android").get())
    }
}

private fun Project.configureAndroidTestingPackagingForLibrary() {
    extensions.configure<LibraryExtension> {
        packaging {
            resources {
                excludes += junitResourceExcludes
            }
        }
    }
}

private fun Project.configureAndroidTestingPackagingForApplication() {
    extensions.configure<ApplicationExtension> {
        packaging {
            resources {
                excludes += junitResourceExcludes
            }
        }
    }
}

private val junitResourceExcludes = setOf(
    "META-INF/LICENSE.md",
    "META-INF/LICENSE-notice.md",
)
