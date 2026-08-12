# Sprint 7: Main Search and Map Layout Shell

## Milestone 연결

- Milestone: M4 - 주택 기본정보와 필수 UI / API 연결.
- 목적: 앱의 메인 화면 골격을 만든다. 좌측에는 주택 검색창과 주택 목록을 배치하고, 목록 항목을 선택하면 좌측 영역이 상세 패널로 전환된다. 우측에는 추후 Kakao Map 연동을 위한 지도 UI placeholder를 둔다.

## Sprint6 Reviewer 검증사항 확인

- Sprint6 Reviewer 재검증 결과: Pass.
- Blocking findings: none.
- 확인된 사항:
  - 회원가입 필수값 검증, 이메일 중복 방지, BCrypt 해시 저장이 구현됐다.
  - API 응답 DTO는 비밀번호와 비밀번호 해시를 노출하지 않는다.
  - 로그인 성공 시 세션에는 `LOGIN_MEMBER_ID`만 저장한다.
  - `/api/members/me` 조회/수정/삭제는 세션의 현재 회원 ID만 사용한다.
  - 회원 삭제는 물리 삭제이며, 성공 후 세션을 무효화한다.
  - `MemberMapperTest`가 실제 MyBatis mapper SQL과 schema를 실행한다.
  - Docker MySQL + Spring Boot live API 흐름에서 회원 가입, 중복 가입 실패, 로그인, 세션 `/me` 조회, 수정, 삭제, 삭제 후 401, DB row count 0을 확인했다.
  - `.\\mvnw.cmd test` 성공: 42 tests, failures 0, errors 0, skipped 0.
- Sprint6에서 다음으로 넘기는 잔여 리스크:
  - 중복 회원가입 경쟁 상황의 API 응답 변환은 아직 별도 검증되지 않았다.
  - 인증 경계는 컨트롤러/세션 기준선이며 전역 인터셉터나 Spring Security 필터 기반 보호는 아니다.
  - stale Docker volume이 있는 다른 환경에서는 schema migration 또는 volume 재초기화가 필요할 수 있다.
- 판단:
  - Sprint7은 회원 기능을 본격 연결하지 않고 상단 로그인/내 정보 진입 자리만 만든다. Sprint6 잔여 리스크는 Sprint7 메인 레이아웃 구현을 막지 않는다.

## 확정된 사용자 결정

- Sprint7 범위: 메인 UI shell 먼저 구현한다.
- 지도 구현 수준: 실제 Kakao Map 연동 없이 지도 placeholder UI만 만든다.
- 좌측 검색 조건: `시/구/동`, `아파트명`, `거래월` 기본안을 사용한다.
- 주택 목록 기본 표시 정보: 아파트명, 동/지번, 거래금액, 전용면적, 층, 거래일.
- 목록-상세 상호작용: 목록 항목 선택 시 지도는 그대로 두고, 좌측 목록 영역이 주택 상세 패널 하나로 전환된다.
- 로그인 UI 위치: 상단 바에 로그인/내 정보 버튼 자리만 둔다.
- 모바일 레이아웃: 이번 Sprint에서는 신경 쓰지 않는다.

## Manager 구현 지시

Generator는 `docs/PRD.md`, `docs/spec.md`, `docs/sprints/Sprint7.md`, 그리고 프론트엔드 구현에 필요한 파일만 읽고 Sprint7 범위만 구현한다.

이번 Sprint는 프론트엔드 메인 화면의 큰 틀을 만드는 작업이다. 실제 Kakao Map SDK 연동, 지도 마커, 회원 폼, 주택 상세 API 추가, 즐겨찾기, 주변 상권, 비밀번호 찾기, 관리자 기능은 구현하지 않는다.

기존 `frontend` 구조는 단일 `main.js`와 `style.css` 기반의 최소 Vue 앱이다. Generator는 과도한 라우터/상태관리 라이브러리를 도입하지 않는다. 필요하면 작은 컴포넌트 파일을 추가할 수 있지만, 현재 구조에 맞춰 단순하게 구현한다.

