# NOHOME-14 Vercel 프론트엔드 배포 기반

React/Vite Frontend를 Vercel에서 정적 배포하고 `/api` 요청을 Render Backend로 전달하기 위한 저장소 설정이다. 실제 Vercel 프로젝트와 공개 URL은 이 작업에서 만들지 않는다.

## 기대 동작

- Vercel 프로젝트의 Root Directory는 `Frontend`를 사용한다.
- 브라우저는 Backend 주소를 알지 못하고 기존처럼 상대경로 `/api`만 호출한다.
- Vercel은 `/api/**`를 `BACKEND_ORIGIN`으로 먼저 전달하고, 나머지 경로는 SPA 진입점인 `index.html`로 연결한다.
- `BACKEND_ORIGIN`이 누락되거나 안전하지 않은 형식이면 배포 설정 평가가 명확하게 실패한다.
- HttpOnly·Secure·SameSite=Lax 쿠키와 별도 CORS 설정이 없는 동일 출처 형태를 유지한다.

## 환경변수 역할

- `BACKEND_ORIGIN`: Vercel 서버 측 Rewrite가 사용할 Render Backend의 HTTPS origin이다. 브라우저 번들에 노출되는 `VITE_` 접두사를 사용하지 않는다.
- `VITE_API_PROXY_TARGET`: 로컬 Vite 개발 서버가 `/api`를 전달할 주소다.
- `VITE_KAKAO_MAP_API_KEY`: 지도 기능 검증 시 입력하는 선택 변수이며 기본 테스트와 빌드에는 필요하지 않다.

실제 주소와 Key는 Git에 기록하지 않고 Vercel Dashboard에서 관리한다.

## Vercel에 연결할 때

1. 저장소를 Vercel 프로젝트로 가져오고 Root Directory를 `Frontend`로 지정한다.
2. Project Settings의 Environment Variables에 `BACKEND_ORIGIN`을 추가한다.
3. 값에는 `https://<render-service-host>`처럼 path가 없는 Render HTTPS origin만 입력한다.
4. Preview와 Production 중 실제로 배포할 환경마다 변수를 설정한 뒤 배포한다.

`BACKEND_ORIGIN`은 Vercel이 설정을 평가할 때만 사용되고 Vite의 `VITE_` 변수가 아니므로 브라우저 번들에 포함되지 않는다. Backend 쿠키에는 `Domain`이 없기 때문에 Rewrite 응답을 받은 브라우저는 쿠키를 Vercel Frontend host의 host-only 쿠키로 저장한다. 이후 브라우저가 동일한 `/api` 경로를 호출하므로 CORS 허용이나 JavaScript의 토큰 접근이 필요하지 않다.

## 완료 결과

- 동적 Backend origin 검증, API Rewrite와 SPA fallback을 저장소 설정에 반영했다.
- Kakao Key 없이 Frontend 테스트와 운영 빌드를 통과했으며 Backend origin이 브라우저 번들에 포함되지 않음을 확인했다.
- 실제 Vercel 프로젝트 생성과 Render 연결은 배포 M3에서 진행한다.
