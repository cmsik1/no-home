# Sprint 9: Kakao Map Address Geocoding and Search Pagination

## 2026-06-05 Latest Manager Verification Update

- Supersedes earlier local-key residual notes in this file: local `.env` now has both `KAKAO_MAP_API_KEY` and `VITE_KAKAO_MAP_API_KEY` present. The key value was not printed or committed.
- A small UX correction was added after Generator implementation: house search API loading is no longer blocked until Kakao address geocoding finishes. The list can settle after the API response while map geocoding uses the separate map loading state.
- Latest frontend bundle copied to Spring Boot static resources:
  - `src/main/resources/static/assets/index-CzoUnHrE.js`
  - `src/main/resources/static/assets/index-D0UP2Wi1.css`
- Latest verification commands passed:
  - `npm run test:auto-import`: 3 tests passed.
  - `npm run build`: success.
  - `npm run build:backend`: success.
  - `.\mvnw.cmd process-resources`: success.
  - `.\mvnw.cmd test`: 42 tests passed, failures 0, errors 0, skipped 0.
- Latest live Spring Boot check on `http://127.0.0.1:18082`:
  - Root static page returned HTTP 200.
  - `GET /api/houses/search?lawdCd=11680&dealYmd=202605&autoImport=false&page=1&size=10` returned `page=1`, `size=10`, `items=10`, `totalCount=188`.
  - Seoul-wide `sido=서울특별시&dealYmd=202605&autoImport=false&page=1&size=10` returned `page=1`, `size=10`, `items=10`, `totalCount=330`.
- Latest Kakao SDK dependency check:
  - `https://dapi.kakao.com/v2/maps/sdk.js?...&libraries=services&autoload=false` returned HTTP 200 with the local `VITE_KAKAO_MAP_API_KEY`.
  - Response content included `kakao.maps` and services library content.
  - The key value was not printed.
- Latest browser-domain check:
  - With browser-style `Referer: http://127.0.0.1:18082/`, Kakao SDK returned HTTP 401.
  - With browser-style `Referer: http://localhost:18082/`, Kakao SDK returned HTTP 401.
  - This indicates the Kakao Developers Web platform site domain setting does not currently allow the local app origin.
  - Add the local origin used for verification, for example `http://127.0.0.1:18082` and/or `http://localhost:18082`, in Kakao Developers before final visual verification.
  - Official Kakao docs confirm the same requirement: Kakao SDK for JavaScript and Kakao Maps JavaScript API must be used from a registered JavaScript SDK domain / site domain.
  - References: https://developers.kakao.com/docs/ko/javascript/getting-started, https://developers.kakao.com/docs/latest/ko/app-setting/app, https://apis.map.kakao.com/web/guide/
- In-app browser verification is still blocked by the local browser runtime error `windows sandbox failed: spawn setup refresh`. Because of this, actual Kakao tile/geocoder/marker rendering still needs manual browser confirmation in the user's browser or another working browser environment.

## 2026-06-05 Reviewer Status

- Status: Blocked by external Kakao Developers domain setting, not final Pass yet.
- Passed evidence:
  - Source inspection confirms `SEARCH_PAGE_SIZE = 10`, previous/next pagination methods, and current-page marker arrays.
  - Source inspection confirms Kakao Maps JavaScript SDK dynamic loading with `VITE_KAKAO_MAP_API_KEY`, `libraries=services`, and `autoload=false`.
  - Source inspection confirms `kakao.maps.services.Geocoder().addressSearch(...)` is used with the current page item address.
  - Source inspection confirms markers are created with `kakao.maps.Marker`, old markers are cleared before refresh, and bounds/center are adjusted for current page markers.
  - Request builder tests confirm specific Seoul district search uses `lawdCd`, page/size, and auto import rules, while Seoul-wide search uses one paginated `sido=서울특별시` request with `autoImport=false`.
  - Build, backend static resource copy, Maven resources, Maven tests, Spring Boot static HTTP 200, and live search API page-size-10 checks passed.
- Missing evidence:
  - Actual browser screenshot/interaction proving Kakao tiles render and address geocoded markers appear on the map. Chrome headless currently shows the app-level Kakao SDK load failure because Kakao returns HTTP 401 for the local app origin Referer.