검색 결과는 가능하면 기존 `GET /api/houses/search`를 호출해 실제 DB 기반 결과를 표시한다. 다만 Sprint7의 핵심은 UI shell이므로, 백엔드 실행 또는 데이터가 없는 환경에서도 화면 구조가 깨지지 않아야 한다. API 실패/빈 결과는 좌측 목록 영역에서 명확히 표시한다.

## 범위

- 메인 앱 레이아웃:
  - 상단 앱 바.
  - 상단 우측 로그인/내 정보 진입 버튼 자리.
  - 좌측 패널: 검색 폼과 결과 목록.
  - 우측 패널: 지도 placeholder.
- 검색 폼:
  - 시/도 입력 또는 선택 UI.
  - 구/군 입력 또는 선택 UI.
  - 동 입력.
  - 아파트명 입력.
  - 거래월 입력: `yyyyMM` 형식 또는 month input에서 `yyyyMM` 변환.
  - 검색 버튼.
  - 초기화 버튼.
- 검색 결과 목록:
  - 아파트명.
  - 동/지번.
  - 거래금액.
  - 전용면적.
  - 층.
  - 거래일.
  - 결과 수 또는 로딩/빈 결과 상태.
- 상세 패널:
  - 목록 항목을 선택하면 좌측 영역이 상세 패널로 전환된다.
  - 상세 패널에는 선택한 항목의 목록 기본 표시 정보를 더 여유 있게 보여준다.
  - 뒤로 가기 버튼으로 다시 목록으로 돌아간다.
  - 지도 placeholder는 선택 중에도 우측에 그대로 유지된다.
- 지도 placeholder:
  - 실제 지도 SDK를 로드하지 않는다.
  - 지도 영역의 시각적 자리, 선택된 주택 요약, 좌표 미연동 상태를 표현한다.
  - 마커, 지도 이동, 외부 API key는 사용하지 않는다.
- API 연결:
  - 가능하면 기존 `GET /api/houses/search`를 사용한다.
  - 검색 파라미터는 `sido`, `sigungu`, `umdNm`, `aptName`, `dealYmd`를 우선 사용한다.
  - `autoImport`는 명시하지 않거나 기존 기본값을 따른다. 자동 적재 UX 보강은 하지 않는다.
- 빌드/검증:
  - `npm run build` 또는 `npm run build:backend` 성공.
  - 가능하면 Spring Boot static build 또는 Vite dev 환경에서 검색 UI가 렌더링되는지 확인.

## 제외 범위

- Kakao Map 실제 연동.
- 지도 마커 표시.
- 지도 이동/확대/축소 기능.
- 좌표 수집 또는 좌표 보강.
- 회원 가입/로그인/내 정보 실제 폼 연결.
- 회원 API 호출.
- 주택 상세 신규 API 구현.
- 백엔드 검색 API 변경.
- 주변 상권, 즐겨찾기, 환경 정보.
- 비밀번호 찾기/재설정.
- 관리자 기능.
- JWT/OAuth.
- 모바일 전용 레이아웃 대응.
- 공공데이터 자동 적재 UX 보강.

## 완료 기준

- 첫 화면이 메인 주택 검색 UI로 시작한다.
- 좌측에는 검색 폼과 주택 목록 영역이 있다.
- 우측에는 실제 지도 대신 지도 placeholder UI가 있다.
- 검색 조건으로 `시/구/동`, `아파트명`, `거래월`을 입력할 수 있다.
- 검색 결과 목록은 아파트명, 동/지번, 거래금액, 전용면적, 층, 거래일을 표시한다.
- 목록 항목 선택 시 좌측 목록 영역이 상세 패널로 전환된다.
- 상세 패널에서 뒤로 가기를 누르면 목록으로 돌아간다.
- 지도 placeholder는 목록/상세 전환 중에도 우측에 유지된다.
- 상단 바에는 로그인/내 정보 진입 버튼 자리가 있다.
- API 로딩, 실패, 빈 결과 상태가 화면에서 확인 가능하다.
- 실제 Kakao Map, 회원 폼, 모바일 대응, 범위 밖 기능이 구현되지 않는다.
- `npm run build` 또는 `npm run build:backend`가 성공한다.
- 변경 파일, 구현 내용, 검증 결과, 에러, 잔여 리스크가 이 문서에 기록된다.

