# working-memory.md

## Latest Review Update

- 2026-06-14: Sprint12 legal-dong dropdown stabilization completed. Backend `/api/regions?lawdCd={code}` now merges repaired DB region rows with a Seoul 25-district legal-dong catalog, so districts with no prior imported deals still expose selectable legal dongs. Mojibake region names are repaired before returning API responses, and the frontend mojibake guard was broadened for stale or cached region labels. Verification passed: `.\mvnw.cmd test` (43), `npm run test:auto-import` (3), and `npm run build`.

- 2026-06-14: Sprint11 implementation completed for Founder pink search UI cleanup. Frontend result cards now use matching `result-card`/`item-meta-grid` styling, selected/hover/status accents use `#FBEAEB`, map floating overlay was removed, and map status moved to the left panel. The `umdNm` field is now a DB-backed legal-dong select loaded from `/api/regions?lawdCd={code}`; options are deduped after mojibake repair and only existing DB/API dong values are offered. Verification passed: `npm run test:auto-import` (3), `npm run build`, `.\mvnw.cmd test` (42), `localhost:5173` HTTP 200, Vite proxy `/api/regions?lawdCd=11590`, and Chrome headless screenshot `target/sprint11-frontend.png`.

- 2026-06-12: Sprint10 folder-based split implemented. Repository now uses ordinary `Backend/`, `Frontend/`, and `Artifact/` folders, not submodules. Backend static Vue build artifacts were removed from `Backend/src/main/resources/static`; frontend development now uses Vite `/api` proxy to `http://localhost:8080`. Verification passed: `Backend` `.\mvnw.cmd test` (42 tests), `Frontend` `npm run test:auto-import` (3 tests), and `Frontend` `npm run build`. Local residual: root `.mvn/wrapper/maven-wrapper.jar` is locked by another process and remains physically present, while the backend copy is available.

- 2026-06-05: Sprint9 contract created for Kakao Map address geocoding and search pagination.
- Confirmed user decisions: current page only, 10 results per page, next/previous pagination, address-based Kakao geocoding from deal address, no separate no-coordinate policy because DB coordinates are not used for this Sprint.
- 2026-06-05: Sprint9 Generator implementation completed. Search now requests 10 results per page, includes previous/next pagination, changes Seoul-wide search to one paginated `sido=서울특별시` request, and adds Kakao Map SDK dynamic loading with address-based geocoder markers for the current page.
- Verified by Generator: `npm run test:auto-import`, `npm run build`, `npm run build:backend`, `.\mvnw.cmd process-resources`, `.\mvnw.cmd test`, Spring Boot static HTTP 200, and live `/api/houses/search` page size 10 responses for Gangnam and Seoul-wide searches.
- Residual risk: in-app browser verification still fails in this environment, so actual Kakao tiles/geocoder/markers need manual browser confirmation even though local `VITE_KAKAO_MAP_API_KEY` is now present.
- Next action: Sprint9 Reviewer verification from source/build/API evidence, plus manual browser screenshot confirmation for final submission.

