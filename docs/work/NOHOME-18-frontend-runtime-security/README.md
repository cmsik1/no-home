# NOHOME-18 Frontend 런타임·보안 기준

Frontend 개발·Docker·Vercel 실행 환경을 Node 24 LTS로 통일하고, high·critical npm 취약점을 자동으로 차단하는 작업이다.

Vite 8.2.1, Vitest 4.1.10과 React 플러그인 6.0.5만 정확한 버전으로 올린다. 다른 UI·테스트 의존성은 불필요하게 변경하지 않는다. Dependabot은 npm·Maven·GitHub Actions 갱신 PR을 만들고, CodeQL은 Backend Java와 Frontend JavaScript를 분석한다.

```powershell
cd Frontend
npm ci
npm test
npm run build
npm run audit:security
```

CodeQL의 실제 GitHub 실행 확인은 원격 push가 허용되는 `NOHOME-21`에서 수행한다.

## 완료 결과

- Node 24, Vite 8.2.1, Vitest 4.1.10과 React 플러그인 6.0.5로 전환했다.
- 기존 Frontend 테스트 62개, 운영 빌드와 Docker 실행이 통과했다.
- 기존 npm 취약점 5건을 제거했고 전체 감사 결과가 0건임을 확인했다.
- Dependabot과 CodeQL 설정은 YAML 및 actionlint 검사를 통과했다.
- 실제 GitHub CodeQL 실행 결과 확인은 `NOHOME-21`에 남겼다.
