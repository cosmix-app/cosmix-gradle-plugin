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

import app.cosmix.gradle.download
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.net.URI
import javax.inject.Inject

abstract class GenerateSourcesTask : DefaultTask() {

    @get:Inject
    abstract val progressLoggerFactory: ProgressLoggerFactory

    @get:Input
    abstract val urlPrefix: Property<String>

    @get:OutputFile
    abstract val sourcesJarFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val logger = progressLoggerFactory
            .newOperation("Download sources")
            .apply { description = "Download sources" }

        val url = URI("${urlPrefix.get()}/app-sources.jar").toURL()
        url.download(sourcesJarFile.get().asFile, logger)
    }
}
