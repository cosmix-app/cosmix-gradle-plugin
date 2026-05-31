/*
 * Cosmix Gradle Plugin
 * Copyright (C) 2026 Cosmix
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.cosmix.gradle.configuration

import app.cosmix.gradle.ApkInfo
import app.cosmix.gradle.download
import app.cosmix.gradle.getCosmix
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.net.URI
import javax.inject.Inject

abstract class CosmixConfigurationProvider : IConfigurationProvider {

    override val name: String
        get() = "cosmix"

    @get:Inject
    abstract val progressLoggerFactory: ProgressLoggerFactory

    override fun provide(project: Project, dependency: Dependency) {
        val extension = project.extensions.getCosmix()
        if (extension.apkinfo == null) {
            extension.apkinfo = ApkInfo(extension, dependency.version ?: "pre-release")
        }

        val apkinfo = extension.apkinfo!!
        apkinfo.cache.mkdirs()
        if (!apkinfo.jarFile.exists()) {
            project.logger.lifecycle("Fetching JAR: ${apkinfo.jarFile.name}")
            val logger = progressLoggerFactory
                .newOperation("Download JAR")
                .apply { description = "Download JAR" }

            val url = URI("${apkinfo.urlPrefix}/classes.jar").toURL()
            url.download(apkinfo.jarFile, logger)
        }

        project.dependencies.add("compileOnly", project.files(apkinfo.jarFile))
    }
}
