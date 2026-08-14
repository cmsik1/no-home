# NOHOME-18 구현 계획

## 목표

Frontend 런타임을 Node 24 LTS로 표준화하고 의존성 및 정적 보안 검사를 저장소 계약으로 만든다.

## 범위

- package.json, Docker와 Vercel의 Node 24 계약
- Vite 8.2.1, Vitest 4.1.10, React 플러그인 6.0.5
- high 이상을 실패 처리하는 npm audit 명령
- npm·Maven·GitHub Actions용 주간 Dependabot
- Java와 JavaScript/TypeScript CodeQL

일반 CI, Compose Smoke Test, 실제 GitHub CodeQL 실행은 `NOHOME-19~21`에서 진행한다.

## 구현 순서

1. Node 24와 정확한 Frontend 도구 버전을 선언하고 lockfile을 재생성한다.
2. 기존 사전 검사에 Node 24 계약을 추가한다.
3. Dependabot의 생태계별 주간 갱신 정책을 추가한다.
4. 운영 소스만 분석하는 CodeQL workflow를 추가한다.
5. 테스트·빌드·audit·Docker·정적 workflow 검증을 실행한다.

## 인터페이스

- `engines.node`: `24.x`
- 새 npm 명령: `npm run audit:security`
- GitHub 자동화: Dependabot 주간 갱신, CodeQL push·PR·주간·수동 분석
- 애플리케이션 API와 DB schema 변경 없음

## 완료 기준

- npm high·critical 취약점이 0개다.
- Unit 58개와 Component 4개 테스트, 운영 빌드가 통과한다.
- Frontend Docker 이미지가 Node 24로 빌드되고 외부 API Key 없이 응답한다.
- Dependabot과 CodeQL 설정의 정적 검증이 통과한다.
- Backend 운영 소스와 API는 변경되지 않는다.

## 검증

- `npm ci`, `npm test`, `npm run build`, `npm run audit:security`
- Frontend Docker 이미지 빌드, Node 버전과 HTTP 응답 확인
- 배포 사전 검사와 문서 링크 검사
- actionlint 및 YAML 구문 검사
- Backend·API 무변경 검사
- `git diff --check`

## 검증 결과

- Node 24 깨끗한 설치와 npm 감사: 취약점 0건
- Unit 58개·Component 4개 테스트: 통과
- Vite 8 운영 빌드: 통과
- Frontend Docker 빌드와 Node v24.18.0 확인: 통과
- 외부 API Key 없는 Frontend HTTP 200 응답: 통과
- Node 20 불일치 fixture 차단: 통과
- Dependabot YAML, CodeQL 범위와 actionlint: 통과
- 배포 사전 검사, 문서 링크와 `git diff --check`: 통과
- Backend 운영 소스·API 무변경: 확인

사용자가 결과를 승인했다. 실제 GitHub CodeQL 실행과 일반 CI 연결은 후속 작업에서 진행한다.
