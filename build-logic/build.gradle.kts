plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.bundles.build.logic)
}

gradlePlugin {
    plugins {
        create("liquibaseConvention") {
            id = "dev.ian.gradle.liquibase-convention"
            implementationClass = "dev.ian.gradle.liquibase.LiquibaseConventionPlugin"
        }
    }
}
