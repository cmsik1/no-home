# NOHOME-13 구현 계획

## 목표

실제 Render·Neon 계정 없이 Spring Boot Backend의 무료 배포 구성, 운영 자원 제한과 HTTP health 계약을 저장소에서 검증 가능하게 만든다.

## 범위

- Render Docker Blueprint와 수동 배포 정책
- Render `PORT`, `prod` profile과 JVM 메모리 비율 설정
- 운영 Hikari 최대 5개·최소 유휴 0개 설정
- Neon PostgreSQL JDBC와 TLS 계약
- DB 상태에 따른 `/api/health`의 200·503 응답
- `/api/**`의 `Cache-Control: no-store` 정책

## 제외 범위

- 실제 Render·Neon 리소스 생성과 외부 API 호출
- Vercel Rewrite, CI/CD, 운영 모니터링과 백업
- DB schema와 일반 API 요청·응답 변경

## 구현 순서

1. Render Blueprint에 Backend Docker build, Singapore 리전, health path와 수동 배포 정책을 선언한다.
2. 운영 포트, JVM과 Hikari 제한 및 Neon TLS URL 계약을 구성한다.
3. health의 DB 장애 상태를 HTTP 503으로 변환한다.
4. API 정상·오류 응답에 공통 no-store filter를 적용한다.
5. 단위·통합·Docker black-box 검증과 정적 검사를 실행한다.

## 완료 기준

- Blueprint의 비밀값은 Dashboard 입력 대상으로만 선언된다.
- Docker 이미지가 동적 `PORT`와 운영 profile로 기동된다.
- DB 정상 시 health 200, 기동 후 DB 장애 시 503을 반환한다.
- 모든 API 응답에 no-store가 적용된다.
- 실제 Neon 계정과 외부 API Key 없이 테스트가 통과한다.
- 사용자 소유 `.gitignore` 변경을 수정하지 않는다.

## 자동 검증

- Backend 집중 테스트와 전체 테스트·패키징
- Backend Docker 이미지 빌드
- 로컬 PostgreSQL 기반 운영 컨테이너와 DB 중단 black-box 검사
- Render Blueprint, 비밀값, 문서 링크와 `git diff --check` 검사

## 사용자 확인

- `render.yaml`에서 서비스, 수동 배포와 Dashboard 입력값을 확인한다.
- 작업 README에서 Neon JDBC 변환 방법과 실제 값의 보관 위치를 확인한다.
- health 상태 코드와 API no-store 정책이 의도와 일치하는지 확인한다.

## 완료 기록

- 집중 테스트 10개와 Backend 전체 테스트 184개가 통과했다.
- Backend 패키징과 Docker 이미지 빌드가 통과했다.
- 운영 유사 컨테이너에서 DB 정상 시 health 200, DB 중단 시 503과 두 응답의 `Cache-Control: no-store`를 확인했다.
- `render.yaml`은 Render 공식 JSON Schema 검사를 통과했다.
- 비밀값·운영 주소, Markdown 링크와 `git diff --check` 검사가 통과했다.
- 사용자가 결과를 승인했으며 후속 작업은 NOHOME-14다.