- Reviewer decision:
  - Do not mark Sprint9 final Pass until the Kakao Developers Web platform site domain is configured for the local origin and browser confirmation is captured.

## 2026-06-05 Blocker

- Blocking condition: Kakao Developers does not currently allow the local verification origin for the JavaScript key.
- Evidence:
  - Direct SDK request without browser Referer returns HTTP 200.
  - Browser-style SDK requests with `Referer: http://127.0.0.1:18082/` and `Referer: http://localhost:18082/` return HTTP 401.
  - Chrome headless screenshot shows the app-level Kakao domain registration error message.
- Required external action:
  - In Kakao Developers, add the local app origin to the JavaScript SDK domain / Web platform site domain list.
  - Recommended entries: `http://127.0.0.1:18082` and `http://localhost:18082`.
- Resume condition:
  - After domain registration, reload `http://127.0.0.1:18082`, run the manual browser verification checklist, capture map-marker screenshots, and then update this Sprint to Reviewer Pass if successful.

## 2026-06-05 Post-Domain Verification and Fix

- User registered Kakao Developers domains and reported a follow-up issue: the initial Kakao map rendered on port 8080, but after search the map disappeared.
- Reproduced on `http://localhost:8080` after domain registration.
- Root cause:
  - Kakao inserted tile/marker DOM into the Vue-managed `.map-canvas`.
  - Search result state updates caused Vue to re-render and remove Kakao's non-Vue child DOM.
  - The result list also expanded the page height, making the map frame unstable.
- Fix:
  - Keep the app shell at viewport height and make only the result list scroll.
  - Add explicit Kakao map `relayout()` calls around marker rendering.
  - Move Kakao map/marker storage out of reactive render state.
  - Render map markers after Vue status text updates have flushed, so Kakao's DOM remains present after search and page changes.
  - Restore normal UTF-8 Seoul district constants and visible UI strings touched during debugging.
- Latest static bundle:
  - `src/main/resources/static/assets/index-Cl14HC-M.js`
  - `src/main/resources/static/assets/index-B3p0nFl6.css`
- Verification:
  - `npm run test:auto-import`: 3 passed.
  - `npm run build:backend`: success.
  - `.\mvnw.cmd process-resources`: success.
  - `.\mvnw.cmd test`: 42 passed, failures 0, errors 0.
  - Chrome headless on `http://localhost:8080`, Seoul/Gangnam-gu/2026-05 search:
    - Page 1: 10 result cards, 20 Kakao tile images, 10 marker images, status `현재 페이지 10개 주소를 지도에 표시했습니다.`
    - Page 2 after clicking next: `2 / 19 페이지`, 10 result cards, 20 Kakao tile images, 10 marker images.
  - Screenshots:
    - `target/sprint9-localhost8080-after-search-final3.png`
    - `target/sprint9-localhost8080-page2-final.png`

## 2026-06-05 Reviewer Final Status

- Status: Pass.
- Blocking findings: none.
- Remaining note: `http://127.0.0.1:8080` was still not allowed by Kakao during verification; use `http://localhost:8080` or register the exact 127.0.0.1 origin before testing that URL.

## Manual Browser Verification Checklist

Use this checklist in the user's local browser because the Codex in-app browser cannot run in this environment.

1. Start the app on port 18082.
   - Command: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=18082"`
   - URL: `http://127.0.0.1:18082`
   - Kakao Developers Web platform site domain must include the same origin, for example `http://127.0.0.1:18082`.
2. Confirm the first screen shows the search form, result list area, and real Kakao map area instead of the old placeholder-only map.
3. Search with a district and deal month that has local DB data.
   - Example API-equivalent condition: `lawdCd=11680`, `dealYmd=202605`.
   - UI equivalent: Seoul / Gangnam-gu / May 2026.
4. Confirm the result list shows 10 items for the current page.
5. Confirm the map loads Kakao tiles and shows markers for geocoded current-page addresses only.
6. Click a list item and confirm the selected house summary updates and the map centers on that marker when the address was geocoded.
7. Click the next page button.
   - Confirm the list moves to the next 10 results.
   - Confirm old markers are cleared and markers are recreated for the new current page.
8. If any current-page address cannot be geocoded, confirm the list remains usable and only that marker is omitted.
9. Capture screenshots for final submission:
   - Page 1 list with Kakao map markers.
   - Page 2 list with changed markers.
   - Selected item detail/summary with map centered on marker.

