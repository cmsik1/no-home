# Sprint 1: Spring Boot / Vue 골격과 DB 연결 기준선

## Milestone 연결

- Milestone: M1 - 프로젝트 골격, DB 연결, MyBatis 설정
- 목적: 이후 실거래가, 회원, 지도 기능을 구현할 수 있는 실행 가능한 Spring Boot + MyBatis + Vue/Vite 기준선을 만든다.

## Manager 구현 지시

Generator는 `docs/spec.md`와 이 문서를 기준 계약으로 읽고, Sprint 1 범위만 구현한다.

이번 Sprint에서 구현할 것은 실제 도메인 기능이 아니라 프로젝트 실행 기반이다. Spring Boot 애플리케이션이 Maven / Java 17 기준으로 실행되고, MyBatis가 MySQL 연결을 사용할 수 있는 구조를 갖추며, Vue/Vite 프론트엔드 원본과 최종 정적 빌드 포함 방식이 준비되어야 한다.

프롬프트로 전달되는 지시는 실행 트리거일 뿐이며, 구현 범위의 기준은 이 문서다. 범위 밖 작업이 필요하면 임의 구현하지 말고 "결정 필요 항목" 또는 "남은 리스크"에 기록한다.

## 확정 기술 결정

- Backend: Spring Boot.
- Build: Maven.
- Java: 17.
- Persistence: MyBatis.
- Database: MySQL.
- Frontend: Vue.js + Vite.
- 프로젝트 구조: 루트는 Spring Boot 프로젝트, `frontend/`는 Vue/Vite 원본.
- 최종 실행 구조: Vue 빌드 결과를 `src/main/resources/static`에 포함해 Spring Boot 애플리케이션 하나로 실행한다.
- DB 설정 방식: `application.properties`에는 공통값/로컬 예시를 두고, 실제 비밀번호는 gitignore 대상 파일 또는 환경변수로 분리한다.
- API 구조: REST API. JSON 응답은 `success`, `message`, `data` 형태를 따른다.

## 범위

- 루트 Spring Boot Maven 프로젝트 골격 생성 또는 정리.
- Java 17, Spring Boot, MyBatis, MySQL Connector 의존성 설정.
- 기본 패키지 구조 생성.
  - `com.ssafy.home`
  - `common`
  - 필요 시 `common.response`, `common.config`, `common.health` 등 하위 패키지.
- 공통 JSON 응답 DTO 또는 record 작성.
- 헬스 체크 REST API 작성.
  - 예: `GET /api/health`
  - 응답은 공통 JSON 응답 형태를 따른다.
- MyBatis mapper scan 설정.
- DB 연결 확인용 샘플 Mapper 작성.
  - 예: `SELECT 1` 또는 DB 현재 시간 조회.
  - 도메인 테이블을 만들지 않는다.
- DB 설정 파일 분리.
  - 커밋 가능한 예시 설정.
  - 실제 비밀번호가 들어갈 파일 또는 환경변수 방식.
  - `.gitignore` 반영.
- Vue/Vite 원본 프로젝트 위치 준비.
  - `frontend/`
  - 최소 화면은 Spring Boot 헬스 체크 API를 호출할 수 있는 구조로 둔다.
- Vue 빌드 결과를 Spring Boot 정적 리소스에 포함하는 방식 문서화 또는 스크립트화.
  - 실제 복사 자동화는 가능한 선에서 구현한다.
  - 네트워크나 패키지 설치 제약으로 완료하지 못하면 정확한 사유와 후속 작업을 기록한다.
- 실행/검증 명령을 README 또는 Sprint 작업 로그에 기록.

## 제외 범위

- 실거래가 DB 스키마 생성.
- 공공데이터 적재 로직.
- 회원 가입, 로그인, 세션 인터셉터 구현.
- 지도 API 연동.
- 주변 상권 지도 기능.
- Vue 화면의 최종 디자인.
- 배포용 운영 설정.

