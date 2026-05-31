/*
 * Cosmix Gradle Plugin
 * Copyright (C) 2026 Cosmix
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.cosmix.gradle.tasks

import com.android.build.gradle.tasks.ProcessLibraryManifest
import app.cosmix.gradle.LibraryExtensionCompat
import app.cosmix.gradle.getCosmix
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

const val TASK_GROUP = "cosmix"

fun registerTasks(project: Project) {
    val extension = project.extensions.getCosmix()
    val intermediatesDir = project.layout.buildDirectory.dir("intermediates")

    if (project.rootProject.tasks.findByName("makePluginsJson") == null) {
        project.rootProject.tasks.register("makePluginsJson", MakePluginsJsonTask::class.java) { task ->
            task.group = TASK_GROUP
            task.outputs.upToDateWhen { false }
            task.outputFile.set(task.project.layout.buildDirectory.file("plugins.json"))
        }
    }

    project.tasks.register("generateSources", GenerateSourcesTask::class.java) { task ->
        task.group = TASK_GROUP
        val apkinfoProvider = project.provider {
            extension.apkinfo ?: error(
                "Task 'generateSources' requires APK info to be configured, " +
                "but none was found. If this project does not use cosmix.jar, " +
                "this task does not apply."
            )
        }

        task.urlPrefix.set(apkinfoProvider.map { it.urlPrefix })
        task.sourcesJarFile.set(project.layout.file(
            project.provider {
                apkinfoProvider.get().cache.resolve("cosmix-sources.jar")
            })
        )
    }

    val pluginClassFile = intermediatesDir.map { it.file("pluginClass") }

    val compileDex = project.tasks.register("compileDex", CompileDexTask::class.java) { task ->
        task.group = TASK_GROUP

        task.pluginClassFile.set(pluginClassFile)
        task.outputFile.set(intermediatesDir.map { dir -> dir.file("classes.dex") })

        val android = LibraryExtensionCompat(project)
        task.minSdk.set(android.minSdk)
        task.bootClasspath.from(android.bootClasspath)

        val kotlinTask = project.tasks.findByName("compileDebugKotlin") as? KotlinCompile
        if (kotlinTask != null) {
            task.dependsOn(kotlinTask)
            task.input.from(kotlinTask.destinationDirectory)
        }
    }

    // resApkFile resolved as a provider at configuration time so it can be
    // referenced in the make task without capturing project at execution time.
    val resApkFile = intermediatesDir.map { it.file("res.apk") }

    val compileResources =
        project.tasks.register("compileResources", CompileResourcesTask::class.java) { task ->
            task.group = TASK_GROUP

            val processManifestTask =
                project.tasks.named("processDebugManifest", ProcessLibraryManifest::class.java)
            task.dependsOn(processManifestTask)

            val android = LibraryExtensionCompat(project)
            task.input.set(android.mainResSrcDir)

            task.manifestFile.set(processManifestTask.flatMap { it.manifestOutputFile })
            task.outputFile.set(resApkFile)

            task.aaptExecutable.set(project.layout.file(project.provider {
                android.sdkDirectory
                    .resolve("build-tools")
                    .resolve(android.buildToolsVersion)
                    .resolve(if (OperatingSystem.current().isWindows) "aapt2.exe" else "aapt2")
            }))

            task.androidJar.set(project.layout.file(project.provider {
                android.sdkDirectory
                    .resolve("platforms")
                    .resolve(android.compileSdk)
                    .resolve("android.jar")
            }))
        }

    val compilePluginJar = project.tasks.register("compilePluginJar", CompilePluginJarTask::class.java) { task ->
        task.group = TASK_GROUP
        task.dependsOn(compileDex) // compileDex creates pluginClass
        task.finalizedBy("ensureJarCompatibility") // Ensure compiled JAR is valid

        val jarTask = project.tasks.named("createFullJarDebug")
        task.dependsOn(jarTask) // Ensure JAR is built before copying

        task.hasCrossPlatformSupport.set(extension.isCrossPlatform)
        task.pluginClassFile.set(pluginClassFile)
        task.jarInputFile.fileProvider(jarTask.map { it.outputs.files.singleFile })
        task.targetJarFile.set(project.layout.buildDirectory.file("${project.name}.jar"))
    }

    project.tasks.register("ensureJarCompatibility", EnsureJarCompatibilityTask::class.java) { task ->
        task.dependsOn(compilePluginJar)
        task.hasCrossPlatformSupport.set(extension.isCrossPlatform)
        if (extension.isCrossPlatform) {
            task.jarFile.set(project.layout.buildDirectory.file("${project.name}.jar"))
            task.doLast {
                task.checkOutput()
            }
        }
    }

    val manifestFile = intermediatesDir.map { it.file("manifest.json") }

    val generateManifest = project.tasks.register("generateManifest", GenerateManifestTask::class.java) { task ->
        task.group = TASK_GROUP
        task.dependsOn(compileDex)

        task.pluginClassFile.set(pluginClassFile)
        task.outputFile.set(manifestFile)

        task.pluginName.set(project.name)
        task.pluginVersion.set(
            project.provider {
                project.version.toString().toIntOrNull(10).also { v ->
                    if (v == null) project.logger.warn(
                        "'${project.version}' is not a valid version in ${project.name}. Use an integer."
                    )
                } ?: -1
            }
        )
        task.requiresResources.set(extension.requiresResources)
        task.iconUrl.set(project.provider { extension.iconUrl })
        task.lang.set(project.provider { extension.language ?: "en" })
    }

    val makeCsx = project.tasks.register("makeCsx", Zip::class.java) { task ->
        task.group = TASK_GROUP
        task.dependsOn(compileDex)
        task.dependsOn(compilePluginJar)

        task.dependsOn(generateManifest)

        task.from(manifestFile)
        task.from(compileDex.flatMap { it.outputFile }) { copySpec ->
            copySpec.rename { "android.dex" }
        }

        task.from(compilePluginJar.flatMap { it.targetJarFile }) { copySpec ->
            copySpec.rename { "desktop.jar" }
        }

        if (extension.requiresResources) {
            task.dependsOn(compileResources)
            task.from(project.zipTree(resApkFile)) { copySpec ->
                copySpec.exclude("AndroidManifest.xml")
            }
        }

        task.isPreserveFileTimestamps = false
        task.archiveBaseName.set(project.name)
        task.archiveExtension.set("csx")
        task.archiveVersion.set("")
        task.destinationDirectory.set(project.layout.buildDirectory)

        task.doLast {
            task.logger.lifecycle("Made Cosmix package at ${task.outputs.files.singleFile}")
        }
    }

    val pluginEntryFile = project.layout.buildDirectory.file("plugin-entry.json")

    val writeCacheEntry = project.tasks.register("writeCacheEntry", WriteCacheEntryTask::class.java) { task ->
        task.group = TASK_GROUP
        task.dependsOn(makeCsx)
        if (extension.isCrossPlatform) task.dependsOn(compilePluginJar)

        task.pluginName.set(project.name)
        task.pluginVersion.set(project.provider {
            project.version.toString().toIntOrNull(10) ?: -1
        })
        task.repoUrl.set(project.provider { extension.repository?.url })
        task.repoRawLink.set(project.provider { extension.repository?.getRawLink("{file}", extension.buildBranch) })
        task.buildBranch.set(project.provider { extension.buildBranch })
        task.status.set(project.provider { extension.status })
        task.authors.set(project.provider { extension.authors })
        task.pluginDescription.set(project.provider { extension.description })
        task.language.set(project.provider { extension.language })
        task.iconUrl.set(project.provider { extension.iconUrl })
        task.apiVersion.set(project.provider { extension.apiVersion })
        task.tvTypes.set(project.provider { extension.tvTypes })

        task.csxFile.set(makeCsx.flatMap { zip ->
            zip.outputs.files.let { project.layout.buildDirectory.file("${project.name}.csx") }
        })
        if (extension.isCrossPlatform) {
            task.jarFile.set(project.layout.buildDirectory.file("${project.name}.jar"))
        }
        task.outputFile.set(pluginEntryFile)
    }

    project.rootProject.tasks.named("makePluginsJson", MakePluginsJsonTask::class.java).configure { task ->
        task.dependsOn(writeCacheEntry)
        task.pluginEntryFiles.from(pluginEntryFile)
    }

    project.tasks.register("cleanCache", CleanCacheTask::class.java) { task ->
        task.group = TASK_GROUP
        val apkinfoProvider = project.provider {
            extension.apkinfo ?: error(
                "Cannot clean cache: no cached APK info found. " +
                "This task only applies to projects that depend on cosmix.jar."
            )
        }

        task.jarFile.set(project.layout.file(
            apkinfoProvider.map { it.jarFile }
        ))
    }

    project.tasks.register("deployWithAdb", DeployWithAdbTask::class.java) { task ->
        task.group = TASK_GROUP
        task.dependsOn(makeCsx)

        val android = LibraryExtensionCompat(project)
        task.adbPath.set(android.adb.absolutePath)
        task.pluginFile.set(project.layout.file(
            makeCsx.map { it.outputs.files.singleFile }
        ))
    }
}
