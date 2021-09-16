import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jfrog.gradle.plugin.artifactory.dsl.*

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
    val xcf = XCFramework()
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
    iosX64 {
        binaries {
            framework {
                baseName = frameworkBaseName
                xcf.add(this)
            }
        }
    }
    iosArm32 {
        binaries {
            framework {
                baseName = frameworkBaseName
                xcf.add(this)
            }
        }
    }
    iosArm64 {
        binaries {
            framework {
                baseName = frameworkBaseName
                xcf.add(this)
            }
        }
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
                    "androidDebug", "androidRelease", "ios", "iosArm64", "kotlinMultiplatform", "metadata"
                )
            )
        })
    })
}
