rootProject.name = "main"

rootDir
    .listFiles()
    ?.filter {
        it.isDirectory &&
            it.name != "build-logic" &&
            File(it, "build.gradle.kts").exists()
    }?.forEach { include(it.name) }

pluginManagement {
    includeBuild("build-logic")
}