## Milestone 연결

- Milestone: M4 - 주택 기본정보와 필수 UI / API 연결.
- 목적: Sprint7의 지도 placeholder를 실제 Kakao Map으로 전환하고, 검색 결과를 페이지당 10개로 제한해 현재 페이지의 주택만 지도에 표시한다.

## Sprint8 Reviewer 확인 결과

- Sprint8 Reviewer 검증 결과: Pass.
- Blocking findings: none.
- 확인된 사항:
  - 회원가입, 로그인, 로그아웃, 내 정보 조회/수정/삭제 UI가 존재한다.
  - Sprint6 회원 API 경로가 연결됐고 `credentials: 'include'`를 사용한다.
  - live API 응답에서 `password`, `passwordHash` 필드가 노출되지 않았다.
  - Sprint7 검색/지도 shell이 유지됐다.
  - `npm run test:auto-import`, `npm run build`, `npm run build:backend`, `.\mvnw.cmd process-resources`, `.\mvnw.cmd test`, Docker MySQL live 회원 API 검증이 성공했다.
- Sprint8 잔여 리스크:
  - in-app browser 직접 클릭/스크린샷 검증은 환경 오류로 실패한다.
  - 실제 브라우저 레이아웃/조작감은 제출 안정화 단계에서 수동 또는 다른 브라우저 환경으로 재확인하는 편이 좋다.
- 판단:
  - 위 리스크는 Kakao Map 연동 구현을 막지 않는다.

## 확정된 사용자 결정

- 실제 지도 API: Kakao Map JavaScript SDK.
- 좌표 확보 방식: DB 좌표를 기다리지 않고 각 매매 결과의 주소명으로 Kakao geocoder 검색을 수행한다.
- 지도 표시 대상: 전체 검색 결과가 아니라 현재 페이지에 표시되는 10개 항목만 지도에 마커로 표시한다.
- 페이지 크기: 검색 결과 한 페이지는 10개로 제한한다.
- 더 많은 결과 조회: 다음/이전 페이지 버튼으로 페이지를 이동한다.
- 좌표 없는 거래 정책: 별도 예외 정책을 두지 않는다. 주소명 geocoding 실패 항목은 지도 마커만 생략하고 목록은 유지한다.

## Kakao 공식 문서 확인

- Kakao 지도 Web API 공식 문서는 JavaScript SDK에서 `libraries=services`를 포함해 services 라이브러리를 로드하고 `kakao.maps.services.Geocoder()`를 사용한다.
- 주소 좌표 변환은 `geocoder.addressSearch(addr, callback, options)`로 수행하며, status가 `kakao.maps.services.Status.OK`일 때 결과 좌표를 사용할 수 있다.
- 참고: https://apis.map.kakao.com/web/documentation/
- 참고: https://apis.map.kakao.com/web/sample/addr2coord/

## Manager 구현 지시

Generator는 `docs/PRD.md`, `docs/spec.md`, `docs/sprints/Sprint9.md`, 프론트엔드 검색/지도 관련 파일, 그리고 필요한 경우 기존 검색 API 구현 파일만 읽고 Sprint9 범위만 구현한다.

이번 Sprint는 사용자가 검색한 현재 페이지의 결과를 실제 지도에 표시하는 작업이다. 백엔드 검색 API는 이미 `page`와 `size`를 지원하므로 새 검색 API를 만들지 않는다. 프론트엔드 검색 요청을 페이지당 10개로 조정하고, 페이지 이동 시 같은 조건으로 `page`만 바꿔 다시 조회한다.

기존 Sprint7에서 서울 전체 조회를 프론트엔드가 25개 구로 fan-out하던 방식은 페이지네이션과 맞지 않는다. Sprint9에서는 서울 전체 조회를 `sido=서울특별시`, `size=10`, `page=N`, `autoImport=false` 단일 요청으로 바꾼다. 특정 구 + 거래월 조회는 기존처럼 `lawdCd`와 `autoImport=true`를 사용할 수 있다.

Kakao Map API key는 `VITE_KAKAO_MAP_API_KEY`를 우선 사용한다. 키가 없거나 SDK 로드가 실패하면 지도 영역은 깨지지 않고 설정 필요 상태를 표시한다.

## 범위

