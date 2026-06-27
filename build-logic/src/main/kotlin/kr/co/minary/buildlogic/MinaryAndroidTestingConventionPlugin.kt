package kr.co.minary.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class MinaryAndroidTestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        configureUnitTestDependenciesOnce()

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            jvmArgs(
                "-XX:+EnableDynamicAgentLoading",
                "-Xshare:off",
            )
        }
    }
}

private fun Project.configureUnitTestDependenciesOnce() {
    var configured = false

    fun configure() {
        if (configured) return
        configured = true
        dependencies {
            testImplementation(libs.findLibrary("junit-jupiter").get())
            testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
        }
    }

    pluginManager.withPlugin("java") { configure() }
    pluginManager.withPlugin("java-library") { configure() }
    pluginManager.withPlugin("com.android.library") { configure() }
    pluginManager.withPlugin("com.android.application") { configure() }
}
