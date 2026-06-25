# Sprint 6: Member CRUD and Session Auth Baseline

## Milestone 연결

- Milestone: M3 - 회원 CRUD와 인증.
- 목적: 과제 필수 기능인 회원 가입, 로그인, 로그아웃, 내 정보 조회, 내 정보 수정, 회원 물리 삭제의 백엔드 기준선을 만든다.

## Sprint5 Reviewer 확인 결과

- Sprint5 Reviewer result: Pass.
- Blocking findings: none.
- 확인된 사항:
  - 검색 응답은 coverage fill 이후에도 DB 기반 결과를 반환한다.
  - 공공데이터 API 호출은 자동 적재 coverage fill 경로로 제한된다.
  - 완료 coverage 기준은 `status='success'` 및 `total_count <= imported_count + skipped_count`이다.
  - 완료 range는 재적재하지 않고, 미완료 range만 적재한다.
  - `dealYmd` 없음, `aptName` 단독, `autoImport=false` 요청은 자동 적재하지 않는다.
  - 서울 25개 구 resolver와 동작구 `11590` 처리가 확인됐다.
  - 실제 공공데이터 API key 노출은 확인되지 않았다.
- Sprint6으로 넘기는 잔여 리스크:
  - 서울 전체 검색은 최대 25회 import를 유발할 수 있다.
  - API/network 실패 시 사용자 복구 안내는 아직 최소 수준이다.
- 판단:
  - 위 리스크는 검색/공공데이터 UX 보강 항목이며, 회원/인증 기준선 구현을 막지 않는다.

## Manager 구현 지시

Generator는 `docs/spec.md`, `docs/PRD.md`, `docs/sprints/Sprint6.md`, 그리고 회원/인증 구현에 필요한 소스 파일만 읽고 Sprint6 범위만 구현한다.

이번 Sprint의 우선순위는 보안과 서버 측 권한 경계다. 화면 구현, 비밀번호 찾기, 관리자 기능, 즐겨찾기, 주변 상권, 지도 연동은 구현하지 않는다.

기존 프로젝트 구조와 응답 패턴을 우선 사용한다. API 응답은 기존 `ApiResponse` 패턴을 유지한다.

비밀번호는 평문 저장하지 않는다. Spring Security 전체 인증 체계를 도입할지 여부는 이번 Sprint에서 확정하지 않는다. 기본안은 외부 인증 프레임워크 전체 도입 없이 `PasswordEncoder` 수준의 해시 컴포넌트만 사용하고, 세션에는 현재 로그인한 회원 식별자만 저장하는 것이다. 단, 기존 의존성/구조상 더 단순한 로컬 해시 유틸이 적합하면 Generator는 이유를 Sprint 문서에 기록하고 구현할 수 있다.

회원 삭제 정책은 `docs/spec.md`의 현재 결정인 물리 삭제를 따른다.

## 확정 기술 결정

- 인증 방식: 세션 기반 로그인.
- 회원 삭제: 물리 삭제.
- API 응답: 기존 `ApiResponse` 형식.
- 보호 대상:
  - `GET /api/members/me`
  - `PUT /api/members/me`
  - `DELETE /api/members/me`
- 공개 대상:
  - `POST /api/members`
  - `POST /api/auth/login` 또는 기존 라우팅 관례에 맞춘 로그인 API
  - `POST /api/auth/logout` 또는 기존 라우팅 관례에 맞춘 로그아웃 API

## 결정 필요 항목

아래 항목은 Generator가 임의로 제품 방향을 넓히지 않는다. 기본안을 따르되, 구현 중 충돌이 있으면 이 섹션에 기록한다.

1. 로그인 식별자 필드
   - 선택지 A: `email`
     - 장점: 사용자에게 익숙하고 중복 검증이 쉽다.
     - 단점: 과제 샘플이나 기존 DB가 `login_id` 중심이면 변환이 필요하다.
   - 선택지 B: `login_id`
     - 장점: 학교 과제형 회원 예제와 잘 맞을 수 있다.
     - 단점: 사용자 연락 이메일과 분리된다.
   - 추천 기본안: 현재 스키마/코드에 이미 더 가까운 필드를 우선 사용한다. 새로 결정해야 한다면 `email`을 우선한다.

2. 비밀번호 해시 방식
   - 선택지 A: BCrypt `PasswordEncoder`
     - 장점: 검증된 해시 방식이다.
     - 단점: Spring Security crypto 의존성 추가가 필요할 수 있다.
   - 선택지 B: SHA-256 + salt 로컬 유틸
     - 장점: 의존성이 작다.
     - 단점: BCrypt보다 인증 보안 기본값이 약하다.
   - 추천 기본안: BCrypt.

