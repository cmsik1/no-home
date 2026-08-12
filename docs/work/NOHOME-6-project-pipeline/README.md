# NOHOME-6 프로젝트 파이프라인 문서 작성

## 목적

실제 소스를 기준으로 서버 기동, 사용자 요청, 데이터 변환과 저장 흐름을 설명하고 백엔드 면접에서 설계 근거를 답할 수 있는 기준 문서를 만든다.

## 결과

- `docs/project-pipeline.md`에 전체 시스템과 Compose·Spring Boot·Frontend 기동 순서를 정리했다.
- 공통 HTTP 요청과 주택 검색, 로그인·JWT, AI 답변·화면 명령의 데이터 흐름을 실제 소스에 연결했다.
- DB 관계, Flyway, 트랜잭션, 예외 처리와 공공데이터 비동기 적재를 설명했다.
- Maven Wrapper, Maven cache, `target`, `node_modules`, `.vite`, `dist`, Docker·DB 생성물의 생성 주체와 관리 기준을 한곳에 정리했다.
- 계층 분리, native SQL, 인증, 외부 API와 테스트 전략을 면접 질문 형식으로 설명하고 현재 구조의 트레이드오프를 기록했다.

## 검증 결과

- 현재 문서의 로컬 Markdown 링크 45개가 모두 실제 파일 또는 디렉터리를 가리킨다.
- endpoint, 주요 설정 값, dependency version과 DB table을 실제 소스와 대조했다.
- Backend와 Frontend 운영 소스 변경은 0개다.
- `git diff --check`가 통과했다.