## Generator 작업 지시

1. `docs/spec.md`와 이 문서를 먼저 읽는다.
2. 현재 저장소에 기존 소스가 있는지 확인한다.
3. 기존 소스가 없다면 루트 Spring Boot Maven 프로젝트와 `frontend/` Vue/Vite 구조를 생성한다.
4. 기존 소스가 있다면 기존 구조를 우선하고, 이 Sprint의 완료 기준에 맞게 최소 변경한다.
5. DB 비밀번호, API 키, 개인 로컬 경로는 커밋하지 않는다.
6. 구현 중 발생한 에러, 우회, 미완료 항목은 이 문서의 작업 로그와 남은 리스크에 기록한다.
7. 변경 파일 목록을 이 문서에 기록한다.

Generator에게 전달할 프롬프트 예시:

```text
docs/spec.md와 docs/sprints/Sprint1.md를 읽고 Sprint1 범위만 구현해.
Spring Boot Maven Java17 골격, MyBatis/MySQL 연결 기준선, 공통 JSON 응답, 헬스 체크 REST API, frontend/ Vue/Vite 원본 위치와 정적 빌드 포함 구조를 준비해.
변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크는 docs/sprints/Sprint1.md에 기록해.
범위 밖 기능은 구현하지 말고 결정 필요 항목으로 남겨.
```

## Reviewer 검증 지시

Reviewer는 Generator 구현 후 다음 기준으로 검증한다.

- `docs/spec.md`의 기술 기준선과 충돌하지 않는지 확인한다.
- Spring Boot 애플리케이션이 Java 17 / Maven 기준으로 빌드 가능한지 확인한다.
- 공통 JSON 응답 형태가 지켜지는지 확인한다.
- 헬스 체크 API가 정상 응답하는지 확인한다.
- MyBatis mapper scan과 샘플 쿼리 구조가 적절한지 확인한다.
- DB 비밀번호나 API 키가 커밋 대상 파일에 들어가지 않았는지 확인한다.
- Vue/Vite 원본 위치와 Spring Boot 정적 리소스 포함 방식이 명확한지 확인한다.
- 범위 밖 기능을 임의 구현하지 않았는지 확인한다.

## 완료 기준

- Maven 기반 Spring Boot 프로젝트가 존재한다.
- Java 17 기준으로 빌드 설정이 되어 있다.
- Spring Boot 애플리케이션 엔트리포인트가 존재한다.
- `GET /api/health` 또는 이에 준하는 헬스 체크 REST API가 공통 JSON 응답 형태로 동작한다.
- MyBatis 설정과 샘플 Mapper가 존재한다.
- DB 연결 정보는 실제 비밀번호를 커밋하지 않는 방식으로 분리되어 있다.
- `frontend/` Vue/Vite 원본 위치가 존재하거나, 네트워크 제약 시 생성 방법과 미완료 사유가 기록되어 있다.
- Vue 빌드 결과를 `src/main/resources/static`에 포함하는 방식이 구현 또는 문서화되어 있다.
- 변경 파일, 구현 내용, 검증 결과, 남은 리스크가 이 문서에 기록되어 있다.

## 검증 명령 후보

Generator 또는 Reviewer는 가능한 범위에서 아래 명령을 실행하고 결과를 기록한다.

```bash
mvn test
mvn spring-boot:run
```

Vue 의존성이 설치된 경우:

```bash
cd frontend
npm install
npm run build
```

환경 제약으로 실행하지 못한 명령은 실패로 숨기지 말고 사유를 기록한다.

## 결정 필요 항목

- 세션 만료 시간, 로그인 필요 화면, 인터셉터 적용 범위는 M3에서 결정한다.
- 공공데이터 초기 적재 범위와 미적재 요청 처리 방식은 M2에서 결정한다.
- 지도 API와 주변 상권 데이터 출처는 M5 전에 결정한다.