## Generator 작업 지시

Generator에게 전달할 프롬프트 예시:

```text
docs/PRD.md, docs/spec.md, docs/sprints/Sprint7.md를 읽고 Sprint7 범위만 구현해.
메인 화면을 좌측 검색/목록 패널과 우측 지도 placeholder 패널로 구성해.
검색 조건은 시/구/동, 아파트명, 거래월을 사용해.
목록은 아파트명, 동/지번, 거래금액, 전용면적, 층, 거래일을 표시해.
목록 항목을 선택하면 좌측 목록 영역이 상세 패널로 전환되고, 뒤로 가기로 목록에 돌아오게 해.
우측 지도 placeholder는 목록/상세 전환 중에도 그대로 유지해.
상단에는 로그인/내 정보 진입 버튼 자리만 만들어. 회원 폼이나 회원 API 연결은 구현하지 마.
Kakao Map SDK, 지도 마커, 주택 상세 신규 API, 백엔드 API 변경, 주변 상권, 즐겨찾기, 모바일 대응은 구현하지 마.
가능하면 기존 GET /api/houses/search를 호출해 검색 결과를 표시하고, API 실패/빈 결과 상태도 UI에 표시해.
npm run build 또는 npm run build:backend를 실행하고 결과를 docs/sprints/Sprint7.md에 기록해.
변경 파일, 구현 내용, 검증 결과, 에러, 잔여 리스크도 Sprint7 문서에 기록해.
```

## Reviewer 검증 지시

Reviewer는 Generator 구현 후 다음 기준으로 검증한다.

- Sprint7 계약 범위와 완료 기준을 충족하는지 확인한다.
- 첫 화면이 메인 검색/지도 shell인지 확인한다.
- 좌측 검색 폼과 목록, 우측 지도 placeholder가 명확히 구분되는지 확인한다.
- 검색 조건이 `시/구/동`, `아파트명`, `거래월` 기본안을 따르는지 확인한다.
- 목록 표시 정보가 아파트명, 동/지번, 거래금액, 전용면적, 층, 거래일을 포함하는지 확인한다.
- 목록 항목 선택 시 좌측 영역이 상세 패널로 전환되고, 뒤로 가기로 목록에 돌아오는지 확인한다.
- 지도 placeholder가 실제 Kakao Map 연동 없이 유지되는지 확인한다.
- 상단 로그인/내 정보 버튼 자리는 있으나 회원 폼/API 연결이 구현되지 않았는지 확인한다.
- 모바일 전용 대응, 지도 SDK, 마커, 주변 상권, 즐겨찾기, 백엔드 API 변경 등 제외 범위가 구현되지 않았는지 확인한다.
- API 로딩, 실패, 빈 결과 상태가 화면에서 확인 가능한지 확인한다.
- `npm run build` 또는 `npm run build:backend` 결과가 충분한지 확인한다.

## 작업 로그

