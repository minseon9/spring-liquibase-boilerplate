package dev.ian.gradle.liquibase.utils

import org.gradle.api.Project

data class LiquibaseProjectContext(
    val pathResolver: LiquibasePathResolver,
    val dbProps: DbProps,
) {
    companion object {
        fun create(project: Project): LiquibaseProjectContext {
            val pathResolver = LiquibasePathResolver(project)
            val dbProps = DbPropsLoader.load(project)

            return LiquibaseProjectContext(pathResolver, dbProps)
        }
    }
}
