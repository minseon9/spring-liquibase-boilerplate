package dev.ian.gradle.liquibase

import dev.ian.gradle.liquibase.config.LiquibaseConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getByType

class LiquibaseConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
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

            val targetModule = project.findProperty("module") as String? ?: "main"
            val targetProject = project.rootProject.project(":$targetModule")
            targetProject.plugins.apply("java")
            add(
                "liquibaseRuntime",
                targetProject.extensions
                    .getByType<JavaPluginExtension>()
                    .sourceSets
                    .getByName("main")
                    .runtimeClasspath,
            )
        }

        LiquibaseConfig.configureLiquibase(project)
    }
}