- 2026-06-05: Sprint9 latest verification update. Local `.env` now has `VITE_KAKAO_MAP_API_KEY` present, copied from the existing local Kakao map key without printing the secret. Latest checks passed: `npm run test:auto-import` (3 passed), `npm run build`, `npm run build:backend`, `.\mvnw.cmd process-resources`, and `.\mvnw.cmd test` (42 passed). Spring Boot live check on `127.0.0.1:18082` returned static root HTTP 200 and page-size-10 search responses for Gangnam and Seoul-wide searches.
- Sprint9 residual risk updated: in-app browser verification still fails with `windows sandbox failed: spawn setup refresh`, so actual Kakao tile/geocoder/marker rendering must be confirmed manually in a working browser environment before final submission screenshots.
- Next action: Sprint9 Reviewer verification can proceed from source/build/API evidence, with manual Kakao map screenshot as the remaining submission-stabilization check.
- 2026-06-05: Spring Boot was started on `http://127.0.0.1:18082` and the user's local browser was opened for manual Kakao map verification. Sprint9 now includes a manual browser verification checklist for page 1 markers, page 2 marker replacement, and selected-item map focus.
- 2026-06-05: Kakao Maps JavaScript SDK dependency check passed with the local `VITE_KAKAO_MAP_API_KEY` without printing the secret. The SDK URL returned HTTP 200 and included `kakao.maps` plus services library content. Remaining Sprint9 evidence gap is visual browser confirmation of tiles/geocoded markers.
- 2026-06-05: Browser-style Kakao SDK domain check found the specific blocker. Kakao returns HTTP 401 when the SDK is requested with `Referer: http://127.0.0.1:18082/` or `Referer: http://localhost:18082/`. The local Kakao Developers Web platform site domain setting must include the verification origin before actual map tiles/geocoded markers can render.
- 2026-06-05: Sprint9 is blocked on external Kakao Developers domain registration, not on implementation. Resume after adding `http://127.0.0.1:18082` and/or `http://localhost:18082` to the JavaScript SDK domain / Web platform site domain list, then rerun the manual browser verification checklist.
- 2026-06-05: Sprint9 resumed after Kakao domain registration. User reported the map rendered initially on port 8080 but disappeared after search. Root cause was Vue re-rendering and removing Kakao's non-Vue map DOM, plus result-list page height expansion. Fixed by viewport-locking the app layout, making only the result list scroll, rendering Kakao markers after Vue status updates, and keeping map/marker storage out of reactive render state. Verification on `http://localhost:8080` passed: page 1 and page 2 both showed 10 results, 20 Kakao tile images, and 10 marker images. `npm run test:auto-import`, `npm run build:backend`, `.\mvnw.cmd process-resources`, and `.\mvnw.cmd test` all passed. Sprint9 Reviewer final status: Pass.

- 2026-06-05: Sprint8 Reviewer verification completed with Pass. Blocking findings: none.
- Verified: member signup/login/logout/current-member lookup/update/delete UI exists, Sprint6 member API paths are called, member API calls use `credentials: 'include'`, unauthenticated `/me` is treated as logged-out state, delete confirmation is required, password/hash fields are not exposed in live API responses, Sprint7 search/map shell remains present, and excluded backend/API/JWT/OAuth/Kakao/admin features were not introduced.
- Verification commands: `npm run test:auto-import`, `npm run build`, `npm run build:backend`, `.\mvnw.cmd process-resources`, `.\mvnw.cmd test`, Docker MySQL status check, and Spring Boot live member API flow on `127.0.0.1:18082` all passed.
- Residual risk: in-app browser visual/click verification still fails with `windows sandbox failed: spawn setup refresh`; use manual browser capture or another browser environment during submission stabilization.
- Next recommended planning area: Kakao Map/coordinate policy Sprint for real map display, then first additional feature Sprint for nearby commercial map.

- 2026-06-05: Sprint8 Generator implementation completed. Member frontend integration now connects the Sprint6 session/member APIs from Vue.
- Implemented: top account entry, signup, login, logout, current-member lookup on app mount/panel entry, profile update, delete confirmation, member API loading/success/error states, `credentials: 'include'` for session cookies, and Spring Boot static resource rebuild.
- Verified by Generator: `npm run build`, `npm run test:auto-import`, `npm run build:backend`, `.\mvnw.cmd process-resources`, `.\mvnw.cmd test`, static page HTTP 200 on `127.0.0.1:18082`, and live member API flow signup/login/me/update/delete/delete-after-401 against Docker MySQL.
- Follow-up: Sprint8 Reviewer verification was completed later on 2026-06-05 with Pass.

- 2026-06-05: Sprint7 Reviewer verification completed with Pass. Blocking findings: none.
- Verified: first screen search/map shell source structure, left search/list and right map placeholder separation, search fields for sido/sigungu/umdNm/aptName/dealMonth, `/api/houses/search` only, loading/error/empty states, list/detail method transition, disabled login/my-info placeholder, no Kakao Map SDK/member API/extra feature implementation, Vite HTTP 200, `npm run test:auto-import`, `npm run build`, `npm run build:backend`, `.\mvnw.cmd process-resources`, and `.\mvnw.cmd test` success.
- Residual risk: in-app browser visual/click verification still fails in this environment with `windows sandbox failed: spawn setup refresh`; reviewer compensated with HTTP, source, build artifact, and component-method checks. Real DB-backed browser search flow was not reverified in this pass.
- Next recommended planning area: M4 member frontend integration with Sprint6 session/member APIs, or a separate Kakao Map/coordinate policy Sprint if map delivery is prioritized.

