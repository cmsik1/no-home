# Sprint 5: 미적재 범위 자동 적재 후 검색 연결 기준선

## Milestone 연결

- Milestone: M2 - 실거래가 스키마, 데이터 적재, 검색
- 목적: Sprint4의 DB 기반 검색 API 앞에 coverage 판단과 부족분 자동 적재를 연결해, 명시된 지역/계약년월 범위가 DB에 없으면 부족분만 공공데이터 API로 적재한 뒤 DB에서 다시 검색한다.

## Manager 구현 지시

Generator는 `docs/spec.md`, `docs/sprints/Sprint4.md`, 이 문서를 기준 계약으로 읽고 Sprint 5 범위만 구현한다.

이번 Sprint의 핵심은 검색 API의 조회 정확도를 유지하면서, 사용자가 명시한 지역/계약년월이 아직 적재되지 않은 경우 부족한 범위만 자동으로 적재한 뒤 같은 DB 검색 흐름을 재사용하는 것이다.

검색 자체는 여전히 DB 조회가 기준이다. 공공데이터 API는 coverage가 부족한 지역/계약년월을 채우는 적재 단계에서만 호출한다.

Vue 화면, Kakao Map 연동, 좌표 수집, 회원/인증, 관리자용 강제 전체 재적재 API, 대량 배치 스케줄러는 구현하지 않는다.

프롬프트는 실행 트리거이고, 구현 범위의 기준은 이 문서다. 범위 밖 작업이 필요하면 임의 구현하지 말고 "결정 필요 항목" 또는 "남은 리스크"에 기록한다.

## 확정 기술 결정

- 장기 데이터 전략: 하이브리드.
  - 기본 시연/초기 범위는 선적재한다.
  - 검색은 DB를 우선한다.
  - 미적재 지역/기간은 부족분만 API 호출해 적재한 뒤 DB에서 다시 조회한다.
  - 요청 범위 전체 재적재는 관리자용 강제 새로고침으로 분리한다.
- Sprint5 자동 적재 대상은 아파트 매매 실거래가로 제한한다.
- 자동 적재는 `dealYmd`가 명시된 요청에서만 수행한다.
  - `dealYmd`가 없으면 기존 Sprint4처럼 DB에 이미 적재된 전체 계약년월 범위에서만 검색한다.
- 자동 적재는 지역 범위를 해석할 수 있을 때만 수행한다.
  - `lawdCd`가 있으면 해당 코드 1개를 대상으로 한다.
  - `sido=서울특별시&sigungu=...`가 있으면 서울 구 코드 기준선에서 1개 `LAWD_CD`로 해석한다.
  - `sido=서울특별시`만 있고 `sigungu`가 없으면 서울 전체 구 코드 목록을 대상으로 한다.
  - 그 외 시/도는 Sprint5 범위 밖이다.
- `aptName`만 있는 검색은 지역 coverage를 특정할 수 없으므로 자동 적재하지 않고 DB 검색만 수행한다.
- 이미 성공적으로 적재 완료된 범위는 다시 적재하지 않는다.
- 부분 성공 batch는 완료된 coverage로 보지 않는다.
  - 기준: `status='success'`이고 `total_count <= imported_count + skipped_count`.
- 자동 적재 실패 시 전체 검색 요청을 성공으로 숨기지 않는다. 실패 응답 또는 명확한 오류 메시지를 반환한다.
- `autoImport` 기본값은 `true`다. 사용자가 일반 검색을 수행하면, 서버는 해석 가능한 미적재 지역/계약년월 범위를 자동으로 적재한 뒤 DB 검색 결과를 반환한다.
- 자동 적재를 원하지 않는 호출자는 `autoImport=false`를 명시한다.
- 자동 적재 실패 응답은 기존 `ApiResponse` 패턴을 유지한다.
- API key는 기존 `public-data.service-key` local config 주입 방식을 유지하고, 실제 key 값은 저장소/문서에 기록하지 않는다.

## 지역 코드 기준선

Sprint5에서는 서울특별시 구 단위 `LAWD_CD` 기준선을 코드 또는 데이터 구조로 둔다.

초기 필수 대상:

- 동작구: `11590`

서울 전체 검색 지원을 위해 가능한 경우 서울 25개 구 코드를 모두 포함한다.

- 종로구 `11110`
- 중구 `11140`
- 용산구 `11170`
- 성동구 `11200`
- 광진구 `11215`
- 동대문구 `11230`
- 중랑구 `11260`
- 성북구 `11290`
- 강북구 `11305`
- 도봉구 `11320`
- 노원구 `11350`
- 은평구 `11380`
- 서대문구 `11410`
- 마포구 `11440`
- 양천구 `11470`
- 강서구 `11500`
- 구로구 `11530`
- 금천구 `11545`
- 영등포구 `11560`
- 동작구 `11590`
- 관악구 `11620`
- 서초구 `11650`
- 강남구 `11680`
- 송파구 `11710`
- 강동구 `11740`