- 2026-06-01: Manager가 Sprint6 Reviewer Pass 및 Docker MySQL live verification 기록을 확인하고 Sprint7 계약 초안을 작성했다.
- 2026-06-01: 개발자가 Sprint7 방향을 회원/인증 UI가 아닌 메인 검색/지도 UI shell로 조정했다.
- 2026-06-01: 개발자 결정 사항을 반영했다. 좌측 검색/목록, 우측 지도 placeholder, 목록 선택 시 좌측 상세 전환, 상단 로그인/내 정보 자리, 모바일 제외.
- 2026-06-01: localhost:8080에서 빈 화면이 보여 런타임 원인을 확인했다. Vue 런타임 template 컴파일 의존을 없애기 위해 메인 화면을 `App.vue` SFC로 분리하고, Vite Vue plugin 설정을 `vue()`로 수정했다. `npm run build:backend`와 `.\mvnw.cmd process-resources`를 실행해 Spring Boot가 참조하는 `target/classes/static`까지 최신 번들을 반영했다.
- 2026-06-01: 개발자 조정 요청을 접수했다. `서울특별시` 단독 선택 시 서울 전체 구 매매 현황을 조회해야 하고, 지역 선택은 직접 타이핑이 아니라 선택 목록 기반이어야 하며, 목록/상세의 깨진 한글 표시를 보정해야 한다.
- 2026-06-01: 조정 요청을 반영했다. 시도/시군구 입력을 선택 목록으로 변경하고, `서울특별시` 단독 선택 시 서울 25개 구 `lawdCd`를 순차 조회해 합산하도록 했다. API 응답의 mojibake 한글은 화면 표시 직전에 UTF-8로 보정한다.
- 2026-06-01: 상세 패널 상단 지역 표시(`sido`, `sigungu`)도 mojibake 보정을 타도록 수정했다. Docker MySQL 직접 조회 결과 현재 `house_deals`는 `lawd_cd=11590` 3건, 전체 3건뿐임을 확인했다.
- 2026-06-01: 개발자 확인에 따라 거래월 입력을 `month` 선택 하나로 정리하고, 구 선택 + 거래월 선택 시에만 `autoImport=true`를 사용하도록 조정했다. 서울 전체 조회는 `autoImport=false`를 유지한다. 로그인/내 정보 버튼은 다음 Sprint 연결 예정임을 알 수 있도록 disabled placeholder로 변경했다.
- 2026-06-01: 루트 `.env` 기반 API key 설정을 추가했다. Spring Boot는 `spring.config.import=optional:file:.env[.properties]`로 루트 `.env`를 읽고, `PUBLIC_DATA_SERVICE_KEY`를 `public-data.service-key`로 매핑한다. Kakao Map용 `KAKAO_MAP_API_KEY`, `VITE_KAKAO_MAP_API_KEY` 자리도 함께 추가했다.
- 2026-06-01: `.env`에 실제 공공데이터 API key 입력 후 새 Spring Boot 인스턴스(`18082`)로 자동 적재 live 검증을 수행했다. `GET /api/houses/search?lawdCd=11680&dealYmd=202605&autoImport=true&page=1&size=5`가 성공했고, 강남구 202605 데이터 188건이 DB에 적재됐다.
- 2026-06-01: Sprint7 autoImport 검증 작업을 추가했다. 프론트엔드에는 별도 테스트 러너 의존성이 없어 새 테스트 스택을 추가하지 않고 Node 내장 `node:test`로 순수 요청 파라미터 helper만 검증했다.

## 변경 파일 목록

- `frontend/src/main.js`
- `frontend/src/App.vue`
- `frontend/src/houseSearchParams.js`
- `frontend/src/houseSearchParams.test.js`
- `frontend/package.json`
- `frontend/src/style.css`
- `frontend/vite.config.js`
- `src/main/resources/static/index.html`
- `src/main/resources/static/assets/*`

## 검증 결과

