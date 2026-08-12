# NOHOME-7 세부 계획

## 검토 범위

- `Backend/src/main/java`의 운영 Java 파일 전체
- 공통 설정·응답·health, 주택 검색, 공공데이터, 회원·인증, 공지·관심 지역, AI 기능

## 주석 적용 기준

- 계층에서 클래스가 맡는 책임과 다음 계층으로 전달하는 데이터를 설명한다.
- 유스케이스를 조율하거나 데이터를 크게 변환하는 주요 메서드를 설명한다.
- 트랜잭션, 비동기 처리, 보안 검증과 동적 SQL처럼 중요한 선택의 이유를 설명한다.
- 단순 DTO·entity·repository, 자명한 위임 메서드에는 주석을 강제하지 않는다.
- 현재 endpoint와 맞지 않는 기존 주석은 수정한다.

## 완료 기준

- [x] 운영 Java 134개를 분류·검토한다.
- [x] 주요 요청 경로가 class/method 주석만으로 추적 가능하다.
- [x] 공개 API, 메서드 시그니처와 schema 변경이 없다.
- [x] Backend test 175개와 package가 통과한다.

## 검증 명령

- `mvn test` — Docker의 Maven 3.9.9·Temurin 17 환경에서 PostgreSQL Testcontainers와 함께 실행
- `mvn -DskipTests package` — 실행 가능한 Spring Boot JAR 패키징 확인
- `powershell -File scripts/check-markdown-links.ps1`
- `git diff --check`