- 2026-06-01: Sprint6 Reviewer verification completed with Pass; later Docker MySQL live verification was added by Generator.
- Verified: member signup/login/logout/me lookup/update/delete, BCrypt password hashing, password/hash response exclusion, session `LOGIN_MEMBER_ID`, physical delete, mapper integration tests, Docker MySQL live API flow, and `.\\mvnw.cmd test` success with 42 tests.
- Next recommended planning area: M4 main search/list/detail and map placeholder UI shell.

- 2026-06-01: Sprint5 Reviewer verification completed with Pass.
- Verified: DB-backed search after coverage fill, auto-import only for interpretable `LAWD_CD` + explicit `dealYmd`, complete coverage skip rule, Seoul 25 district resolver, Dongjak-gu `11590`, `autoImport=true` default, and no real public data API key exposure.
- Next recommended planning area: M3 member CRUD and session auth baseline.

- 2026-05-29: Sprint4 Reviewer verification completed with Pass.
- Verified: DB-backed unified search API, minimum-condition 400, exact region filters, partial apartment-name filter, newest-first sorting, pagination defaults/cap, nullable coordinates, empty-list response for missing DB data, Docker MySQL live search.
- Next recommended planning area: missing region/period coverage detection and automatic additional import before DB re-query.

## Dashboard

| 역할 | 상태 | 현재 작업 |
| --- | --- | --- |
| Manager | 진행 중 | Sprint 9 구현 결과 기록 완료 |
| Generator | 완료 | Sprint 9 지도 실제 연동 및 페이지네이션 구현 완료 |
| Reviewer | 완료 | Sprint 8 Pass 보고 완료 |

활성 스프린트: Sprint 9 - Kakao Map Address Geocoding and Search Pagination

### Previous Dashboard Snapshot

| 역할 | 상태 | 현재 작업 |
| --- | --- | --- |
| Manager | 대기 | Sprint 5 계약 작성 완료 |
| Generator | 완료 | Sprint 4 구현 완료 |
| Reviewer | 완료 | Sprint 4 Pass 보고 완료 |

활성 스프린트: Sprint 5 - 미적재 범위 자동 적재 후 검색 연결 기준선.

## Sprint 진행 요약

### Sprint 0 - Planning Harness

상태: 완료.

핵심 결과:

- Manager / Generator / Reviewer 운영 문서 구조를 만들었다.
- `PRD.md`, `spec.md`, `plan.md`, `working-memory.md`, `docs/sprints` 구조를 준비했다.
- `plan.md`는 Sprint 백로그가 아니라 Milestone 로드맵으로 역할을 정리했다.

### Sprint 1 - Spring Boot / Vue 골격과 DB 연결 기준선

상태: 조건부 Pass.

핵심 결과:

- 루트 Spring Boot Maven 프로젝트 골격이 추가됐다.
- Java 17, Spring Boot, MyBatis, MySQL Connector 기준 의존성이 설정됐다.
- 공통 JSON 응답과 `GET /api/health` 헬스 체크 API가 추가됐다.
- MyBatis mapper scan 설정과 `SELECT 1` 기반 DB probe mapper가 추가됐다.
- DB 비밀번호는 환경변수 또는 gitignore 대상 로컬 설정으로 분리하는 구조가 추가됐다.
- `frontend/` Vue/Vite 원본과 `npm run build:backend` 정적 빌드 포함 방식이 추가됐다.
- Maven wrapper가 추가되어 로컬 Maven 없이 백엔드 테스트를 실행할 수 있게 됐다.
- 개발용 MySQL `docker-compose.yml`과 `.env.example`이 추가됐다.

검증 상태:

- `npm install`, `npm run build`, `npm run build:backend`는 성공했다.
- `.\mvnw.cmd test`는 성공했다. 1개 테스트, failures/errors 0.
- `docker compose config`는 성공했다. 단, Docker CLI가 로컬 `C:\Users\SSAFY\.docker\config.json` 접근 권한 경고를 출력했다.
- Reviewer 검증 결과: 조건부 Pass. Blocking 이슈와 스펙 이탈은 없다.
- 라이브 MySQL 컨테이너 기동, DB probe, Spring Boot 앱 기동 후 `/api/health` HTTP 호출은 아직 검증하지 못했다.

남은 리스크:

- MySQL 컨테이너를 실제 기동하지는 않아 live DB probe는 아직 미검증이다.
- `/api/health` 컨트롤러/HTTP 직렬화 테스트와 Spring Boot 런타임 HTTP 호출 테스트가 없다.
- 현재 `@MapperScan("com.ssafy.home")`는 기준선에서는 허용되지만, 이후 non-mapper interface가 늘어나면 scan 범위를 좁히는 편이 좋다.
- Docker CLI의 로컬 config 접근 권한 경고가 남아 있다.
- npm audit에서 moderate 취약점 2개가 보고됐다. Sprint1 범위 밖 dependency 업데이트라 아직 수정하지 않았다.

다음 절차:

- M1은 조건부 완료로 본다.
- Sprint2 계약은 작성 완료했다.
- Sprint2 Generator 구현은 완료됐다.
- Reviewer에게 Sprint2 검증을 위임한다.
- Docker MySQL live 검증은 Docker daemon 접근 실패로 아직 남아 있다.

## 다음 Sprint 계획 초안

### Sprint 2 - 실거래가 스키마와 샘플 조회 기준선

상태: 조건부 Pass.

핵심 결과:

- `regions`, `houses`, `house_deals`, `public_data_import_batches` 스키마 SQL이 추가됐다.
- 서울특별시 동작구 `LAWD_CD=11590` 샘플 데이터 SQL이 추가됐다.
- `api_row_hash` 생성 유틸과 deterministic test가 추가됐다.
- `house` 패키지에 Controller / Service / Mapper / DTO 기준선이 추가됐다.
- 최소 REST API가 추가됐다.
  - `GET /api/regions?lawdCd=11590`
  - `GET /api/houses?aptName=...`
  - `GET /api/house-deals?lawdCd=11590&dealYmd=yyyyMM`
- `/api/health` HTTP 직렬화 테스트가 추가됐다.

검증 상태:

- `cmd /c .\mvnw.cmd test` 성공. Tests run: 5, Failures: 0, Errors: 0.
- `cmd /c .\mvnw.cmd clean test` 성공. main/test 재컴파일 포함.
- `docker compose config` 성공.
- Spring Boot를 18080 포트로 기동한 뒤 `/api/health` HTTP 호출 성공.
- Reviewer 검증 결과: 조건부 Pass. Blocking 이슈와 스펙 이탈은 없다.
- Docker daemon 접근 실패로 MySQL 컨테이너 기동, schema/data 적용, live Mapper/API 조회는 검증하지 못했다.

남은 리스크:

- Mapper XML은 live MySQL에서 직접 검증되지 않았다.
- Docker MySQL이 가능한 환경에서 샘플 API 3종을 재검증해야 한다.
- 기존 Docker volume이 있으면 `/docker-entrypoint-initdb.d` SQL이 재실행되지 않을 수 있다.
- `/api/regions`, `/api/houses`, `/api/house-deals`의 MockMvc 또는 live DB HTTP 테스트는 없다.

### Sprint 3 - 공공데이터 API 적재 기준선

상태: Pass.

목표:
국토교통부 아파트 매매 실거래가 API에서 서울특별시 동작구 최신 조회 가능 계약년월 1개월 데이터를 가져와 DB 적재 경로를 만든다.

권장 범위:

- 공공데이터 API 클라이언트 구조 작성.
- API 키는 `application-local.properties` 같은 gitignored local config로만 주입.
- 개발용 MySQL을 기동하고 Spring Boot DB 연결을 확인한 뒤 실제 적재 검증을 시도.
- XML 응답 파싱 DTO 작성.
- API row를 `regions`, `houses`, `house_deals`, `public_data_import_batches`에 적재하는 Service 작성.
- `api_row_hash` 기반 중복 skip 구현.
- 미적재 요청 시 부족분만 적재하는 정책의 기준선 구현.
- 운영/관리자용 전체 재적재 API는 아직 제외.
- Docker MySQL live 검증은 환경 가능 시 함께 수행하고, 불가하면 리스크로 기록.
- 기존 Docker volume 초기화가 필요할 수 있지만 `docker compose down -v`는 데이터 삭제 명령이므로 사용자 승인 없이 실행하지 않는다.

Manager 점검 결과:

- Docker Desktop 실행 후 개발용 MySQL 컨테이너를 기동했다.
- 최초에는 로컬 MySQL80 서비스와 3306 포트가 충돌해 `MYSQL_PORT=13306`을 임시 사용했다.
- 이후 개발자가 로컬 MySQL80 서비스를 중지했고, Docker MySQL을 표준 포트 `3306:3306`으로 재생성했다.
- Spring Boot `/api/health`에서 DB probe 성공을 확인했다.
- Generator 구현에서 런타임 생성자 주입 실패와 공공데이터 API 페이지네이션 누락을 발견해 보강했다.
- `.\mvnw.cmd test` 성공. Tests run: 12, Failures: 0, Errors: 0.
- 실제 API 호출 `POST /api/public-data/apt-trades/import?lawdCd=11590&dealYmd=202605` 성공.
- 최종 import batch: `totalCount=142`, `importedCount=121`, `skippedCount=21`.
- 최종 DB 확인: `house_deals`의 202605 동작구 row 수는 131건.
- 같은 import API 재호출 시 `alreadyImported=true`로 일반 요청 skip을 확인했다.
- 최종 개발 DB 기준은 Docker MySQL `localhost:3306`이다.
- Reviewer 검증 결과: Pass. Blocking 이슈와 스펙 이탈은 없다.

### Sprint 4 - 지역/아파트명 기반 실거래가 검색 API 기준선

상태: Pass.

목표:
적재된 실거래가 DB 데이터를 사용해 지역/아파트명 기반 검색을 안정적으로 조회하는 REST API 기준선을 만든다.

권장 범위:

- 현재 분리된 `/api/regions`, `/api/houses`, `/api/house-deals` 조회를 과제 요구의 “지역/아파트명 기반 실거래가 검색” 흐름으로 정리한다.
- `GET /api/houses/search?lawdCd=&sido=&sigungu=&umdNm=&aptName=&dealYmd=` 또는 이에 준하는 통합 검색 API 계약을 확정한다.
- 동작구 `202605` 실제 적재 데이터 기준으로 시/구/동 지역 검색과 아파트명 검색을 검증한다.
- Sprint4 검색은 DB에 이미 적재된 범위 안에서만 결과를 반환한다.
- 검색 요청에서 부족한 지역/기간을 자동 추가 적재한 뒤 검색하는 흐름은 다음 Sprint로 분리한다.
- 장기 데이터 전략은 하이브리드 방식이다. 기본 시연/초기 범위는 선적재하고, 검색은 DB 우선 조회를 사용하며, 미적재 범위는 이후 Sprint에서 부족분만 API 호출해 적재한 뒤 DB에서 다시 조회한다.
- Sprint4 검색 API는 통합 엔드포인트 하나, 최소 조건 1개 필수, 지역명 정확 일치, 아파트명 부분 검색, 거래일 최신순, `page`/`size` 페이지네이션, nullable `lat`/`lng` 포함을 기준으로 한다.
- 검색 결과 DTO에 지도 표시를 위한 nullable `lat`, `lng`를 포함할지 결정한다. 좌표 확보 자체는 Sprint4 범위 밖으로 두는 것을 권장한다.
- MockMvc 또는 live DB 기반 검색 API 테스트를 추가한다.
- Vue 화면, Kakao Map 연동, 회원/인증은 아직 제외한다.

완료 기준 후보:

- 지역 검색이 `lawdCd`, `sido`, `sigungu`, `umdNm`, `dealYmd` 조건에 맞는 실거래가 목록을 반환한다.
- 아파트명 검색이 부분 문자열 조건에 맞는 실거래가 목록을 반환한다.
- 검색 API는 공통 `ApiResponse` 형식을 따른다.
- DB에 적재되지 않은 조건은 빈 목록을 반환하되 오류로 처리하지 않는다.
- 검색 조건이 모두 비어 있으면 실패 응답을 반환한다.
- `.\mvnw.cmd test`와 가능하면 Docker MySQL live 검색 검증이 성공한다.

계약 문서:

- `docs/sprints/Sprint4.md`

Generator/Manager 점검 결과:

- Generator가 통합 검색 API `GET /api/houses/search`를 구현했다.
- `.\mvnw.cmd test` 성공. Tests run: 18, Failures: 0, Errors: 0.
- Docker MySQL `localhost:3306` healthy.
- live 검색 검증 성공:
  - `lawdCd=11590&dealYmd=202605&page=1&size=5`: `totalCount=131`.
  - `sido=서울특별시&sigungu=동작구&dealYmd=202605&page=1&size=3`: `totalCount=131`.
  - `aptName=래미안`: `totalCount=4`.
  - `size=200` 요청 시 `size=100` cap 적용.
  - 미존재 `lawdCd=99999`는 빈 목록 반환.
  - 조건 없는 검색은 HTTP 400.
