package dev.ian.gradle.liquibase.utils

import dev.ian.gradle.liquibase.constants.LiquibaseConstants
import org.gradle.api.Project

data class LiquibaseProjectContext(
    val pathResolver: LiquibasePathResolver,
    val dbProps: DbProps,
    val entityPackage: String,
) {
    companion object {
        fun create(project: Project): LiquibaseProjectContext {
            val pathResolver = LiquibasePathResolver(project)
            val dbProps = DbPropsLoader.load(project)
            val entityPackage =
                project.findProperty(LiquibaseConstants.LIQUIBASE_ENTITY_PACKAGE_PROPERTY) as String?
                    ?: "${LiquibaseConstants.DEFAULT_ENTITY_PACKAGE_PREFIX}.${project.name}.domain.entities"

            return LiquibaseProjectContext(pathResolver, dbProps, entityPackage)
        }
    }
}
