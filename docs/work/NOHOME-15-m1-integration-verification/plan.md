# NOHOME-15 구현 계획

## 목표

실제 클라우드 계정과 외부 API Key 없이 NOHOME-12~14 결과가 통합된 배포 준비 상태를 재현 가능한 절차로 검증한다.

## 범위

- Backend 전체 테스트·패키징과 Docker 이미지 빌드
- Frontend 전체 테스트와 Key 없는 운영 빌드
- 격리된 Docker Compose 프로젝트의 전체 서비스 기동
- Frontend proxy를 통한 health 200·503·복구 200 검사
- Render·Vercel 설정, 비밀값·운영 주소와 문서 링크 검사
- 검증 결과와 실제 클라우드 잔여 항목 기록

## 제외 범위

- 실제 Neon·Render·Vercel 리소스 생성과 외부 API 호출
- CI/CD와 범용 사전 검사 스크립트 구축
- 의존성 업그레이드와 M1 범위 밖 기능 수정

## 검증 순서

1. Backend와 Frontend의 전체 테스트·빌드를 실행한다.
2. Backend Docker 이미지를 독립적으로 빌드한다.
3. 외부 API Key가 비어 있는 검증용 `.env`를 임시 생성한다.
4. 별도 Compose 프로젝트로 전체 서비스를 기동한다.
5. Frontend, Backend와 Frontend 경유 health를 확인한다.
6. PostgreSQL 중단 시 503, 재시작 후 200 복구를 확인한다.
7. 검증용 컨테이너·볼륨과 임시 `.env`를 제거한다.
8. 배포 설정, 비밀값, 문서 링크와 Git diff를 검사한다.

## 완료 기준

- 모든 자동 검증이 통과한다.
- 외부 API Key 없이 Frontend·Backend·PostgreSQL이 함께 기동한다.
- DB 상태가 Frontend 경유 health 응답의 200·503·200으로 반영된다.
- API 응답에 `Cache-Control: no-store`가 유지된다.
- 실제 비밀값과 하드코딩된 운영 주소가 없다.
- 사용자 소유 `.gitignore` 변경이 보존된다.
- 실제 클라우드에서 검증할 항목이 작업 README에 분리된다.

## 실패 처리

M1 배포 설정에서 발생한 결함만 최소 수정하고 전체 검증을 다시 실행한다. 다른 기능이나 의존성 문제는 결과에 기록하고 후속 계획에서 다룬다.

## 사용자 확인

- 작업 README의 로컬 검증 범위와 클라우드 잔여 범위를 확인한다.
- 자동 검증 결과를 확인한 뒤 NOHOME-15와 M1 마감 여부를 승인한다.

## 완료 기록

- Backend 전체 테스트 184개와 패키징이 통과했다.
- Frontend unit 테스트 58개, component 테스트 4개와 Key 없는 운영 빌드가 통과했다.
- 최종 Backend Docker 이미지와 격리된 전체 Compose 스택 빌드·기동이 통과했다.
- Frontend 경유 health의 200·503·복구 200과 모든 응답의 `Cache-Control: no-store`를 확인했다.
- Render·Vercel 계약, 비밀값·운영 주소, 브라우저 번들, Markdown 링크 47개와 `git diff --check` 검사가 통과했다.
- 검증용 컨테이너·네트워크·볼륨과 임시 환경 파일을 제거했다.
- 사용자 승인으로 NOHOME-15와 배포 M1을 마감하며 다음 단계는 M2 세부 계획 수립이다.