- `npm run build:backend`: success.
- `.\mvnw.cmd process-resources`: success.
- `target/classes/static/index.html`이 새 bundle `/assets/index-D8A4gisC.js`를 참조하는 것을 확인했다.
- `localhost:8080`은 문서 갱신 시점에 연결되지 않았다. Spring Boot 재시작 후 브라우저에서 강력 새로고침이 필요하다.
- 조정 반영 후 `npm run build:backend`: success.
- 조정 반영 후 `.\mvnw.cmd process-resources`: success.
- 서울특별시 단독 조회는 프론트엔드에서 `autoImport=false`와 `size=100`으로 25개 구를 조회해 합산한다.
- 상세 지역 표시 보정 후 `npm run build:backend`: success.
- 상세 지역 표시 보정 후 `.\mvnw.cmd process-resources`: success.
- Docker MySQL 직접 조회: `SELECT lawd_cd, COUNT(*) FROM house_deals GROUP BY lawd_cd` 결과 `11590=3`; `SELECT COUNT(*) FROM house_deals` 결과 `3`.
- 거래월/autoImport/login placeholder 보강 후 `npm run build:backend`: success.
- 거래월/autoImport/login placeholder 보강 후 `.\mvnw.cmd process-resources`: success.
- `.env` 설정 연결 후 `.\mvnw.cmd test`: success, 42 tests, failures 0, errors 0, skipped 0.
- 자동 적재 live 검증: success. 응답은 `autoImportAttempted=true`, `importedRanges=[11680/202605 success]`, `totalCount=188`.
- Docker MySQL 확인: `house_deals`의 `lawd_cd=11680`, `deal_ymd=202605` row count는 188. `public_data_import_batches` 최신 row는 `status=success`, `total_count=188`, `imported_count=188`, `skipped_count=0`.
- autoImport 검증 추가 후 `frontend`에서 `npm run test:auto-import`: success. Node 내장 테스트 3건 모두 통과.
- autoImport 검증 추가 후 `frontend`에서 `npm run build`: success. Vite production build 성공.
- autoImport 검증 추가 후 루트에서 `.\mvnw.cmd test`: success. 42 tests, failures 0, errors 0, skipped 0.
- 검증된 프론트엔드 요청 조건:
  - `서울특별시 + 동작구 + 2026-05`는 단일 요청 `lawdCd=11590`, `dealYmd=202605`, `autoImport=true`.
  - `서울특별시 + 서울 전체 + 2026-05`는 서울 25개 구 요청으로 분기하며 모든 요청이 `autoImport=false`.
  - `서울특별시 + 동작구 + 거래월 없음`은 단일 요청 `lawdCd=11590`, `autoImport=false`.

## 잔여 리스크 / 인계 사항

- 실제 지도 연동은 다음 Kakao Map Sprint에서 별도 계약이 필요하다.
- 지도 마커 표시를 위해서는 주택 좌표 확보 정책이 필요하다.
- Sprint7은 회원 버튼 자리만 만들며, Sprint6 회원 API를 Vue에서 연결하는 작업은 별도 Sprint로 남긴다.
- 모바일 레이아웃은 이번 Sprint에서 의도적으로 제외한다.
- Spring Boot가 이미 실행 중인 상태에서 static resource를 다시 빌드하면 `target/classes/static`이 오래된 번들을 들고 있을 수 있다. 이 경우 `npm run build:backend`, `.\mvnw.cmd process-resources`, Spring Boot 재시작 또는 브라우저 강력 새로고침으로 확인한다.
- Sprint7 보강은 백엔드 API 변경 없이 처리한다. `서울특별시` 단독 조회는 프론트엔드에서 서울 25개 `lawdCd`를 순차 조회해 합산하는 방식으로 구현한다.
- 서울특별시 단독 조회는 최대 25회 API 호출을 발생시킨다. Sprint7에서는 백엔드 변경 없이 UI 정확성을 우선했으며, 추후 검색 API가 다중 `lawdCd` 또는 시도 단위 조회를 지원하면 단일 요청으로 개선할 수 있다.
 
## Generator 구현 기록

### 변경 파일

- `frontend/src/main.js`: Sprint7 메인 주택 검색 UI, 검색 폼, API 호출, loading/error/empty 상태, 결과 목록, 선택 상세 패널, 지도 placeholder 요약을 구현했다.
- `frontend/src/App.vue`: autoImport 요청 파라미터 계산을 별도 helper로 위임하고 기존 화면 동작을 유지했다.
- `frontend/src/houseSearchParams.js`: 서울 구별 `lawdCd`, 거래월 정규화, 검색 요청 파라미터 생성 로직을 순수 함수로 분리했다.
- `frontend/src/houseSearchParams.test.js`: Sprint7 autoImport 조건 3가지를 Node 내장 테스트로 검증한다.
- `frontend/package.json`: `npm run test:auto-import` 스크립트를 추가했다.
- `frontend/src/style.css`: 상단 바, 좌측 검색/목록/상세 패널, 우측 지도 placeholder 레이아웃과 텍스트 overflow 방지 스타일을 구현했다.
- `docs/sprints/Sprint7.md`: Generator 구현 내역, 검증 결과, 오류 및 잔여 리스크를 기록했다.

