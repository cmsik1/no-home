# Sprint 8: Member Frontend Integration

## Milestone 연결

- Milestone: M4 - 주택 기본정보와 필수 UI / API 연결.
- 목적: Sprint6에서 구현한 회원 API를 Vue 화면에 연결해 회원가입, 로그인, 로그아웃, 내 정보 조회, 수정, 삭제 흐름을 사용자 화면에서 수행할 수 있게 한다.

## Sprint7 Reviewer 확인 결과

- Sprint7 Reviewer 검증 결과: Pass.
- Blocking findings: none.
- 확인된 사항:
  - 첫 화면은 메인 주택 검색 UI shell로 시작한다.
  - 좌측 검색/목록/상세 패널과 우측 지도 placeholder가 분리되어 있다.
  - `/api/houses/search`만 호출하며 회원 API, Kakao Map SDK, 마커, 추가 기능은 구현하지 않았다.
  - `npm run test:auto-import`, `npm run build`, `npm run build:backend`, `.\mvnw.cmd process-resources`, `.\mvnw.cmd test`가 성공했다.
- Sprint7에서 다음으로 넘기는 잔여 리스크:
  - in-app browser 직접 클릭/스크린샷 검증은 환경 오류로 실패했다.
  - 실제 DB 데이터가 있는 Spring Boot 통합 브라우저 검색 흐름은 재검증하지 못했다.
  - 서울 전체 조회는 프론트엔드에서 최대 25회 `/api/houses/search` 호출을 발생시킨다.
- 판단:
  - 위 리스크는 회원 프론트 연결을 막지 않는다. Sprint8은 Sprint7의 검색/지도 shell을 유지하면서 상단 회원 진입 영역과 회원 패널만 연결한다.

## 확정된 사용자 결정

- Sprint8 범위: 필수 회원 UI 연결.
- 연결 대상 API:
  - `POST /api/members`
  - `POST /api/auth/login`
  - `POST /api/auth/logout`
  - `GET /api/members/me`
  - `PUT /api/members/me`
  - `DELETE /api/members/me`
- 로그인 식별자: Sprint6 기준인 `email`.
- 인증 방식: 세션 쿠키 기반. 프론트엔드는 `fetch` 호출에 `credentials: 'include'`를 사용한다.
- UI 톤: Sprint7의 조용한 업무형 검색 화면 스타일을 유지한다.
- 라우터/상태관리 라이브러리: 도입하지 않는다.

## Manager 구현 지시

Generator는 `docs/PRD.md`, `docs/spec.md`, `docs/sprints/Sprint8.md`, Sprint6 회원 API 구현 파일, 그리고 프론트엔드 구현에 필요한 파일만 읽고 Sprint8 범위만 구현한다.

이번 Sprint는 백엔드 회원 API를 새로 만들거나 바꾸는 작업이 아니다. 기존 Sprint6 API 계약을 Vue 화면에서 호출하고, 성공/실패/로딩/비로그인 상태를 사용자가 확인할 수 있게 만든다.

기존 Sprint7 검색/지도 shell은 유지한다. 상단의 disabled 로그인/내 정보 placeholder를 실제 진입 버튼으로 바꾸고, 회원 영역은 현재 화면 안의 패널/모달/탭 중 단순한 구조로 구현한다. 별도 라우터는 도입하지 않는다.

회원 삭제는 물리 삭제 API를 호출하므로 UI에서 명확한 확인 절차를 둔다. 삭제 성공 후 프론트 상태를 로그아웃 상태로 정리한다.

## 범위

- 상단 회원 진입 UI:
  - 비로그인 상태: 로그인, 회원가입 진입.
  - 로그인 상태: 사용자 이름 또는 이메일 요약, 내 정보, 로그아웃 진입.
- 회원가입 화면:
  - email, password, name, phone 입력.
  - 필수 입력 안내.
  - `POST /api/members` 호출.
  - 성공 시 가입된 사용자 정보를 표시하고 로그인 화면으로 자연스럽게 이동하거나, 즉시 로그인 안내를 제공한다.
- 로그인 화면:
  - email, password 입력.
  - `POST /api/auth/login` 호출.
  - 성공 시 현재 회원 상태를 저장하고 회원 패널을 닫거나 내 정보 화면으로 이동한다.
- 로그아웃:
  - `POST /api/auth/logout` 호출.
  - 성공 또는 이미 로그아웃 상태에서 프론트 상태를 비로그인으로 정리한다.
- 내 정보 조회:
  - 앱 시작 또는 회원 패널 진입 시 `GET /api/members/me`를 호출해 세션 상태를 확인한다.
  - 401은 정상적인 비로그인 상태로 처리한다.
