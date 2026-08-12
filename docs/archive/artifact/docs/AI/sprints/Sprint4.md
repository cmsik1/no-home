# Sprint 4: 지역/아파트명 기반 실거래가 검색 API 기준선

## Milestone 연결

- Milestone: M2 - 실거래가 스키마, 데이터 적재, 검색
- 목적: Sprint3에서 실제 DB에 적재한 실거래가 데이터를 기준으로 지역/아파트명 기반 실거래가 검색 REST API를 안정화한다.

## Manager 구현 지시

Generator는 `docs/spec.md`와 이 문서를 기준 계약으로 읽고, Sprint 4 범위만 구현한다.

이번 Sprint의 핵심은 공공데이터 API 재호출이 아니라, 이미 DB에 적재된 `regions`, `houses`, `house_deals` 데이터를 사용해 과제 필수 기능인 지역/아파트명 기반 실거래가 검색을 제공하는 것이다.

Vue 화면, Kakao Map 연동, 좌표 수집, 회원/인증, 관리자용 강제 재적재 API는 구현하지 않는다. 검색 결과는 나중에 Vue 화면과 지도에서 사용할 수 있도록 REST API 응답 DTO를 정리한다.

프롬프트는 실행 트리거이고, 구현 범위의 기준은 이 문서다. 범위 밖 작업이 필요하면 임의 구현하지 말고 "결정 필요 항목" 또는 "남은 리스크"에 기록한다.

## 확정 기술 결정

- 검색 데이터 원천: DB에 적재된 실거래가 데이터.
- 초기 검증 데이터: Docker MySQL `localhost:3306`의 서울 동작구 `LAWD_CD=11590`, `DEAL_YMD=202605` 데이터.
- 검색 범위: 아파트 매매 실거래가.
- 지역 검색 조건: `lawdCd`, `sido`, `sigungu`, `umdNm`를 지원한다.
- `lawdCd`가 있으면 지역 코드 기준으로 필터링하고, `sido`, `sigungu`, `umdNm`가 있으면 지역명 기준으로 추가 필터링한다.
- Sprint4 검색은 현재 DB에 적재된 범위 안에서만 결과를 반환한다.
- 미적재 지역/기간을 검색 요청 시 자동으로 추가 적재한 뒤 검색하는 흐름은 다음 Sprint로 분리한다.
- 장기 데이터 전략은 하이브리드 방식으로 한다. 기본 시연/초기 범위는 선적재하고, 검색은 항상 DB를 우선 조회한다. 미적재 범위는 이후 Sprint에서 부족분만 API 호출해 적재한 뒤 DB에서 다시 조회하도록 연결한다.
- 요청 범위 전체 재적재는 관리자용 강제 새로고침으로 분리한다.
- API 응답 형식: 기존 공통 `ApiResponse` 형식을 사용한다.
- 좌표: Sprint4에서는 새로 수집하지 않는다. 검색 응답에 포함하더라도 `lat`, `lng`는 nullable이다.
- 미적재 데이터 처리: Sprint4에서는 검색 시점 자동 적재를 구현하지 않는다. DB에 없으면 빈 목록을 반환한다.
- Sprint4 API 형태: 통합 검색 엔드포인트 하나를 기본으로 한다.
- 최소 검색 조건: `lawdCd`, `sido`, `sigungu`, `umdNm`, `aptName`, `dealYmd` 중 1개 이상을 요구한다.
- 지역명 검색 방식: `sido`, `sigungu`, `umdNm`는 정확 일치로 검색한다.
- 아파트명 검색 방식: `aptName`은 부분 문자열 검색을 허용한다.
- 정렬 기준: 거래일 최신순을 기본으로 한다.
- 페이지네이션: `page`, `size`를 지원한다. `page` 기본값은 1, `size` 기본값은 20, 권장 상한은 100이다.
- `dealYmd` 기본 동작: 값이 없으면 DB에 적재된 전체 계약년월 범위에서 검색한다.

