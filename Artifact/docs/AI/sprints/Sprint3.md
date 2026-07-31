# Sprint 3: 공공데이터 API 적재 기준선

## Milestone 연결

- Milestone: M2 - 실거래가 스키마, 데이터 적재, 검색
- 목적: 국토교통부 아파트 매매 실거래가 API 응답을 파싱하고, 서울특별시 동작구 최신 조회 가능 계약년월 1개월 데이터를 DB에 적재하는 기준선을 만든다.

## Manager 구현 지시

Generator는 `docs/spec.md`와 이 문서를 기준 계약으로 읽고, Sprint 3 범위만 구현한다.

이번 Sprint의 핵심은 공공데이터 API 적재 경로다. Sprint2에서 만든 스키마, 샘플 조회 기준선, `api_row_hash`를 재사용하여 API row를 `regions`, `houses`, `house_deals`, `public_data_import_batches`에 저장한다. 사용자 검색 UX, Vue 화면, 지도, 회원 기능은 구현하지 않는다.

API 키는 `application-local.properties` 같은 gitignored local config에서만 주입한다. 저장소에 실제 API 키를 커밋하면 안 된다.

프롬프트는 실행 트리거이고, 구현 범위의 기준은 이 문서다. 범위 밖 작업이 필요하면 임의 구현하지 말고 "결정 필요 항목" 또는 "남은 리스크"에 기록한다.

## 확정 기술 결정

- 대상 API: 국토교통부 아파트 매매 실거래가 자료.
- API 키 제공 방식: `application-local.properties` 같은 gitignored local config. 저장소 커밋 금지.
- 초기 적재 지역: 서울특별시 동작구.
- API 지역 코드: `LAWD_CD=11590`.
- 초기 적재 기간: 제일 최근 조회 가능 계약년월 1개월.
- 최신 `DEAL_YMD` 기본값: 실행 시점의 최신 `yyyyMM`. API 응답이 비면 직전 월 재시도 여부를 작업 로그에 남긴다.
- 주택 유형: 아파트 매매.
- 미적재 요청 처리: 이미 적재된 범위를 제외하고 부족분만 추가 적재.
- 전체 재적재: 운영/관리자용 강제 새로고침 옵션으로 분리. Sprint3에서는 구현하지 않는다.
- 중복 판정: `api_row_hash`를 사용하고 중복 row는 skip한다.
- 좌표: Sprint3에서는 수집하지 않는다. `lat`, `lng`는 nullable 유지.

## DB 실행 전제

Sprint3의 실제 DB 적재 검증은 개발용 MySQL이 실행 중이어야 한다.

1. `docker compose up -d mysql`로 개발용 MySQL을 기동한다.
2. `docker compose ps` 또는 Docker Desktop 상태로 MySQL 컨테이너 상태를 확인한다.
3. Spring Boot는 개발용 MySQL 접속 정보와 `application-local.properties` 계열 local config를 사용해 실행한다.
4. 기존 Docker volume이 있으면 `/docker-entrypoint-initdb.d`의 `schema.sql`, `data.sql`이 재실행되지 않을 수 있다. 데이터 삭제가 필요한 `docker compose down -v`는 사용자 승인 없이 실행하지 않는다.
5. Docker daemon 접근 실패, 포트 충돌, DB 인증 실패 등으로 MySQL을 기동하지 못하면 실제 DB 적재 검증은 보류하고, 실패 사유와 대체 검증 결과를 이 문서에 기록한다.

## 범위

- 공공데이터 API 설정 추가.
  - 예: `public-data.service-key`.
  - `application-local.properties` 예시는 문서화하되 실제 키는 만들거나 커밋하지 않는다.
- 개발용 MySQL 기동과 Spring Boot DB 연결 확인.
  - DB 환경이 가능하면 실제 MySQL을 대상으로 schema/data 적용 상태를 확인한다.
  - DB 환경이 불가능하면 원인과 미검증 범위를 남은 리스크로 기록한다.
