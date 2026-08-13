# NOHOME-12 환경변수 계약 및 운영 프로필 정리

NoHome의 로컬·테스트·운영 환경에서 사용하는 설정값을 분류하고, 운영에 반드시 필요한 DB·JWT 값과 없어도 기동 가능한 외부 API Key를 명확히 구분한다.

이 작업은 실제 클라우드 서비스나 외부 API에 연결하지 않는다. 예시 환경변수, Spring profile과 운영 설정 검증을 정리해 이후 Render·Neon·Vercel 설정이 동일한 계약을 사용하도록 만드는 것이 목적이다.

## 기대 동작

- 로컬 개발은 루트 `.env`와 Docker Compose 흐름을 그대로 사용한다.
- 테스트는 실제 외부 API Key 없이 독립적으로 실행된다.
- 운영 profile은 DB와 JWT 필수값이 빠지거나 안전하지 않으면 기동을 거부한다.
- 선택적 API Key가 없으면 해당 기능만 사용할 수 없고 기본 빌드와 기동은 가능하다.

## 완료 결과

- 운영 profile은 DB 접속 정보, 강한 JWT secret과 Secure cookie 설정을 기동 전에 검증한다.
- 테스트는 개인 `.env` 대신 격리된 DB와 테스트 전용 JWT 설정을 사용한다.
- 환경변수 예시와 Backend·Frontend 문서에서 필수값, 선택값과 브라우저 공개값을 구분한다.
- 실제 JAR의 fail-closed 동작과 Docker Testcontainers를 포함한 전체 테스트를 확인했다.
