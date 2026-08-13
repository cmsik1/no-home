# NOHOME-14 구현 계획

## 목표

실제 Vercel·Render 계정 없이 Frontend의 정적 배포, API Rewrite와 SPA fallback 구성을 저장소에서 검증 가능하게 만든다.

## 범위

- `Frontend` 기준 Vercel 프로그램형 설정
- `BACKEND_ORIGIN` 검증과 `/api/**` 외부 Rewrite
- API Rewrite 이후 적용되는 SPA fallback
- 기존 상대경로 API 호출과 쿠키 인증 계약 유지
- 구성 단위 테스트와 배포 사용법 문서화

## 제외 범위

- 실제 Vercel 프로젝트·도메인 생성과 Render 연결
- 실제 Kakao Key 입력과 외부 API 검증
- Backend CORS 변경, CI/CD와 자동 배포
- React Router 또는 Next.js 도입

## 구현 순서

1. 환경변수 검증과 Vercel 구성 생성을 테스트 가능한 모듈로 분리한다.
2. `vercel.ts`에서 동적 `BACKEND_ORIGIN`을 읽어 API Rewrite와 SPA fallback을 선언한다.
3. 누락·잘못된 origin, 정상화와 Rewrite 우선순위를 단위 테스트한다.
4. 환경변수 예시와 Vercel Dashboard 입력 절차를 문서화한다.
5. Frontend 전체 테스트·빌드와 정적 검사를 실행한다.

## 인터페이스

- 새 배포 변수: `BACKEND_ORIGIN=https://<render-service-host>`
- 브라우저 API 계약: 기존 `/api/**` 유지
- Vercel 라우팅: `/api/**` Rewrite가 `index.html` fallback보다 우선
- 공개 API, DB schema와 화면 동작은 변경하지 않는다.

## 완료 기준

- 실제 Kakao Key 없이 Frontend 테스트와 운영 빌드가 통과한다.
- Backend origin이 소스나 브라우저 번들에 하드코딩되지 않는다.
- `BACKEND_ORIGIN` 누락, HTTP·credential·path·query·fragment 포함 값이 거부된다.
- 유효한 HTTPS origin은 trailing slash 없이 정규화된다.
- API Rewrite 우선순위와 SPA fallback이 자동 검사된다.
- 동일 출처 쿠키 흐름을 위해 별도 CORS 설정을 추가하지 않는다.
- 사용자 소유 `.gitignore` 변경을 수정하지 않는다.

## 자동 검증

- Vercel 구성 단위 테스트
- Frontend 전체 unit·component 테스트
- Kakao Key 없는 Vite 운영 빌드
- 브라우저 번들의 Backend origin·비밀값 정적 검사
- 문서 링크와 `git diff --check` 검사

## 사용자 확인

- 작업 README에서 Vercel Root Directory와 Dashboard 변수 입력 위치를 확인한다.
- `/api` Rewrite가 SPA fallback보다 먼저 선언됐는지 확인한다.
- 실제 클라우드 연결 검증은 배포 M3에서 진행한다.

## 완료 기록

- Vercel 구성 테스트 4개, 기존 unit 테스트 54개와 component 테스트 4개가 통과했다.
- Kakao Key 없는 Vite 운영 빌드가 통과했다.
- Backend origin·배포 변수의 브라우저 번들 미포함과 하드코딩된 Render 주소 부재를 확인했다.
- Markdown 로컬 링크 47개와 `git diff --check` 검사가 통과했다.
- 사용자가 결과를 승인했으며 실제 공개 배포 검증은 M3 범위로 남겼다.
