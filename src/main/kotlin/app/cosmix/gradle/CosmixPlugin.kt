/*
 * Cosmix Gradle Plugin
 * Copyright (C) 2026 Cosmix
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.cosmix.gradle

import app.cosmix.gradle.configuration.registerConfigurations
import app.cosmix.gradle.tasks.registerTasks
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class CosmixPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("cosmix", CosmixExtension::class.java, project)
        registerTasks(project)
        registerConfigurations(project)
    }
}
