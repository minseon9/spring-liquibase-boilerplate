package dev.ian.gradle.liquibase.utils

import dev.ian.gradle.liquibase.constants.LiquibaseConstants
import org.gradle.api.Project

class LiquibasePathResolver(
    private val project: Project,
) {
    private val mainProjectName: String by lazy {
        project.findProperty(LiquibaseConstants.MAIN_PROJECT_NAME_PROPERTY) as String
    }

    val isMainProject: Boolean by lazy {
        project.name == mainProjectName
    }

    val moduleBasePath: String by lazy {
        LiquibaseConstants.CHANGELOG_DIR
    }

    val moduleMainChangelog: String by lazy {
        "${project.name}/$moduleBasePath/db.changelog-${project.name}.yml"
    }

    val mainAggregateChangelog: String by lazy {
        "$mainProjectName/${LiquibaseConstants.CHANGELOG_DIR}/db.changelog-main.yml"
    }

    fun getMigrationOutputFileName(
        timestamp: Long,
        description: String,
    ): String = "$timestamp-$description-changelog-${project.name}.yml"

    fun getMigrationOutputFilePath(outputFileName: String): String = "${LiquibaseConstants.MIGRATIONS_DIR}/$outputFileName"

    fun getChangeLogFilePath(): String = if (isMainProject) mainAggregateChangelog else moduleMainChangelog

    fun getChangeLogAbsolutePath(): String {
        val changeLogPath = if (isMainProject) mainAggregateChangelog else moduleMainChangelog

        return project.rootProject.file(changeLogPath).absolutePath
    }

    fun getMigrationsAbsolutePath(): String = project.file("$moduleBasePath/${LiquibaseConstants.MIGRATIONS_DIR}").absolutePath

    fun getMigrationOutputFileAbsolutePath(outputFileName: String): String =
        project
            .file(
                "${getMigrationsAbsolutePath()}/$outputFileName",
            ).absolutePath

    /**
     * Liquibase 검색 경로를 계산합니다.
     * 메인 프로젝트인 경우 모든 활성 모듈의 changelog 디렉토리를 포함하고,
     * 그 외는 해당 모듈의 changelog 디렉토리만 포함합니다.
     */
    fun getSearchPaths(): String =
        buildList {
            add(project.rootProject.projectDir.absolutePath)
            if (isMainProject) {
                val enabledModules =
                    project.rootProject.subprojects.filter {
                        (it.findProperty(LiquibaseConstants.LIQUIBASE_ENABLED_PROPERTY) as String?)?.toBoolean() == true
                    }
                addAll(
                    enabledModules.map {
                        it.projectDir.resolve(LiquibaseConstants.CHANGELOG_DIR).absolutePath
                    },
                )
            } else {
                add(project.projectDir.resolve(LiquibaseConstants.CHANGELOG_DIR).absolutePath)
            }
        }.joinToString(",")
}
