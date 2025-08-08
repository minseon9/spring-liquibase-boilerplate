package buildsrc.liquibase

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.util.Properties

data class DbProps(
    val url: String,
    val username: String,
    val password: String,
    val driver: String,
)

object DbPropsLoader {
    fun load(
        project: Project,
        relativePathFromRoot: String = "application.properties",
    ): DbProps {
        val file = File(project.rootProject.projectDir, relativePathFromRoot)
        if (!file.exists()) {
            throw GradleException("DB properties not found: ${file.absolutePath}")
        }

        val props = Properties().apply { file.inputStream().use(::load) }

        val url = System.getProperty("liquibase.url") ?: props.getProperty("spring.datasource.url").orEmpty()
        val username = System.getProperty("liquibase.username") ?: props.getProperty("spring.datasource.username").orEmpty()
        val password = System.getProperty("liquibase.password") ?: props.getProperty("spring.datasource.password").orEmpty()
        val driver =
            System.getProperty("liquibase.driver")
                ?: props.getProperty("spring.datasource.driver-class-name")
                ?: "org.postgresql.Driver"

        if (url.isBlank() || username.isBlank()) {
            throw GradleException("DB properties missing (url/username)")
        }

        return DbProps(url = url, username = username, password = password, driver = driver)
    }
}
