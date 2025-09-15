package dev.ian.gradle.liquibase.config

import dev.ian.gradle.liquibase.constants.LiquibaseConstants
import dev.ian.gradle.liquibase.tasks.GenerateMigrationTask
import dev.ian.gradle.liquibase.tasks.InitMigrationTask
import dev.ian.gradle.liquibase.utils.LiquibaseProjectContext
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.liquibase.gradle.LiquibaseExtension

object LiquibaseConfig {
    fun configureLiquibase(project: Project) {
        project.plugins.withId("org.liquibase.gradle") {
            configureLiquibaseTasks(project)
        }
    }

    private fun configureLiquibaseTasks(project: Project) {
        val context = LiquibaseProjectContext.create(project)

        registerLiquibaseActivity(project, context)
        createInitMigrationTask(project, context)
        createGenerateMigrationTask(project, context)
    }

    private fun registerLiquibaseActivity(
        project: Project,
        context: LiquibaseProjectContext,
    ) {
        project.extensions.configure<LiquibaseExtension> {
            activities.register(LiquibaseConstants.ACTIVITY_NAME) {
                arguments =
                    mapOf(
                        "changeLogFile" to context.pathResolver.getChangeLogFilePath(),
                        "url" to context.dbProps.url,
                        "username" to context.dbProps.username,
                        "password" to context.dbProps.password,
                        "driver" to context.dbProps.driver,
                        "logLevel" to LiquibaseConstants.LOG_LEVEL,
                        "verbose" to LiquibaseConstants.VERBOSE,
                        "searchPath" to context.pathResolver.getSearchPaths(),
                    )
            }
        }
    }

    private fun createInitMigrationTask(
        project: Project,
        context: LiquibaseProjectContext,
    ) {
        project.tasks.register<InitMigrationTask>("initMigration") {
            dependsOn("compileKotlin")

            group = "liquibase"
            description = "Initialize Liquibase migration"
            this.targetModule = project.findProperty("module") as String?
            this.migrationsPath = context.pathResolver.getMigrationsAbsolutePath()
        }
    }

    private fun createGenerateMigrationTask(
        project: Project,
        context: LiquibaseProjectContext,
    ) {
        val timestamp = System.currentTimeMillis()
        val description =
            (project.findProperty(LiquibaseConstants.DESC_PROPERTY) as String?)
                ?: LiquibaseConstants.DEFAULT_DESC
        val migrationFileName = context.pathResolver.getMigrationOutputFileName(timestamp, description)

        project.tasks.register<GenerateMigrationTask>("generateMigration") {
            dependsOn("compileKotlin")
            dependsOn(project.tasks.named("classes"), project.tasks.named("initMigration"))

            group = "liquibase"
            this.description = "Generate Liquibase migration from Hibernate entities"
            this.liquibaseClasspath = project.configurations.getByName("liquibaseRuntime")
            this.entityPackage = LiquibaseConstants.ENTITY_PACKAGE
            this.targetModule = project.findProperty("module") as String?
            this.dbProps = context.dbProps
            this.changelogOutput = context.pathResolver.getMigrationOutputFileAbsolutePath(migrationFileName)
        }
    }
}
