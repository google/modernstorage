import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.agp)
        classpath(libs.kotlin.plugin)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    alias(libs.plugins.spotless)
    alias(libs.plugins.dokka)
    alias(libs.plugins.metalava) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
    outputDirectory.set(rootProject.file("docs/api"))
    failOnWarning.set(true)
}

subprojects {
    apply<com.diffplug.gradle.spotless.SpotlessPlugin>()
    spotless {
        kotlin {
            target("**/*.kt")
            ktlint("0.41.0")
            licenseHeaderFile(rootProject.file("spotless/copyright.txt"))
        }

        groovyGradle {
            target("**/*.gradle")
            greclipse().configFile(rootProject.file("spotless/greclipse.properties"))
            licenseHeaderFile(
                rootProject.file("spotless/copyright.txt"),
                "(buildscript|apply|import|plugins)"
            )
        }
    }

    if (project.hasProperty("POM_ARTIFACT_ID") && project.properties["POM_ARTIFACT_ID"] != "modernstorage-bom") {
        apply<me.tylerbwong.gradle.metalava.plugin.MetalavaPlugin>()

        metalava {
            filename = "api/current.api"
            reportLintsAsErrors = true
        }
    }
}

// Extension function due to metalava not having proper Kotlin DSL
fun Project.metalava(configure: Action<me.tylerbwong.gradle.metalava.extension.MetalavaExtension>): Unit =
    (this as ExtensionAware).extensions.configure("metalava", configure)
