plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // kotlin-dsl 플러그인이 Gradle API/Tooling API 제공
    implementation("org.yaml:snakeyaml:2.2")
}
