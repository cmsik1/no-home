# NOHOME-26 Vercel Frontend 연결 및 공개 검증

## 완료 결과

- Vercel 프로젝트의 Root Directory를 `Frontend`로 지정했다.
- 서버 측 `BACKEND_ORIGIN` 하나만 설정해 `/api/**` Rewrite를 Render HTTPS origin에 연결했다.
- 자동 감지된 개발용 DB·JWT·외부 API 환경 변수는 모두 제거했다.
- 기본 HTTPS 도메인에서 SPA 화면, `/api/health` Rewrite와 지도 API 키 미설정 안내 상태를 확인했다.

Vercel 기본 URL과 환경 변수 값은 문서에 기록하지 않는다.
