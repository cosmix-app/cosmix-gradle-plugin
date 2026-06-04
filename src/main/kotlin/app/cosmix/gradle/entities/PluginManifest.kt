/*
 * Cosmix Gradle Plugin
 * Copyright (C) 2026 Cosmix
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.cosmix.gradle.entities

data class PluginManifest(
    val name: String,
    val version: String,
    val iconUrl: String?,
    val lang: String,
    val hasMovies: Boolean,
    val hasSeries: Boolean,
    val hasAnime: Boolean,
    val hasLiveTV: Boolean,
    val minCosmixVersion: Int,
    val pluginClassName: String
)