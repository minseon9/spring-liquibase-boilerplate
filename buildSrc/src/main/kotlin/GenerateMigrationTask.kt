package buildsrc.liquibase

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class GenerateMigrationTask : DefaultTask() {
    @get:Inject
    abstract val execOps: ExecOperations

    @Input
    lateinit var entityPackage: String

    @InputFile
    lateinit var propertiesFile: File

    @OutputFile
    lateinit var changelogOutputFile: File

    @Classpath
    lateinit var liquibaseClasspath: FileCollection

    private fun loadDbProps(): Triple<String, String, String> {
        val relativePath =
            propertiesFile
                .takeIf { it.exists() }
                ?.relativeToOrNull(project.rootProject.projectDir)
                ?.path
                ?: "application.properties"

        val cfg = DbPropsLoader.load(project, relativePath)
        return Triple(cfg.url, cfg.username, cfg.password)
    }

    @TaskAction
    fun run() {
        val (dbUrl, dbUsername, dbPassword) = loadDbProps()
        val output = changelogOutputFile.absolutePath

        try {
            val result =
                execOps.javaexec {
                    classpath = liquibaseClasspath
                    mainClass.set("liquibase.integration.commandline.Main")
                    args(
                        "--url=$dbUrl",
                        "--username=$dbUsername",
                        "--password=$dbPassword",
                        "--driver=${DbPropsLoader.load(project).driver}",
                        "--referenceUrl=hibernate:spring:$entityPackage" +
                            "?dialect=org.hibernate.dialect.PostgreSQLDialect" +
                            "&hibernate.physical_naming_strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy" +
                            "&hibernate.implicit_naming_strategy=org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy",
                        "--changelogFile=$output",
                        "--logLevel=DEBUG",
                        "--verbose=true",
                        "diffChangeLog",
                    )
                    standardOutput = System.out
                    errorOutput = System.err
                    isIgnoreExitValue = true
                }
            result.assertNormalExitValue()
            println("[INFO] Liquibase diffChangeLog completed for project: ${project.name}")
        } catch (e: Exception) {
            println("[ERROR] Liquibase diffChangeLog 실행 실패 (project=${project.name}): ${e.message}")
            throw GradleException("Liquibase diffChangeLog 실행 실패", e)
        }
    }
}
