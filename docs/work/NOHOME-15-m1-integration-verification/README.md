# NOHOME-15 M1 배포 기반 통합 검증

배포 M1에서 준비한 환경변수 계약, Render·Neon Backend 기반과 Vercel Frontend 기반이 하나의 저장소에서 함께 동작하는지 검증하는 작업이다. 실제 클라우드 리소스와 외부 API Key는 사용하지 않는다.

## 확인 대상

- Backend 전체 테스트, 패키징과 Docker 이미지
- Frontend 전체 테스트와 운영 빌드
- Docker Compose의 Frontend·Backend·PostgreSQL 연결
- DB 정상·장애·복구에 따른 `/api/health` 상태
- Render·Vercel 배포 설정과 비밀값 보관 경계

## 클라우드에서 남은 검증

M1은 저장소와 로컬 컨테이너 수준까지만 검증한다. Neon 실제 TLS 연결, Render 공개 Backend, Vercel Rewrite와 실제 도메인의 쿠키 흐름은 배포 M3에서 확인한다. 외부 API Key와 운영 장애·로그·백업 검증은 M4 범위다.

## 완료 결과

- 외부 API Key 없이 Backend·Frontend 빌드와 전체 로컬 스택 기동을 확인했다.
- Frontend 경유 health가 DB 정상 200, 장애 503, 복구 후 200을 반환하고 `no-store`를 유지했다.
- 실제 클라우드 연결은 M3~M4 범위로 남겼다.
- Frontend 개발 도구 의존성 감사에서 기존 취약점 5개가 확인됐으며 M2의 자동 검증·의존성 정책에서 다룬다.
