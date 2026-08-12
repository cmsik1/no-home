# NoHome

NoHome은 공공데이터 기반 아파트 실거래가를 검색하고, 지도와 AI 도우미를 통해 원하는 주거 정보를 빠르게 확인할 수 있는 주택 검색 서비스입니다.

## 주요 기능

- 아파트 매매, 전세, 월세, 전월세 실거래가 검색
- 시도/시군구/동, 아파트명, 거래월 조건 기반 검색
- 거래 유형별 가격 필터와 정렬
- Kakao Map 기반 위치 확인과 검색 결과 마커 표시
- 회원가입, 로그인, 내 정보 조회 및 수정, 회원 탈퇴
- 공지사항 조회와 관리자 공지 등록/수정/삭제
- 관리자 회원 검색
- AI 도우미를 통한 검색 조건 변경, 결과 요약, 페이지 이동 지원

## 제출 문서

루트에는 최종 제출용 문서를 모아두었습니다.

| 문서 | 내용 |
| --- | --- |
| [요구사항_정의서.md](요구사항_정의서.md) | 서비스 요구사항, 기능 명세, 데이터 기준 |
| [WBS-간트_차트.md](WBS-간트_차트.md) | 작업 일정과 산출물 관리 |
| [Use-Case_다이어그램.md](Use-Case_다이어그램.md) | 사용자와 외부 시스템 기준 유스케이스 |
| [클래스_다이어그램.md](클래스_다이어그램.md) | 백엔드 주요 도메인 클래스 구조 |
| [ER_다이어그램.md](ER_다이어그램.md) | 주요 테이블과 관계 |
| [화면_설계서.md](화면_설계서.md) | 주요 화면 구성과 사용자 흐름 |

보조 자료와 이미지 파일은 `docs/` 아래에 보관되어 있으며, 루트 문서에서 필요한 이미지와 자료를 참조합니다.

## 실행 준비

로컬에서는 다음 저장소들이 같은 상위 폴더에 있어야 합니다.

```text
no-home/
  no-home-backend/
  no-home-frontend/
  no-home-artifact/
```

필요한 도구:

- Docker Desktop
- Git
- Backend `.env`
- Frontend `.env`

Backend 환경 변수 파일을 준비합니다.

```powershell
cd C:\SSAFY\no-home\no-home-backend
Copy-Item .env.example .env
```

Frontend 환경 변수 파일을 준비합니다.

```powershell
cd C:\SSAFY\no-home\no-home-frontend
Copy-Item .env.example .env
```

주요 환경 변수:

```text
PUBLIC_DATA_SERVICE_KEY=
PUBLIC_DATA_APT_RENT_SERVICE_KEY=
KAKAO_MAP_API_KEY=
VITE_KAKAO_MAP_API_KEY=
```

- `PUBLIC_DATA_SERVICE_KEY`: 아파트 매매 실거래가 공공데이터 API 키
- `PUBLIC_DATA_APT_RENT_SERVICE_KEY`: 아파트 전월세 실거래가 공공데이터 API 키
- `KAKAO_MAP_API_KEY`, `VITE_KAKAO_MAP_API_KEY`: Kakao Map 사용을 위한 키

## Docker Compose 실행

`no-home-artifact`에서 Backend, Frontend, MySQL을 함께 실행합니다.

```powershell
cd C:\SSAFY\no-home\no-home-artifact
docker compose up -d --build
```

접속 주소:

```text
Frontend: http://localhost:5173
Backend:  http://localhost:8080
Health:   http://localhost:8080/api/health
```

상태 확인:

```powershell
docker compose ps
```

로그 확인:

```powershell
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
```

종료:

```powershell
docker compose down
```

데이터베이스 볼륨까지 삭제해야 할 때만 다음 명령을 사용합니다.

```powershell
docker compose down -v
```

## 사용 방법

1. Frontend 주소로 접속합니다.
2. 지역, 아파트명, 거래월, 거래 유형을 선택해 실거래가를 검색합니다.
3. 검색 결과 목록과 지도 마커에서 아파트 위치와 거래 정보를 확인합니다.
4. 가격 조건이나 정렬을 바꿔 원하는 결과를 좁힙니다.
5. 회원가입 또는 로그인 후 내 정보 관리, 공지사항 확인, AI 도우미 기능을 사용할 수 있습니다.
6. 관리자 계정은 공지사항 관리와 회원 검색 기능을 사용할 수 있습니다.

## 코드 변경 반영

코드를 수정한 뒤 컨테이너에 반영하려면 이미지를 다시 빌드합니다.

```powershell
docker compose up -d --build --force-recreate
```

Frontend만 다시 빌드:

```powershell
docker compose up -d --build --force-recreate frontend
```

Backend만 다시 빌드:

```powershell
docker compose up -d --build --force-recreate backend
```
