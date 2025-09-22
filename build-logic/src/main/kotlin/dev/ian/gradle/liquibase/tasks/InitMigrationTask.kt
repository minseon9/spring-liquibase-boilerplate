package dev.ian.gradle.liquibase.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class InitMigrationTask : DefaultTask() {
    @Input
    var targetModule: String? = null

    @Input
    lateinit var migrationsPath: String

    @TaskAction
    fun execute() {
        if (targetModule == null) {
            logger.error("module name을 입력해야합니다.(e.g. generateMigration -Pmodule=place")
            return
        }

        logger.lifecycle("Initializing module '$targetModule' Liquibase structure")

        val migrationsDir = File(migrationsPath)
        migrationsDir.mkdirs()

        val schemaChangelogPath = project.rootProject.file("schema/db.changelog.yml").absolutePath
        val modulePath = "$targetModule/src/main/resources/db/changelog/migrations"

        MigrationPathAppender.appendIncludeAll(schemaChangelogPath, modulePath, logger)

        logger.lifecycle("Added includeAll for module '$targetModule' to schema changelog")
    }
}
