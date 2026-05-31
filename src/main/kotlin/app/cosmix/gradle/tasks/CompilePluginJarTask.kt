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

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CompilePluginJarTask : DefaultTask() {

    @get:Input
    abstract val hasCrossPlatformSupport: Property<Boolean>

    @get:InputFile
    abstract val pluginClassFile: RegularFileProperty

    @get:InputFile
    abstract val jarInputFile: RegularFileProperty

    @get:OutputFile
    abstract val targetJarFile: RegularFileProperty

    @TaskAction
    fun compileJar() {

        val jarFile = jarInputFile.get().asFile
        val targetFile = targetJarFile.get().asFile

        jarFile.copyTo(targetFile, overwrite = true)
        logger.lifecycle("Made Cosmix cross-platform package at ${targetFile.absolutePath}")
    }
}
