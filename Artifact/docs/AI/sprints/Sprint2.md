# Sprint 2: 실거래가 스키마와 샘플 조회 기준선

## Milestone 연결

- Milestone: M2 - 실거래가 스키마, 데이터 적재, 검색
- 목적: 공공데이터 API 실제 적재 구현 전에 MySQL 스키마, 샘플 데이터, MyBatis 조회 기준선, DB 연결 검증을 만든다.

## Manager 구현 지시

Generator는 `docs/spec.md`와 이 문서를 기준 계약으로 읽고, Sprint 2 범위만 구현한다.

이번 Sprint의 핵심은 큰 공공데이터 적재 시스템이 아니라, 이후 적재/검색을 안전하게 붙일 수 있는 데이터 모델 기준선이다. Docker MySQL을 실제로 기동할 수 있으면 live DB probe와 샘플 조회까지 검증한다. Docker 기동이 환경상 불가능하면 SQL, Mapper, 테스트 가능한 범위를 최대한 검증하고 사유를 남긴다.

프롬프트는 실행 트리거이고, 구현 범위의 기준은 이 문서다. 범위 밖 작업이 필요하면 임의 구현하지 말고 "결정 필요 항목" 또는 "남은 리스크"에 기록한다.

## 확정 기술 결정

- 대상 API: 국토교통부 아파트 매매 실거래가 자료.
- 초기 적재 지역: 서울특별시 동작구.
- API 지역 코드: `LAWD_CD=11590`.
- 초기 적재 기간: 제일 최근 조회 가능 계약년월 1개월.
- 주택 유형: 아파트 매매.
- API 요청 기준: `LAWD_CD`는 법정동코드 10자리 중 앞 5자리, `DEAL_YMD`는 계약년월 6자리.
- 미적재 요청 처리: 이미 적재된 범위를 제외하고 부족분만 추가 적재.
- 전체 재적재: 운영/관리자용 강제 새로고침 옵션으로 분리.
- 중복 판정: `source_api`, `lawd_cd`, `deal_ymd`, `umd_nm`, `jibun`, `apt_nm`, `deal_year`, `deal_month`, `deal_day`, `deal_amount`, `exclu_use_ar`, `floor` 기반 `api_row_hash`.
- 좌표: 추후 Kakao Map API로 확보한다. Sprint2에서는 `lat`, `lng`를 nullable로 둔다.

## 범위

- Sprint1 잔여 검증 보강.
  - Docker MySQL 컨테이너 기동 가능 여부 확인.
  - Spring Boot DB 연결과 live DB probe 검증 시도.
  - `/api/health` HTTP 호출 검증 또는 controller test 추가.
- MySQL 스키마 SQL 작성.
  - `regions`
  - `houses`
  - `house_deals`
  - `public_data_import_batches`
- 샘플 데이터 SQL 작성.
  - 서울특별시 동작구 `LAWD_CD=11590`.
  - 동작구 내 1~2개 법정동 샘플.
  - 아파트 1~3개 샘플.
  - 아파트 매매 실거래 2~5건 샘플.
- `api_row_hash` 생성 유틸 또는 서비스 로직 작성.
- MyBatis Mapper 작성.
  - 지역 코드/법정동 조회.
  - 아파트명 기준 house 조회.
  - 샘플 실거래 목록 조회.
  - import batch 조회 또는 upsert 기준선.
- 최소 REST API 작성.
  - `GET /api/regions?lawdCd=11590`
  - `GET /api/houses?aptName=...`
  - `GET /api/house-deals?lawdCd=11590&dealYmd=yyyyMM`
- 공통 JSON 응답 형태 유지.
- 테스트 작성.
  - `api_row_hash` deterministic test.
  - Mapper SQL 또는 Service 단위 테스트 가능한 범위.
  - 가능하면 Testcontainers 없이도 실행 가능한 순수 단위 테스트를 우선한다.
- 실행/검증 명령과 결과를 이 문서에 기록.

## 제외 범위

- 공공데이터 API 실제 호출.
- 공공데이터 인증키 설정.
- 최신 조회 가능 계약년월 자동 탐색 구현.
- 동별/아파트명별 최종 검색 UX.
- Vue 화면 구현.
- Kakao Map API 연동과 좌표 수집.
- 회원/인증 기능.
- 운영/관리자용 강제 새로고침 API.
- 대량 적재 성능 최적화.

## Generator 작업 지시

