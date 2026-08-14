# NOHOME-21 구현 계획

## 목표

M2-01~04의 자동 검증을 실제 GitHub 실행 결과와 실패 fixture로 종합 확인하고 M2 완료 근거를 남긴다.

## 경계

- CI·CodeQL 실행 결과와 기존 검사 계약을 검증한다.
- README에는 상태 배지와 로컬 검사 사용법만 추가한다.
- 애플리케이션 API, DB schema와 운영 소스는 변경하지 않는다.
- 실제 클라우드 배포와 외부 API 호출은 수행하지 않는다.

## 검증 순서

1. 기존 로컬 `main`을 push해 CI와 CodeQL을 실행한다.
2. Preflight, Backend, Frontend, Compose Smoke와 언어별 CodeQL 결과를 확인한다.
3. 임시 복사본에서 설정 누락·비밀값·운영 주소와 과거 취약 lockfile을 검사한다.
4. README, 작업 문서, 문서 링크와 Git diff를 검증한다.
5. 사용자 승인 후 M2 마일스톤과 Jira 작업·에픽을 완료 처리한다.

## 완료 기준

- GitHub CI 4개 작업과 CodeQL 2개 작업이 모두 통과한다.
- 실패 fixture가 값을 노출하지 않고 의도한 검사를 실패시킨다.
- 현재 설정과 의존성 보안 검사는 통과한다.
- 문서 링크, 비밀값 검사와 `git diff --check`가 통과한다.
- 사용자가 GitHub Actions 결과를 확인하고 승인한다.
