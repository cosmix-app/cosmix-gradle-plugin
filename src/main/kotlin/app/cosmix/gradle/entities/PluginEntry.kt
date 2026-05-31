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

data class PluginEntry(
    val url: String,
    val status: Int,
    val version: Int,
    val name: String,
    val internalName: String,
    val authors: List<String>,
    val description: String?,
    val fileSize: Long?,
    val repositoryUrl: String?,
    val language: String?,
    val tvTypes: List<String>?,
    val iconUrl: String?,
    val apiVersion: Int,
    val fileHash: String?,

    // For cross-platform
    val jarFileSize: Long?,
    val jarUrl: String?,
    val jarHash: String?
)