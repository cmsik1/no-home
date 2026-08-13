# NOHOME-12 구현 계획

## 목표

필수 운영 변수와 선택적 외부 API Key를 구분하고 로컬·테스트·운영 설정의 계약을 코드, 예시 파일과 테스트에서 동일하게 만든다.

## 범위

- Spring 공통 설정과 `prod` profile의 필수값 정책 정리
- DB 접속 정보와 JWT 보안 설정의 운영 fail-closed 검증
- 외부 API Key가 비어 있어도 가능한 빌드·기동 정책 유지
- 루트와 Frontend 환경변수 예시 및 실행 문서 정리
- 설정 검증 단위 테스트와 기존 전체 테스트 실행

## 제외 범위

- Render Blueprint, JVM·Hikari 튜닝과 health HTTP 상태 변경
- Neon·Vercel 실연결과 API Rewrite
- CI/CD, 실제 API 호출과 클라우드 블랙박스 테스트

## 구현 순서

1. 현재 환경변수를 운영 필수값, 로컬 인프라값, 선택적 기능값과 Frontend 공개값으로 분류한다.
2. `prod` profile에서 DB URL·사용자·비밀번호와 JWT secret·Secure cookie를 검증한다.
3. 오류 메시지는 누락된 변수명만 알리고 설정값은 노출하지 않게 한다.
4. 외부 API Key의 빈 값 허용과 기존 기능별 지연 실패 정책을 보존한다.
5. `.env.example`, Backend·Frontend README를 동일한 계약으로 갱신한다.
6. 집중 테스트와 Backend·Frontend 전체 검증을 실행한다.

## 영향받는 계약

- 운영 필수 환경변수: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_COOKIE_SECURE=true`
- 선택적 Backend 기능값: 공공데이터, Kakao REST, SSAFY GMS 관련 Key
- Frontend 빌드 공개값: `VITE_KAKAO_MAP_API_KEY`
- 로컬 인프라값: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`

공개 HTTP API와 DB schema는 변경하지 않는다.

## 완료 기준

- 운영 필수값이 누락되거나 JWT 설정이 약하면 명확한 오류로 기동을 거부한다.
- 오류에 비밀번호, URL credential이나 JWT 원문이 출력되지 않는다.
- 선택적 외부 API Key가 없어도 Backend·Frontend 빌드와 기본 테스트가 가능하다.
- 예시 파일과 README가 변수의 필수 여부와 입력 위치를 일치되게 설명한다.
- 기존 Docker Compose 로컬 실행 계약이 유지된다.
- 사용자 소유 `.gitignore` 변경을 수정하지 않는다.

## 자동 검증

- 운영 설정 validator 단위 테스트
- `Backend\\mvnw.cmd test`
- `Backend\\mvnw.cmd package`
- `npm test` 및 `npm run build`
- 비밀값 패턴과 Git diff 공백 오류 검사

## 사용자 확인

- `.env.example`만 보고 로컬용 값과 운영 필수값을 구분할 수 있는지 확인
- 외부 API Key를 비워 둔 상태가 허용된다는 설명 확인
- 실제 개인 `.env`와 `.gitignore`가 변경되지 않았는지 확인

## 완료 기록

- 운영 필수값 누락 시 실제 실행 JAR이 설정값을 노출하지 않고 기동을 거부함을 확인했다.
- Backend 전체 테스트 179개, Backend 패키징, Frontend 테스트 58개와 운영 빌드가 통과했다.
- Markdown 로컬 링크 47개, 비밀값 패턴 검사와 `git diff --check`가 통과했다.
- 사용자 소유 `.gitignore` 변경은 수정하지 않았고 후속 작업은 NOHOME-13이다.
