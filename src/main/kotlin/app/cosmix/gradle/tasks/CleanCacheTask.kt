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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class CleanCacheTask : DefaultTask() {

    @get:Internal abstract val jarFile: RegularFileProperty

    @TaskAction
    fun clean() {
        val file = jarFile.asFile.get()
        if (file.exists()) file.delete() else {
            logger.lifecycle("JAR file does not exist; nothing to clean.")
        }
    }
}
