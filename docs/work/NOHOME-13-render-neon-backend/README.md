# NOHOME-13 Render·Neon 백엔드 배포 기반

Spring Boot Backend를 Render의 Docker Web Service로 실행하고 Neon PostgreSQL에 TLS로 연결하기 위한 저장소 설정이다. 실제 클라우드 리소스는 이 작업에서 생성하지 않는다.

## 기대 동작

- Render가 제공하는 `PORT`와 `prod` profile로 Backend가 기동된다.
- 운영 인스턴스는 제한된 JVM heap과 DB connection pool을 사용한다.
- `/api/health`는 DB 정상 시 200, 장애 시 503을 반환한다.
- 모든 `/api/**` 응답은 `Cache-Control: no-store`를 포함한다.
- 실제 DB와 JWT 비밀값은 Render Dashboard에서만 입력한다.

## Render와 Neon 입력 규칙

`render.yaml`로 Blueprint를 생성할 때 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`을 Dashboard에 입력한다. 선택적 외부 API Key는 기본 배포에 필요하지 않으며 실제 기능을 검증할 때 별도로 추가한다.

Neon Console의 host, database, role과 password를 분리해 다음 JDBC 형태로 입력한다.

```text
jdbc:postgresql://<neon-host>/<database>?sslmode=require
```

Backend는 장기 실행되는 단일 Render 인스턴스와 작은 Hikari pool을 사용하므로 Neon의 직접 endpoint를 기본값으로 삼는다. 실제 endpoint와 credential은 문서나 Git에 기록하지 않는다.

## 완료 결과

- Render Blueprint, 운영 자원 제한과 Neon TLS 연결 계약을 저장소에 반영했다.
- 운영 유사 Docker 환경에서 동적 포트, health 200·503과 API `no-store` 동작을 확인했다.
- 실제 Render·Neon 리소스는 생성하지 않았으며 후속 Vercel 작업과 클라우드 연결 작업에서 사용한다.