### 구현 요약

- 첫 화면을 랜딩/마케팅 화면이 아닌 주택 실거래가 검색 화면으로 구성했다.
- 검색 파라미터는 `sido`, `sigungu`, `umdNm`, `aptName`, `dealYmd`를 사용하며, month 입력값은 `yyyyMM`으로 변환한다.
- `GET /api/houses/search`만 호출하며 백엔드 API 변경, 신규 상세 API, 회원 API 호출은 추가하지 않았다.
- API 실패 또는 백엔드 미실행 상황에서도 좌측 패널에 명확한 오류 상태가 표시되도록 처리했다.
- 목록/상세 전환은 좌측 패널 내부에서만 일어나며 우측 지도 placeholder는 계속 유지된다.

### 검증 결과

- 2026-06-01: `frontend`에서 최초 `npm run build` 실행 시 `vite` 실행 파일이 없어 실패했다. 원인은 `node_modules` 미설치 상태였다.
- 2026-06-01: sandbox 안에서 `npm ci` 실행 시 npm 내부 오류 메시지(`Exit handler never called`)가 출력되어 의존성 설치가 완전하지 않았다.
- 2026-06-01: 승인된 escalated `npm ci` 실행 성공. 31 packages installed, audit 결과 moderate 2건이 보고되었으나 Sprint7 기능 구현 범위 밖이므로 패키지 변경은 하지 않았다.
- 2026-06-01: `npm run build` 재실행 중 Vue template 문자열 내부 중첩 backtick 때문에 Rollup parse error가 발생했다. 목록 item key 생성을 `itemKey` 메서드로 분리해 수정했다.
- 2026-06-01: `npm run build` 최종 성공. Vite v5.4.10 production build가 `dist/index.html`, `dist/assets/index-BWwg_Qbj.css`, `dist/assets/index-FiguXnhK.js`를 생성했다.
- 2026-06-01: Vite dev server를 `http://127.0.0.1:5173`에서 실행하고 `Invoke-WebRequest`로 HTTP 200 응답을 확인했다.
- 2026-06-01: in-app browser 기반 화면 검증을 2회 시도했으나 브라우저 런타임이 `windows sandbox failed: spawn setup refresh`로 종료되어 클릭/시각 검증은 완료하지 못했다. 빌드와 로컬 서버 응답 기준으로 정적 검증을 완료했다.
- 2026-06-01: `npm run test:auto-import` 성공. `서울특별시 + 동작구 + 거래월`은 `lawdCd=11590`, `autoImport=true`; `서울특별시 + 서울 전체 + 거래월`은 25개 구 요청 모두 `autoImport=false`; `서울특별시 + 동작구 + 거래월 없음`은 `autoImport=false`를 검증했다.
- 2026-06-01: `npm run build` 성공. helper 분리 후 Vue/Vite production build가 정상 완료됐다.
- 2026-06-01: `.\mvnw.cmd test` 성공. 백엔드 production/test 코드는 변경하지 않았으며, 기존 HouseService/HouseController 포함 전체 42개 테스트가 통과했다.

### 남은 리스크

- API 응답 필드는 현재 `HouseSearchPageResponse.items`와 `totalCount` 기준으로 매핑했다. 백엔드 응답 계약이 바뀌면 프론트 매핑 조정이 필요하다.
- 백엔드가 실행되지 않거나 `/api/houses/search`가 실패하면 에러 상태를 표시하도록 처리했지만, 실제 DB 데이터가 있는 live 검색 플로우는 이번 Sprint에서 검증하지 못했다.
- `npm audit`에서 moderate 2건이 보고되었다. Sprint7 범위에서는 의존성 업그레이드나 `npm audit fix`를 수행하지 않았다.