1. `docs/spec.md`와 이 문서를 먼저 읽는다.
2. Sprint1 구현 결과를 되돌리지 말고, 기존 구조와 함께 동작하도록 최소 변경한다.
3. DB 스키마는 반복 실행 가능성을 고려한다.
4. SQL 리소스 위치는 Spring Boot/MyBatis 관례에 맞춘다.
5. 실제 API 키, 실제 비밀번호, 개인 로컬 경로는 커밋하지 않는다.
6. Controller는 SQL을 직접 호출하지 않는다. Controller - Service - Mapper 계층을 지킨다.
7. Mapper에는 인증/정책 판단을 넣지 않는다.
8. 변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크를 이 문서에 기록한다.

Generator에게 전달할 프롬프트 예시:

```text
docs/spec.md와 docs/sprints/Sprint2.md를 읽고 Sprint2 범위만 구현해.
Docker MySQL live 검증을 시도하고, regions/houses/house_deals/public_data_import_batches 스키마와 샘플 데이터, MyBatis 조회 기준선, 최소 REST API를 작성해.
공공데이터 API 실제 호출, Vue 화면, Kakao Map, 회원/인증은 구현하지 마.
변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크는 docs/sprints/Sprint2.md에 기록해.
```

## Reviewer 검증 지시

Reviewer는 Generator 구현 후 다음 기준으로 검증한다.

- `docs/spec.md`의 M2 결정과 충돌하지 않는지 확인한다.
- Sprint2 범위 밖 기능을 구현하지 않았는지 확인한다.
- DB 스키마가 `regions`, `houses`, `house_deals`, `public_data_import_batches`를 포함하는지 확인한다.
- `api_row_hash` 구성 필드가 계약과 일치하는지 확인한다.
- 중복 방지 유니크 키 또는 제약이 존재하는지 확인한다.
- Controller - Service - Mapper 계층이 지켜졌는지 확인한다.
- 공통 JSON 응답 형태가 유지되는지 확인한다.
- 실제 secret이 커밋되지 않았는지 확인한다.
- 가능한 경우 Docker MySQL 기동, DB schema 적용, 샘플 조회 API를 검증한다.

## 완료 기준

- Docker MySQL 기동 또는 기동 실패 사유가 기록된다.
- `regions`, `houses`, `house_deals`, `public_data_import_batches` 스키마 SQL이 존재한다.
- 서울특별시 동작구 샘플 데이터 SQL이 존재한다.
- `api_row_hash` 생성 기준이 코드와 테스트에 반영된다.
- 샘플 조회용 MyBatis Mapper와 Service가 존재한다.
- 최소 조회 REST API가 공통 JSON 응답 형태로 존재한다.
- `.\mvnw.cmd test`가 성공하거나 실패 사유가 기록된다.
- 가능하면 DB 연결 후 샘플 조회가 검증된다.
- 변경 파일, 구현 내용, 검증 결과, 남은 리스크가 이 문서에 기록된다.

## 검증 명령 후보