## 작업 로그

- 2026-05-29: Manager가 Sprint 1 계약을 작성했다.

## 변경 파일 목록

- 아직 Generator 구현 전.

## 검증 결과

- 아직 Generator 구현 전.

## 남은 리스크 / 인계 사항

- Vue/Vite 의존성 설치는 네트워크 접근이 필요할 수 있다.
- 로컬 MySQL 접속 정보가 없으면 DB 샘플 쿼리 실행 검증은 보류될 수 있다.

## Generator Implementation Log - 2026-05-29

### Scope Implemented

- Created the root Spring Boot Maven project skeleton with Java 17.
- Added Spring Boot Web, MyBatis Spring Boot starter, MySQL Connector/J, and Spring Boot test dependencies.
- Created the base package `com.ssafy.home`.
- Created common packages: `common.config`, `common.response`, and `common.health`.
- Added the common JSON response record with `success`, `message`, and `data`.
- Added `GET /api/health`.
- Added MyBatis mapper scan configuration.
- Added a DB probe mapper using `SELECT 1`.
- Split DB configuration so committed values are examples only. Runtime credentials can be provided with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, or gitignored local properties files.
- Added `frontend/` Vue/Vite source location.
- Added `npm run build:backend`, which writes Vue build output to `src/main/resources/static`.
- Generated Spring Boot static resources by running the frontend backend-build command.

### Changed Files

- `.gitignore`
- `pom.xml`
- `src/main/java/com/ssafy/home/HomeApplication.java`
- `src/main/java/com/ssafy/home/common/config/MyBatisConfig.java`
- `src/main/java/com/ssafy/home/common/response/ApiResponse.java`
- `src/main/java/com/ssafy/home/common/health/controller/HealthController.java`
- `src/main/java/com/ssafy/home/common/health/dto/DatabaseHealth.java`
- `src/main/java/com/ssafy/home/common/health/dto/HealthResponse.java`
- `src/main/java/com/ssafy/home/common/health/mapper/HealthCheckMapper.java`
- `src/main/java/com/ssafy/home/common/health/service/HealthService.java`
- `src/main/resources/application.properties`
- `src/main/resources/static/index.html`
- `src/main/resources/static/assets/index-BInmR4rY.js`
- `src/main/resources/static/assets/index-BWBBHJgy.css`
- `src/test/java/com/ssafy/home/common/response/ApiResponseTest.java`
- `frontend/index.html`
- `frontend/package.json`
- `frontend/package-lock.json`
- `frontend/vite.config.js`
- `frontend/src/main.js`
- `frontend/src/style.css`
- `frontend/README.md`

### Validation Results

- `mvn test`: not executed successfully. `mvn` is not installed or not available on PATH in this environment.
- `mvn spring-boot:run`: not executed successfully. `mvn` is not installed or not available on PATH in this environment.
- `cmd /c npm install`: success after elevated network access approval. 31 packages installed; npm audit reported 2 moderate vulnerabilities.
- `cmd /c npm run build`: success. Vite production build completed into `frontend/dist`.
- `cmd /c npm run build:backend`: success. Vite production build completed into `src/main/resources/static`.

### Errors / Adjustments

- Initial Vue SFC build using `App.vue` failed with a Vite import-analysis error even though `@vitejs/plugin-vue` was installed and visible in the resolved Vite plugin list.
- To keep Sprint1's Vue/Vite baseline buildable in this environment, the minimal app was moved into `frontend/src/main.js` as a Vue component object. No product feature outside Sprint1 was added.

### Remaining Risks / Blockers

- Backend Maven build and Spring Boot runtime were not verified because Maven is unavailable on PATH.
- DB probe execution was not verified against a live MySQL instance. Local DB credentials must be supplied through environment variables or a gitignored local properties file.
- `npm install` reported 2 moderate vulnerabilities. They were not changed in Sprint1 to avoid broad dependency updates outside the baseline setup task.
- Existing repository files had pre-existing modifications before Generator work began; they were left untouched.

