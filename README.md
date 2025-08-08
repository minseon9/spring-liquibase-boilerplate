## spring-liquibase workflow

This repository is a template/sample to use Liquibase consistently in a Gradle multi-project. Custom Gradle tasks handle changelog initialization, migration generation from entity diffs, and automatic include management.

### Versions

- Gradle: 8.14.x
- Spring Boot: 3.5.x (see root `build.gradle.kts`)
- Kotlin: 2.0.x
- Liquibase Gradle Plugin: 2.2.2
- Liquibase Hibernate: 4.33.0
- Java: 17

---

## Project structure

- Main (aggregate) module: `:main`
- Example module: `:example`

Each module keeps its own changelog:

- `src/main/resources/db/changelog/db.changelog-<module>.yml`
- `src/main/resources/db/changelog/migrations/` (auto-created for non-main modules)

Main aggregate changelog:

- `main/src/main/resources/db/changelog/db.changelog-main.yml`

Generated migration filename convention:

- `<timestamp>-<desc>-changelog-<module>.yml` (e.g., `1754663326307-change-changelog-example.yml`)

Project inclusion:

- `settings.gradle.kts` automatically includes subprojects under the root that contain a `build.gradle.kts` file.

---

## Setup

### 1) Select the main module

Set the main module name in the root `gradle.properties`:

```properties
mainProjectName=main
```

The main module aggregates includes from all enabled modules.

### 2) Enable Liquibase per module (opt-in)

Enable in each module’s `gradle.properties`:

```properties
liquibaseEnabled=true
liquibaseEntityPackage=<entity root package>
```

Examples:

- `main/gradle.properties` → `liquibaseEntityPackage=dev.ian`
- `example/gradle.properties` → `liquibaseEntityPackage=dev.ian.example.domain.entities`

You can also enable temporarily via CLI: `-PliquibaseEnabled=true`.

### 3) Database connection properties

Defaults are read from the root `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/example
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

Override at runtime using JVM system properties:

```bash
./gradlew :example:generateMigration \
  -Dliquibase.url=jdbc:postgresql://localhost:5432/example \
  -Dliquibase.username=postgres \
  -Dliquibase.password=postgres \
  -Dliquibase.driver=org.postgresql.Driver
```

Note: custom tasks do not read environment variables directly. Use system properties as shown above if you need overrides.

---

## How it works

When Liquibase is enabled on a module, the build applies the Liquibase plugin and configures a `main` activity:

- `changeLogFile`: `db.changelog-main.yml` for the main module, or `db.changelog-<module>.yml` for other modules
- `searchPath`: root + module changelog dirs. The main module includes all enabled modules’ changelog directories to support cross-module includes

Custom tasks orchestrate the workflow:

- `initMigration`: creates changelog files and the `migrations` folder (for non-main modules)
- `generateMigration`: runs Liquibase+Hibernate to generate a new migration from your entity package
- `appendMigrationInclude`: appends an include entry for the generated file into the module changelog
- `appendMainInclude`: for non-main modules, appends the module changelog into the main aggregate changelog

All tasks print `[INFO]` and `[ERROR]` logs for clarity.

---

## Quickstart

### 0) Prerequisites to run the example as-is

- PostgreSQL running locally on port 5432
- Database `example` exists
  - e.g., `psql -U postgres -c "CREATE DATABASE example;"`
- Credentials: `postgres / postgres` (update `application.properties` if needed)

### 1) Verify settings

- Root `gradle.properties`: `mainProjectName=main`
- Root `application.properties`: confirm the DB connection above
- `main/gradle.properties`, `example/gradle.properties`: confirm `liquibaseEnabled=true` and `liquibaseEntityPackage`

### 2) Initialize changelog scaffolding (if needed)

```bash
./gradlew :example:initMigration
```

### 3) Generate migration from entities (optional)

```bash
./gradlew :example:generateMigration -Pdesc=init-schema
```

### 4) Apply migrations

Choose one:

```bash
# Aggregate apply from the main module (recommended)
./gradlew :main:update

# Apply only the example module
./gradlew :example:update
```

---

## Command reference

```bash
# List all tasks (root)
./gradlew tasks

# List tasks for a module
./gradlew :example:tasks --all

# Initialize changelog scaffolding
./gradlew :example:initMigration

# Generate migration (with descriptor)
./gradlew :example:generateMigration -Pdesc=add-columns

# Apply from main / module
./gradlew :main:update
./gradlew :example:update

# Dry-run SQL
./gradlew :example:updateSql

# Status / validate
./gradlew :example:status
./gradlew :example:validate
```

System property overrides:

```bash
./gradlew :example:update \
  -Dliquibase.url=jdbc:postgresql://localhost:5432/example \
  -Dliquibase.username=postgres \
  -Dliquibase.password=postgres \
  -Dliquibase.driver=org.postgresql.Driver
```

---

## Troubleshooting

### Liquibase tasks are not visible on my module

- Ensure `liquibaseEnabled=true` in the module’s `gradle.properties` (or pass `-PliquibaseEnabled=true`)
- Ensure `mainProjectName` is set in the root `gradle.properties`

### Connection/authentication issues

- Check the root `application.properties`
- Override via `-Dliquibase.url`, `-Dliquibase.username`, `-Dliquibase.password`, `-Dliquibase.driver`

### Generated file not included

- `appendMigrationInclude` runs automatically as a finalizer of `generateMigration`
- Look for `[INFO] Ensured include ...` in logs

### Include resolution issues

- The main module’s `searchPath` includes all enabled modules’ changelog directories
- Verify relative include paths are correct

---

## End-to-end example

1) Prepare DB

```bash
docker compose up -d 

docker exec spring-liquibase-db psql -U postgres -c "CREATE DATABASE example;"
```

2) Verify settings

```properties
# gradle.properties (root)
mainProjectName=main

# application.properties (root)
spring.datasource.url=jdbc:postgresql://localhost:5432/example
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# example/gradle.properties
liquibaseEnabled=true
liquibaseEntityPackage=dev.ian.example.domain.entities
```

3) (Optional) Generate migration

```bash
./gradlew :example:generateMigration -Pdesc=init-schema
```

4) Apply

```bash
./gradlew :main:update
```
