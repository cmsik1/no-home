# NOHOME-19 GitHub Actions 빌드·테스트 CI

## 목적

Pull Request, `main` 브랜치 반영 및 수동 실행 시 배포 전에 필요한 설정·Backend·Frontend 검사를 동일한 기준으로 자동 실행한다.

## 예상 동작

- Preflight, Backend, Frontend 검사가 서로 독립적으로 병렬 실행된다.
- 환경설정, 테스트, 빌드 또는 보안 검사에 문제가 생기면 해당 CI 작업이 실패한다.
- Backend 테스트 실패 시 Surefire·Failsafe 보고서를 내려받아 원인을 확인할 수 있다.
- 외부 API Key와 클라우드 계정 없이 실행할 수 있으며 실제 배포는 수행하지 않는다.

## 중요 정책

- Java 17과 Node 24만 지원 대상으로 검증한다.
- GitHub Actions는 불변 커밋 SHA로 고정하고 Dependabot으로 갱신한다.
- 동일 브랜치의 이전 실행은 취소해 불필요한 자원 사용을 줄인다.
- 실제 GitHub 실행 결과 확인은 NOHOME-21에서 원격 브랜치를 올린 뒤 수행한다.

## 완료 결과

- `.github/workflows/ci.yml`에 Preflight, Backend, Frontend 병렬 작업을 구성했다.
- 배포 사전 검사와 actionlint 정적 검사가 통과했다.
- Backend Maven `verify`에서 184개 테스트와 Spring Boot JAR 패키징이 통과했다.
- Frontend에서 취약점 0건, 단위 테스트 58개, 컴포넌트 테스트 4개와 운영 빌드가 통과했다.
- 필수 검사 명령을 제거한 임시 fixture 5종이 모두 계약 위반으로 탐지됐다.
- 문서 링크 검사와 `git diff --check`가 통과했다.

실제 GitHub Runner 실행과 CodeQL을 포함한 원격 통합 확인은 NOHOME-21 범위로 남긴다.
