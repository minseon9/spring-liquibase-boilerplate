package dev.ian.gradle.liquibase.constants

object LiquibaseConstants {
    const val CHANGELOG_DIR = "src/main/resources/db/changelog"
    const val MIGRATIONS_DIR = "migrations"

    const val MAIN_PROJECT_NAME_PROPERTY = "MAIN_PROJECT_NAME"

    const val DATASOURCE_URL_PROPERTY = "DATASOURCE_URL"
    const val DATASOURCE_USERNAME_PROPERTY = "DATASOURCE_USERNAME"
    const val DATASOURCE_PASSWORD_PROPERTY = "DATASOURCE_PASSWORD"
    const val DATASOURCE_DRIVER_CLASS_NAME_PROPERTY = "DATASOURCE_DRIVER_CLASS_NAME"
    const val LIQUIBASE_ENABLED_PROPERTY = "liquibaseEnabled"
    const val LIQUIBASE_ENTITY_PACKAGE_PROPERTY = "liquibaseEntityPackage"
    const val DESC_PROPERTY = "desc"

    const val DEFAULT_ENTITY_PACKAGE_PREFIX = "place.tomo"
    const val DEFAULT_DESC = "change"

    const val ACTIVITY_NAME = "main"
    const val LOG_LEVEL = "DEBUG"
    const val VERBOSE = "true"
}