- Manager가 nullable `lat`, `lng` 테스트 표현을 `nullValue()` 검증으로 보강했다.
- Reviewer 검증 결과: Pass. Blocking 이슈와 스펙 이탈은 없다.

### 우선순위 1: Sprint5 후보 - 미적재 범위 자동 적재 후 검색 연결 기준선

상태: 계약 작성 완료. Generator 위임 대기.

목표:
Sprint4 검색 API 앞에 coverage 판단과 부족분 자동 적재를 연결해, 명시된 지역/계약년월이 DB에 없으면 부족분만 공공데이터 API로 적재한 뒤 DB 검색을 다시 수행한다.

권장 범위:

- 검색 요청에서 자동 적재 대상 `LAWD_CD + dealYmd` 범위를 해석한다.
- 완료 적재 기준은 `status='success'`와 `total_count <= imported_count + skipped_count`로 본다.
- 이미 완료된 범위는 다시 적재하지 않는다.
- 미완료 범위만 기존 공공데이터 import Service로 적재한다.
- `dealYmd`가 없는 요청과 `aptName`만 있는 요청은 자동 적재하지 않는다.
- 서울특별시 구 단위 `LAWD_CD` resolver를 둔다.
- 동작구 1개 구와 가능하면 서울 25개 구 확장을 지원한다.
- 자동 적재 후 기존 DB 검색 Service를 재사용한다.
- `autoImport` 기본값은 `true`다. 사용자가 일반 검색을 수행하면 서버가 부족한 데이터를 자동 적재한 뒤 결과를 반환한다.
- 자동 적재를 원하지 않는 호출자는 `autoImport=false`를 명시한다.
- 자동 적재 실패 응답은 기존 `ApiResponse` 패턴을 유지한다.
- Vue, Kakao Map, 회원/인증, 관리자 강제 재적재, 스케줄러는 제외한다.

완료 기준 후보:

- coverage 판단 구조가 존재한다.
- 서울 구 코드 resolver가 존재한다.
- 미완료 범위만 적재한다.
- 자동 적재 후 DB 검색 결과를 반환한다.
- `.\mvnw.cmd test`와 가능하면 Docker MySQL live 검증이 성공한다.

계약 문서:

- `docs/sprints/Sprint5.md`

### 우선순위 2: Sprint2 잔여 검증 보강 후보

Sprint3에 포함하거나 별도 짧은 보강으로 처리할 수 있다.

- Mapper XML live DB 실행 검증.
- `schema.sql`/`data.sql` 실제 MySQL 적용 검증.
- `/api/regions`, `/api/houses`, `/api/house-deals` MockMvc 또는 live DB HTTP 테스트 추가.
- 기존 Docker volume이 있을 때 init SQL 재실행 정책 정리.

## 현재 결정 상태

확정:

- 프로젝트 기준선은 `pjt.pdf`의 SSAFY Home이다.
- 백엔드 전환 목표는 Spring Boot + MyBatis다.
- 프론트엔드는 Vue.js + Vite를 사용한다.
- 화면/API 구조는 Vue 화면이 Spring Boot REST API를 호출하는 방식이다.
- 최종 제출물은 Vue 빌드 결과를 Spring Boot 정적 리소스에 포함한다.
- 필수 기능은 실거래가 검색, 회원 CRUD, 로그인/로그아웃이다.
- 작업은 `AGENTS.md`, `PRD.md`, `spec.md`, `plan.md`, working memory, sprint log로 관리한다.
- 공공데이터 처리는 API/파일 데이터를 DB에 먼저 적재하고, 검색은 DB를 우선 조회한다.
- 회원 삭제 정책은 물리 삭제다.
- 첫 추가 기능은 주변 상권 지도다.
- 실거래가 검색 결과도 지도에 표시한다.
- 인증 방식은 세션 기반 로그인이다.
- Maven wrapper를 사용한다.
- Docker는 개발용 MySQL부터 도입한다.
- 앱 전체 Dockerfile은 지금은 보류하고, 제출 안정화 또는 필요 시 도입한다.
- M2 초기 적재 지역은 서울특별시 동작구다.
- M2 초기 API 지역 코드는 `LAWD_CD=11590`이다.
- M2 초기 적재 기간은 제일 최근 조회 가능 계약년월 1개월이다.
- M2 초기 주택 유형은 아파트 매매다.
- 미적재 요청은 부족분만 추가 적재한다.
- 요청 범위 전체 재적재는 운영/관리자용 강제 새로고침 옵션으로 분리한다.
- API 원본 row 중복 판정은 `source_api`, `lawd_cd`, `deal_ymd`, `umd_nm`, `jibun`, `apt_nm`, `deal_year`, `deal_month`, `deal_day`, `deal_amount`, `exclu_use_ar`, `floor` 기반 `api_row_hash`로 한다.
- 지역 기준은 법정동코드 10자리 중 앞 5자리인 `LAWD_CD`와 API 응답의 법정동명 `umdNm`을 사용한다.
- 좌표는 추후 Kakao Map API로 확보한다. M2에서는 좌표를 nullable로 둔다.
- 공공데이터 API 키는 `application-local.properties` 같은 gitignored local config로 주입하고 저장소에 커밋하지 않는다.

