# NOHOME-9 리팩터링 후보 검토 및 안전한 개선

## 목적

M3 전수 검토에서 발견한 구조적 문제를 영향도와 검증 가능성으로 평가하고, 공개 계약을 바꾸지 않는 작은 개선만 적용한다.

## 적용 결과

- 주택 검색과 가격 범위 요청에 중복된 query 직렬화·응답 해석을 공통 함수로 추출했다.
- Node가 직접 service 계약 테스트를 실행할 수 있도록 관련 ESM import 확장자를 명시했다.
- 검색 endpoint, query 생략 규칙, ApiResponse 해석과 서버 오류 보존을 검증하는 테스트 2개를 추가했다.

## 보류한 후보

- Backend 대형 서비스 분리: 트랜잭션·공공데이터 적재 정책의 회귀 위험이 있어 별도 설계·테스트 작업이 필요하다.
- Vite·Vitest 메이저 업그레이드: npm audit 취약점 해소에 필요하지만 빌드·테스트 도구 호환성 검증 범위가 커 별도 작업으로 분리한다.
- 대형 React hook 분리: hook 간 요청 취소와 지도 수명주기가 결합돼 있어 컴포넌트 수준 회귀 테스트를 먼저 확충해야 한다.

## 검증

- Frontend Node 단위 테스트: 54개 통과
- Frontend Vitest 컴포넌트 테스트: 4개 통과
- Frontend Vite production build: 성공
- Backend Maven test: 175개 통과
- Backend Maven package: 성공
- Markdown 링크 검사와 `git diff --check`: 통과

공개 API와 DB schema는 변경하지 않았다. npm audit의 기존 개발 도구 취약점 5개는 Vite 8·Vitest 4 수준의 메이저 업그레이드가 필요해 보류했다.
