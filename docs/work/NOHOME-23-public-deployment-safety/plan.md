# NOHOME-23 구현 계획

## 구현 계약

- 로컬·테스트에서는 네 기능을 기본 활성화한다.
- `prod` 프로필에서는 비밀번호 재설정, 회원 검색, 공공데이터 실시간 조회와 수동 import를 기본 비활성화한다.
- 차단된 API는 공통 `FEATURE_DISABLED` 메시지와 HTTP 503을 반환한다.
- 운영 주택 검색은 외부 API를 호출하지 않고 DB 결과를 정상 반환한다.
- 관리자 이메일은 명시적으로 설정한 값만 인정한다.
- Neon JDBC URL은 `sslmode=require&channelBinding=require`를 요구한다.

## 자동 검증

- Backend 정책·Controller·검색 회귀 테스트와 전체 Maven 검증
- Frontend 위험 UI 비노출 테스트, 전체 테스트·보안 검사·운영 빌드
- 배포 사전 검사, Markdown 링크 검사, `git diff --check`
- 필요 시 Docker Compose smoke test

## 완료 조건

- 승인된 인터페이스와 운영 기본값이 자동 검사로 확인된다.
- 비밀값·실제 클라우드 주소·DB schema 변경이 없다.
- 사용자 인수 승인 전에는 Jira 완료·커밋·병합하지 않는다.

## 검증 결과

- Backend: `./mvnw --batch-mode --no-transfer-progress verify` — 192개 통과
- Frontend: `npm run audit:security && npm test && npm run build` — 취약점 0건, 64개 통과, 빌드 성공
- Docker: Backend·Frontend 이미지 빌드 성공
- 배포 사전 검사: Compose·Render·Vercel·비밀값·문서 링크 검사 통과
- PostgreSQL JDBC: 42.7.12 해석 확인
- `git diff --check` 통과, DB migration 변경 없음

## 잔여 범위

- 실제 Neon 운영 DB 생성은 NOHOME-24에서 진행한다.
- GitHub CI와 CodeQL은 main 커밋 `ea8df16`에서 모두 성공했다.
