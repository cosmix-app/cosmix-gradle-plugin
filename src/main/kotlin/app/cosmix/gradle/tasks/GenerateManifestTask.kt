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

import app.cosmix.gradle.entities.PluginManifest
import app.cosmix.gradle.utils.network.FaviconFetcher
import groovy.json.JsonBuilder
import groovy.json.JsonGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional

abstract class GenerateManifestTask : DefaultTask() {

    @get:InputFile
    @get:SkipWhenEmpty
    abstract val pluginClassFile: RegularFileProperty

    @get:Input
    abstract val pluginName: Property<String>

    @get:Input
    abstract val pluginVersion: Property<Int>

    @get:Input
    abstract val requiresResources: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val iconUrl: Property<String>

    @get:Input
    @get:Optional
    abstract val lang: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        var finalIconUrl = iconUrl.orNull
        if (finalIconUrl.isNullOrBlank()) {
            val mainUrl = extractMainUrl()
            if (mainUrl != null) {
                finalIconUrl = FaviconFetcher.fetchFavicon(mainUrl)
            }
            if (finalIconUrl == null) finalIconUrl = ""
        }

        val manifest = PluginManifest(
            name = pluginName.get(),
            version = pluginVersion.get().toString(),
            iconUrl = finalIconUrl,
            lang = lang.getOrElse("en"),
            hasMovies = true,
            hasSeries = true,
            hasAnime = false,
            hasLiveTV = false,
            minCosmixVersion = 1,
            pluginClassName = pluginClassFile.get().asFile.readText()
        )

        outputFile.asFile.get().writeText(
            JsonBuilder(
                manifest,
                JsonGenerator.Options().excludeNulls().build()
            ).toString()
        )
    }

    private fun getProjectDir(): java.io.File {
        // outputFile is <projectDir>/build/intermediates/manifest.json
        // Navigate up 3 levels to get the project directory without calling Task.project
        return outputFile.get().asFile.parentFile.parentFile.parentFile
    }

    private fun extractMainUrl(): String? {
        val projDir = getProjectDir()
        val buildFile = java.io.File(projDir, "build.gradle.kts")
        if (buildFile.exists()) {
            val match = Regex("""mainUrl\s*=\s*"([^"]+)"""").find(buildFile.readText())
            if (match != null) return match.groupValues[1]
        }
        
        val srcDir = java.io.File(projDir, "src/main")
        if (srcDir.exists()) {
            val ktFiles = srcDir.walkTopDown().filter { it.isFile && it.extension == "kt" }
            for (file in ktFiles) {
                val match = Regex("""mainUrl\s*=\s*"([^"]+)"""").find(file.readText())
                if (match != null) return match.groupValues[1]
            }
        }
        return null
    }
}