## Manager Follow-up - 2026-05-29

### Generator Result Review

- Generator가 Sprint1 범위 구현을 완료했고, 구현 로그를 이 문서에 남겼다.
- Spring Boot / Maven / Java 17 / MyBatis / MySQL / Vue/Vite 골격은 Sprint1 계약의 의도와 대체로 일치한다.
- `frontend/` 원본과 `src/main/resources/static` 빌드 산출물 포함 방식이 준비됐다.
- `.gitignore`에는 `target/`, `frontend/node_modules/`, `frontend/dist/`, 로컬 Spring 설정 파일, `.env`가 제외되어 있다.

### Review Before Close

- Sprint1은 아직 Reviewer 검증 전이므로 완료 처리하지 않는다.
- Reviewer는 Maven 미설치로 인한 백엔드 미검증을 Fail로 볼지, 환경 리스크로 남기고 조건부 Pass로 볼지 판단해야 한다.
- Reviewer는 DB 비밀번호가 커밋되지 않았는지, 샘플 Mapper가 도메인 범위를 넘지 않았는지, Vue 빌드 산출물 포함 방식이 재현 가능한지 확인해야 한다.

## Manager Supplement - 2026-05-29

### Review Before Supplement Decisions

Sprint1 Reviewer 검증 전에 재현 가능한 개발 환경을 보강한다. 이 작업은 Sprint1 기준선 검증을 가능하게 하기 위한 보강이며, 새 도메인 기능 구현이 아니다.

확정 결정:

- Maven wrapper: 사용한다.
- Docker: 개발용 MySQL부터 도입한다.
- App Dockerfile: 지금은 보류하고, 제출 안정화 또는 필요 시 도입한다.

### Supplement Scope

- Maven wrapper를 추가한다.
  - `mvnw`
  - `mvnw.cmd`
  - `.mvn/wrapper/maven-wrapper.properties`
  - Maven wrapper 실행에 필요한 파일
- 개발용 MySQL `docker-compose.yml`을 추가한다.
- DB 접속 정보 예시를 안전하게 제공한다.
  - 실제 비밀번호는 커밋하지 않는다.
  - 필요하면 `.env.example` 또는 문서화된 환경변수 예시를 추가한다.
- Spring Boot 설정이 Docker MySQL 기본값과 환경변수 방식으로 자연스럽게 연결되는지 확인한다.
- `.gitignore`에 Docker/환경변수/로컬 설정 관련 제외가 부족하면 보강한다.
- 보강 후 실행/검증 명령을 이 문서에 기록한다.

### Supplement Excluded Scope

- 앱 전체 Dockerfile 생성.
- 운영 배포용 Docker 구성.
- 실거래가, 회원, 로그인, 지도, 상권 기능 구현.
- DB 도메인 스키마 생성.

### Generator Supplement Instruction

Generator는 `docs/spec.md`와 이 문서를 읽고 Sprint1 보강 범위만 구현한다.

추가 작업 지시:

1. Maven wrapper를 추가하고, 로컬 Maven 없이도 `mvnw.cmd test` 또는 `./mvnw test`로 검증할 수 있게 한다.
2. 개발용 MySQL `docker-compose.yml`을 추가한다.
3. DB 이름, 사용자, 비밀번호, 포트 등은 개발용 기본값과 환경변수 override가 가능하게 구성한다.
4. 실제 비밀번호/API 키/개인 로컬 경로는 커밋하지 않는다.
5. 앱 전체 Dockerfile은 만들지 않는다.
6. 변경 파일, 구현 내용, 검증 결과, 에러, 남은 리스크를 이 문서에 추가 기록한다.

Generator에게 전달할 보강 프롬프트 예시:

```text
docs/spec.md와 docs/sprints/Sprint1.md를 읽고 Sprint1 Manager Supplement 범위만 구현해.
Maven wrapper와 개발용 MySQL docker-compose.yml을 추가하고, DB 설정 예시와 gitignore를 안전하게 보강해.
앱 전체 Dockerfile은 만들지 마.
변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크는 docs/sprints/Sprint1.md에 기록해.
```
## Generator Supplement Implementation Log - 2026-05-29

### Scope Implemented

- Added Maven Wrapper files so the backend can be tested without a locally installed `mvn`.
- Added `.mvn/wrapper/maven-wrapper.jar` and `.mvn/wrapper/maven-wrapper.properties` using Maven Wrapper 3.3.2 and Maven 3.9.9.
- Added development-only MySQL `docker-compose.yml`.
- Added `.env.example` with non-secret local development example values for Docker MySQL and Spring Boot DB environment variables.
- Aligned `spring.datasource.password` default with the Docker MySQL development default, while preserving environment variable override via `DB_PASSWORD`.
- Expanded `.gitignore` for local Spring config variants, `.env` files, and local Docker/MySQL data directories.
- Did not add an application Dockerfile, production Docker configuration, domain schema, or new domain features.

### Changed Files

- `.env.example`
- `.gitignore`
- `.mvn/wrapper/maven-wrapper.jar`
- `.mvn/wrapper/maven-wrapper.properties`
- `docker-compose.yml`
- `mvnw`
- `mvnw.cmd`
- `src/main/resources/application.properties`
- `docs/sprints/Sprint1.md`

### Validation Results

- `docker compose config`: success. Compose rendered the development MySQL service, named volume, ports, environment defaults, command, and healthcheck. Docker emitted warnings that `C:\Users\SSAFY\.docker\config.json` could not be read due to access denial, but the command exited with code 0.
- `.\mvnw.cmd test`: success after Maven Wrapper script correction. Maven Wrapper downloaded/used Maven, compiled 8 main Java sources and 1 test source, and ran 1 test with 0 failures and 0 errors.

### Errors / Adjustments

- Initial wrapper jar download failed in the sandbox because network access was denied; it succeeded after approved network access to Maven Central.
- Initial `.\mvnw.cmd test` failed because Maven Wrapper needed to download the Maven distribution and sandbox networking denied the connection.
- First elevated `.\mvnw.cmd test` reached the wrapper jar but failed with `-Dmaven.multiModuleProjectDirectory system property is not set`; `mvnw` and `mvnw.cmd` were updated to pass the project directory to the wrapper main class.
- The next `mvnw.cmd` run showed Java usage output because the Windows project path ended with a backslash that escaped the JVM option quote; `mvnw.cmd` was updated to use a trimmed `PROJECT_BASE` path.

### Remaining Risks / Blockers

- `docker compose config` validates the Compose file, but the MySQL container was not started and the DB probe was not tested against a live database in this supplement.
- Docker CLI still reports a local access warning for `C:\Users\SSAFY\.docker\config.json`; this appears to be host-local Docker configuration access, not a Compose file error.
- The committed Docker defaults are development examples only. Real local passwords should be placed in `.env` or gitignored local Spring config and must not be committed.

## Reviewer Verification - 2026-05-29

### Verdict

Conditional Pass.

Sprint1 Generator implementation and Manager Supplement are aligned with the Sprint1 contract for the Spring Boot Maven Java17 baseline, MyBatis/MySQL connection baseline, common JSON response, health endpoint, Vue/Vite source plus Spring Boot static build output, Maven wrapper, development MySQL Compose file, and secret/gitignore handling.

The pass is conditional because runtime verification against a live MySQL container was not performed during this review, and `GET /api/health` was verified by source inspection rather than by an HTTP request against a running application connected to MySQL.

### Contract / Scope Verification

