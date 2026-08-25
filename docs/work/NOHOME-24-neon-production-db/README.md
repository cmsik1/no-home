# NOHOME-24 Neon 운영 DB 생성 및 연결 검증

## 완료 결과

- Neon 무료 프로젝트의 운영 DB와 최소 권한 애플리케이션 역할을 만들고, Render에만 접속 정보를 입력했다.
- JDBC URL은 직접 엔드포인트와 `sslmode=require&channelBinding=require` 계약을 사용했다.
- Render 배포에서 Flyway가 빈 DB에 `V1`을 적용했고, `/api/health`가 DB 연결 `UP`을 반환했다.

접속 URL, DB 사용자명, 비밀번호와 그 밖의 비밀값은 기록하지 않는다.

## 다음 작업

실거래 데이터 적재, 백업과 장애 복구 검증은 M4 범위다.