- 내 정보 수정:
  - name, phone 입력.
  - `PUT /api/members/me` 호출.
  - 성공 시 화면의 현재 회원 정보를 갱신한다.
- 회원 삭제:
  - 사용자가 명시적으로 삭제 확인 문구를 입력하거나 체크해야 한다.
  - `DELETE /api/members/me` 호출.
  - 성공 시 회원 상태와 폼 상태를 정리한다.
- 상태 표시:
  - 회원 API 로딩, 성공, 실패 메시지를 화면에 표시한다.
  - API 응답의 `message`를 우선 사용하되, 사용자가 이해할 수 있는 기본 메시지를 둔다.
- 빌드/검증:
  - `npm run build` 또는 `npm run build:backend` 성공.
  - `npm run test:auto-import`가 기존 검색 요청 파라미터 보장을 계속 통과한다.
  - 루트 `.\mvnw.cmd test` 성공.

## 제외 범위

- 백엔드 회원 API 변경.
- DB 스키마 변경.
- Spring Security 전체 필터/인터셉터 도입.
- 비밀번호 변경.
- 비밀번호 찾기/재설정.
- 이메일 인증.
- 관리자 회원 관리.
- JWT/OAuth/social login.
- 회원과 관심 지역/상권/지도 기능 연결.
- Kakao Map 실제 연동.
- 모바일 전용 레이아웃 재설계.
- 검색 API 또는 공공데이터 자동 적재 UX 변경.

## 완료 기준

- 상단에서 로그인/회원가입/내 정보/로그아웃 흐름에 진입할 수 있다.
- 회원가입 폼이 `POST /api/members`를 호출하고 성공/실패 상태를 표시한다.
- 로그인 폼이 `POST /api/auth/login`을 호출하고 성공 시 현재 회원 상태를 반영한다.
- 로그아웃이 `POST /api/auth/logout`을 호출하고 프론트 상태를 비로그인으로 정리한다.
- 앱이 `GET /api/members/me`로 기존 세션을 확인하며, 401은 오류가 아닌 비로그인 상태로 처리한다.
- 내 정보 화면에서 현재 회원 email/name/phone을 볼 수 있다.
- 내 정보 수정이 `PUT /api/members/me`로 name/phone을 갱신한다.
- 회원 삭제가 확인 절차 후 `DELETE /api/members/me`를 호출하고 성공 시 세션/화면 상태를 정리한다.
- API 응답에서 비밀번호 또는 비밀번호 해시를 화면에 표시하지 않는다.
- Sprint7의 검색/목록/상세/지도 placeholder 기능이 유지된다.
- 범위 밖 기능이 구현되지 않는다.
- `npm run test:auto-import`, `npm run build` 또는 `npm run build:backend`, `.\mvnw.cmd test` 결과가 기록된다.
- 변경 파일, 구현 내용, 검증 결과, 에러, 잔여 리스크가 이 문서에 기록된다.

## Generator 작업 지시

Generator에게 전달할 프롬프트 예시:

```text
docs/PRD.md, docs/spec.md, docs/sprints/Sprint8.md, Sprint6 회원 API 구현 파일, 프론트엔드 파일을 읽고 Sprint8 범위만 구현해.
Sprint7 검색/지도 shell은 유지하고, 상단 회원 버튼을 실제 로그인/회원가입/내 정보/로그아웃 진입 UI로 바꿔.
POST /api/members, POST /api/auth/login, POST /api/auth/logout, GET/PUT/DELETE /api/members/me를 fetch로 연결해.
세션 쿠키 기반이므로 회원 API 호출에는 credentials: 'include'를 사용해.
회원가입, 로그인, 내 정보 조회/수정/삭제 화면과 로딩/성공/실패 상태를 구현해.
회원 삭제에는 명확한 확인 절차를 둬.
백엔드 API 변경, DB 변경, 비밀번호 찾기, 관리자, JWT/OAuth, Kakao Map, 관심 지역/상권 연결, 모바일 전용 재설계는 구현하지 마.
npm run test:auto-import, npm run build 또는 npm run build:backend, .\mvnw.cmd test를 실행하고 결과를 Sprint8 문서에 기록해.
변경 파일, 구현 내용, 에러, 잔여 리스크도 Sprint8 문서에 기록해.
```

## Reviewer 검증 지시

Reviewer는 Generator 구현 후 다음 기준으로 검증한다.

