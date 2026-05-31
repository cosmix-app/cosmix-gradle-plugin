/*
 * Cosmix Gradle Plugin
 * Copyright (C) 2026 Cosmix
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.0"
    id("java-gradle-plugin")
    id("maven-publish")
}

group = "com.github.cosmix-app"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        // Disables some unnecessary features
        freeCompilerArgs.addAll(
            listOf(
                "-Xno-call-assertions",
                "-Xno-param-assertions",
                "-Xno-receiver-assertions"
            )
        )

        jvmTarget.set(JvmTarget.JVM_17)  // Required
    }
}

repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib", kotlin.coreLibrariesVersion))
    compileOnly(gradleApi())

    compileOnly("com.google.guava:guava:33.4.8-jre")
    compileOnly("com.android.tools:sdk-common:32.2.1")
    compileOnly("com.android.tools.build:gradle:9.2.1")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")

    implementation("org.ow2.asm:asm:9.10.1")
    implementation("org.ow2.asm:asm-tree:9.10.1")
    implementation("com.github.vidstige:jadb:v1.3.0")
}

gradlePlugin {
    plugins {
        create("app.cosmix.gradle") {
            id = "app.cosmix.gradle"
            implementationClass = "app.cosmix.gradle.CosmixPlugin"
        }
    }
}

publishing {
    repositories {
        mavenLocal()

        val token = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")

        if (token != null) {
            maven {
                credentials {
                    username = "cosmix"
                    password = token
                }
                setUrl("https://maven.pkg.github.com/cosmix/gradle")
            }
        }
    }
}
