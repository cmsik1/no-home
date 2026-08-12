# NOHOME-8 세부 계획

## 검토 범위

- `Frontend/src`의 운영 JavaScript·JSX 파일 전체
- 앱 조율, 검색·지도, 회원·인증, 관심 지역, 공지와 AI 대화 기능

## 주석 적용 기준

- 컴포넌트, hook과 service가 맡는 책임 및 다음 계층으로 전달하는 데이터를 설명한다.
- 여러 상태를 함께 조율하거나 요청·응답을 크게 변환하는 주요 함수를 설명한다.
- query 직렬화, 인증 재시도, 외부 SDK 수명주기처럼 중요한 처리 이유를 설명한다.
- 단순 표시 컴포넌트, 테스트, 상수와 자명한 위임 함수에는 주석을 강제하지 않는다.
- 현재 동작과 맞지 않는 기존 설명은 수정한다.

## 완료 기준

- [x] 운영 JavaScript·JSX 파일 53개를 분류·검토한다.
- [x] 핵심 사용자 요청 경로가 function 주석만으로 추적 가능하다.
- [x] 공개 API 계약과 화면 동작 변경이 없다.
- [x] Frontend test 56개와 build가 통과한다.

## 검증 명령

- `npm test` — Docker의 Node 20 환경에서 단위 테스트와 Vitest 컴포넌트 테스트 실행
- `npm run build` — Vite production bundle 생성 확인
- `powershell -File scripts/check-markdown-links.ps1`
- `git diff --check`