3. 세션 저장 키
   - 추천 기본안: `LOGIN_MEMBER_ID`.
   - 세션에는 비밀번호, 해시, 회원 전체 객체를 저장하지 않는다.

## 범위

- `members` 테이블이 없거나 Sprint6 요구에 부족하면 마이그레이션/스키마를 보강한다.
- Member DTO, Mapper, Service, Controller 기준선을 구현한다.
- 회원 가입:
  - 필수 입력 검증.
  - 로그인 식별자 중복 방지.
  - 비밀번호 해시 저장.
  - 평문 비밀번호 응답 금지.
- 로그인:
  - 식별자와 비밀번호 검증.
  - 성공 시 세션에 현재 회원 식별자 저장.
  - 실패 시 명확한 실패 응답.
- 로그아웃:
  - 현재 세션 무효화 또는 로그인 식별자 제거.
  - 이미 로그아웃 상태여도 서버가 안전하게 응답한다.
- 내 정보 조회:
  - 로그인된 회원만 조회 가능.
  - 비밀번호 해시 응답 금지.
- 내 정보 수정:
  - 로그인된 회원 자신의 정보만 수정 가능.
  - 이름, 전화번호 등 기본 프로필 수정.
  - 비밀번호 변경을 포함할 경우 현재 비밀번호 확인 또는 별도 명시 검증을 둔다.
- 회원 삭제:
  - 로그인된 회원 자신의 계정만 물리 삭제.
  - 삭제 후 세션 정리.
- 테스트:
  - 회원 가입 성공/중복 실패.
  - 비밀번호 해시 저장 검증.
  - 로그인 성공/실패.
  - 로그아웃.
  - 비로그인 상태의 `/me` 접근 차단.
  - 내 정보 조회/수정/삭제.
  - 다른 회원 정보 수정/삭제가 구조적으로 불가능하거나 차단되는지 검증.

## 제외 범위

- Vue 회원 화면.
- Spring Security 전체 로그인 필터 체계.
- 비밀번호 찾기.
- 이메일 인증.
- 관리자 회원 관리.
- JWT/token 인증.
- OAuth/social login.
- 회원과 관심 지역/상권/지도 기능 연결.
- 공공데이터 자동 적재 UX 보강.

## 완료 기준

- 회원 가입 API가 동작하고 비밀번호를 평문 저장하지 않는다.
- 로그인 성공 시 세션 기반 인증 상태가 만들어진다.
- 로그아웃 API가 세션 인증 상태를 해제한다.
- 로그인된 사용자가 자신의 회원 정보를 조회, 수정, 물리 삭제할 수 있다.
- 비로그인 사용자는 보호 API에 접근할 수 없다.
- 현재 로그인한 사용자 외의 회원 데이터를 수정/삭제할 수 없다.
- API 응답에서 비밀번호 해시가 노출되지 않는다.
- DB 스키마와 Mapper SQL 변경이 테스트로 검증된다.
- `.\mvnw.cmd test`가 성공하거나 실패 사유가 문서에 기록된다.
- 변경 파일, 구현 내용, 검증 결과, 에러, 잔여 리스크가 이 문서에 기록된다.

## Generator 작업 지시

Generator에게 전달할 프롬프트 예시:

```text
docs/PRD.md, docs/spec.md, docs/sprints/Sprint6.md를 읽고 Sprint6 범위만 구현해.
회원 가입, 로그인, 로그아웃, 내 정보 조회, 내 정보 수정, 회원 물리 삭제의 백엔드 기준선을 만들어.
비밀번호는 평문 저장하지 말고, API 응답에도 비밀번호/해시를 노출하지 마.
세션에는 현재 로그인한 회원 식별자만 저장해.
Vue 화면, 비밀번호 찾기, 관리자 기능, JWT/OAuth, 즐겨찾기/상권/지도 연동은 구현하지 마.
기존 ApiResponse 패턴과 Controller-Service-Mapper 계층 구조를 유지해.
테스트를 추가하고 .\mvnw.cmd test 결과를 docs/sprints/Sprint6.md에 기록해.
변경 파일, 구현 내용, 에러, 잔여 리스크도 Sprint6 문서에 기록해.
```

## Reviewer 검증 지시

Reviewer는 Generator 구현 후 다음 기준으로 검증한다.

