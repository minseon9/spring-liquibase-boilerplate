package dev.ian.gradle.liquibase

import dev.ian.gradle.liquibase.config.LiquibaseConfig
import dev.ian.gradle.liquibase.constants.LiquibaseConstants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getByType

class LiquibaseConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val liquibaseEnabled =
            project
                .findProperty(LiquibaseConstants.LIQUIBASE_ENABLED_PROPERTY)
                ?.toString()
                ?.toBoolean() ?: false

        if (!liquibaseEnabled) {
            project.logger.info("Liquibase is disabled for ${project.name}")
            return
        }

        val libs =
            project.rootProject.extensions
                .getByType<VersionCatalogsExtension>()
                .named("libs")
        project.plugins.apply(
            libs
                .findPlugin("liquibase")
                .get()
                .get()
                .pluginId,
        )

        project.dependencies.apply {
            add("liquibaseRuntime", libs.findLibrary("spring.boot.starter.data.jpa").get())
            add("liquibaseRuntime", libs.findLibrary("liquibase.core").get())
            add("liquibaseRuntime", libs.findLibrary("liquibase.hibernate6").get())
            add("liquibaseRuntime", libs.findLibrary("postgresql").get())
            add("liquibaseRuntime", libs.findLibrary("picocli").get())
            add(
                "liquibaseRuntime",
                project.extensions
                    .getByType<JavaPluginExtension>()
                    .sourceSets
                    .getByName("main")
                    .runtimeClasspath,
            )
        }

        LiquibaseConfig.configureLiquibase(project)
    }
}
