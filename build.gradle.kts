import org.jfrog.gradle.plugin.artifactory.dsl.*
import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    kotlin("multiplatform") version "1.5.30"
    id("com.jfrog.artifactory") version "4.18.1"
    id("maven-publish")
}

group = "com.dargoz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val frameworkBaseName = "multi-lib"
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }

    }
    js(LEGACY) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    val iosX64 = iosX64()
    val ios32 = iosArm32()
    val ios64 = iosArm64()
    configure(listOf(iosX64, ios32, ios64)) {
        binaries.framework {
            baseName = frameworkBaseName
        }
    }
    // Create a task to build a fat framework.
    tasks.register<FatFrameworkTask>("debugFatFramework") {
        // The fat framework must have the same base name as the initial frameworks.
        baseName = frameworkBaseName
        // The default destination directory is "<build directory>/fat-framework".
        destinationDir = buildDir.resolve("fat-framework/debug")
        // Specify the frameworks to be merged.
        from(
            iosX64.binaries.getFramework("DEBUG"),
            ios32.binaries.getFramework("DEBUG"),
            ios64.binaries.getFramework("DEBUG")
        )
    }

    tasks.register<FatFrameworkTask>("releaseFatFramework") {
        // The fat framework must have the same base name as the initial frameworks.
        baseName = frameworkBaseName
        // The default destination directory is "<build directory>/fat-framework".
        destinationDir = buildDir.resolve("fat-framework/release")
        // Specify the frameworks to be merged.
        from(
            ios32.binaries.getFramework("DEBUG"),
            ios64.binaries.getFramework("DEBUG")
        )
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        val iosX64Main by getting
        val iosX64Test by getting
        val iosArm32Main by getting
        val iosArm32Test by getting
        val iosArm64Main by getting
        val iosArm64Test by getting

    }
}

artifactory {
    setContextUrl("http://localhost:8081/artifactory")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<DoubleDelegateWrapper> {
            setProperty("repoKey", "gradle-dev-local")
            setProperty("username", "admin")
            setProperty("password", "YOUR_PASSWORD")
            setProperty("maven", true)
        })

        defaults(delegateClosureOf<groovy.lang.GroovyObject> {
            invokeMethod(
                "publications", arrayOf(
                    "androidDebug", "androidRelease", "kotlinMultiplatform", "metadata"
                )
            )
        })
    })
}
