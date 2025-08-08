rootProject.name = "main"

rootDir
    .listFiles()
    ?.filter {
        it.isDirectory &&
            it.name != "buildSrc" &&
            File(it, "build.gradle.kts").exists()
    }?.forEach { include(it.name) }