- 공공데이터 API 클라이언트 작성.
  - `LAWD_CD`, `DEAL_YMD`, service key를 받아 API를 호출하는 구조.
  - 실제 호출은 키가 있을 때만 가능하게 한다.
- XML 응답 파싱 DTO 작성.
  - API item 필드를 Sprint2 스키마에 필요한 내부 DTO로 변환한다.
- 적재 Service 작성.
  - `regions` upsert 기준선.
  - `houses` upsert 기준선.
  - `house_deals` insert or skip 기준선.
  - `public_data_import_batches` 기록.
  - `api_row_hash` 기반 중복 skip.
- 미적재 범위 판단 기준선 작성.
  - `public_data_import_batches`에 성공 이력이 없으면 적재 대상.
  - 성공 이력이 있으면 일반 요청에서는 skip.
- 최소 적재 실행 API 또는 Service 진입점 작성.
  - 예: `POST /api/public-data/apt-trades/import?lawdCd=11590&dealYmd=yyyyMM`
  - API 키가 없으면 명확한 실패 응답 또는 설정 오류를 반환한다.
- 테스트 작성.
  - XML sample parsing test.
  - API row -> domain/upsert command 변환 test.
  - `api_row_hash` 중복 skip Service 단위 test.
  - API 키 미설정 시 실패 동작 test.
- Sprint2 잔여 검증 일부 보강.
  - 가능하면 `/api/regions`, `/api/houses`, `/api/house-deals` MockMvc test 추가.
  - Docker MySQL live 검증은 환경 가능 시 시도하고, 불가하면 리스크로 기록.
- 실행/검증 명령과 결과를 이 문서에 기록.

## 제외 범위

- Vue 화면 구현.
- Kakao Map API 연동과 좌표 수집.
- 회원/인증 기능.
- 운영/관리자용 강제 전체 재적재 API.
- 대량 배치 스케줄러.
- 모든 지역/기간 일괄 적재.
- 아파트 전월세, 연립다세대 매매/전월세.
- 사용자용 최종 동별/아파트명별 검색 UX.
- 실제 API 키 커밋.

## Generator 작업 지시

1. `docs/spec.md`와 이 문서를 먼저 읽는다.
2. Sprint1/Sprint2 구현 결과를 되돌리지 말고 기존 구조와 함께 동작하도록 최소 변경한다.
3. API 키는 gitignored local config에서만 읽는다. 실제 키를 저장소 파일에 쓰지 않는다.
4. API 클라이언트와 파서, 적재 Service를 분리한다.
5. Controller는 SQL을 직접 호출하지 않는다. Controller - Service - Mapper 계층을 지킨다.
6. Mapper에는 인증/정책 판단을 넣지 않는다.
7. 공공 API 호출 실패, 키 미설정, XML 파싱 실패를 명확히 다룬다.
8. 변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크를 이 문서에 기록한다.

Generator에게 전달할 프롬프트 예시:

```text
docs/spec.md와 docs/sprints/Sprint3.md를 읽고 Sprint3 범위만 구현해.
국토교통부 아파트 매매 실거래가 API 클라이언트, XML 파싱 DTO, DB 적재 Service, api_row_hash 중복 skip, import batch 기록, 최소 적재 API를 작성해.
API 키는 application-local.properties 같은 gitignored local config에서만 읽고 저장소에 커밋하지 마.
Vue, Kakao Map, 회원/인증, 관리자 강제 전체 재적재 API는 구현하지 마.
변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크는 docs/sprints/Sprint3.md에 기록해.
```

## Reviewer 검증 지시

Reviewer는 Generator 구현 후 다음 기준으로 검증한다.