## 범위

- coverage 판단 Service 작성 또는 보강.
  - 검색 조건에서 자동 적재 대상 `LAWD_CD` 목록을 해석한다.
  - 각 `LAWD_CD + dealYmd + houseType=apartment + dealType=sale` 범위가 완료 적재 상태인지 확인한다.
  - 미완료 범위만 적재 대상으로 반환한다.
- 지역 코드 resolver 작성.
  - `lawdCd` 직접 입력.
  - `sido=서울특별시&sigungu=동작구` 같은 시/구명 입력.
  - `sido=서울특별시` 전체 구 확장.
- 검색 API와 자동 적재 연결.
  - 권장: 기존 `GET /api/houses/search`에 `autoImport=true|false` 옵션을 추가한다.
  - 기본값은 `true`다.
  - `autoImport=false`일 때는 Sprint4와 동일하게 DB에 이미 적재된 데이터만 검색한다.
  - 자동 적재가 실행된 경우 적재 완료 후 기존 DB 검색 Service를 다시 호출한다.
- 응답 메타데이터 보강.
  - 검색 결과와 함께 자동 적재가 실행되었는지 알 수 있는 최소 메타데이터를 포함한다.
  - 예: `autoImportAttempted`, `importedRanges`, `skippedRanges`.
  - 기존 Sprint4 응답 호환성을 크게 깨지 않도록 한다.
- 테스트 작성.
  - coverage 완료 범위는 적재하지 않는 테스트.
  - coverage 부족 범위만 적재하는 테스트.
  - `dealYmd`가 없으면 자동 적재하지 않는 테스트.
  - `aptName`만 있으면 자동 적재하지 않는 테스트.
  - `sido=서울특별시&sigungu=동작구`가 `11590`으로 해석되는 테스트.
  - 가능하면 `sido=서울특별시`가 서울 구 코드 목록으로 확장되는 테스트.
  - 검색 API가 자동 적재 후 DB 검색 결과를 반환하는 Service/Controller 테스트.
- live 검증.
  - Docker MySQL `localhost:3306` 상태 확인.
  - 이미 적재된 동작구 `202605` 요청은 추가 import 없이 검색되는지 확인.
  - API key와 네트워크가 가능하면 아직 적재되지 않은 서울 구 1개, 최신 조회 가능 계약년월 1개월 범위를 자동 적재 후 검색하는 live 검증을 시도한다.
  - 서울 전체 25개 구 자동 적재 live 검증은 API 호출 수가 많으므로 필수 완료 기준으로 두지 않는다.
- 실행/검증 명령과 결과를 이 문서에 기록.

## 제외 범위

- Vue 검색 화면 구현.
- Kakao Map API 연동과 좌표 수집.
- 관리자용 강제 전체 재적재 API.
- 대량 배치 스케줄러.
- 전국 지역 코드 관리.
- 서울 외 시/도 상위 지역 자동 적재.
- 여러 계약년월 범위 자동 확장.
- 아파트 전월세, 연립다세대 매매/전월세.
- 회원/인증 기능.
- 주변 상권 지도 기능.

## Generator 작업 지시

1. `docs/spec.md`, `docs/sprints/Sprint4.md`, 이 문서를 먼저 읽는다.
2. Sprint1~Sprint4 구현 결과를 되돌리지 말고 기존 구조와 함께 동작하도록 최소 변경한다.
3. 자동 적재는 검색 그 자체가 아니라 검색 전 coverage 보강 단계로 분리한다.
4. 이미 완료 적재된 범위는 다시 적재하지 않는다.
5. 부분 성공 batch는 완료 coverage로 보지 않는다.
6. `dealYmd`가 없거나 지역 범위를 해석할 수 없으면 자동 적재하지 않고 기존 DB 검색으로 처리한다.
7. 공공데이터 API key는 기존 local config에서만 읽고, 실제 값을 기록하지 않는다.
8. Controller - Service - Mapper 계층을 지킨다.
9. 변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크를 이 문서에 기록한다.

Generator에게 전달할 프롬프트 예시:

```text
docs/spec.md, docs/sprints/Sprint4.md, docs/sprints/Sprint5.md를 읽고 Sprint5 범위만 구현해.
검색 요청의 지역/계약년월 coverage를 판단하고, 미적재 범위만 공공데이터 API로 적재한 뒤 기존 DB 검색을 다시 수행하는 기준선을 작성해.
dealYmd가 없는 요청, aptName만 있는 요청, 지역 범위를 해석할 수 없는 요청은 자동 적재하지 말고 기존 DB 검색만 수행해.
서울특별시 구 단위 LAWD_CD resolver를 추가하고, 동작구 11590 및 가능하면 서울 25개 구를 지원해.
Vue, Kakao Map, 회원/인증, 관리자 강제 전체 재적재 API, 스케줄러는 구현하지 마.
변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크는 docs/sprints/Sprint5.md에 기록해.
```

