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

fun registerConfigurations(project: Project) {

    val providers = arrayOf(
        project.objects.newInstance(CosmixConfigurationProvider::class.java)
    )

    for (provider in providers) {
        project.configurations.register(provider.name) {
            it.isTransitive = false
        }
    }

    project.afterEvaluate {
        for (provider in providers) {
            val configuration = project.configurations.getByName(provider.name)
            val dependencies = configuration.dependencies
            require(dependencies.size <= 1) {
                "Only one '${provider.name}' dependency should be specified, but ${dependencies.size} were!"
            }

            for (dependency in dependencies) {
                provider.provide(project, dependency)
            }
        }
    }
}
