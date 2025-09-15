package dev.ian.gradle.liquibase.constants

object LiquibaseConstants {
    const val ENTITY_PACKAGE = "dev.ian"
    const val MIGRATIONS_DIR = "src/main/resources/db/changelog/migrations"
    const val SCHEMA_CHANGELOG = "schema/db.changelog.yml"

    const val DATASOURCE_URL_PROPERTY = "DATASOURCE_URL"
    const val DATASOURCE_USERNAME_PROPERTY = "DATASOURCE_USERNAME"
    const val DATASOURCE_PASSWORD_PROPERTY = "DATASOURCE_PASSWORD"
    const val DATASOURCE_DRIVER_CLASS_NAME_PROPERTY = "DATASOURCE_DRIVER_CLASS_NAME"
    const val DESC_PROPERTY = "desc"
    const val DEFAULT_DESC = "change"

    const val ACTIVITY_NAME = "main"
    const val LOG_LEVEL = "DEBUG"
    const val VERBOSE = "true"
}