- Actual changed files match the Sprint1 and Manager Supplement scope: backend skeleton, common response, health check, MyBatis config, DB probe mapper, DB configuration, Maven wrapper, development MySQL Compose, frontend Vue/Vite source, generated Spring static assets, and Sprint1 documentation.
- No out-of-scope domain features were found. There are no member, auth, house search, public-data loading, map, commercial-area, domain schema, or app Dockerfile implementations.
- No `*Dockerfile*` files were found.
- Java source files are limited to `com.ssafy.home`, `common.config`, `common.response`, and `common.health` baseline packages.

### Source Verification

- `pom.xml` uses Spring Boot `3.3.5`, Java `17`, MyBatis Spring Boot starter `3.0.4`, MySQL Connector/J runtime dependency, and Spring Boot test dependency. This satisfies the Sprint1 baseline.
- `ApiResponse<T>` is a record with `success`, `message`, and `data`, plus `ok` and `fail` factories.
- `GET /api/health` returns `ApiResponse<HealthResponse>` and therefore uses the common JSON response shape.
- MyBatis mapper scan is configured through `@MapperScan("com.ssafy.home")`.
- The DB probe mapper only executes `SELECT 1`; it does not create or depend on domain tables.
- `application.properties` uses environment-variable overrides for `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. `.env.example` contains development example values only. `.gitignore` excludes `.env`, local Spring config files, frontend dependencies/build output, Maven target output, and local Docker/MySQL state directories.
- `docker-compose.yml` defines only a development MySQL service and a named volume. No application Dockerfile or production Docker setup was added.
- `frontend/package.json` includes `build:backend`, which writes Vite output to `../src/main/resources/static` with `--emptyOutDir`; this makes the static inclusion path reproducible when dependencies are installed.

### Commands Executed

- `cmd /c .\mvnw.cmd test`
  - First sandboxed run failed because Maven Wrapper attempted a network connection and the sandbox denied it.
  - Elevated rerun succeeded.
  - Result: Build success. Tests run: 1, failures: 0, errors: 0.
- `docker compose config`
  - Result: success, rendered the MySQL service, environment defaults, port mapping, healthcheck, network, and named volume.
  - Warning: Docker CLI could not read `C:\Users\SSAFY\.docker\config.json` because access was denied. The command still exited successfully; this appears to be host-local Docker config access, not a Compose file defect.
- `rg --files -g "*Dockerfile*"`
  - Result: no Dockerfile files found.
- `cmd /c npm run build:backend` in `frontend`
  - Result: success. Vite built `index.html`, CSS, and JS into `src/main/resources/static`.
- `rg -n "password|secret|api[_-]?key|token|C:\\Users|SSAFY" -S .`
  - Result: only documented example passwords, environment-variable names, and project/document references were found. No real API key or personal local path was found in implementation config.

### Missing Tests

- No controller or Spring context test verifies that `/api/health` serializes as `{ success, message, data }`.
- No integration test verifies the MyBatis DB probe against a running MySQL instance.
- No test starts the Spring Boot app and performs an HTTP request to `/api/health`.
- Frontend build was verified, but there is no frontend unit/e2e test.

### Runtime Risks

- `GET /api/health` returns a common JSON failure payload when DB probing fails, but this review did not verify the HTTP status code behavior at runtime. The contract does not specify status code semantics, so this is a residual behavior risk rather than a Sprint1 failure.
- The MyBatis scan root `com.ssafy.home` is broad. It is acceptable for the current tiny baseline, but future Sprints should consider narrowing mapper scanning if non-mapper interfaces are added.
- Development default DB passwords are committed as examples. They are clearly non-production defaults and can be overridden, but real local values must remain in `.env` or gitignored local Spring config.
- Docker Compose was validated with `config`, but the MySQL container was not started and DB connectivity was not proven.

### Spec Deviation

- No blocking spec deviation found for Sprint1.
- Remaining items are validation gaps and runtime risks, not confirmed implementation failures.