- Sprint8 계약 범위와 완료 기준을 충족하는지 확인한다.
- 회원가입, 로그인, 로그아웃, 내 정보 조회, 수정, 삭제 UI가 모두 존재하는지 확인한다.
- 프론트엔드가 Sprint6 API 경로를 정확히 호출하고 `credentials: 'include'`를 사용하는지 확인한다.
- 비로그인 401이 사용자에게 치명적 오류처럼 표시되지 않는지 확인한다.
- 비밀번호 또는 비밀번호 해시가 화면 상태와 응답 표시에서 노출되지 않는지 확인한다.
- 회원 삭제에 확인 절차가 있는지 확인한다.
- Sprint7 검색/지도 shell이 깨지지 않았는지 확인한다.
- 백엔드 API 변경, DB 변경, JWT/OAuth, 비밀번호 찾기, 관리자, Kakao Map 등 제외 범위가 구현되지 않았는지 확인한다.
- `npm run test:auto-import`, `npm run build` 또는 `npm run build:backend`, `.\mvnw.cmd test` 결과가 충분한지 확인한다.

## 작업 로그

- 2026-06-05: Manager가 Sprint7 Reviewer Pass를 확인하고 Sprint8 계약을 작성했다. 범위는 Sprint6 회원 API를 Vue 화면에 연결하는 필수 UI 흐름이다.
- 2026-06-05: Generator가 Sprint8 구현을 수행했다. Sprint7 검색/지도 shell은 유지하고, 상단 회원 영역과 회원 관리 패널을 추가해 Sprint6 회원 API를 연결했다.
- 2026-06-05: Spring Boot 정적 리소스 실행 환경에서 회원가입, 로그인, 내 정보 조회, 수정, 삭제, 삭제 후 401 흐름을 live API로 확인했다.

## 변경 파일 목록

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `src/main/resources/static/index.html`
- `src/main/resources/static/assets/index-CFW3L8OY.js`
- `src/main/resources/static/assets/index-CKBcWRPe.css`
- `docs/sprints/Sprint8.md`
- `docs/working-memory.md`

## 검증 결과

- `frontend`에서 `npm run build`: success. Vite production build 성공.
- `frontend`에서 `npm run test:auto-import`: success. Node 내장 테스트 3건 통과.
- 루트에서 `.\mvnw.cmd test`: success. 42 tests, failures 0, errors 0, skipped 0.
- `frontend`에서 `npm run build:backend`: success. Spring Boot 정적 리소스 경로에 최신 Vue bundle 생성.
- 루트에서 `.\mvnw.cmd process-resources`: success. `target/classes/static`에 최신 정적 리소스 반영.
- Spring Boot 임시 실행 `http://127.0.0.1:18082`: root 정적 페이지 HTTP 200 확인.
- Spring Boot + Docker MySQL live 회원 API 검증:
  - `POST /api/members`: 201 Created.
  - `POST /api/auth/login`: 200 OK.
  - 세션 유지 후 `GET /api/members/me`: 200 OK.
  - 세션 유지 후 `PUT /api/members/me`: 200 OK, name이 `Sprint8 UI Updated`로 반영됨.
  - 세션 유지 후 `DELETE /api/members/me`: 200 OK.
  - 삭제 후 같은 세션으로 `GET /api/members/me`: 401 Unauthorized.

## 잔여 리스크 / 인계 사항

- in-app browser 직접 검증은 현재 환경에서 실패할 수 있다. 가능하면 HTTP/API와 빌드/정적 소스 검증으로 보완한다.
- Vite dev server 단독 실행은 `/api` 프록시가 없어 회원 API live 검증이 어렵다. 통합 검증은 Spring Boot 정적 리소스 실행 또는 별도 프록시 설정이 필요할 수 있다.
- 이번 검증에서도 in-app browser가 `windows sandbox failed: spawn setup refresh`로 실패했다. 화면 클릭 검증은 수행하지 못했고, HTTP/API live 검증과 빌드/정적 소스 확인으로 대체했다.
- 비밀번호 변경, 비밀번호 찾기, 이메일 변경, 관리자 회원 관리는 Sprint8 범위 밖으로 남아 있다.

## Generator 구현 기록

### 구현 요약

- 상단 disabled 회원 placeholder를 실제 회원 메뉴로 바꿨다.
- 비로그인 상태에서는 로그인/회원가입 버튼을 보여주고, 로그인 상태에서는 현재 회원 요약, 내 정보, 로그아웃 버튼을 보여준다.
- `App.vue`에 회원 패널을 추가해 로그인, 회원가입, 내 정보 조회/수정, 회원 삭제를 한 화면 안에서 전환한다.
- 모든 회원 API 호출은 `credentials: 'include'`를 사용해 Sprint6 세션 쿠키 인증과 연결했다.
- 앱 mount 시 `GET /api/members/me`로 기존 세션을 확인하고, 401은 정상 비로그인 상태로 처리한다.
- 회원가입 성공 후 비밀번호 입력값을 비우고 로그인 화면으로 이동해 방금 가입한 email로 로그인할 수 있게 했다.
- 로그인 성공 시 현재 회원 상태와 프로필 폼을 갱신한다.
- 로그아웃 성공 또는 로그아웃 호출 실패 시에도 프론트 상태를 비로그인으로 정리한다.
- 내 정보 수정은 name, phone만 `PUT /api/members/me`로 전송한다.
- 회원 삭제는 확인 입력값이 `삭제`일 때만 `DELETE /api/members/me`를 호출한다.
- API 응답의 비밀번호 또는 비밀번호 해시는 화면 상태에 저장하거나 표시하지 않는다.