- Sprint6 계약 범위와 완료 기준을 충족하는지 확인한다.
- 비밀번호가 평문 저장되거나 응답에 노출되지 않는지 확인한다.
- 세션에는 최소 식별자만 저장되는지 확인한다.
- 보호 API가 비로그인 접근을 차단하는지 확인한다.
- 회원 조회/수정/삭제가 현재 로그인한 사용자에게만 허용되는지 확인한다.
- 회원 삭제가 물리 삭제 정책을 따르고 세션을 정리하는지 확인한다.
- Mapper SQL과 DB 스키마 변경이 테스트로 검증되는지 확인한다.
- Vue, JWT/OAuth, 비밀번호 찾기, 관리자 기능 등 제외 범위가 구현되지 않았는지 확인한다.
- `.\mvnw.cmd test` 결과와 가능한 live 검증 결과가 충분한지 확인한다.

## 작업 로그

- 2026-06-01: Manager가 Sprint5 Reviewer Pass를 확인하고 Sprint6 계약 초안을 작성했다.

## 변경 파일 목록

- 아직 Generator 구현 전.

## 검증 결과

- 아직 Generator 구현 전.

## 잔여 리스크 / 인계 사항

- 로그인 식별자 필드는 기존 코드/스키마 확인 후 `email` 또는 `login_id` 중 더 자연스러운 쪽을 선택해야 한다.
- BCrypt 사용 시 의존성 추가가 필요할 수 있다.
- 세션 만료 시간과 전역 인증 인터셉터 적용 범위는 구현 중 기존 구조에 맞춰 최소 기준으로 결정하고 문서에 기록한다.
## Generator Implementation Record (2026-06-01)

### Work Log

- Generator implemented the Sprint6 backend baseline for member signup, login, logout, current member lookup, current member update, and physical member deletion.
- Login identifier selected: `email`, because no previous member schema existed and Sprint6 allowed either `email` or `login_id`; `email` matches the default recommendation.
- Password hashing selected: BCrypt via `spring-security-crypto`. Full Spring Security auth/filter flow was not introduced; only the crypto component is used.
- Session storage selected: only the current member id is stored under `LOGIN_MEMBER_ID`.

### Changed Files

- `pom.xml`: added `spring-security-crypto`.
- `src/main/resources/schema.sql`: added `members` table with unique `email`, `password_hash`, `name`, `phone`, `created_at`, and `updated_at`.
- `src/main/resources/mappers/member/MemberMapper.xml`: added member insert/select/update/delete SQL.
- `src/main/java/com/ssafy/home/member/dto/*`: added member model, request DTOs, and password-free response DTO.
- `src/main/java/com/ssafy/home/member/mapper/*`: added `MemberMapper` and `MemberInsertCommand`.
- `src/main/java/com/ssafy/home/member/service/*`: added member service, BCrypt password hasher, and member exception/error code.
- `src/main/java/com/ssafy/home/member/controller/MemberController.java`: added `/api/members`, `/api/auth/login`, `/api/auth/logout`, and `/api/members/me` endpoints.
- `src/test/java/com/ssafy/home/member/service/MemberServiceTest.java`: added service tests for signup, duplicate email, password hashing, login, unauthenticated access, current-member update/delete.
- `src/test/java/com/ssafy/home/member/controller/MemberControllerTest.java`: added controller tests for ApiResponse shape, session login/logout, unauthenticated `/me`, and current-member-only route behavior.

### Implementation Summary

- Signup validates required `email`, `password`, and `name`, rejects duplicate email, stores only BCrypt password hash, and returns `MemberResponse` without password or hash.
- Login validates email/password and stores only `LOGIN_MEMBER_ID` in the session on success. Invalid credentials return `ApiResponse.fail` with HTTP 401.
- Logout invalidates the current session if one exists and safely succeeds when already logged out.
- `GET /api/members/me`, `PUT /api/members/me`, and `DELETE /api/members/me` derive the target member only from the session id.
- `PUT /api/members/me` updates only `name` and `phone`, keeping password/email changes out of Sprint6 scope.
- `DELETE /api/members/me` physically deletes the member row and clears the session.

### Test Result

- Command: `.\mvnw.cmd test`
- Result: BUILD SUCCESS.
- Summary: Tests run: 37, Failures: 0, Errors: 0, Skipped: 0.
- Sprint6 coverage: signup success and duplicate failure, hashed password storage, login success/failure, logout, unauthenticated `/me` blocked, current member lookup/update/delete, and other-member modification/deletion blocked by route/service structure.

### Errors

- No implementation or test failure remains.
- `git status` initially failed because the repository owner differs from the sandbox user. Status was checked with `git -c safe.directory=C:/SSAFY/workspace/NoHome status --short` without changing global git config.

### Remaining Risks