- 검색 페이지네이션:
  - 기본 검색 page는 1.
  - 검색 size는 10.
  - 새 검색 조건으로 검색하면 page 1부터 조회한다.
  - 다음/이전 페이지 버튼을 제공한다.
  - 현재 페이지, 전체 건수, 전체 페이지 수를 표시한다.
  - 다음 페이지가 없으면 다음 버튼을 비활성화한다.
- 검색 요청 파라미터:
  - 특정 서울 구 선택 시 `lawdCd`, `dealYmd`, `autoImport` 규칙을 유지하되 `size=10`, `page=N`을 사용한다.
  - 서울 전체 선택 시 25개 구 fan-out 대신 `sido=서울특별시`, `dealYmd`, `autoImport=false`, `size=10`, `page=N` 단일 요청을 사용한다.
  - 서울 외 또는 지역 코드 미해석 조건은 기존 `sido`, `sigungu`, `umdNm`, `aptName`, `dealYmd` 조건으로 단일 요청한다.
- Kakao Map:
  - 지도 placeholder를 실제 Kakao Map container로 바꾼다.
  - SDK는 동적으로 로드한다.
  - `libraries=services`를 포함한다.
  - 현재 페이지 `items`의 주소로 geocoder `addressSearch`를 수행한다.
  - 주소명은 가능한 한 `sido + sigungu + umdNm + jibun`을 사용한다.
  - geocoding 성공 항목만 마커를 표시한다.
  - 마커 클릭 또는 선택 시 주택 요약을 지도 영역에 표시한다.
  - 목록 항목 선택 시 지도에서도 해당 항목 요약을 갱신한다.
  - 여러 마커가 있으면 bounds로 현재 페이지 마커가 보이게 조정한다.
- 상태 표시:
  - API key 없음.
  - SDK 로딩 중.
  - SDK 로드 실패.
  - 주소 검색 중.
  - 주소 검색 실패/마커 없음.
  - 현재 페이지 마커 수.
- 빌드/검증:
  - `npm run test:auto-import` 또는 이름 변경 시 동등한 프론트 테스트 성공.
  - `npm run build`.
  - `npm run build:backend`.
  - `.\mvnw.cmd process-resources`.
  - `.\mvnw.cmd test`.

## 제외 범위

- DB 좌표 저장/보강.
- 백엔드 좌표 컬럼 업데이트.
- 백엔드 신규 검색 API.
- 다중 `lawdCd` 검색 API.
- Kakao Local REST API 서버 호출.
- 지도 클러스터링.
- 길찾기/현재 위치/지도 검색창.
- 주변 상권 지도.
- 관심 지역.
- 회원 기능 변경.
- 모바일 전용 레이아웃 재설계.

## 완료 기준

- 검색 결과 요청이 page당 10개로 제한된다.
- 검색 화면에서 현재 페이지와 전체 페이지/전체 건수를 확인할 수 있다.
- 다음/이전 페이지로 검색 결과를 이동할 수 있다.
- 서울 전체 조회가 프론트 25개 구 fan-out이 아니라 단일 페이지 요청으로 처리된다.
- 지도 영역이 Kakao Map SDK를 사용해 렌더링된다.
- 현재 페이지 결과의 주소명으로 geocoding을 수행한다.
- 현재 페이지에서 geocoding 성공한 항목만 지도 마커로 표시된다.
- 주소 검색 실패 항목이 있어도 목록과 전체 화면은 깨지지 않는다.
- 지도 API key가 없거나 SDK 로드가 실패하면 명확한 상태 메시지가 표시된다.
- Sprint8 회원 UI와 Sprint7 검색/목록/상세 흐름이 유지된다.
- 범위 밖 기능이 구현되지 않는다.
- 검증 명령 결과와 잔여 리스크가 이 문서에 기록된다.

## Generator 작업 지시

Generator에게 전달할 프롬프트 예시:

```text
docs/PRD.md, docs/spec.md, docs/sprints/Sprint9.md와 프론트 검색/지도 관련 파일을 읽고 Sprint9 범위만 구현해.
검색 결과는 page당 10개로 제한하고, 이전/다음 페이지 이동을 구현해.
서울 전체 검색은 25개 구 fan-out을 중단하고 sido=서울특별시 단일 요청으로 바꿔.
Kakao Map JavaScript SDK를 VITE_KAKAO_MAP_API_KEY로 동적 로드하고 libraries=services를 포함해.
현재 페이지 items의 주소명(sido + sigungu + umdNm + jibun)을 kakao.maps.services.Geocoder().addressSearch로 좌표 변환해서 마커를 표시해.
geocoding 실패 항목은 마커만 생략하고 목록은 유지해.
API key 없음, SDK 로딩/실패, 주소 검색 중/마커 없음 상태를 지도 영역에 표시해.
DB 좌표 저장, 백엔드 신규 API, 클러스터링, 주변 상권, 관심 지역, 회원 기능 변경, 모바일 전용 재설계는 구현하지 마.
npm run test:auto-import, npm run build, npm run build:backend, .\mvnw.cmd process-resources, .\mvnw.cmd test를 실행하고 결과를 Sprint9 문서에 기록해.
```

## Reviewer 검증 지시

Reviewer는 Generator 구현 후 다음 기준으로 검증한다.

- Sprint9 계약 범위와 완료 기준을 충족하는지 확인한다.
- 검색 요청 size가 10이고 page 이동이 가능한지 확인한다.
- 서울 전체 조회가 25개 구 fan-out이 아닌 단일 요청인지 확인한다.
- Kakao SDK 로드에 `libraries=services`와 `VITE_KAKAO_MAP_API_KEY`가 사용되는지 확인한다.
- `kakao.maps.services.Geocoder().addressSearch`가 현재 페이지 items 주소명으로 호출되는지 확인한다.
- 지도 마커가 현재 페이지 결과만 대상으로 생성되는지 확인한다.
- API key 없음/SDK 실패/geocoding 실패 상태가 화면에서 처리되는지 확인한다.
- 회원 UI와 기존 검색/상세 흐름이 깨지지 않았는지 확인한다.
- DB 좌표 저장, 백엔드 신규 API, 주변 상권 등 제외 범위가 구현되지 않았는지 확인한다.
- 검증 명령 결과가 충분한지 확인한다.

## 작업 로그

- 2026-06-05: Manager가 Sprint8 Reviewer Pass와 사용자 결정을 확인하고 Sprint9 계약을 작성했다.
- 2026-06-05: Generator가 Sprint9 구현을 수행했다. 검색 결과를 페이지당 10개로 제한하고, 서울 전체 조회를 25개 구 fan-out이 아닌 단일 `sido=서울특별시` 페이지 요청으로 변경했다.
- 2026-06-05: Kakao Map JavaScript SDK 동적 로드와 주소명 기반 geocoding 마커 표시 로직을 추가했다. 로컬 `.env`에는 `VITE_KAKAO_MAP_API_KEY`가 비어 있어 실제 Kakao 지도 렌더링은 live 확인하지 못했고, 키 없음 상태 메시지 경로만 확인 가능한 상태다.

## 변경 파일 목록

- `frontend/src/App.vue`
- `frontend/src/houseSearchParams.js`
- `frontend/src/houseSearchParams.test.js`
- `frontend/src/style.css`
- `frontend/vite.config.js`
- `src/main/resources/static/index.html`
- `src/main/resources/static/assets/index-B-nMRWwb.js`
- `src/main/resources/static/assets/index-D0UP2Wi1.css`
- `docs/sprints/Sprint9.md`
- `docs/working-memory.md`

## 검증 결과

- `frontend`에서 `npm run test:auto-import`: success. Node 내장 테스트 3건 통과.
  - 특정 서울 구 + 거래월은 `lawdCd=11590`, `page=2`, `size=10`, `autoImport=true`.
  - 서울 전체 + 거래월은 단일 `sido=서울특별시`, `page=3`, `size=10`, `autoImport=false`.
  - 특정 서울 구 + 거래월 없음은 `autoImport=false`.
- `frontend`에서 `npm run build`: success. Vite production build 성공.
- `frontend`에서 `npm run build:backend`: success. Spring Boot 정적 리소스 경로에 최신 Vue bundle 생성.
- 루트에서 `.\mvnw.cmd process-resources`: success. `target/classes/static`에 최신 정적 리소스 반영.
- 루트에서 `.\mvnw.cmd test`: success. 42 tests, failures 0, errors 0, skipped 0.
- Spring Boot 임시 실행 `http://127.0.0.1:18082`: root 정적 페이지 HTTP 200 확인.
- Spring Boot live 검색 API 확인:
  - `GET /api/houses/search?lawdCd=11680&dealYmd=202605&autoImport=false&page=1&size=10`: 200 OK, `page=1`, `size=10`, `items=10`, `totalCount=188`.
  - `GET /api/houses/search?sido=서울특별시&dealYmd=202605&autoImport=false&page=1&size=10`: 200 OK, `page=1`, `size=10`, `items=10`, `totalCount=330`.
