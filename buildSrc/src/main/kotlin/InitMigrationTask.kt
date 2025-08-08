package buildsrc.liquibase

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class InitMigrationTask : DefaultTask() {
    @TaskAction
    fun run() {
        val mainProjectName = project.rootProject.findProperty("mainProjectName")
        val resourcesDir = File(project.projectDir, "src/main/resources")
        val changelogDir = File(resourcesDir, "db/changelog")
        changelogDir.mkdirs()

        if (project.name != mainProjectName) {
            println("[INFO] initMigration for module='${project.name}', main='${mainProjectName}'")
            val migrationsDir = File(changelogDir, "migrations")
            migrationsDir.mkdirs()
        }

        val projectName =
            if (project.name != mainProjectName) {
                project.name
            } else {
                "main"
            }

        val moduleChangelog = File(changelogDir, "db.changelog-$projectName.yml")
        if (!moduleChangelog.exists()) {
            moduleChangelog.writeText("databaseChangeLog:\n")
            println("[INFO] Created module changelog: ${moduleChangelog.absolutePath}")
        } else {
            println("[INFO] Module changelog already exists: ${moduleChangelog.absolutePath}")
        }
    }
}