## Reviewer 검증 지시

Reviewer는 Generator 구현 후 다음 기준으로 검증한다.

- Sprint5 계약 범위와 완료 기준을 충족하는지 확인한다.
- 검색 API가 DB 조회를 기준으로 유지하고, 공공데이터 API는 coverage 부족분 적재 단계에서만 호출되는지 확인한다.
- 완료 coverage 판단이 `status='success'`와 `total_count <= imported_count + skipped_count`를 기준으로 하는지 확인한다.
- 이미 적재 완료된 범위를 재적재하지 않는지 확인한다.
- `dealYmd`가 없거나 `aptName`만 있는 요청에서 자동 적재하지 않는지 확인한다.
- 서울 구 코드 resolver가 동작구와 서울 전체 구 확장을 올바르게 처리하는지 확인한다.
- API key가 저장소/문서에 노출되지 않았는지 확인한다.
- 범위 밖 기능을 구현하지 않았는지 확인한다.
- 테스트와 가능한 live 검증 결과가 충분한지 확인한다.

## 완료 기준

- 검색 요청에서 자동 적재 대상 coverage를 판단하는 구조가 존재한다.
- 서울 구 단위 `LAWD_CD` resolver가 존재한다.
- 완료 적재 범위는 재적재하지 않는다.
- 미완료 범위만 공공데이터 API 적재 Service로 전달한다.
- 자동 적재 후 기존 DB 검색 API가 결과를 반환한다.
- `dealYmd`가 없는 요청은 자동 적재하지 않는다.
- `aptName`만 있는 요청은 자동 적재하지 않는다.
- API key가 저장소나 문서에 기록되지 않는다.
- `.\mvnw.cmd test`가 성공하거나 실패 사유가 기록된다.
- 가능하면 Docker MySQL live 검증이 기록된다.
- 변경 파일, 구현 내용, 검증 결과, 남은 리스크가 이 문서에 기록된다.

## 검증 명령 후보

```bash
.\mvnw.cmd test
docker compose ps
.\mvnw.cmd spring-boot:run
```

Spring Boot 실행 후 예시:

```bash
curl "http://localhost:8080/api/houses/search?lawdCd=11590&dealYmd=202605&autoImport=true&page=1&size=20"
curl "http://localhost:8080/api/houses/search?sido=서울특별시&sigungu=동작구&dealYmd=202605&autoImport=true&page=1&size=20"
```

API key와 네트워크가 있는 경우, 아직 적재되지 않은 서울 구 1개로 추가 live 검증을 시도할 수 있다.

환경 제약으로 실행하지 못한 명령은 실패로 숨기지 말고 사유를 기록한다.

## 결정 필요 항목

- 현재 Sprint5 시작 전 결정 필요 항목은 없다.

## 작업 로그

- 2026-05-29: Manager가 Sprint 5 계약을 작성했다.
- 2026-05-29: 개발자가 사용자 경험을 우선해 `autoImport` 기본값을 `true`로 확정했다. 일반 검색에서 부족한 데이터는 서버가 공공데이터 API로 적재한 뒤 DB 검색 결과를 반환한다. 자동 적재 실패 응답은 기존 `ApiResponse` 패턴을 유지한다.

## 변경 파일 목록

- 아직 Generator 구현 전.

## 검증 결과

- 아직 Generator 구현 전.

## 남은 리스크 / 인계 사항

- 서울 전체 자동 적재는 최대 25개 구 API 호출이 발생할 수 있다. Sprint5에서는 구조와 제한된 live 검증을 우선한다.
- API 호출 제한, 네트워크 오류, 일부 구의 응답 없음에 대한 세부 UX는 이후 보강될 수 있다.
- Windows `MySQL80` 서비스가 다시 시작되면 Docker MySQL의 3306 포트와 충돌할 수 있다.

## Generator Sprint5 Implementation Log

- Date: 2026-05-29.
- Role: Generator.
- Scope implemented:
  - Added `autoImport` option to `GET /api/houses/search`; default behavior is `autoImport=true`.
  - Search still returns DB-backed results. Public data API is called only when the request has an interpretable `LAWD_CD` coverage target and explicit `dealYmd`.
  - Added coverage judgment for `lawdCd + dealYmd + houseType=apartment + dealType=sale`.
  - Complete coverage requires `status='success'` and `total_count <= imported_count + skipped_count`.
  - Completed ranges are skipped; missing, partial, failed, or undercounted ranges are passed to the existing public data import service.
  - Requests without `dealYmd`, requests with only `aptName`, and requests whose region cannot be resolved do not auto-import and keep Sprint4 DB-only behavior.
  - Added Seoul district `LAWD_CD` resolver with all 25 districts. Dongjak-gu resolves to `11590`; Seoul-only requests expand to 25 district codes.
  - Added minimal response metadata: `autoImportAttempted`, `importedRanges`, `skippedRanges`.
  - Kept `ApiResponse` response pattern for validation and auto-import failure responses.
  - Updated import region upsert metadata so Seoul district imports store the resolved `sigungu` instead of always using Dongjak-gu.