- Authentication is a controller/session baseline, not a global Spring Security or interceptor-based auth boundary.
- Future member-linked tables may need explicit FK deletion policy; Sprint6 has no such linked tables.
- Password change, email change, current-password recheck, password reset, admin member management, JWT/OAuth, and Vue member UI remain out of scope.

## Generator Mapper Test Reinforcement (2026-06-01)

### Work Log

- Reviewer non-blocking gap인 `MemberMapper.xml` 통합 테스트 부재를 Sprint6 범위 안에서 보강했다.
- 새 기능 구현 없이 테스트 전용 의존성과 mapper integration test만 추가했다.
- 테스트 DB는 H2 MySQL mode를 사용했고, `@Sql(scripts = "classpath:schema.sql")`로 현재 `schema.sql`의 `members` 테이블 정의와 unique 제약을 함께 검증했다.

### Changed Files

- `pom.xml`: `mybatis-spring-boot-starter-test`, `h2` test scope 의존성 추가.
- `src/test/java/com/ssafy/home/member/mapper/MemberMapperTest.java`: 실제 MyBatis mapper XML과 테스트 DB 스키마를 사용하는 통합 테스트 추가.
- `docs/sprints/Sprint6.md`: Sprint6 보강 작업 내용과 검증 결과 기록.

### Added Tests

- 회원 insert 후 `selectByEmail` 조회 및 `password_hash` 저장/조회 확인.
- 회원 insert 후 `selectById` 조회 확인.
- `updateCurrentMember` 후 `name`, `phone` 반영 및 `email`, `password_hash` 유지 확인.
- `deleteById` 후 id/email 조회 결과 없음 확인.
- 동일 `email` 중복 insert 시 DB unique 제약 기반 `DuplicateKeyException` 확인.

### Test Result

- Command: `.\mvnw.cmd test`
- Result: BUILD SUCCESS.
- Summary: Tests run: 42, Failures: 0, Errors: 0, Skipped: 0.
- Note: 첫 실행은 `@MybatisTest`가 `MemberMapper` scan 범위를 잡지 못해 실패했고, 테스트 클래스에 `@MapperScan("com.ssafy.home.member.mapper")`를 명시해 해결했다.

### Remaining Risks

- 중복 email 경쟁 상황에서 DB unique 제약은 mapper 통합 테스트로 확인했지만, 동시 가입 경쟁 상황의 API 응답 변환 검증은 이번 보강 범위 밖이다.
## Docker MySQL Live Verification (2026-06-01)

### Scope

- Sprint6 구현 변경 없이 실제 Docker MySQL과 Spring Boot HTTP API 흐름을 live 검증했다.
- 테스트용 고유 이메일만 사용했고, 비밀번호와 세션 쿠키 값은 문서에 기록하지 않았다.

### Environment

- Docker command: `docker compose ps`
- Initial status: compose project had no running MySQL container.
- Action: `docker compose up -d mysql`
- Verified status: `no-home-mysql` running healthy, `0.0.0.0:3306->3306/tcp`.
- App command: Spring Boot app launched temporarily on `http://127.0.0.1:18080`.
- App cleanup: live verification Spring Boot process was stopped after verification.

### Live API Result

- Test email: `sprint6_live_20260601111729@example.test`
- `POST /api/members`: 201 Created.
- Duplicate `POST /api/members` with same email: 409 Conflict.
- `POST /api/auth/login`: 200 OK and session cookie received.
- `GET /api/members/me` with session: 200 OK.
- `PUT /api/members/me` with session: 200 OK, response name changed to `Sprint6 Live Updated`.
- `DELETE /api/members/me` with session: 200 OK, response `deleted=true`.
- `GET /api/members/me` with same session after delete: 401 Unauthorized.
- Direct Docker MySQL query after delete: `SELECT COUNT(*) FROM members WHERE email='sprint6_live_20260601111729@example.test';` returned `0`.

### Automated Test Result

- Command: `.\mvnw.cmd test`
- Result: BUILD SUCCESS.
- Summary: Tests run: 42, Failures: 0, Errors: 0, Skipped: 0.

### Notes / Environment Constraints

- First `docker compose ps` attempt failed inside sandbox because Docker config access was denied. Re-running with approved Docker command permission succeeded.
- First temporary app launch attempt failed because Maven wrapper download/connect was denied in the sandboxed background process. Re-running the app launch with approved Maven wrapper permission succeeded.
- Docker MySQL container was started for live verification and remained healthy after the app process cleanup.

### Remaining Risks

- Live flow verifies one sequential session/API path against Docker MySQL. Concurrent duplicate signup race handling is still not live-tested at API level.
- The verification used the current local Docker volume initialized from the repository schema. A stale pre-existing volume in another environment may still require schema migration or volume reinitialization.