## 범위

- 지역/아파트명 기반 실거래가 검색 API 계약 정리.
  - 권장: `GET /api/houses/search?lawdCd=&sido=&sigungu=&umdNm=&aptName=&dealYmd=`
  - 기존 `/api/regions`, `/api/houses`, `/api/house-deals`와 충돌하지 않게 구성한다.
  - 지역 검색과 아파트명 검색을 하나의 통합 검색 API로 처리할지, 별도 API로 유지할지 구현 중 판단하고 문서에 기록한다.
- 검색 Query/Mapper 작성 또는 기존 Mapper 보강.
  - `lawdCd` 필터.
  - `sido` 필터.
  - `sigungu` 필터.
  - `dealYmd` 필터.
  - `umdNm` 필터.
  - `aptName` 부분 문자열 검색.
  - 거래일 최신순 또는 일관된 정렬 기준.
- 페이지네이션 구현.
  - `page` 기본값 1.
  - `size` 기본값 20.
  - `size` 상한 적용. 권장 상한은 100.
- 검색 응답 DTO 작성.
  - 아파트명.
  - 시/도.
  - 시/군/구.
  - 법정동명.
  - 지번.
  - 건축년도.
  - 거래년월일.
  - 거래금액.
  - 전용면적.
  - 층.
  - nullable `lat`, `lng`.
- Service 계층 작성 또는 보강.
  - Controller가 Mapper/SQL을 직접 호출하지 않게 한다.
  - DB에 없는 조건은 예외가 아니라 빈 목록으로 반환한다.
- 테스트 작성.
  - Mapper 또는 Service 단위 테스트.
  - 가능하면 MockMvc 검색 API 테스트.
  - Docker MySQL이 가능하면 live DB 검색 검증.
- 실행/검증 명령과 결과를 이 문서에 기록.

## 제외 범위

- Vue 검색 화면 구현.
- Kakao Map API 연동.
- 좌표 수집/주소 지오코딩.
- 공공데이터 자동 추가 적재.
- 시/도 전체, 시/군/구 전체 등 상위 지역 검색 시 미적재 하위 지역을 자동 판별하고 적재하는 기능.
- 서울특별시 전체 구 코드 목록 관리.
- 운영/관리자용 강제 전체 재적재 API.
- 회원/인증 기능.
- 관심 지역 기능.
- 주변 상권 지도 기능.
- 아파트 전월세, 연립다세대 매매/전월세 검색.

## Generator 작업 지시

1. `docs/spec.md`와 이 문서를 먼저 읽는다.
2. Sprint1~Sprint3 구현 결과를 되돌리지 말고 기존 구조와 함께 동작하도록 최소 변경한다.
3. 검색은 DB에 적재된 데이터만 대상으로 한다. 공공데이터 API를 검색 중 직접 호출하지 않는다.
4. Controller - Service - Mapper 계층을 지킨다.
5. 검색 조건이 비어 있거나 조합되는 경우의 동작을 명확히 한다.
   - 조건이 모두 비어 있으면 명확한 실패 응답을 반환한다.
   - `page`, `size`는 기본값과 상한을 적용한다.
6. DB에 데이터가 없으면 빈 목록을 반환한다.
7. 기존 API와 새 API의 역할이 겹치면 Sprint4 문서에 정리한다.
8. 변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크를 이 문서에 기록한다.

Generator에게 전달할 프롬프트 예시:

```text
docs/spec.md와 docs/sprints/Sprint4.md를 읽고 Sprint4 범위만 구현해.
DB에 적재된 실거래가 데이터를 기준으로 지역/아파트명 기반 검색 REST API를 작성해.
공공데이터 API 재호출, Vue 화면, Kakao Map, 회원/인증, 관리자 강제 재적재 API는 구현하지 마.
Controller-Service-Mapper 계층을 지키고, 검색 결과 DTO와 테스트를 추가해.
Docker MySQL live 검증이 가능하면 202605 동작구 데이터로 지역/아파트명 검색을 확인해.
변경 파일, 구현 내용, 실행/검증 결과, 에러, 남은 리스크는 docs/sprints/Sprint4.md에 기록해.
```

