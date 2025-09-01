package dev.ian.gradle.liquibase.utils

import dev.ian.gradle.liquibase.constants.LiquibaseConstants
import org.gradle.api.Project

data class DbProps(
    val url: String,
    val username: String,
    val password: String,
    val driver: String,
) {
    init {
        require(url.isNotBlank()) { "url must not be blank" }
        require(username.isNotBlank()) { "username must not be blank" }
        require(password.isNotBlank()) { "password must not be blank" }
        require(driver.isNotBlank()) { "driver must not be blank" }
    }
}

object DbPropsLoader {
    fun load(project: Project): DbProps {
        val url = project.findProperty(LiquibaseConstants.DATASOURCE_URL_PROPERTY)!! as String
        val username = project.findProperty(LiquibaseConstants.DATASOURCE_USERNAME_PROPERTY)!! as String
        val password = project.findProperty(LiquibaseConstants.DATASOURCE_PASSWORD_PROPERTY)!! as String
        val driver = project.findProperty(LiquibaseConstants.DATASOURCE_DRIVER_CLASS_NAME_PROPERTY)!! as String

        return DbProps(url = url, username = username, password = password, driver = driver)
    }
}