- `docs/spec.md`의 M2 결정과 충돌하지 않는지 확인한다.
- API 키가 저장소에 커밋되지 않았는지 확인한다.
- API 설정이 gitignored local config로 주입되는지 확인한다.
- XML 파싱 필드가 스키마와 `api_row_hash` 계약에 맞는지 확인한다.
- `regions`, `houses`, `house_deals`, `public_data_import_batches` 적재 책임이 Service 계층에 있는지 확인한다.
- 중복 row skip이 `api_row_hash` 기반으로 동작하는지 확인한다.
- 성공 import batch가 있으면 일반 요청에서 재적재하지 않는지 확인한다.
- 범위 밖 기능을 구현하지 않았는지 확인한다.
- 테스트가 API 키 없이도 의미 있게 통과하는지 확인한다.

## 완료 기준

- 공공데이터 API 클라이언트 구조가 존재한다.
- XML sample parsing test가 존재한다.
- API row를 내부 적재 모델로 변환하는 로직이 존재한다.
- `api_row_hash` 기반 중복 skip이 구현되어 있다.
- `public_data_import_batches` 성공 이력 기반 부족분 판단 기준선이 존재한다.
- 최소 적재 API 또는 Service 진입점이 존재한다.
- API 키 미설정 시 실패 동작이 명확하다.
- 실제 API 키가 커밋되지 않았다.
- 개발용 MySQL 기동 성공 여부가 기록된다.
- MySQL 기동 성공 시 실제 DB를 대상으로 적재 API 또는 Service가 row 생성과 중복 skip을 수행하는지 검증한다.
- MySQL 기동 실패 시 실제 DB 적재 검증 미수행 사유와 로컬 sample XML/단위 테스트 기반 대체 검증 결과가 기록된다.
- `.\mvnw.cmd test`가 성공하거나 실패 사유가 기록된다.
- 가능하면 실제 API 호출 또는 로컬 sample XML 기반 적재 검증이 기록된다.
- 변경 파일, 구현 내용, 검증 결과, 남은 리스크가 이 문서에 기록된다.

## 검증 명령 후보

```bash
.\mvnw.cmd test
```

API 키와 DB 환경이 있는 경우:

```bash
docker compose up -d mysql
docker compose ps
.\mvnw.cmd spring-boot:run
curl -X POST "http://localhost:8080/api/public-data/apt-trades/import?lawdCd=11590&dealYmd=yyyyMM"
```

기존 Docker volume 초기화가 필요할 수 있지만, `docker compose down -v`는 데이터 삭제 명령이므로 사용자 승인 없이 실행하지 않는다.

환경 제약으로 실행하지 못한 명령은 실패로 숨기지 말고 사유를 기록한다.

## 결정 필요 항목

- 현재 Sprint3 시작 전 결정 필요 항목은 없다.
- 운영/관리자용 강제 전체 재적재 API는 이후 Sprint에서 계약한다.

## 작업 로그