## Reviewer 검증 지시

Reviewer는 Generator 구현 후 다음 기준으로 검증한다.

- `docs/spec.md`의 M2 검색 요구와 충돌하지 않는지 확인한다.
- 검색 API가 DB 우선 조회 원칙을 지키는지 확인한다.
- Controller-Service-Mapper 계층이 지켜지는지 확인한다.
- 지역 검색과 아파트명 검색이 실제 조건에 맞는 결과를 반환하는지 확인한다.
- 검색 조건이 없는 경우, 없는 데이터 조건, 부분 문자열 검색의 동작이 명확한지 확인한다.
- 최소 검색 조건, 지역명 정확 일치, 아파트명 부분 검색, 정렬, 페이지네이션이 계약대로 동작하는지 확인한다.
- 응답 DTO가 화면/지도 후속 작업에 필요한 최소 정보를 포함하는지 확인한다.
- 범위 밖 기능을 구현하지 않았는지 확인한다.
- 테스트와 live DB 검증 결과가 충분한지 확인한다.

## 완료 기준

- 지역 기반 실거래가 검색 API가 존재한다.
- 아파트명 실거래가 검색 API가 존재하거나, 지역/아파트명 검색을 포괄하는 통합 검색 API가 존재한다.
- 검색 API는 `ApiResponse` 형식으로 응답한다.
- 검색 결과는 거래 기본 정보와 주택 기본 정보를 함께 제공한다.
- DB에 없는 조건은 빈 목록을 반환한다.
- 모든 검색 조건이 비어 있으면 실패 응답을 반환한다.
- 검색 결과는 거래일 최신순으로 정렬된다.
- 검색 API는 `page`, `size` 페이지네이션을 지원한다.
- 공공데이터 API를 검색 시점에 직접 호출하지 않는다.
- `.\mvnw.cmd test`가 성공하거나 실패 사유가 기록된다.
- 가능하면 Docker MySQL `localhost:3306`의 동작구 `202605` 데이터로 live 검색 검증이 기록된다.
- 변경 파일, 구현 내용, 검증 결과, 남은 리스크가 이 문서에 기록된다.

## 검증 명령 후보

```bash
.\mvnw.cmd test
docker compose ps
.\mvnw.cmd spring-boot:run
```

Spring Boot 실행 후 예시:

```bash
curl "http://localhost:8080/api/houses/search?lawdCd=11590&dealYmd=202605&umdNm=상도동&page=1&size=20"
curl "http://localhost:8080/api/houses/search?sido=서울특별시&sigungu=동작구&dealYmd=202605&page=1&size=20"
curl "http://localhost:8080/api/houses/search?lawdCd=11590&dealYmd=202605&aptName=아파트&page=1&size=20"
```

환경 제약으로 실행하지 못한 명령은 실패로 숨기지 말고 사유를 기록한다.

## 결정 필요 항목

- 현재 Sprint4 시작 전 결정 필요 항목은 없다.

## 다음 Sprint 후보

- 미적재 지역/기간 부족분 자동 적재 후 검색 연결.
- 상위 지역 검색을 위한 지역 코드 기준선 관리.
  - 예: 서울특별시 전체 구 `LAWD_CD` 목록.
- 검색 요청 범위와 적재 완료 범위를 비교하는 coverage 판단 로직.

## 작업 로그

