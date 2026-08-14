# NOHOME-20 구현 계획

## 목표

Docker Compose 전체 스택의 핵심 요청 경로와 DB 장애 복구를 로컬 및 GitHub Actions에서 반복 검증할 수 있게 한다.

## 경계

- Compose 호스트 포트 계약, Smoke 스크립트와 CI 작업만 변경한다.
- 애플리케이션 API, DB schema와 Backend·Frontend 운영 소스는 변경하지 않는다.
- 실제 클라우드 리소스와 외부 API는 사용하지 않는다.

## 구현 순서

1. Backend·Frontend 호스트 포트를 환경변수로 분리한다.
2. 임시 환경파일과 고유 프로젝트를 관리하는 PowerShell 스크립트를 작성한다.
3. 정상 200, DB 중단 503, DB 재시작 200 복구를 직접·프록시 경로에서 검증한다.
4. 실패 진단 수집과 항상 실행되는 격리 자원 정리를 적용한다.
5. 기존 CI에 독립적인 `compose-smoke` 작업을 추가한다.

## 완료 기준

- Frontend 루트와 Backend health가 200을 반환한다.
- Frontend health 프록시가 `no-store`와 올바른 DB 상태를 전달한다.
- PostgreSQL 중단·복구가 503·200으로 반영된다.
- 실행 후 고유 프로젝트의 컨테이너·네트워크·볼륨이 남지 않는다.
- 기존 로컬 Compose 프로젝트와 볼륨은 유지된다.
- Preflight, actionlint, 문서 링크와 `git diff --check`가 통과한다.