- 2026-05-29: Manager가 Sprint 3 계약을 작성했다.
- 2026-05-29: Manager가 개발용 MySQL 기동, DB 연결 확인, 실제 적재 검증 또는 실패 사유 기록을 Sprint3 완료 기준에 보강했다.
- 2026-05-29: Generator가 Sprint3 범위 구현을 시작하고 `docs/spec.md`, `docs/sprints/Sprint3.md`, 기존 `house` 계층과 schema/mapper/test 구조를 확인했다.
- 2026-05-29: Generator가 공공데이터 아파트 매매 실거래가 API 클라이언트, XML 파서, 적재 Service, 적재 Mapper, 최소 적재 API를 추가했다.
- 2026-05-29: Generator가 API 키를 `public-data.service-key` local config에서만 읽도록 구성했다. 실제 키 값은 문서와 저장소 파일에 기록하지 않았다.
- 2026-05-29: Generator가 sample XML 파싱, API row -> 적재 command 변환, `api_row_hash` 중복 skip, API 키 미설정 실패 테스트를 추가했다.
- 2026-05-29: `.\mvnw.cmd test` 성공. 총 11개 테스트 통과.
- 2026-05-29: `docker compose up -d mysql` 실행 시 Docker daemon 접근 실패로 개발용 MySQL 컨테이너 기동과 live DB/API 적재 검증은 보류했다.
- 2026-05-29: Manager 점검 중 Docker Desktop 실행 후 `docker compose up -d mysql`을 재시도했다. 기본 3306 포트가 이미 사용 중이라 `MYSQL_PORT=13306`으로 개발용 MySQL 컨테이너를 재생성했고, MySQL health가 `healthy` 상태임을 확인했다.
- 2026-05-29: Manager live 점검 중 `PublicDataAptTradeClient`가 런타임에서 생성자 주입에 실패하는 문제를 발견했다. public 생성자에 명시적 `@Autowired`를 추가해 Spring Boot 앱 부팅 실패를 수정했다.
- 2026-05-29: Manager live 점검 중 공공데이터 API 기본 응답이 10건만 반환되어 `totalCount=142` 중 일부만 적재되는 문제를 발견했다. `pageNo`, `numOfRows` 요청 파라미터와 전체 페이지 반복 적재를 추가하고, `total_count <= imported_count + skipped_count`인 성공 batch만 일반 요청 skip 대상으로 보도록 보강했다.
- 2026-05-29: 보강 후 `.\mvnw.cmd test` 성공. 총 12개 테스트 통과.
- 2026-05-29: Spring Boot를 18080 포트, MySQL을 localhost:13306으로 연결해 `/api/health`를 호출했고 DB probe 성공을 확인했다.
- 2026-05-29: 실제 공공데이터 API로 `POST /api/public-data/apt-trades/import?lawdCd=11590&dealYmd=202605`를 호출했다. 최종 결과는 `totalCount=142`, `importedCount=121`, `skippedCount=21`, `alreadyImported=false`였다.
- 2026-05-29: 같은 import API를 재호출했을 때 성공 batch 이력으로 `alreadyImported=true`가 반환되어 일반 요청 재적재 skip 동작을 확인했다.
- 2026-05-29: 개발자가 Windows 로컬 MySQL80 서비스를 중지한 뒤 Manager가 Docker MySQL 컨테이너를 재생성했다. 최종 Docker MySQL은 표준 포트 `0.0.0.0:3306->3306/tcp`로 healthy 상태다.
- 2026-05-29: Spring Boot를 기본 datasource 포트 3306으로 실행해 `/api/health` DB probe 성공, 기존 `202605` 적재 데이터 131건 유지, import API 재호출 `alreadyImported=true`를 확인했다.

## 변경 파일 목록

- `src/main/java/com/ssafy/home/publicdata/client/PublicDataApiKeyProvider.java`
- `src/main/java/com/ssafy/home/publicdata/client/PublicDataAptTradeClient.java`
- `src/main/java/com/ssafy/home/publicdata/client/PublicDataAptTradeXmlParser.java`
- `src/main/java/com/ssafy/home/publicdata/controller/PublicDataImportController.java`
- `src/main/java/com/ssafy/home/publicdata/dto/AptTradeApiItem.java`
- `src/main/java/com/ssafy/home/publicdata/dto/AptTradeApiResponse.java`
- `src/main/java/com/ssafy/home/publicdata/dto/PublicDataImportResult.java`
- `src/main/java/com/ssafy/home/publicdata/mapper/HouseDealInsertCommand.java`
- `src/main/java/com/ssafy/home/publicdata/mapper/HouseUpsertCommand.java`
- `src/main/java/com/ssafy/home/publicdata/mapper/PublicDataImportMapper.java`
- `src/main/java/com/ssafy/home/publicdata/service/AptTradeImportCommandFactory.java`
- `src/main/java/com/ssafy/home/publicdata/service/PublicDataImportService.java`
- `src/main/resources/mappers/publicdata/PublicDataImportMapper.xml`
- `src/test/java/com/ssafy/home/publicdata/client/PublicDataApiKeyProviderTest.java`
- `src/test/java/com/ssafy/home/publicdata/client/PublicDataAptTradeXmlParserTest.java`
- `src/test/java/com/ssafy/home/publicdata/service/AptTradeImportCommandFactoryTest.java`
- `src/test/java/com/ssafy/home/publicdata/service/PublicDataImportServiceTest.java`
- `docs/sprints/Sprint3.md`

