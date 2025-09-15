## Spring Boot & Liquibase Boilerplate

Gradle 멀티모듈 프로젝트에서 Liquibase를 일관되게 사용하기 위한 템플릿입니다.  
커스텀 Gradle 태스크가 changelog 초기화, 엔티티 diff 기반 마이그레이션 생성, 자동 include 관리를 처리합니다.

### 환경 

- **Gradle**: 8.14.x
- **Spring Boot**: 3.5.0
- **Kotlin**: 2.1.21
- **Liquibase Gradle Plugin**: 2.2.2
- **Liquibase Hibernate**: 4.33.0
- **Java**: 21

### 프로젝트 구조

- **루트 프로젝트**: Liquibase 플러그인으로 마이그레이션을 중앙에서 관리
  - `schema/db.changelog.yml`: 모든 모듈의 마이그레이션을 통합
- **서브모듈**: `main`, `order`, `product` 등 각 도메인별 모듈
  - 각 모듈별 마이그레이션: `{module}/src/main/resources/db/changelog/migrations/`
- **빌드 로직**: `:build-logic` (Liquibase 커스텀 플러그인)
  - JPA 엔티티를 자동으로 스캔하여 `includeObjects` 생성
  - target module의 entity 외의 다른 module의 변경사항은 무시

**마이그레이션 파일 명명 규칙**:
- `<timestamp>-<desc>-changelog-<module>.yml` (예: `1754663326307-change-changelog-example.yml`)
`desc`는 cli에서 -pDesc 옵션으로 받아 사용하며, 기본값으로 `change`을 사용

### 설정

**루트 `gradle.properties`에 데이터베이스 연결 정보 설정**:

```properties
DATASOURCE_URL=jdbc:postgresql://localhost:5432/
DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=postgres
```

### 명령어

**기본 명령어**:
```bash
# 마이그레이션 초기화
./gradlew :{module}:initMigration

# 엔티티 기반 마이그레이션 생성
./gradlew :{module}:generateMigration -Pdesc=설명 -Pmodule={module}

# liquibase 기본 명령어
## 마이그레이션 적용 (중앙 changelog 사용)
./gradlew update

## SQL 미리보기
./gradlew updateSql

## 상태 확인
./gradlew status
```

### 빠른 시작

**1) 데이터베이스 준비**:
```bash
# Docker로 PostgreSQL 실행
docker compose up -d
```

**2) 마이그레이션 생성 및 적용**:
```bash
# 마이그레이션 생성 (엔티티 스캔 → diff 생성)
./gradlew generateMigration -Pmodule=order -Pdesc=init-schema

# 마이그레이션 적용
./gradlew update
```

**3) 전체 워크플로우**:
```bash
# 1. 마이그레이션 초기화
./gradlew initMigration -Pmodule=order

# 2. 엔티티 변경사항을 마이그레이션으로 생성
./gradlew generateMigration -Pmodule=order -Pdesc=add-user-table

# 3. 모든 모듈의 마이그레이션을 중앙에서 적용
./gradlew update
```