개발자 선택 필요:

- 주변 상권 지도에 사용할 데이터 출처, 업종 분류 범위, 지도 API.
- 실거래가 데이터의 좌표 확보 방식과 좌표가 없는 항목의 표시 정책.
- 세션 만료 시간, 로그인 필요 화면, 인터셉터 적용 범위.
- Vue/Vite 프로젝트 위치와 빌드 산출물 복사 방식.

## 트러블슈팅 아카이브

- 2026-05-29: 로컬 셸에 `pdftotext`가 없어 번들 Python의 `pypdf`로 PDF 텍스트를 추출했다.
- 2026-05-29: Sprint1 구현 중 로컬 PATH에 `mvn`이 없어 백엔드 Maven 검증을 실행하지 못했다.
- 2026-05-29: Sprint1 보강에서 Maven wrapper를 추가했고 `.\mvnw.cmd test`가 성공했다.
- 2026-05-29: Sprint1 보강에서 개발용 MySQL `docker-compose.yml`을 추가했고 `docker compose config`가 성공했지만, Docker CLI의 로컬 config 접근 권한 경고가 남았다.
- 2026-05-29: Vue SFC 기반 초기 빌드가 Vite import-analysis 오류를 냈고, Sprint1 기준선 유지를 위해 `frontend/src/main.js`의 Vue component object 방식으로 최소 앱을 구성했다.
- 2026-05-29: Reviewer가 Sprint1을 조건부 Pass로 판정했다. Blocking 이슈는 없고, live MySQL DB probe와 `/api/health` 런타임 HTTP 검증이 남았다.
- 2026-05-29: M2 초기 공공데이터 범위를 서울특별시 동작구, 최신 조회 가능 계약년월 1개월, 아파트 매매로 결정했다.
- 2026-05-29: 미적재 요청은 부족분만 추가 적재하고, 요청 범위 전체 재적재는 운영/관리자용 강제 새로고침 옵션으로 분리하기로 결정했다.
- 2026-05-29: API 원본 row 중복 판정은 주요 원본 필드 조합 기반 `api_row_hash`로 결정했다.
- 2026-05-29: Reviewer가 Sprint2를 조건부 Pass로 판정했다. Blocking 이슈는 없고, Docker daemon 접근 실패로 live MySQL 적용과 샘플 API 3종 실제 조회 검증이 남았다.
- 2026-05-29: 공공데이터 API 키는 gitignored `application-local.properties` 계열 local config로 주입하고 저장소에 커밋하지 않기로 결정했다.
- 2026-05-29: Sprint3 계약에 개발용 MySQL 기동, Spring Boot DB 연결 확인, 실제 적재 검증 또는 실패 사유 기록을 완료 기준으로 보강했다.
- 2026-05-29: Sprint3 Manager live 점검에서 Docker Desktop 실행 후 MySQL 컨테이너 기동에 성공했다. 3306 포트 충돌로 `MYSQL_PORT=13306`을 사용했다.
- 2026-05-29: Sprint3 Manager live 점검에서 `PublicDataAptTradeClient` 생성자 주입 실패를 발견해 수정했다.
- 2026-05-29: Sprint3 Manager live 점검에서 공공데이터 API 페이지네이션 누락으로 142건 중 10건만 적재되는 문제를 발견해 `pageNo`, `numOfRows` 기반 전체 페이지 적재로 보강했다.
- 2026-05-29: Sprint3 실제 API 적재 검증에서 서울 동작구 `202605` 데이터 `totalCount=142`를 처리했고, DB에는 중복 제외 131건이 적재됐다. 재호출 시 성공 batch 이력으로 skip되는 것을 확인했다.
- 2026-05-29: 개발자가 Windows 로컬 MySQL80 서비스를 중지한 뒤 Docker MySQL을 표준 `3306:3306` 포트로 재생성했다. Spring Boot 기본 datasource 포트로 DB probe와 import skip 재검증에 성공했다.
- 2026-05-29: Manager가 Sprint4 계약을 작성했다. 범위는 DB 기반 지역/아파트명 실거래가 검색 API 기준선이며, Vue/Kakao Map/회원/인증은 제외했다.
- 2026-05-29: 개발자 결정에 따라 Sprint4 검색 범위를 동별/아파트명에서 지역/아파트명 기반으로 넓혔다. 검색 조건은 `lawdCd`, `sido`, `sigungu`, `umdNm`, `aptName`, `dealYmd`를 기준으로 한다.
- 2026-05-29: 개발자 확인에 따라 Sprint4는 DB 기반 검색 API에 집중하고, 미적재 지역/기간 부족분 자동 적재 후 검색 연결은 다음 Sprint로 분리하기로 했다.
- 2026-05-29: 장기 데이터 전략은 하이브리드로 확정했다. 기본 범위는 선적재하고, 검색은 DB 우선이며, 미적재 범위는 이후 Sprint에서 부족분만 API 호출해 적재 후 DB 재조회한다.
- 2026-05-29: Sprint4 검색 API 세부 정책을 확정했다. 통합 엔드포인트, 최소 조건 1개 필수, 지역명 정확 일치, 아파트명 부분 검색, 거래일 최신순, `page`/`size` 페이지네이션, nullable `lat`/`lng` 포함.
- 2026-05-29: Sprint4 Generator 구현 후 Manager가 직접 점검했다. 테스트 18개 성공, Docker MySQL live 검색 성공, 조건 없음 400, size 상한, 빈 목록, nullable 좌표 응답을 확인했다.
- 2026-05-29: Sprint4 Reviewer 검증 결과 Pass. Blocking 이슈와 스펙 이탈은 없다.
- 2026-05-29: Manager가 Sprint5 계약을 작성했다. 범위는 미적재 지역/기간 coverage 판단, 부족분 자동 적재, DB 재검색 연결 기준선이다.
- 2026-05-29: 개발자 결정에 따라 Sprint5의 `autoImport` 기본값은 `true`로 확정했다. 검색 시 부족한 데이터는 서버가 자동 적재 후 반환하며, 자동 적재 실패 응답은 기존 `ApiResponse` 패턴을 유지한다.
## Reviewer 누적 기록

