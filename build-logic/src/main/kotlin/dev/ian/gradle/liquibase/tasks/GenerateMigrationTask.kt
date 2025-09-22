package dev.ian.gradle.liquibase.tasks

import dev.ian.gradle.liquibase.utils.DbProps
import dev.ian.gradle.liquibase.utils.IncludeObjectResolver
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
    var targetModule: String? = null

    @Internal
    lateinit var dbProps: DbProps

    @Input
    var entityPackage: String? = null

    @Input
    lateinit var changelogOutput: String

    @Classpath
    lateinit var liquibaseClasspath: FileCollection

    @TaskAction
    fun execute() {
        if (targetModule == null) {
            logger.error("module name을 입력해야합니다.(e.g. generateMigration -Pmodule=place")
            return
        }
        
        val includeObjects = IncludeObjectResolver.getModuleIncludeObjects(project, targetModule!!)

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
                        "--includeObjects=$includeObjects",
                        "--logLevel=INFO",
                        "--verbose=true",
                        "diffChangeLog",
                    )
                    isIgnoreExitValue = true
                }

            result.assertNormalExitValue()
            logger.lifecycle("Liquibase diffChangeLog completed for module: $targetModule")
        } catch (e: Exception) {
            logger.error("Liquibase diffChangeLog 실행 실패 (module=$targetModule): ${e.message}")
            throw GradleException("Liquibase diffChangeLog 실행 실패", e)
        }
    }
}
