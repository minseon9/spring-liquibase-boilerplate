package dev.ian.gradle.liquibase.tasks

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.io.File

object MigrationPathAppender {
    fun appendIncludeAll(
        changelogPath: String,
        modulePath: String,
        logger: Logger,
    ) {
        try {
            val file = File(changelogPath)
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.writeText("databaseChangeLog:\n")
            }

            val yaml = Yaml(SafeConstructor(LoaderOptions().apply { isAllowDuplicateKeys = false }))
            val content = file.readText().ifBlank { "databaseChangeLog:\n" }
            val root = (yaml.load<Any>(content) as? Map<*, *>)?.toMutableMap() ?: mutableMapOf()

            val list = when (val v = root["databaseChangeLog"]) {
                is List<*> -> v.toMutableList()
                else -> mutableListOf()
            }

            val includeAllMap = mapOf(
                "includeAll" to mapOf(
                    "path" to modulePath,
                    "relativeToChangelogFile" to false,
                    "errorIfMissingOrEmpty" to false
                )
            )

            val already = list.any { item ->
                (item as? Map<*, *>)
                    ?.get("includeAll")
                    ?.let { incAll ->
                        (incAll as? Map<*, *>)?.get("path") == modulePath
                    } == true
            }

            if (!already) {
                list.add(includeAllMap)
            }

            val newRoot = mutableMapOf<String, Any>("databaseChangeLog" to list)
            val dumpOpt = DumperOptions().apply {
                defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                isPrettyFlow = true
            }

            val dumper = Yaml(dumpOpt)
            file.writeText(dumper.dump(newRoot))
            logger.lifecycle("Ensured includeAll for '$modulePath' in ${file.path}")
        } catch (e: Exception) {
            logger.error("Changelog includeAll 추가 실패: ${e.message}")
            throw GradleException("Changelog includeAll 추가 실패", e)
        }
    }
}