## Reviewer 검증 결과

- 검증일: 2026-06-05.
- 판정: Pass.
- Blocking findings: none.
- 스펙 이탈: 없음.

### Reviewer 확인 사항

- `docs/PRD.md`, `docs/spec.md`, Sprint7 계약의 완료 기준을 기준으로 검증했다.
- `frontend/src/App.vue`에서 첫 화면이 랜딩 페이지가 아니라 `NoHome 실거래가 검색` 메인 검색 shell로 시작하는 것을 확인했다.
- 좌측 `left-panel`에는 검색 폼, 결과 목록, 로딩/실패/빈 결과 상태가 있고, 우측 `map-panel`에는 실제 Kakao Map 대신 placeholder가 분리되어 있음을 확인했다.
- 검색 조건은 `시도`, `시군구`, `읍면동`, `아파트명`, `거래월`을 제공하며 거래월은 `frontend/src/houseSearchParams.js`에서 `yyyyMM`으로 변환된다.
- 결과 목록은 아파트명, 동/지번, 거래금액, 전용면적, 층, 거래일을 표시한다.
- `selectItem` / `backToList` 메서드를 Node 검증 스크립트로 실행해 목록 항목 선택 시 `selectedItem`이 설정되고 뒤로 가기 시 `null`로 복귀하는 것을 확인했다. `mapSummary`도 선택 상태에서 갱신된다.
- 지도 placeholder는 `map-panel`, `map-surface`, `map-grid`, `map-copy`, `coordinate-badge` 구조와 `Kakao Map SDK 미연결 · 마커 없음` 문구로 실제 지도 연동 없이 표현된다.
- 상단 로그인/내 정보 버튼은 disabled placeholder이며 회원 폼 또는 회원 API 호출은 프론트엔드에 추가되지 않았다.
- 프론트엔드는 `/api/houses/search`만 호출하며, 신규 주택 상세 API, 백엔드 검색 API 변경, 주변 상권, 즐겨찾기, 관리자, JWT/OAuth, 모바일 전용 구현은 확인되지 않았다.
- `rg` 정적 확인에서 Kakao Map SDK 호출, 지도 마커 구현, 회원 API 호출, 라우터/상태관리 도입은 발견되지 않았다.

### Reviewer 검증 명령

- `frontend`에서 `npm run test:auto-import`: success. Node 내장 테스트 3건 통과.
- `frontend`에서 `npm run build`: success. Vite production build 성공.
- 루트에서 `.\mvnw.cmd test`: success. 42 tests, failures 0, errors 0, skipped 0.
- `frontend`에서 `npm run build:backend`: success. `src/main/resources/static/index.html`, `assets/index-C3SPNCs6.js`, `assets/index-DpqZUG0i.css` 생성.
- 루트에서 `.\mvnw.cmd process-resources`: success. `target/classes/static`에 최신 정적 리소스 반영.
- Vite dev server `http://127.0.0.1:5173` HTTP 200 응답 확인.

### Reviewer 잔여 리스크

- in-app browser 기반 직접 클릭/스크린샷 검증은 2회 시도했으나 현재 환경에서 `windows sandbox failed: spawn setup refresh`로 실패했다. Reviewer는 HTTP 응답, 소스 구조, 빌드 산출물, 컴포넌트 메서드 실행 검증으로 대체 확인했다.
- 실제 DB 데이터가 있는 Spring Boot 통합 브라우저 검색 흐름은 이번 Reviewer pass에서 재검증하지 못했다. 다만 Sprint7 계약의 핵심은 UI shell이며, API 실패/빈 결과 상태와 빌드/정적 리소스 반영은 확인했다.
- 서울 전체 조회는 프론트엔드에서 최대 25회 `/api/houses/search` 호출을 발생시킨다. 이는 Sprint7의 백엔드 변경 제외 결정에 따른 잔여 개선사항이며 blocking은 아니다.
