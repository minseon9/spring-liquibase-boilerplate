package dev.ian.gradle.liquibase.utils

import org.gradle.api.Project
import java.io.File
import dev.ian.gradle.liquibase.constants.LiquibaseConstants

class LiquibasePathResolver(
    private val project: Project,
) {
    fun getChangeLogFilePath(): String = LiquibaseConstants.SCHEMA_CHANGELOG

    fun getMigrationsAbsolutePath(): String {
        val module = project.findProperty("module") as String
        return project.rootProject.file("$module/${LiquibaseConstants.MIGRATIONS_DIR}").absolutePath
    }

    fun getMigrationOutputFileAbsolutePath(outputFileName: String): String {
        val migrationsPath = getMigrationsAbsolutePath()
        return File(migrationsPath, outputFileName).absolutePath
    }

    fun getMigrationOutputFileName(
        timestamp: Long,
        description: String,
    ): String {
        val module = project.findProperty("module") as String

        val fileFormat = project.findProperty(LiquibaseConstants.MIGRATION_FILE_FORMAT) as String? ?: "yml"
        return "$timestamp-$description-changelog-$module.$fileFormat"
    }

    fun getSearchPaths(): String = project.rootProject.projectDir.absolutePath
}
