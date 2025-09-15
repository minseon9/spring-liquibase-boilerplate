dependencies {
    implementation(project(":order"))
    implementation(project(":product"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.bootJar {
    enabled = true
    archiveClassifier = ""
}
