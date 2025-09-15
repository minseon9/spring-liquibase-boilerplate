plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false

    id("dev.ian.gradle.liquibase-convention")
}

allprojects {
    group = "dev.ian"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    apply(plugin = "kotlin")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "kotlin-spring")
    apply(plugin = "kotlin-jpa")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        enabled = false
    }

    tasks.getByName<Jar>("jar") {
        enabled = true
    }

    val libs = rootProject.libs
    dependencies {
        add("implementation", libs.bundles.kotlin.basic)
        add("implementation", libs.bundles.spring.boot.core)
        add("implementation", libs.bundles.spring.boot.data)
        add("runtimeOnly", libs.postgresql)
    }
}
