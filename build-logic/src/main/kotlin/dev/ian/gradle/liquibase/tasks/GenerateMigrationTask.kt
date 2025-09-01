package dev.ian.gradle.liquibase.tasks

import dev.ian.gradle.liquibase.utils.DbProps
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class GenerateMigrationTask : DefaultTask() {
    @get:Inject
    abstract val execOps: ExecOperations

    @Input
    lateinit var entityPackage: String

    @Internal
    lateinit var dbProps: DbProps

    @Input
    lateinit var changelogOutput: String

    @Classpath
    lateinit var liquibaseClasspath: FileCollection

    @TaskAction
    fun execute() {
        val referenceUrl =
            "hibernate:spring:$entityPackage" +
                "?dialect=org.hibernate.dialect.PostgreSQLDialect" +
                "&hibernate.physical_naming_strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy" +
                "&hibernate.implicit_naming_strategy=org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy"

        try {
            val result =
                execOps.javaexec {
                    classpath = liquibaseClasspath
                    mainClass.set("liquibase.integration.commandline.Main")
                    args(
                        "--url=${dbProps.url}",
                        "--username=${dbProps.username}",
                        "--password=${dbProps.password}",
                        "--driver=${dbProps.driver}",
                        "--referenceUrl=$referenceUrl",
                        "--changelogFile=$changelogOutput",
                        "--logLevel=INFO",
                        "--verbose=true",
                        "diffChangeLog",
                    )
                    isIgnoreExitValue = true
                }
            result.assertNormalExitValue()
            logger.lifecycle("Liquibase diffChangeLog completed for project: ${project.name}")
        } catch (e: Exception) {
            logger.error("Liquibase diffChangeLog 실행 실패 (project=${project.name}): ${e.message}")
            throw GradleException("Liquibase diffChangeLog 실행 실패", e)
        }
    }
}