```bash
docker compose up -d mysql
docker compose ps
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

가능한 경우 별도 터미널 또는 HTTP 클라이언트로 확인한다.

```bash
curl http://localhost:8080/api/health
curl "http://localhost:8080/api/regions?lawdCd=11590"
curl "http://localhost:8080/api/houses?aptName=..."
curl "http://localhost:8080/api/house-deals?lawdCd=11590&dealYmd=yyyyMM"
```

환경 제약으로 실행하지 못한 명령은 실패로 숨기지 말고 사유를 기록한다.

## 결정 필요 항목

- 현재 Sprint2 시작 전 결정 필요 항목은 없다.
- API 실제 호출과 최신 계약년월 자동 탐색은 다음 Sprint에서 계약한다.

## 작업 로그

- 2026-05-29: Manager가 Sprint 2 계약을 작성했다.

## 변경 파일 목록

- 아직 Generator 구현 전.

## 검증 결과

- 아직 Generator 구현 전.

## 남은 리스크 / 인계 사항

- Docker CLI의 로컬 config 접근 권한 경고가 Sprint1에서 관찰됐다.
- 실제 MySQL 컨테이너 기동이 환경에 따라 실패할 수 있다.
- 공공데이터 API 원본 필드 일부는 nullable일 수 있으므로 샘플 스키마에서 optional 필드를 허용해야 한다.
## Generator 구현 로그

- 2026-05-29: Sprint2 범위로 MySQL 스키마 SQL, 동작구 샘플 데이터 SQL, `api_row_hash` 유틸, MyBatis Mapper, Service, REST Controller, 단위 테스트를 구현했다.
- 공공데이터 API 실제 호출, 인증키 설정, 최신 계약년월 자동 탐색, Vue/Kakao Map/회원/인증 기능은 구현하지 않았다.
- Controller - Service - Mapper 계층을 유지했고 Controller에서 SQL을 직접 호출하지 않았다.

## Generator 변경 파일 목록

- `docker-compose.yml`: MySQL 최초 초기화용 `schema.sql`, `data.sql` read-only mount 추가.
- `src/main/resources/application.properties`: SQL init 기본값을 `SQL_INIT_MODE` 환경변수로 제어하고, health probe 지연을 줄이는 Hikari connection timeout 기본값 추가.
- `src/main/resources/schema.sql`: `regions`, `houses`, `house_deals`, `public_data_import_batches` MySQL 테이블과 unique/index/FK 기준선 추가.
- `src/main/resources/data.sql`: 서울특별시 동작구 `LAWD_CD=11590`의 흑석동/상도동, 아파트 3개, 202405 매매 실거래 3건, import batch 샘플 추가.
- `src/main/java/com/ssafy/home/house/util/ApiRowHashInput.java`
- `src/main/java/com/ssafy/home/house/util/ApiRowHashGenerator.java`
- `src/main/java/com/ssafy/home/house/dto/RegionResponse.java`
- `src/main/java/com/ssafy/home/house/dto/HouseResponse.java`
- `src/main/java/com/ssafy/home/house/dto/HouseDealResponse.java`
- `src/main/java/com/ssafy/home/house/dto/ImportBatchResponse.java`
- `src/main/java/com/ssafy/home/house/mapper/HouseMapper.java`
- `src/main/resources/mappers/house/HouseMapper.xml`
- `src/main/java/com/ssafy/home/house/service/HouseService.java`
- `src/main/java/com/ssafy/home/house/controller/HouseController.java`
- `src/test/java/com/ssafy/home/house/util/ApiRowHashGeneratorTest.java`
- `src/test/java/com/ssafy/home/house/service/HouseServiceTest.java`
- `src/test/java/com/ssafy/home/common/health/controller/HealthControllerTest.java`

## Generator 검증 결과

- `cmd /c .\mvnw.cmd test`: 성공. Tests run: 5, Failures: 0, Errors: 0, Skipped: 0.
- `docker compose up -d mysql`: 실패. Docker Desktop Linux engine pipe `//./pipe/dockerDesktopLinuxEngine`를 찾을 수 없어 Docker API에 연결하지 못했다.
- `docker compose ps`: 실패. 동일하게 Docker API 연결 실패.
- Spring Boot `/api/health` HTTP 검증: `.\mvnw.cmd spring-boot:run`을 `--server.port=18080`으로 기동 후 `Invoke-WebRequest http://localhost:18080/api/health` 호출 성공. 응답 HTTP 200, body는 `success=false`, `status=DEGRADED`, `database.connected=false`, `error=MyBatisSystemException`. MySQL 컨테이너가 실행되지 않은 상태라 live DB probe는 실패로 정상 기록한다.
- `/api/regions?lawdCd=11590`, `/api/houses?aptName=...`, `/api/house-deals?lawdCd=11590&dealYmd=202405`: Docker MySQL 미기동으로 live HTTP 조회 검증은 수행하지 못했다. Controller/Service/Mapper 구현은 추가했고, Service 단위 테스트 범위에서 위임 흐름을 확인했다.

## Generator 남은 리스크 / 인계 사항

- 현재 환경에서는 Docker daemon이 실행 중이지 않거나 접근할 수 없어 MySQL 컨테이너, schema/data 적용, live Mapper 조회를 검증하지 못했다.
- 기존 Docker volume이 이미 생성된 환경에서는 `/docker-entrypoint-initdb.d` SQL이 다시 실행되지 않을 수 있다. 이 경우 DB를 새로 만들거나 `SQL_INIT_MODE=always`로 Spring SQL init을 명시 실행해 확인해야 한다.
- Mapper XML은 live DB에서 직접 실행 검증하지 못했다. Docker MySQL이 가능한 환경에서 `docker compose up -d mysql`, Spring Boot 실행, 샘플 API 3종 호출로 후속 검증이 필요하다.

## Reviewer Verification - 2026-05-29

### Judgment

Conditional Pass.

