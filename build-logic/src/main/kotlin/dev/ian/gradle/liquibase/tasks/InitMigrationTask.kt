package dev.ian.gradle.liquibase.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class InitMigrationTask : DefaultTask() {
    @Input
    var mainProject: Boolean = false

    @Input
    lateinit var changeLogPath: String

    @Input
    lateinit var migrationsPath: String

    @TaskAction
    fun execute() {
        if (!mainProject) {
            logger.lifecycle("initMigration for module='${project.name}'")
            val migrationsDir = File(migrationsPath)
            migrationsDir.mkdirs()
        }

        val changelog = File(changeLogPath)
        if (!changelog.exists()) {
            changelog.writeText("databaseChangeLog:\n")
            logger.lifecycle("Created module changelog: ${changelog.absolutePath}")
        } else {
            logger.lifecycle("Module changelog already exists: ${changelog.absolutePath}")
        }
    }
}
