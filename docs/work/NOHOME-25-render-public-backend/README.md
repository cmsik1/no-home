# NOHOME-25 Render Backend 공개 배포

## 완료 결과

- Render 무료 Docker Web Service를 Neon과 같은 Ohio 리전에 배포했다.
- `prod` profile, 동적 `PORT`, health check와 API `Cache-Control: no-store`를 공개 HTTPS 요청으로 확인했다.
- 운영 기본값에서 비밀번호 재설정, 회원 검색, 공공데이터 실시간 조회·수동 import가 차단되도록 설정했다.

Render URL, DB URL, JWT 및 환경 변수 값은 문서에 기록하지 않는다.
