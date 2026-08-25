# NOHOME-27 M3 공개 환경 통합 검증

## 완료 결과

- Vercel 기본 HTTPS 도메인 경유 `/api/health`는 `200`, `Cache-Control: no-store`, DB `UP` JSON을 반환했다.
- 빈 운영 DB 검색은 `200`과 빈 `items`, `totalCount: 0`을 반환했고 자동 적재를 시도하지 않았다.
- 수동 공공데이터 적재와 비밀번호 재설정은 `503 FEATURE_DISABLED`를 반환했다.
- 테스트 전용 `example.invalid` 계정으로 회원가입(`201`), Secure·HttpOnly 쿠키 로그인(`200`), 현재 사용자 조회(`200`), 로그아웃(`200`), 로그아웃 뒤 보호 API의 `401`을 확인했다.
- 로그인 후 회원 검색도 `503 FEATURE_DISABLED`를 반환했다.

## M4 잔여 항목

- 실제 공공데이터 API 키 입력 및 실거래 데이터 적재
- 커스텀 도메인, 모니터링·알림, 백업·복구 및 콜드 스타트 대응
- Neon PostgreSQL 신규 주 버전에 대한 드라이버/Flyway 호환성 추적
- 운영 테스트 계정의 보존 또는 정리 정책 결정

실제 서비스 URL, DB 접속 정보, JWT, 쿠키 값과 테스트 계정 비밀번호는 기록하지 않는다.
