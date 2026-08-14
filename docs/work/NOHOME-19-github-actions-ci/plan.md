# NOHOME-19 구현 계획

## 목표

GitHub Actions에서 로컬 사전 검사, Backend 전체 테스트·패키징, Frontend 보안 검사·테스트·운영 빌드를 자동화한다.

## 경계

- `.github/workflows/ci.yml`과 이 작업 문서만 추가한다.
- CodeQL, 애플리케이션 API, DB schema와 운영 소스는 변경하지 않는다.
- 실제 원격 실행, 배포 및 브랜치 보호 설정은 이 작업에서 수행하지 않는다.

## 구현 순서

1. PR, `main` push와 수동 실행 트리거를 정의한다.
2. 최소 권한, 중복 실행 취소와 작업별 제한 시간을 적용한다.
3. Preflight, Backend, Frontend 작업을 의존 관계 없이 구성한다.
4. 액션을 커밋 SHA로 고정하고 Backend 실패 보고서를 보존한다.
5. 정적 검사와 각 작업의 로컬 동등 명령을 실행한다.

## 계약

- Preflight: Node 24에서 배포 사전 검사 스크립트를 실행한다.
- Backend: Temurin 17에서 Maven `verify`를 실행하고 실패 보고서를 7일 보존한다.
- Frontend: Node 24에서 clean install, high 이상 취약점 검사, 62개 테스트와 운영 빌드를 실행한다.
- 세 작업은 실제 비밀값이나 외부 API Key를 요구하지 않는다.

## 완료 기준

- actionlint와 workflow 의미 검사가 통과한다.
- 세 작업과 동일한 로컬 검증 명령이 모두 통과한다.
- 실패 조건이 CI 작업 실패로 전달됨을 임시 fixture에서 확인한다.
- 문서 링크 검사와 `git diff --check`가 통과한다.

## 검증 명령

- `pwsh -NoProfile -File scripts/check-deployment-preflight.ps1`
- `./mvnw --batch-mode --no-transfer-progress verify`
- `npm ci && npm run audit:security && npm test && npm run build`
- `actionlint .github/workflows/ci.yml`
- `pwsh -NoProfile -File scripts/check-markdown-links.ps1`
- `git diff --check`