Sprint2 schema SQL, Dongjak-gu sample data SQL, `api_row_hash` generation and deterministic test, house Controller-Service-Mapper-DTO layers, minimal REST API, and `/api/health` HTTP serialization test were verified from source and unit tests. Docker daemon access failed, so live MySQL startup, real schema/data application, Mapper XML execution against MySQL, and the three sample house API calls with live DB remain unverified. I treat that as an environment-limited risk, not an implementation Fail by itself.

### Commands And Results

- `cmd /c .\mvnw.cmd test`: Pass. Tests run: 5, Failures: 0, Errors: 0, Skipped: 0.
- `cmd /c .\mvnw.cmd clean test`: First attempt failed under sandbox network restriction while Maven Wrapper tried to access its distribution. Approved rerun passed, recompiling 17 main sources and 4 test sources. Tests run: 5, Failures: 0, Errors: 0, Skipped: 0.
- `docker compose config`: Pass. MySQL 8.4 service, read-only `schema.sql`/`data.sql` init mounts, and named volume were rendered.
- `docker compose up -d mysql`: Fail to execute in this environment. Docker API pipe `npipe:////./pipe/dockerDesktopLinuxEngine` was not found.
- `docker compose ps`: Fail for the same Docker API connection reason.
- Temporary Spring Boot on `--server.port=18080` plus `Invoke-WebRequest http://localhost:18080/api/health`: HTTP 200. Body was `{"success":false,"message":"application is running, but database check failed","data":{"status":"DEGRADED","database":{"connected":false,"probe":null,"error":"MyBatisSystemException"}}}`.
- `/api/regions?lawdCd=11590`, `/api/houses?aptName=...`, `/api/house-deals?lawdCd=11590&dealYmd=202405`: Not executed against live DB because Docker MySQL could not start.

### Verification Details

- Sprint2 scope vs files: the Generator-recorded Sprint2 file list matches the contracted scope overall. The repository also contains broader untracked/modified Sprint1/base files such as `frontend/`, `src/main/resources/static/`, and multiple docs, so the exact Sprint2 diff boundary cannot be proven from git status alone.
- Out-of-scope features: no public data API live call, API key setup, Kakao Map integration, member/auth implementation, or admin forced refresh API was found in the Sprint2 house implementation. Static Vue build output exists in the repo, but it is not listed in the Sprint2 Generator file list, so I did not classify it as a Sprint2 scope violation.
- Schema: `regions`, `houses`, `house_deals`, and `public_data_import_batches` exist. `lat`/`lng` are nullable, `lawd_cd=11590` is supported, and the model aligns with the M2 apartment sale decision.
- Sample data: `data.sql` contains Seoul Dongjak-gu `11590`, Heukseok-dong/Sangdo-dong regions, three apartments, three 202405 sale deals, and one import batch sample.
- `api_row_hash`: code uses the contracted fields `source_api`, `lawd_cd`, `deal_ymd`, `umd_nm`, `jibun`, `apt_nm`, `deal_year`, `deal_month`, `deal_day`, `deal_amount`, `exclu_use_ar`, `floor`; trims strings, removes commas from deal amount, and generates SHA-256 hex. Deterministic expected-hash test exists.
- Duplicate prevention: unique keys exist on `regions(lawd_cd, umd_nm)`, `houses(sgg_cd, umd_nm, jibun, apt_nm, build_year)`, `house_deals(api_row_hash)`, and `public_data_import_batches(source_api, lawd_cd, deal_ymd, house_type, deal_type)`.
- Layering: Controller calls Service, Service calls Mapper, and SQL is in MyBatis XML. No direct SQL in Controller was found.
- Common JSON response: house APIs and health API use `ApiResponse(success,message,data)`. `/api/health` MockMvc and live HTTP response both confirm serialization shape.
- Secrets/local paths: no real public data API key or personal local path was found. Docker/MySQL passwords appear to be development defaults in `.env.example`, `docker-compose.yml`, and `application.properties`, not committed real secrets.

### Missing Tests And Residual Risks

- No integration test executes `HouseMapper.xml` against MySQL.
- `schema.sql` and `data.sql` were not actually applied to MySQL in this environment.
- The three minimal house REST APIs do not have MockMvc tests or live DB HTTP verification; current coverage is mostly Service delegation plus hash and health tests.
- Existing Docker named volumes may skip `/docker-entrypoint-initdb.d` SQL on later runs, so a fresh volume or explicit `SQL_INIT_MODE=always` verification is needed.
- Some Korean text appeared garbled in raw PowerShell `Get-Content` output, but `rg` and compile/test confirmed the source/sample data content itself is usable.
