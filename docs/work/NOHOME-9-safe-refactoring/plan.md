# NOHOME-9 세부 계획

## 적용 범위

- `Frontend/src/services/houseService.js`의 중복 HTTP 처리
- 해당 동작을 고정하는 service 단위 테스트
- 리팩터링 후보와 보류 사유 문서화

## 제외 범위

- 공개 API, DB schema와 사용자 기능 변경
- Backend 트랜잭션 경계 변경
- Vite·Vitest 메이저 버전 갱신
- 테스트 보호가 부족한 대형 hook 분해

## 완료 기준

- [x] 후보의 영향도와 구현·보류 판단을 기록한다.
- [x] 선택한 중복 로직을 단일 내부 함수로 통합한다.
- [x] endpoint·query·응답·오류 계약 테스트를 추가한다.
- [x] Backend 175개, Frontend 58개 테스트와 양쪽 build가 통과한다.
- [x] API와 schema 변경이 없음을 확인한다.

## 검증 명령

- `mvn test`
- `mvn -DskipTests package`
- `npm test`
- `npm run build`
- `npm audit --json`
- `powershell -File scripts/check-markdown-links.ps1`
- `git diff --check`