## 검증 결과

- `.\mvnw.cmd test`
  - 결과: 성공.
  - 테스트: 11개 실행, 11개 통과.
  - 검증 범위: 기존 health/response/house service/hash 테스트, Sprint3 API 키 미설정 실패, XML sample parsing, API row 변환, `api_row_hash` 중복 skip.
- `docker compose up -d mysql`
  - 결과: 실패.
  - 실패 사유: Docker API `npipe:////./pipe/dockerDesktopLinuxEngine`에 연결하지 못했다. Docker Desktop/daemon이 실행 중이 아니거나 접근할 수 없는 상태로 보인다.
  - 영향: 개발용 MySQL 컨테이너 기동, schema/data 실제 적용, Spring Boot 실행 후 `POST /api/public-data/apt-trades/import?lawdCd=11590&dealYmd=yyyyMM` live 검증은 미수행.
- Docker Desktop 실행 후 재검증
  - `docker compose up -d mysql`: 최초 재시도에서 3306 포트 충돌로 실패.
  - `MYSQL_PORT=13306` 적용 후 `docker compose up -d mysql`: 성공.
  - `docker compose ps`: `no-home-mysql` healthy, `0.0.0.0:13306->3306/tcp`.
- Spring Boot live 검증
  - 최초 부팅: `PublicDataAptTradeClient` 생성자 주입 실패로 실패.
  - 수정 후 `/api/health`: 성공. DB probe `connected=true`, `probe=1`.
  - 최초 import 점검: `totalCount=142` 중 기본 10건만 적재되는 페이지네이션 누락 발견.
  - 페이지네이션 보강 후 `.\mvnw.cmd test`: 성공. Tests run: 12, Failures: 0, Errors: 0.
  - 페이지네이션 보강 후 import API: 성공. `totalCount=142`, `importedCount=121`, `skippedCount=21`.
  - 최종 DB 확인: `house_deals`의 `lawd_cd=11590`, `deal_ymd=202605` row 수는 131건.
  - import API 재호출: `alreadyImported=true`로 일반 요청 skip 확인.
- Docker MySQL 표준 포트 재검증
  - Windows 로컬 MySQL80 서비스 중지 후 `docker compose down`, `docker compose up -d mysql` 실행.
  - `docker compose ps`: `no-home-mysql` healthy, `0.0.0.0:3306->3306/tcp`.
  - Spring Boot 기본 datasource 포트 3306으로 `/api/health` 성공.
  - `house_deals`의 202605 동작구 row 수 131건 유지.
  - import API 재호출: `alreadyImported=true`.

## 남은 리스크 / 인계 사항

- 개발 표준은 Docker MySQL `localhost:3306`으로 정리했다. Windows 로컬 MySQL80 서비스가 다시 시작되면 3306 포트 충돌이 재발할 수 있다.
- 실제 공공데이터 API 호출은 local config의 `public-data.service-key`와 네트워크 접근이 있어야 검증 가능하다. 2026-05-29 live 검증에서는 202605 동작구 데이터를 실제 호출했다.
- 기존 Docker volume이 이미 있으면 `/docker-entrypoint-initdb.d`의 `schema.sql`, `data.sql`이 재실행되지 않을 수 있다. 데이터 삭제가 필요한 `docker compose down -v`는 사용자 승인 없이 실행하지 않는다.
- API 명세의 일부 응답 필드는 데이터 건별로 누락될 수 있으므로 파서는 nullable 필드를 허용한다. 다만 거래일/아파트명/법정동/지번처럼 적재 키에 필요한 필드가 누락된 실제 row에 대한 정책은 이후 live 검증 결과에 따라 보강할 수 있다.
## Reviewer 검증 결과

