package dev.ian.gradle.liquibase.tasks

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.io.File

object MigrationPathAppender {
    fun append(
        changelogPath: String,
        pathToAppend: String,
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

            val list =
                when (val v = root["databaseChangeLog"]) {
                    is List<*> -> v.toMutableList()
                    else -> mutableListOf()
                }

            val includeMap = mapOf("include" to mapOf("file" to pathToAppend))
            val already =
                list.any {
                    (it as? Map<*, *>)
                        ?.get("include")
                        ?.let { inc -> (inc as? Map<*, *>)?.get("file") == pathToAppend } == true
                }
            if (!already) list.add(includeMap)

            val newRoot = mutableMapOf<String, Any>("databaseChangeLog" to list)
            val dumpOpt =
                DumperOptions().apply {
                    defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                    isPrettyFlow = true
                }

            val dumper = Yaml(dumpOpt)
            file.writeText(dumper.dump(newRoot))
            logger.lifecycle("Ensured include for '$pathToAppend' in ${file.path}")
        } catch (e: Exception) {
            logger.error("Changelog include 추가 실패: ${e.message}")
            throw GradleException("Changelog include 추가 실패", e)
        }
    }
}