- 정적 소스 확인:
  - `VITE_KAKAO_MAP_API_KEY`를 사용한다.
  - Kakao SDK URL에 `libraries=services`와 `autoload=false`를 포함한다.
  - `kakao.maps.services.Geocoder()`와 `geocoder.addressSearch(...)`를 사용한다.
  - `kakao.maps.Marker`와 `kakao.maps.LatLngBounds`로 현재 페이지 마커를 표시한다.
  - `frontend/vite.config.js`에 `envDir: '..'`를 설정해 루트 `.env`의 `VITE_` 값을 읽을 수 있게 했다.

## 잔여 리스크 / 인계 사항

- Kakao Map JavaScript key가 로컬 `.env` 또는 빌드 환경의 `VITE_KAKAO_MAP_API_KEY`에 없으면 실제 지도는 렌더링되지 않고 설정 필요 상태가 표시된다.
- in-app browser 직접 검증은 기존 환경에서 실패했다. 가능하면 Spring Boot static HTTP, 소스 검증, 빌드 검증으로 보완하고, 제출 안정화에서 실제 브라우저 수동 캡처를 권장한다.
- 현재 로컬 `.env`에는 `VITE_KAKAO_MAP_API_KEY`가 비어 있어 실제 Kakao 지도 타일, geocoder 결과, 마커 렌더링은 live 확인하지 못했다. 키 설정 후 실제 브라우저에서 주소 검색/마커 표시를 재확인해야 한다.
- 주소명 geocoding은 `sido + sigungu + umdNm + jibun`을 사용한다. Kakao가 주소를 찾지 못한 항목은 목록에는 남고 마커만 생략된다.
- 지도 클러스터링은 제외했다. 현재 페이지가 10개로 제한되어 이번 Sprint에서는 필요성이 낮다.

## Generator 구현 기록

### 구현 요약

- 검색 기본 페이지 크기를 `SEARCH_PAGE_SIZE = 10`으로 설정했다.
- `searchPage`, `pageSize`, `totalPages`, 이전/다음 버튼, 페이지 요약 표시를 추가했다.
- 검색 조건 변경 후 검색하면 1페이지부터 조회하고, 페이지 이동은 기존 조건에서 `page`만 바꿔 다시 조회한다.
- `buildHouseSearchRequests`를 단일 페이지 요청 중심으로 바꿨다.
- 서울 전체 조회는 더 이상 25개 구 요청으로 분기하지 않고 `sido=서울특별시`, `size=10`, `page=N`, `autoImport=false` 단일 요청을 만든다.
- 특정 구 + 거래월 조회는 `lawdCd`, `dealYmd`, `autoImport=true`, `size=10`, `page=N`을 사용한다.
- Kakao Map SDK를 동적으로 로드하는 `loadKakaoMapsSdk`를 추가했다.
- SDK 로드 URL은 `https://dapi.kakao.com/v2/maps/sdk.js?appkey=...&libraries=services&autoload=false`를 사용한다.
- 지도 canvas를 실제 map container로 두고, API key 없음/SDK 로딩/SDK 실패/주소 검색 중/마커 없음 상태를 지도 패널에 표시한다.
- 현재 페이지 `items`에 대해 `sido + sigungu + umdNm + jibun` 주소를 만들고 `addressSearch`로 좌표를 얻는다.
- geocoding 성공 항목만 Kakao marker로 표시하고, 여러 마커가 있으면 bounds로 지도 영역을 맞춘다.
- 마커 클릭 또는 목록 선택 시 선택 주택 요약과 지도 중심이 갱신된다.

### 제외 범위 준수

- DB 좌표 저장/보강, 백엔드 신규 API, 다중 `lawdCd` API, Kakao Local REST 서버 호출, 지도 클러스터링, 주변 상권, 관심 지역, 회원 기능 변경은 구현하지 않았다.