- 2026-05-29: Reviewer가 Sprint3 결과물을 `docs/PRD.md`, `docs/spec.md`, 본 Sprint 계약, 관련 소스, 테스트 및 Docker/DB 상태 기준으로 검증했다.
- 판정: Pass.

### 확인 내용

- Sprint3 계약 범위인 공공데이터 아파트 매매 실거래가 API client/parser/import service/controller/mapper가 존재한다.
- `PublicDataImportController`는 Service만 호출하고, SQL은 MyBatis Mapper XML에 분리되어 Controller-Service-Mapper 계층을 지킨다.
- API 키는 `PublicDataApiKeyProvider`가 `${public-data.service-key:}`로 주입받고, 미설정 시 명확히 실패한다.
- `.gitignore`에 `config/application-local.properties`와 `src/main/resources/application-local.properties`가 포함되어 있으며, 저장소/문서/소스 검색에서 실제 API 키 값 노출은 발견하지 못했다.
- XML parser는 `sggCd`, `umdNm`, `jibun`, `aptNm`, `buildYear`, `dealYear`, `dealMonth`, `dealDay`, `dealAmount`, `excluUseAr`, `floor` 및 nullable 보조 필드를 DTO로 읽는다.
- `api_row_hash`는 `source_api`, `lawd_cd`, `deal_ymd`, `umd_nm`, `jibun`, `apt_nm`, `deal_year`, `deal_month`, `deal_day`, `deal_amount`, `exclu_use_ar`, `floor` 기준으로 생성되며, 금액 콤마 제거와 trim 정규화를 적용한다.
- `regions`, `houses`, `house_deals`, `public_data_import_batches` 적재 책임은 Service/Mapper에 있고, `house_deals`는 `api_row_hash` 기반 `INSERT IGNORE`로 중복 row를 skip한다.
- 성공 batch skip 조건은 `status='success'`뿐 아니라 `total_count <= imported_count + skipped_count`를 요구하도록 보강되어 부분 성공 batch가 일반 요청을 막지 않는다.
- 페이지네이션은 `pageNo`, `numOfRows`를 사용하며 `processedCount >= totalCount`까지 반복 호출하는 구조다.
- Vue, Kakao Map, 회원/인증, 관리자 강제 재적재 API, 스케줄러 등 Sprint3 제외 범위 구현은 발견하지 못했다.

### Reviewer 실행 검증

- `.\mvnw.cmd test`
  - 결과: 성공.
  - Tests run: 12, Failures: 0, Errors: 0, Skipped: 0.
- `docker compose ps`
  - 결과: `no-home-mysql` healthy.
  - 포트: `0.0.0.0:3306->3306/tcp`, `[::]:3306->3306/tcp`.
- `docker compose exec mysql mysql -uno_home -p... no_home -e "..."`
  - 결과: `house_deals`의 `lawd_cd=11590`, `deal_ymd=202605` row 수 131건.
  - import batch: `status=success`, `total_count=142`, `imported_count=121`, `skipped_count=21`.
- `netstat -ano | findstr 3306`
  - 결과: 3306 listener PID는 Docker 포트 프록시로 보이는 `4108`이며, 이전 로컬 `mysqld.exe` PID 4656 점유는 보이지 않았다.

### 남은 리스크

- Windows `MySQL80` 서비스 상태는 Reviewer 권한에서 `Get-CimInstance Win32_Service`가 `Access is denied`로 실패해 직접 확인하지 못했다. 대신 Docker Compose 상태와 3306 포트 점유 상태로 최종 DB 환경을 확인했다.
- Docker MySQL은 현재 `localhost:3306`을 사용한다. Windows `MySQL80` 서비스가 재시작되면 포트 충돌이 재발할 수 있다.
- live API 검증은 `202605` 동작구 범위에서 수행되었다. 다른 월/다른 지역의 누락 필드 정책은 이후 Sprint에서 검색/적재 범위를 넓힐 때 추가 검증이 필요하다.
