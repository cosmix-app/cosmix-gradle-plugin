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
        val manifest = PluginManifest(
            name = pluginName.get(),
            version = pluginVersion.get().toString(),
            iconUrl = iconUrl.orNull,
            lang = lang.getOrElse("en"),
            hasMovies = true,
            hasSeries = true,
            hasAnime = false,
            hasLiveTV = false,
            minCosmixVersion = 1
        )

        outputFile.asFile.get().writeText(
            JsonBuilder(
                manifest,
                JsonGenerator.Options().excludeNulls().build()
            ).toString()
        )
    }
}