- 2026-05-29: Sprint3 Reviewer 검증 결과 Pass. `.\mvnw.cmd test` 성공(12 tests), Docker MySQL `localhost:3306` healthy, DB의 동작구 `202605` 거래 131건 및 import batch `success / 142 / 121 / 21` 확인.
- 남은 리스크: Windows `MySQL80` 서비스가 재시작되면 3306 포트 충돌이 재발할 수 있다. Reviewer 권한에서는 서비스 상태 직접 조회가 거부되어 Docker/포트/DB 상태로 대체 확인했다.
- 2026-05-29: Sprint4 Reviewer 검증 결과 Pass. DB 기반 통합 검색 API, 최소 조건 400, 지역명 정확 일치, 아파트명 부분 검색, 최신순 정렬, 페이지네이션, nullable 좌표, 빈 목록 응답, Docker MySQL live 검색을 확인했다.
- 2026-06-01: Sprint5 Reviewer 검증 결과 Pass. Blocking 이슈는 없다. DB 기반 검색 후 coverage fill, 해석 가능한 `LAWD_CD` + 명시적 `dealYmd`에서만 자동 적재, 완료 coverage skip 기준, 서울 25개 구 resolver, 동작구 `11590`, `autoImport=true` 기본값, 실제 공공데이터 API key 미노출을 확인했다. 잔여 리스크는 서울 전체 검색 시 최대 25회 import와 API/network 실패 시 사용자 복구 안내가 최소 수준인 점이다.
