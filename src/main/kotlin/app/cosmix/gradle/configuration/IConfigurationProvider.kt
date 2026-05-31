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

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

interface IConfigurationProvider {
    val name: String

    fun provide(project: Project, dependency: Dependency)
}