- 2026-05-29: Manager가 Sprint 4 계약을 작성했다.
- 2026-05-29: 개발자와 Manager가 장기 데이터 전략을 하이브리드 방식으로 정리했다. 기본 범위는 선적재하고, 그 밖의 검색 요청은 이후 Sprint에서 DB 우선 조회 + 부족분 API 적재 + DB 재조회 흐름으로 연결한다.
- 2026-05-29: 개발자와 Manager가 Sprint4 검색 API 세부 정책을 확정했다. 통합 엔드포인트, 최소 조건 1개 필수, 지역명 정확 일치, 아파트명 부분 검색, 거래일 최신순, `page`/`size` 페이지네이션, nullable `lat`/`lng` 포함을 기준으로 한다.
- 2026-05-29: Manager가 Generator 구현을 점검했다. live API 응답은 `lat:null`, `lng:null`을 포함하지만 MockMvc 테스트 표현이 `doesNotExist()`라 계약과 어긋나 보여 `nullValue()` 검증으로 보강했다.

## 변경 파일 목록

- 아직 Generator 구현 전.
- 2026-05-29 Manager 점검 후 `src/test/java/com/ssafy/home/house/controller/HouseControllerTest.java`의 nullable `lat`, `lng` 응답 검증을 계약에 맞게 수정했다.

## 검증 결과

- 아직 Generator 구현 전.
- 2026-05-29 Manager 점검:
  - `.\mvnw.cmd test`: 성공. Tests run: 18, Failures: 0, Errors: 0.
  - `docker compose ps`: `no-home-mysql` healthy, `0.0.0.0:3306->3306/tcp`.
  - `/api/health`: DB connected true.
  - `GET /api/houses/search?lawdCd=11590&dealYmd=202605&page=1&size=5`: 성공. `totalCount=131`, 최신순 5건 반환, `lat`/`lng`는 null 필드로 포함.
  - `GET /api/houses/search?sido=서울특별시&sigungu=동작구&dealYmd=202605&page=1&size=3`: 성공. `totalCount=131`.
  - `GET /api/houses/search?lawdCd=11590&dealYmd=202605&aptName=래미안&page=1&size=10`: 성공. `totalCount=4`.
  - `GET /api/houses/search?lawdCd=11590&dealYmd=202605&page=1&size=200`: 성공. `size=100`으로 cap 적용.
  - `GET /api/houses/search?lawdCd=99999&dealYmd=202605&page=1&size=5`: 성공. 빈 목록 반환.
  - `GET /api/houses/search`: HTTP 400 반환.

## 남은 리스크 / 인계 사항

- Windows `MySQL80` 서비스가 다시 시작되면 Docker MySQL의 3306 포트와 충돌할 수 있다.
- 현재 실제 적재 검증 데이터는 서울 동작구 `202605` 중심이다.
- 좌표는 아직 수집하지 않았으므로 지도 표시 품질은 이후 Kakao Map 연동 Sprint에서 다룬다.

## Generator Sprint4 Implementation Log

- Date: 2026-05-29.
- Role: Generator.
- Scope implemented:
  - Added unified DB-backed search API: `GET /api/houses/search?lawdCd=&sido=&sigungu=&umdNm=&aptName=&dealYmd=&page=&size=`.
  - Search source is only already-imported DB data.
  - Did not implement public data API recall, missing coverage auto-import, Vue screen, Kakao Map, member/auth, admin force refresh API, or scheduler.
- Policy implemented:
  - At least one search condition among `lawdCd`, `sido`, `sigungu`, `umdNm`, `aptName`, `dealYmd` is required.
  - Blank strings are normalized to null.
  - `sido`, `sigungu`, `umdNm` use exact match.
  - `aptName` uses partial match.
  - Results sort by `deal_date DESC, deal_id DESC`.
  - `page` default is 1, `size` default is 20, and `size` is capped at 100.
  - Response includes nullable `lat` and `lng`.
  - Empty result conditions return an empty list, not an exception.
- Changed files:
  - `src/main/java/com/ssafy/home/house/controller/HouseController.java`
  - `src/main/java/com/ssafy/home/house/service/HouseService.java`
  - `src/main/java/com/ssafy/home/house/mapper/HouseMapper.java`
  - `src/main/resources/mappers/house/HouseMapper.xml`
  - `src/main/java/com/ssafy/home/house/dto/HouseSearchCondition.java`
  - `src/main/java/com/ssafy/home/house/dto/HouseSearchPageResponse.java`
  - `src/main/java/com/ssafy/home/house/dto/HouseSearchResultResponse.java`
  - `src/test/java/com/ssafy/home/house/service/HouseServiceTest.java`
  - `src/test/java/com/ssafy/home/house/controller/HouseControllerTest.java`
