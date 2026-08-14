# NOHOME-20 Docker Compose 통합 Smoke Test

## 목적

배포 전에 PostgreSQL, Spring Boot Backend와 Vite Frontend가 하나의 임시 Compose 스택에서 연결되는지 자동 확인한다.

## 예상 동작

- 실제 비밀값이나 외부 API Key 없이 `prod` 프로필의 전체 스택을 기동한다.
- Backend 직접 요청과 Frontend API 프록시에서 DB 정상·장애·복구 상태를 검증한다.
- 매 실행마다 고유한 Compose 프로젝트와 임의 포트를 사용한다.
- 종료 시 해당 프로젝트의 컨테이너·네트워크·볼륨과 로컬 빌드 이미지만 제거한다.
- 실패 시 Compose 상태와 로그를 남겨 CI에서 내려받을 수 있다.

실제 GitHub Ubuntu Runner 결과 확인은 NOHOME-21에서 진행한다.

## 완료 결과

- `prod` 프로필의 PostgreSQL·Backend·Frontend 전체 스택이 외부 API Key 없이 기동했다.
- Backend 직접 요청과 Frontend 프록시가 DB 정상 200, 중단 503, 복구 200을 반환했다.
- 정상·장애·복구 응답에서 `Cache-Control: no-store`와 health JSON 상태를 확인했다.
- 실행 전후 기존 Docker 컨테이너·네트워크·볼륨의 차이가 0임을 확인했다.
- 의도적인 Docker 연결 실패가 비정상 종료되고 진단 파일을 남기며 임시 환경파일을 제거했다.
- Preflight, actionlint, 비밀값·문서 링크·공백 검사가 통과했다.

실제 GitHub Runner 실행과 로그 아티팩트 확인은 NOHOME-21 범위로 남긴다.