### 제외 범위 준수

- 백엔드 회원 API, DB 스키마, Mapper SQL은 변경하지 않았다.
- 라우터/상태관리 라이브러리를 추가하지 않았다.
- JWT/OAuth, 비밀번호 찾기/재설정, 관리자 회원 관리, Kakao Map, 관심 지역/상권 연결은 구현하지 않았다.
- Sprint7 검색/목록/상세/지도 placeholder 구조는 유지했다.

## Reviewer 검증 결과

- 검증일: 2026-06-05.
- 판정: Pass.
- Blocking findings: none.
- 스펙 이탈: 없음.

### Reviewer 확인 사항

- `docs/PRD.md`, `docs/spec.md`, Sprint8 계약의 완료 기준을 기준으로 검증했다.
- `frontend/src/App.vue`에 회원가입, 로그인, 로그아웃, 내 정보 조회, 내 정보 수정, 회원 삭제 UI가 모두 존재한다.
- 프론트엔드는 Sprint6 API 경로인 `POST /api/members`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/members/me`, `PUT /api/members/me`, `DELETE /api/members/me`를 호출한다.
- 회원 API 공통 호출 함수 `requestMemberApi`는 `credentials: 'include'`를 사용해 세션 쿠키를 포함한다.
- 앱 mount 및 내 정보 진입 시 `GET /api/members/me`를 호출하고, 401은 정상 비로그인 상태로 처리한다.
- 회원 삭제는 `deleteConfirm` 값이 `삭제`일 때만 진행되며, 확인 문구 입력 UI가 존재한다.
- 회원가입/로그인/내 정보 API 응답 확인 결과 `password`, `passwordHash` 필드는 응답 data에 포함되지 않았다.
- Sprint7 검색 shell의 `주택 검색`, `result-list`, `detail-panel`, `map-panel`, 지도 placeholder 문구가 유지된다.
- 정적 검색 결과 백엔드 API 변경, DB 스키마 변경, 라우터/상태관리 도입, JWT/OAuth, 비밀번호 찾기, 관리자 회원 관리, Kakao Map 실제 연동은 Sprint8 구현에서 확인되지 않았다.

### Reviewer 검증 명령

- `frontend`에서 `npm run test:auto-import`: success. Node 내장 테스트 3건 통과.
- `frontend`에서 `npm run build`: success. Vite production build 성공.
- 루트에서 `.\mvnw.cmd test`: success. 42 tests, failures 0, errors 0, skipped 0.
- `frontend`에서 `npm run build:backend`: success. Spring Boot 정적 리소스 경로에 최신 Vue bundle 생성.
- 루트에서 `.\mvnw.cmd process-resources`: success. `target/classes/static`에 최신 정적 리소스 반영.
- `docker compose ps`: `no-home-mysql` healthy.
- Spring Boot 임시 실행 `http://127.0.0.1:18082`: root 정적 페이지 HTTP 200 확인.
- Spring Boot + Docker MySQL live 회원 API 재검증:
  - `POST /api/members`: 201 Created.
  - `POST /api/auth/login`: 200 OK.
  - 세션 유지 후 `GET /api/members/me`: 200 OK.
  - 세션 유지 후 `PUT /api/members/me`: 200 OK, name이 `Sprint8 Reviewer Updated`로 반영됨.
  - 세션 유지 후 `DELETE /api/members/me`: 200 OK.
  - 삭제 후 같은 세션으로 `GET /api/members/me`: 401 Unauthorized.
  - signup/login/me 응답 data에 `password`, `passwordHash` 필드가 없음을 확인.

### Reviewer 잔여 리스크

- in-app browser 기반 직접 클릭/스크린샷 검증은 현재 환경에서 계속 `windows sandbox failed: spawn setup refresh`로 실패한다. Reviewer는 HTTP 응답, live API, 빌드 산출물, 정적 소스 검증으로 대체 확인했다.
- 화면 클릭 검증이 없으므로 실제 브라우저에서 회원 패널 조작감과 레이아웃은 제출 안정화 단계에서 수동 캡처 또는 다른 브라우저 환경으로 재확인하는 편이 좋다.
- 비밀번호 변경, 비밀번호 찾기, 이메일 변경, 관리자 회원 관리는 Sprint8 범위 밖으로 남아 있다.