- Test result:
  - Command: `.\mvnw.cmd test`
  - Result: success. Tests run: 18, Failures: 0, Errors: 0, Skipped: 0.
  - Covered by tests: missing minimum condition failure, page/size normalization and cap, exact region condition pass-through, partial apartment-name condition pass-through, empty list response, MockMvc search success/failure response.
- Live verification:
  - `docker compose ps`: `no-home-mysql` healthy, `0.0.0.0:3306->3306/tcp`.
  - Spring Boot temporary run: `.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=18080`.
  - `/api/health`: DB connected true.
  - `GET /api/houses/search?lawdCd=11590&dealYmd=202605&page=1&size=5`: success, `totalCount=131`, first page returned 5 newest rows.
  - `GET /api/houses/search`: HTTP 400 confirmed.
- Remaining risks:
  - Sprint4 search intentionally covers only already-imported data. Missing region/period auto-import and coverage judgment remain for the next Sprint.
  - Coordinates are still null until a later Kakao Map integration Sprint.
  - If Windows `MySQL80` starts again, it may conflict with Docker MySQL on port 3306.

## Reviewer Sprint4 Verification Log

- Date: 2026-05-29.
- Role: Reviewer.
- Result: Pass.
- Scope review:
  - `GET /api/houses/search?lawdCd=&sido=&sigungu=&umdNm=&aptName=&dealYmd=&page=&size=` is implemented.
  - Search uses already-imported DB data through `HouseController` -> `HouseService` -> `HouseMapper`.
  - No public data API recall, missing coverage auto-import, Vue screen, Kakao Map, coordinate collection, member/auth, admin force refresh API, or scheduler was added in Sprint4 search flow.
  - The public data import endpoint remains separate from search.
- Contract behavior verified:
  - At least one search condition is required. Empty `/api/houses/search` returns HTTP 400.
  - `sido`, `sigungu`, and `umdNm` are exact-match filters.
  - `aptName` is a partial-match filter.
  - Results are ordered by `deal_date DESC, deal_id DESC`.
  - `page` defaults to 1, `size` defaults to 20, and `size` is capped at 100.
  - Response DTO includes deal fields, house fields, and nullable `lat`, `lng`.
  - Nonexistent DB conditions return an empty list rather than an error.
- Commands executed:
  - `.\mvnw.cmd test`: success. Tests run: 18, Failures: 0, Errors: 0, Skipped: 0.
  - `docker compose ps`: `no-home-mysql` healthy, `0.0.0.0:3306->3306/tcp`.
  - Temporary Spring Boot run on `18080`: started successfully and connected to DB.
  - `GET /api/health`: success, database connected true.
  - `GET /api/houses/search?lawdCd=11590&dealYmd=202605&page=1&size=5`: success, `totalCount=131`, 5 newest rows returned, `lat`/`lng` included as null.
  - `GET /api/houses/search?sido=서울특별시&sigungu=동작구&dealYmd=202605&page=1&size=3`: success, `totalCount=131`.
  - `GET /api/houses/search?lawdCd=99999&dealYmd=202605&page=1&size=5`: success, empty list with `totalCount=0`.
  - `GET /api/houses/search`: HTTP 400 confirmed.
- Findings:
  - No blocking issue found.
  - The Sprint4 contract and completion criteria are satisfied.
- Remaining risks:
  - Search still only covers data already present in DB. Missing region/period coverage detection and automatic additional import remain for the next Sprint.
  - Coordinates remain null until Kakao Map integration.
  - If Windows `MySQL80` starts again, it can conflict with Docker MySQL on port 3306.
