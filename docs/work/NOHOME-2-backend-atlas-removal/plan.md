# NOHOME-2 구현 계획

## 목표

`BackendAtlas` 디렉터리 전체와 현재 사용 안내의 관련 참조를 제거한다.

## 범위

- `BackendAtlas` 전체 삭제
- 루트 `README.md`의 Backend Atlas 소개와 링크 삭제
- 삭제 대상, Git 추적 상태와 복구 가능 범위 검증

## 제외 범위

- Backend 및 Frontend 운영 소스
- API와 데이터베이스 스키마
- Jira 및 마일스톤 문서에 남는 작업 이력
- 사용자의 기존 `.gitignore` 변경

## 구현 순서

1. 삭제 전 파일 수, 용량과 Git 추적 상태를 확인한다.
2. `BackendAtlas` 전체를 삭제한다.
3. 루트 README의 현재 사용 안내에서 Atlas 참조를 제거한다.
4. 경로와 참조의 잔존 여부를 검사한다.
5. Git 변경 범위와 공백 오류를 검사한다.

## 완료 기준

- `BackendAtlas` 디렉터리가 존재하지 않는다.
- 현재 사용 안내에 Atlas 링크가 남지 않는다.
- Backend와 Frontend 운영 소스가 변경되지 않는다.
- 사용자의 기존 `.gitignore` 변경이 보존된다.
- `git diff --check`가 통과한다.

## 검증

- 삭제 대상 경로 존재 여부 검사
- 현재 문서 내 Atlas 참조 검사
- `git diff --name-status` 변경 범위 검사
- `git diff --check`

## 완료 결과

- `BackendAtlas`의 Git 추적 파일 25개와 로컬 생성 파일을 삭제했다.
- 삭제 전 확인된 전체 크기는 약 585.8MB이며 대부분은 `node_modules`였다.
- 루트 README에서 Backend Atlas 소개와 링크를 제거했다.
- 경로 및 현재 문서 참조 검사, 운영 소스 무변경 검사와 `git diff --check`가 통과했다.
- 사용자가 수동 확인 후 종료를 승인했다.

## 잔여 사항

- Jira와 마일스톤 문서에는 작업 이력으로 `BackendAtlas` 명칭을 유지한다.
- 다음 티켓 계획은 이 작업이 종료된 상태를 기준으로 별도 작성한다.
