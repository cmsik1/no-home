# NOHOME-21 M2 자동 검증 체계 통합 검증

## 목적

M2에서 구축한 로컬 검사와 GitHub Actions가 실제 원격 저장소에서 같은 기준으로 동작하는지 확인하고, 개발자가 검사 결과와 실행 방법을 쉽게 찾을 수 있게 한다.

## 확인 대상

- GitHub CI의 Preflight, Backend, Frontend와 Compose Smoke
- Java 및 JavaScript/TypeScript CodeQL
- 필수 설정 누락, 비밀값, 클라우드 주소와 취약 의존성 차단
- README의 상태 배지와 로컬 실행 명령

실제 Render·Neon·Vercel 리소스 생성이나 비밀값 입력은 수행하지 않는다.

## 완료 결과

- [GitHub CI](https://github.com/cmsik1/no-home/actions/runs/31761358739)에서 Preflight, Backend, Frontend와 Compose Smoke가 모두 통과했다.
- [CodeQL](https://github.com/cmsik1/no-home/actions/runs/31761358724)에서 Java와 JavaScript/TypeScript 분석이 모두 통과했다.
- 설정 누락, 비밀값·운영 주소, 과거 취약 lockfile과 Docker 연결 장애 fixture가 의도대로 실패했다.
- README에 CI·CodeQL 상태 배지와 로컬 사전 검사·Smoke Test 명령을 추가했다.

## 검증

- `scripts/check-deployment-preflight.ps1`: 통과, Node 24.19.0 사용
- `scripts/check-markdown-links.ps1`: 현재 문서 링크 47개 통과
- GitHub CI 및 CodeQL: 6개 작업 통과
- `git diff --check`: 통과

## 제외 범위와 다음 작업

실제 Neon·Render·Vercel 리소스와 외부 API Key는 사용하지 않았다. 다음 단계인 M3에서 클라우드 리소스를 연결하고 공개 환경의 핵심 사용자 흐름을 검증한다.