- Changed files:
  - `src/main/java/com/ssafy/home/common/region/SeoulLawdCodeResolver.java`
  - `src/main/java/com/ssafy/home/house/controller/HouseController.java`
  - `src/main/java/com/ssafy/home/house/dto/AutoImportRangeResponse.java`
  - `src/main/java/com/ssafy/home/house/dto/HouseSearchPageResponse.java`
  - `src/main/java/com/ssafy/home/house/service/AutoImportException.java`
  - `src/main/java/com/ssafy/home/house/service/HouseService.java`
  - `src/main/java/com/ssafy/home/publicdata/mapper/PublicDataImportMapper.java`
  - `src/main/java/com/ssafy/home/publicdata/service/PublicDataImportService.java`
  - `src/main/resources/mappers/publicdata/PublicDataImportMapper.xml`
  - `src/test/java/com/ssafy/home/house/controller/HouseControllerTest.java`
  - `src/test/java/com/ssafy/home/house/service/HouseServiceTest.java`
  - `src/test/java/com/ssafy/home/publicdata/service/PublicDataImportServiceTest.java`
- Test result:
  - Command: `.\mvnw.cmd test`
  - Result: success. Tests run: 26, Failures: 0, Errors: 0, Skipped: 0.
  - Covered by tests: complete coverage skip, partial coverage import, success-but-undercounted coverage import, no auto-import without `dealYmd`, no auto-import for apt-name-only request, Dongjak-gu to `11590`, Seoul expansion to 25 district codes, DB search after auto-import, `autoImport=false`.
- Live verification:
  - `docker compose ps`: `no-home-mysql` healthy, `0.0.0.0:3306->3306/tcp`.
  - Temporary Spring Boot run on `18080`: started successfully after adding explicit constructor autowiring.
  - `/api/health`: success, database connected true.
  - `GET /api/houses/search?lawdCd=11590&dealYmd=202605&autoImport=true&page=1&size=5`: success, `totalCount=131`, `autoImportAttempted=true`, `importedRanges=[]`, `skippedRanges=[11590/202605]`; no additional import was run for the completed Dongjak-gu coverage.
  - `GET /api/houses/search?lawdCd=11680&dealYmd=202605&autoImport=false&page=1&size=1`: success before import, `totalCount=0`.
  - `GET /api/houses/search?lawdCd=11680&dealYmd=202605&autoImport=true&page=1&size=3`: success after auto-import, `totalCount=173`, `importedRanges=[11680/202605]`, first page returned DB search results for Gangnam-gu.
- Errors encountered:
  - First live Spring Boot run failed because `HouseService` had multiple constructors and Spring could not choose one. Fixed by annotating the production constructor with `@Autowired`.
  - One initial foreground-style live command timed out; the later background server workflow completed and the temporary process was stopped.
- Remaining risks:
  - Seoul-wide auto-import can trigger up to 25 public data API imports from one search request.
  - API/network/rate-limit failures return a failure `ApiResponse`, but user-facing recovery guidance remains minimal.
  - Public data API key value was not printed or recorded.
  - Coordinates remain null until a later Kakao Map integration Sprint.

## Reviewer Sprint5 Review Log

- Date: 2026-06-01.
- Role: Reviewer.
- Result: Pass.
- Blocking findings: none.
- Verified:
  - Search response remains DB-backed after the coverage fill step.
  - Public data API calls are limited to the auto-import coverage fill path.
  - Complete coverage uses `status='success'` and `total_count <= imported_count + skipped_count`.
  - Completed ranges are skipped, and incomplete ranges are imported.
  - Requests without `dealYmd`, apt-name-only requests, and `autoImport=false` requests do not auto-import.
  - `autoImport` defaults to `true`.
  - Seoul resolver includes all 25 district codes and resolves Dongjak-gu to `11590`.
  - No real public data API key exposure was found in repository files or sprint docs.
- Test review:
  - `.\mvnw.cmd test` result from Generator log is accepted: 26 tests, 0 failures, 0 errors, 0 skipped.
  - Reviewer noted a non-blocking test gap: `failed` batch and completely missing batch cases are not named explicitly in unit tests, although current implementation imports whenever coverage is not complete.
- Residual risks:
  - Seoul-wide auto-import can trigger up to 25 public data API imports from one search request.
  - API/network failure responses are clear enough for Sprint5, but user recovery guidance remains minimal.
