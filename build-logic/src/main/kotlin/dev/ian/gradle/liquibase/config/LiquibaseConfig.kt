package dev.ian.gradle.liquibase.config

import dev.ian.gradle.liquibase.constants.LiquibaseConstants
import dev.ian.gradle.liquibase.tasks.GenerateMigrationTask
import dev.ian.gradle.liquibase.tasks.InitMigrationTask
import dev.ian.gradle.liquibase.tasks.MigrationPathAppender
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
            this.mainProject = context.pathResolver.isMainProject
            this.changeLogPath = context.pathResolver.getChangeLogAbsolutePath()
            this.migrationsPath = context.pathResolver.getMigrationsAbsolutePath()
        }

        if (!context.pathResolver.isMainProject) {
            project.tasks.named("initMigration") {
                doLast {
                    MigrationPathAppender.append(
                        context.pathResolver.mainAggregateChangelog,
                        context.pathResolver.moduleMainChangelog,
                        project.logger,
                    )
                }
            }
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
            liquibaseClasspath = project.configurations.getByName("liquibaseRuntime")
            entityPackage = context.entityPackage
            this.dbProps = context.dbProps
            changelogOutput = context.pathResolver.getMigrationOutputFileAbsolutePath(migrationFileName)

            doLast {
                MigrationPathAppender.append(
                    context.pathResolver.moduleMainChangelog,
                    context.pathResolver.getMigrationOutputFilePath(migrationFileName),
                    project.logger,
                )
            }
        }
    }
}
