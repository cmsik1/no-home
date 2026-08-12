# 프로젝트 전체 검토 결과

## 검토 결과

M1부터 M5까지 프로젝트 구조, 현재 문서, Backend·Frontend 운영 소스와 검증 파이프라인을 순서대로 확인했다. 현재 프로젝트를 이해할 때는 루트 `README.md`, `docs/project-pipeline.md`, 각 소스의 클래스·함수 설명 순서로 읽으면 된다.

## 완료한 작업

- 사용하지 않는 `BackendAtlas`를 제거했다.
- `Artifact` 89개와 `Backend/docs` 44개, 총 133개 기존 자료를 `docs/archive`에 원형 보존했다.
- 현재 문서 인덱스와 로컬 Markdown·이미지 링크 검사를 구성했다.
- 서버 기동, HTTP 요청, 인증, DB, 공공데이터, AI 처리와 Maven Wrapper·생성 파일을 설명하는 프로젝트 파이프라인 문서를 작성했다.
- Backend 운영 Java 134개를 검토하고 핵심 흐름 61개 파일에 클래스·메서드 설명을 보강했다.
- Frontend 운영 JavaScript·JSX 53개를 검토하고 핵심 흐름 29개 파일에 함수 설명을 보강했다.
- 주택 검색과 가격 범위 요청의 중복 HTTP 처리를 공통화하고 계약 테스트 2개를 추가했다.

## 최종 검증

- Backend: Maven test 175개 통과, 실행 JAR package 성공
- Frontend: Node 단위 테스트 54개와 Vitest 컴포넌트 테스트 4개 통과, Vite production build 성공
- Docker Compose: `.env.example` 기반 구성 검사 통과
- 문서: 현재 문서의 로컬 링크 46개 통과
- 구조: `BackendAtlas`, `Artifact`, `Backend/docs` 부재 확인
- 계약: Backend resource·Flyway migration·Maven 설정 변경 없음
- 생성 파일: `target`, `dist`, `node_modules`가 Git 변경에 포함되지 않음
- Git: `git diff --check` 통과, 기존 사용자 변경인 `.gitignore` 보존

## 남은 위험과 후속 권고

- npm audit는 개발 도구인 Vite·Vitest 계열에서 5개 취약점을 보고한다. 완전 해소에는 Vite 8·Vitest 4 수준의 메이저 업그레이드와 회귀 검증이 필요하다.
- Backend의 주택 검색·공공데이터 적재 서비스와 Frontend의 검색·지도 hook은 책임이 크다. 분리 전 트랜잭션·요청 취소·지도 수명주기 테스트를 먼저 확충해야 한다.
- Archive는 과거 경로를 포함한 원형 기록이므로 현재 구현 판단에는 사용하지 않는다.
- 실제 브라우저에서 로그인, 주택 검색, 지도 마커와 AI 명령을 확인하는 수동 시나리오는 유효한 API 키와 로컬 환경 변수가 준비된 환경에서 수행한다.
