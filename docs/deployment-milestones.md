# 무료 운영 배포 마일스톤

## 목표

실제 API Key와 서비스 접속 정보만 입력하면 Vercel, Render, Neon 환경에서 테스트하고 배포할 수 있는 구조를 만든다.

## M1. 배포 기반 표준화

- 로컬, 테스트, 운영 환경을 분리한다.
- 실제 키 없이도 빌드 가능한 배포 설정을 준비한다.
- 완료: NOHOME-12~15에서 환경변수, Render·Neon, Vercel 기반과 로컬 통합 검증을 마쳤다.

## M2. 자동 검증 체계 구축

- Backend, Frontend, 환경설정과 배포 구성을 CI와 사전 검사로 검증한다.
- 완료: NOHOME-17~21에서 로컬 사전 검사, Node 24 보안 기준, GitHub CI, Compose Smoke Test와 CodeQL 통합 검증을 마쳤다.

## M3. 클라우드 연결 및 공개 배포

- Neon DB, Render Backend, Vercel Frontend를 실제로 연결한다.
- 핵심 사용자 흐름을 공개 환경에서 검증한다.
- 완료: NOHOME-24~27에서 Neon 운영 DB를 Flyway `V1`부터 초기화하고, Render·Vercel 기본 HTTPS 도메인 연결, Secure 세션 인증과 운영 기능 차단 정책을 공개 환경에서 검증했다. 실제 URL·DB 접속 정보·비밀값은 작업 문서에 기록하지 않는다.

## M4. 운영 안정화 및 문서화

- 외부 API, 인증, 장애, 콜드 스타트, 로그와 백업을 검증한다.
- 운영 절차와 면접 방어 문서를 완성한다.

## 진행 원칙

- 현재 마일스톤의 세부 계획과 Jira 작업만 확정하고, 완료 후 다음 마일스톤을 계획한다.
- Jira 작업 계획 승인 후 `docs/work/<JIRA-KEY>-<slug>/` 문서와 전용 브랜치를 만든다.
- M1과 M2는 실제 API Key 없이 완료하고, 실제 서비스 연결과 외부 API 검증은 M3과 M4에서 진행한다.
- React/Vite, Spring Boot, Maven, PostgreSQL 구조를 유지한다.

