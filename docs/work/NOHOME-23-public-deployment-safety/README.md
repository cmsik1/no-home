# NOHOME-23 공개 배포 안전장치

공개 배포 전에 계정·회원 조회·공공데이터 기능을 운영 환경에서 기본 차단하고 Neon 연결 계약을 강화한다.

## 상태

- Jira: NOHOME-23 (`진행 중`)
- 브랜치: `codex/NOHOME-23-public-deployment-safety`
- 현재 단계: 사용자 승인 완료, 마감

## 범위

- 운영 비밀번호 재설정·회원 검색 차단
- 운영 공공데이터 실시간 조회·수동 import 차단
- 관리자 이메일 기본값 제거와 Frontend 위험 UI 숨김
- PostgreSQL JDBC 42.7.12 및 Neon channel binding 계약
- 배포 사전 검사 확장

## 제외 범위

- Neon·Render·Vercel 리소스 생성
- 실제 외부 API Key 입력과 주택 데이터 적재
- DB schema 변경

세부 구현 및 검증 기준은 [plan.md](plan.md)를 따른다.

## 완료 결과

- 운영 프로필에서 비밀번호 재설정, 회원 검색, 공공데이터 실시간 조회와 수동 import를 기본 차단했다.
- 차단 API는 HTTP 503과 `FEATURE_DISABLED`를 반환하며, 주택 검색은 외부 API 없이 DB 결과를 반환한다.
- 관리자 이메일 기본값과 관련 Frontend UI 노출을 제거했다.
- PostgreSQL JDBC 42.7.12 및 Neon channel binding 연결 계약을 반영했다.
- 실제 Neon·Render·Vercel 리소스 생성과 외부 API Key 입력은 후속 작업으로 남겼다.
