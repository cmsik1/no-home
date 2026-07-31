# plan.md

## 2026-06-05 Sprint9 Active Verification Update

- Active contract: `docs/sprints/Sprint9.md`.
- M4 real map/search pagination work is implemented and build/API verified.
- Remaining M4 verification risk: actual Kakao map visual rendering and marker display still need manual browser confirmation because the in-app browser runtime fails in this environment.

## 2026-06-01 Milestone Status Update

- M2 housing data/import/search backend baseline is considered complete through Sprint5 Reviewer Pass.
- Sprint5 residual risks are non-blocking for the next milestone: Seoul-wide auto-import may call up to 25 API imports, and API/network failure recovery guidance remains minimal.
- Next planned milestone work is M3 member CRUD and session auth.
- Active contract: `docs/sprints/Sprint6.md`.

## 2026-06-01 Sprint6 Completion / Sprint7 Planning Update

- M3 member CRUD and session auth backend baseline is considered complete through Sprint6 Reviewer Pass plus Docker MySQL live verification.
- Sprint6 residual risks are non-blocking for frontend integration: concurrent duplicate signup race response conversion and stale Docker volume migration remain follow-up concerns.
- Next planned milestone work is M4 main search/list/detail and map placeholder UI shell.
- Active contract: `docs/sprints/Sprint7.md`.

## 2026-06-05 Sprint8 Completion / Next Planning Update

- M3 member CRUD and session auth is complete through Sprint6 backend Reviewer Pass and Sprint8 Vue integration Reviewer Pass.
- M4 required UI/API connection baseline is partially complete: main search/list/detail shell, map placeholder, Spring Boot static delivery, and member signup/login/logout/me update/delete flows are connected and verified.
- Remaining M4 work before full milestone completion: real map display, coordinate policy, and search-result marker behavior.
- Next planned milestone work is Kakao Map/coordinate integration, followed by the first additional feature milestone for nearby commercial map.
- Active contract: `docs/sprints/Sprint8.md`.

## Master Plan

이 프로젝트는 작은 Sprint 계약 단위로 실행한다. 다만 `plan.md`는 Sprint 목록이나 상세 백로그를 관리하는 문서가 아니라, 여러 Sprint를 묶는 Milestone 로드맵과 주요 결정 체크포인트를 관리한다.

과제 필수 기능을 먼저 안정화하고, 추가/심화 기능은 필수 흐름이 검증된 뒤 개발자가 선택한다. 개별 Sprint의 목표, 범위, 완료 기준, 작업 로그는 `docs/sprints/SprintN.md`에 기록하고, 여러 Sprint를 거친 현재 진행상황과 핵심 결론은 `docs/working-memory.md`에 누적한다.

## 문서 역할

| 문서 | 역할 |
| --- | --- |
| `docs/PRD.md` | 사용자 관점의 제품 요구사항과 기능 가치 |
| `docs/spec.md` | 기술 아키텍처, DB, API, 검증 규칙 |
| `docs/plan.md` | Milestone 로드맵과 주요 결정 체크포인트 |
| `docs/working-memory.md` | 현재 대시보드, 활성 Sprint, 완료 Sprint 요약, 누적 트러블슈팅 |
| `docs/sprints/SprintN.md` | 개별 Sprint 계약, 실행 로그, 검증 결과, 남은 리스크 |

## 마일스톤

| 마일스톤 | 목표 | 포함될 수 있는 Sprint | 상태 |
| --- | --- | --- | --- |
| M0 | 계획 문서와 Decision Board 생성 | Sprint 0 | 완료 |
| M1 | 프로젝트 골격, DB 연결, MyBatis 설정 | Sprint 1 | 완료 |
| M2 | 실거래가 스키마, 데이터 적재, 검색 | Sprint 2-5 | 완료 |
| M3 | 회원 CRUD와 인증 | Sprint 6, Sprint 8 | 완료 |
| M4 | 주택 기본정보와 필수 화면/API 연결 | Sprint 7-8... | 진행 중 |
| M5 | 선택된 추가 기능 구현 | Sprint N... | 대기 |
| M6 | 시간이 남을 경우 심화 기능 구현 | Sprint N... | 대기 |
| M7 | 필수 기능 검증과 제출 산출물 정리 | Sprint N... | 대기 |

## 마일스톤 상세

### M0: 계획 문서와 운영 하네스

목표:
`AGENTS.md`, `PRD.md`, `spec.md`, `plan.md`, `working-memory.md`, 초기 Sprint 문서를 정리해 Manager / Generator / Reviewer 흐름을 시작할 수 있게 한다.

주요 산출:

- 과제 필수 기능을 보존한 PRD.
- 기술 기준선과 검증 규칙.
- Milestone 중심의 Master Plan.
- 현재 상태를 보여주는 working memory.
- Sprint별 계약을 담는 `docs/sprints` 구조.

완료 판단:

- 필수 문서가 존재한다.
- 확정 요구사항과 결정 필요 항목이 분리된다.
- Sprint 상세 계약은 `docs/sprints/SprintN.md`에 위치한다.

### M1: 백엔드 골격과 DB 연결

목표:
Spring + MyBatis 기반으로 실행 가능한 서버와 DB 연결 기준선을 만든다.

주요 산출:

- 프로젝트 구조.
- MySQL 연결.
- MyBatis mapper scan과 샘플 쿼리.
- 환경변수 또는 gitignore 기반 설정 관리.

완료 판단:

- 이 마일스톤에 속한 Sprint가 Pass 된다.
- 서버 실행과 DB 연결이 검증된다.
- 관련 결정 결과가 `working-memory.md`에 반영된다.

### M2: 실거래가 데이터

목표:
필수 주택 실거래가 데이터를 저장하고, 동별/아파트명별로 검색할 수 있게 한다.

주요 산출:

- `houses`, `house_deals` 스키마.
- `regions` 스키마와 지역 조회 흐름.
- 공공데이터 seed/import 경로.
- 동별 검색.
- 아파트명 검색.

완료 판단:

- 이 마일스톤에 속한 Sprint가 Pass 된다.
- 샘플 실거래가 데이터가 DB에서 조회된다.
- 동별/아파트명별 검색이 조건에 맞는 결과를 반환한다.
- 공공데이터 적재와 중복 처리 리스크가 기록된다.

### M3: 회원 CRUD와 인증

목표:
회원 가입, 조회, 수정, 삭제와 로그인/로그아웃 흐름을 구현한다.

주요 산출:

- 회원 가입.
- 로그인과 로그아웃.
- 내 회원 정보 조회.
- 내 회원 정보 수정.
- 현재 로그인 사용자의 회원 물리 삭제.

완료 판단:

- 이 마일스톤에 속한 Sprint가 Pass 된다.
- 비밀번호가 평문으로 저장되지 않는다.
- 현재 로그인 사용자의 회원 정보만 수정/삭제할 수 있다.
- 보호된 회원 화면 또는 엔드포인트 접근이 로그인 상태로 제어된다.

### M4: 주택 기본정보와 필수 UI / API 연결

목표:
실거래가 데이터와 주택 기본정보를 연결하고, 필수 사용자 흐름을 end-to-end로 시연할 수 있게 한다.

주요 산출:

- 실거래가에서 주택 기본정보 추출.
- 주택 상세 조회.
- 메인 페이지.
- 동별 검색 결과 흐름.
- 아파트명 검색 결과 흐름.
- 회원 페이지.
- 로그인/로그아웃 흐름.
- Vue 빌드 결과를 Spring Boot 정적 리소스에 포함하는 실행 구조.

완료 판단:

- 이 마일스톤에 속한 Sprint가 Pass 된다.
- 필수 흐름을 end-to-end로 시연할 수 있다.
- 주택 기본정보와 실거래가의 연결 정합성이 확인된다.
- 제출용 실행 화면 캡처가 가능하다.
- Spring Boot 애플리케이션 하나로 Vue 화면과 REST API를 함께 실행할 수 있다.

### M5: 추가 기능

목표:
개발자가 선택한 추가 기능 하나를 필수 기능 위에 안정적으로 확장한다.

후보:

1. 관심 지역.
2. 주변 상권 검색.
3. 주변 환경 정보 지도.
4. 비밀번호 찾기.

완료 판단:

- 구현 전 선택 이유와 완료 기준이 Sprint 문서에 기록된다.
- 구현한 추가 기능이 필수 흐름을 깨지 않는다.
- 시연 가능한 결과가 남는다.

### M6: 심화 기능 후보

목표:
시간이 남을 경우 심화 기능 하나를 선택해 구현한다.

후보:

- 공지사항 관리.
- 환경 정보 지도.
- 주택 관련 뉴스.
- 공유 게시판.

완료 판단:

- 선택 이유와 제외 범위가 Sprint 문서에 기록된다.
- 구현한 심화 기능이 필수 기능을 깨지 않는다.
- 남은 리스크가 `working-memory.md`에 요약된다.

### M7: 제출 안정화

목표:
필수 프로젝트를 안정화하고 제출물을 준비한다.

주요 산출:

- 회귀 검증.
- 개선된 요구사항 목록 정리.
- Class Diagram 갱신.
- README 또는 결과 문서.
- 구현 Source 정리.
- 실행 화면 캡처.

완료 판단:

- 필수 기능 검증이 통과한다.
- 제출 항목이 정리된다.
- 최종 상태와 남은 리스크가 `working-memory.md`에 반영된다.

## 결정 체크포인트

M2 시작 전:

- 공공데이터 초기 적재 지역 범위.
- 공공데이터 초기 적재 기간 범위.
- 공공데이터 초기 적재 주택 유형 범위.
- API 원본 row의 중복 판정용 hash 구성: 완료.
- 실거래가 데이터의 최소 필드.
- 지역 코드와 법정동명 기준.
- 실거래가 데이터의 좌표 확보 방식과 좌표가 없는 항목의 표시 정책.

M5 시작 전:

- 주변 상권 지도에 사용할 데이터 출처.
- 주변 상권 지도 업종 분류 범위.
- 지도 API 선택.

## 에이전트 흐름

각 Sprint는 다음 순서로 진행한다.

1. Manager가 Milestone 목표와 현재 상태를 확인한다.
2. Manager가 활성 `docs/sprints/SprintN.md`를 생성하거나 갱신한다.
3. Generator가 활성 Sprint 범위만 구현하고 작업 로그를 남긴다.
4. Reviewer가 `docs/PRD.md`, `docs/spec.md`, 활성 Sprint 완료 기준으로 검증한다.
5. Manager가 Sprint 결과와 핵심 결론을 `docs/working-memory.md`에 누적한다.
6. Milestone 범위, 순서, 상태가 바뀐 경우에만 `docs/plan.md`를 갱신한다.
