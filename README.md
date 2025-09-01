## spring-liquibase workflow

This repository is a template/sample to use Liquibase consistently in a Gradle multi-project.  
Custom Gradle tasks handle changelog initialization, migration generation from entity diffs, and automatic include management.

### Versions

- Gradle: 8.14.x
- Spring Boot: 3.5.0
- Kotlin: 2.1.21
- Liquibase Gradle Plugin: 2.2.2
- Liquibase Hibernate: 4.33.0
- Java: 21

---

## Project structure

- Main (aggregate) module: `:main`
- Example module: `:example`
- Build logic module: `:build-logic` (Liquibase 커스텀 플러그인)

Each module keeps its own changelog:

- `src/main/resources/db/changelog/db.changelog-<module>.yml`
- `src/main/resources/db/changelog/migrations/` (auto-created for non-main modules)

Main aggregate changelog:

- `main/src/main/resources/db/changelog/db.changelog-main.yml`

Generated migration filename convention:

- `<timestamp>-<desc>-changelog-<module>.yml` (e.g., `1754663326307-change-changelog-example.yml`)

Project inclusion:

- `settings.gradle.kts` automatically includes subprojects under the root that contain a `build.gradle.kts` file.
- Liquibase custom tasks are provided by the `:build-logic` module via the `dev.ian.gradle.liquibase-convention` plugin.

---

## Setup

### 1) Select the main module

Set the **main module name** and **database connection properties** in the root `gradle.properties`:

```properties
MAIN_PROJECT_NAME=main
DATASOURCE_URL=jdbc:postgresql://localhost:5432/
DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=postgres
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

## How it works

When Liquibase is enabled on a module, the build applies the Liquibase plugin via the custom `dev.ian.gradle.liquibase-convention` plugin from the `:build-logic` module and configures a `main` activity:

- `changeLogFile`: `db.changelog-main.yml` for the main module, or `db.changelog-<module>.yml` for other modules
- `searchPath`: root + module changelog dirs. The main module includes all enabled modules' changelog directories to support cross-module includes

Custom tasks orchestrate the workflow:

- `initMigration`: creates changelog files and the `migrations` folder (for non-main modules)
- `generateMigration`: runs Liquibase+Hibernate to generate a new migration from your entity package

The custom Liquibase tasks are implemented in the `:build-logic` module and use Version Catalog (`gradle/libs.versions.toml`) for dependency management.

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
- Ensure `MAIN_PROJECT_NAME` is set in the root `gradle.properties`

### Connection/authentication issues

- Ensure `DATASOURCE_URL`, `DATASOURCE_DRIVER_CLASS_NAME`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD` are set properly in the root `gradle.properties`

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
MAIN_PROJECT_NAME=main
DATASOURCE_URL=jdbc:postgresql://localhost:5432/
DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=postgres

# example/gradle.properties
liquibaseEnabled=true
liquibaseEntityPackage=dev.ian.example.domain.entities
```

3) Generate migration

```bash
# generateMigration also initialize migration change log and generate migration file.
./gradlew :example:generateMigration -Pdesc=init-schema
```

4) Apply

```bash
./gradlew :example:update
```
