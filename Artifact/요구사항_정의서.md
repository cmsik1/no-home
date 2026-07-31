# NoHome 요구사항 명세서 (SRS)

> 프로젝트: **NoHome** — 공공데이터 기반 아파트 실거래가 검색 서비스 (SSAFY Home 리뉴얼)
> 문서 버전: 1.0 / 작성일: 2026-06-24
> 근거: `docs/PRD.md`, `docs/spec.md`, `no-home-backend/docs`, 3개 repo git 이력
> 담당: 이정헌(AI 어시스턴트) · 최민식(공공데이터·검색) · 전효준(회원·인증·지도)

---

## 1. 문서 개요

### 1.1 목적
국토교통부 공공데이터(아파트 매매·전월세 실거래가)를 DB에 적재하고, 일반 사용자가 API 지식 없이 지역·거래유형·가격 조건으로 실거래가를 검색하며 지도에서 위치를 확인하고, 자연어 AI 어시스턴트로 검색·화면조작까지 할 수 있는 서비스의 요구사항을 정의한다.

### 1.2 범위
- **필수**: 실거래가 수집·검색, 회원 CRUD, 로그인/로그아웃, 검색결과 지도 표시
- **추가**: AI 어시스턴트(질문/에이전트), 전월세 검색, 관심지역, 공지사항, 회원검색 관리자 권한, 비밀번호 찾기, 단기 대화기억
- **제외(현 범위 밖)**: 서울 외 지역, 주변 상권/환경 지도, 게시판, 뉴스 크롤링

### 1.3 액터 (Use Case 기준)
| 액터 | 설명 |
| --- | --- |
| 비회원 | 가입 전 사용자. 실거래가 검색·지도·공지 조회 가능 |
| 회원 | 로그인 사용자. 비회원 기능 + 내 계정·관심지역·AI 검색 |
| 관리자 | 회원 중 운영 권한자(`notice.admin-emails`). 공지·회원·공공데이터 운영 |
| 외부 시스템 | 공공데이터 API · Kakao Map API · AI Chat(GMS) — usecase 보조 |

### 1.4 용어 정의
| 용어 | 의미 |
| --- | --- |
| LAWD_CD | 법정동코드 10자리 중 앞 5자리(지역 요청 코드) |
| DEAL_YMD | 계약년월 6자리(yyyyMM) |
| dealMode | 거래유형: sale(매매)/jeonse(전세)/monthly(월세)/rent(전월세)/all(전체) |
| api_row_hash | 공공데이터 원본 row 중복 판정용 해시 |
| coverage | 특정 지역·월의 DB 적재 완료 여부 |
| mojibake | 인코딩 깨짐(한글 글자 깨짐) |
| returnDirect | LLM 후처리 없이 도구 결과를 직접 반환하는 Tool Calling 속성 |

### 1.5 참조 문서
- 제품 요구: `docs/PRD.md` / 기술 스펙: `docs/spec.md` / 로드맵: `docs/plan.md`
- 다이어그램: [유스케이스](Use-Case_다이어그램.md) · [클래스](클래스_다이어그램.md) · [ERD](ER_다이어그램.md)
- 공공데이터: `아파트 매매 실거래가 자료 기술문서.pdf`, `아파트 전월세 실거래가 자료 기술문서.pdf`

---

## 2. 시스템 개요

### 2.1 구성 (3-tier)
```
Vue 3 SPA  ──(Vite proxy /api)──▶  Spring Boot REST API  ──MyBatis──▶  MySQL
                                          │
                                          │  [외부 연동]
                                          ├──▶ 국토부 공공데이터 API (매매/전월세)
                                          ├──▶ SSAFY GMS (OpenAI 프록시, LLM)
                                          └──▶ Kakao Map (지도/geocoding)
```

<details>
<summary>📐 Mermaid 버전 (펼치기)</summary>

```mermaid
flowchart LR
    FE["Vue 3 SPA"] -->|"/api (Vite proxy)"| BE["Spring Boot<br/>REST API"]
    BE -->|MyBatis| DB[("MySQL")]
    BE --> PD["국토부 공공데이터 API<br/>(매매/전월세)"]
    BE --> GMS["SSAFY GMS<br/>(OpenAI 프록시, LLM)"]
    BE --> KK["Kakao Map<br/>(지도/geocoding)"]

    subgraph EXT["외부 연동"]
        PD
        GMS
        KK
    end
```

</details>

> 패키지·클래스 구조는 [클래스 다이어그램](클래스_다이어그램.md)(backend-packages, house-search, member-auth, ai-chatbot, common-infra, publicdata-import, notice-interest) 참조.

### 2.2 기술 스택
| 계층 | 스택 |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5.9, MyBatis 3, MySQL 8 |
| AI | Spring AI 1.1.2 (ChatClient·Tool Calling), gpt-4o-mini |
| Frontend | Vue 3.5(Options API/SPA), Vite 5, Kakao Maps SDK |
| 인증 | 자체 JWT(HS256) + BCrypt, HttpOnly·Secure 쿠키 |
| 인프라 | Docker Compose(backend/frontend/MySQL) |

### 2.3 운영 환경
- 서버 포트: backend 8080, frontend 5173(dev) / DB: MySQL(로컬 docker 3307 또는 3306)
- 비밀값은 환경변수·gitignored 설정으로 주입(공공데이터 키 2종, GMS 키, Kakao 키, JWT secret)

---

## 3. 기능 요구사항

> 우선순위: **M**(Must, 과제 필수) / **S**(Should, 구현된 추가기능) / **C**(Could, 후속)
> 담당: 이(이정헌) · 민(최민식) · 효(전효준) · 공(공통)

### 3.1 필수 기능 (과제 명세)
| ID | 기능명 | 설명 | 입력 → 출력 | 우선 | 담당 | 완료 기준 |
| --- | --- | --- | --- | --- | --- | --- |
| F701 | 실거래가 수집 | 아파트 매매·전월세 실거래가를 공공데이터 API로 수집·DB 적재 | LAWD_CD, DEAL_YMD → 적재 건수 | M | 민 | 매매·전월세 데이터를 DB에 적재, 중복 skip |
| F702 | 실거래가 검색 | 지역·아파트명·거래유형·가격 조건으로 실거래가 목록 조회 | 검색조건 → 페이지 목록 | M | 민 | 동별·아파트명 검색이 조건에 맞는 결과 반환 |
| F712 | 회원 등록 | 이메일·비밀번호·이름·전화로 회원 가입 | 가입정보 → 회원 | M | 효 | 필수정보 입력 시 가입, 중복 이메일 거부 |
| F713 | 회원 조회 | 로그인 사용자가 본인 정보 조회 | (인증) → 내 정보 | M | 효 | 로그인 사용자만 본인 정보 조회 |
| F714 | 회원 수정 | 본인 정보(이름·전화 등) 수정 | 수정정보 → 결과 | M | 효 | 본인만 허용 필드 수정 |
| F715 | 회원 삭제 | 본인 회원 정보 물리 삭제 | (인증) → 결과 | M | 효 | 물리 삭제, 본인만 가능 |
| F716 | 로그인 관리 | 로그인/로그아웃 | 자격증명 → 토큰/세션 | M | 효 | 로그인·로그아웃, 보호 화면 접근 제어 |
| F720 | 주택 정보 관리 | 실거래가 기반 주택 기본정보 관리·조회 | 조건 → 주택정보 | M | 민 | houses/house_deals 연계 조회 |
| F-MAP | 검색결과 지도 표시 | 검색 결과를 Kakao Map 마커로 표시 | 결과목록 → 지도 마커 | M | 효 | 주소 geocoding 후 현재 페이지 마커 표시 |

### 3.2 추가 기능 (구현 완료)
| ID | 기능명 | 설명 | 입력 → 출력 | 우선 | 담당 | 완료 기준 |
| --- | --- | --- | --- | --- | --- | --- |
| F-AI1 | AI 질문 모드 | 자연어 질문에 실거래 데이터 요약 답변 | 발화 → 텍스트 답변 | S | 이 | 데이터 조회 도구로 통계 요약 응답 |
| F-AI2 | AI 에이전트 모드 | 자연어로 필터·검색·페이지·지도 조작 | 발화 → 구조화 명령 실행 | S | 이 | 6액션(검색/필터/페이지/선택/지도/초기화) 실행 |
| F-AI3 | 단일 어시스턴트 통합 | 단일 `/assistant`에서 LLM tool calling 분기 | 발화 → answer/command | S | 이 | 모드토글 없이 질문/조작 자동 분기 |
| F-AI4 | 단기 대화기억 | 세션 휘발 멀티턴 맥락(InMemory window 10) | 발화+세션ID → 맥락 응답 | S | 이 | 멀티턴 맥락 유지, 세션 종료 시 초기화 |
| F-RENT | 전월세 검색 | dealMode(전세/월세/전월세) 검색·가격필터 | 거래유형+조건 → 목록 | S | 민 | 전세=보증금, 월세=보증금+월세 필터 |
| F-LIVE | 라이브 검색 성능 | 미캐시 첫 조회를 응답 우선+백그라운드 적재로 개선 | 검색 → 즉시 응답 | S | 민 | 미캐시 첫 조회 지연 완화, 백그라운드 batch |
| F-INT | 관심 지역 | 회원이 관심 지역 등록·관리 | 지역 → 관심목록 | S | 효 | 회원-지역 연결 저장·조회 |
| F-ADM | 회원검색 관리자권한 | `/members/search`를 관리자만 허용 | 키워드 → (관리자)결과 | S | 효 | 일반회원 403, 관리자만 검색 |
| F-PWD | 비밀번호 찾기 | 비밀번호 재설정 | 식별정보 → 재설정 | S | 효 | 비밀번호 재설정 처리 |
| F711 | 공지사항 | 공지 조회(전체)·관리(관리자 작성/수정/삭제) | 공지 → 목록/상세 | S | 효 | 비회원 조회, 관리자만 CRUD |

### 3.3 검색 상세 (dealMode 별 필터/정렬)
| dealMode | 의미 | 가격 필터 | 가격 정렬 |
| --- | --- | --- | --- |
| sale | 매매 | minPrice, maxPrice | priceDesc/Asc |
| jeonse | 전세(월세 0) | minDeposit, maxDeposit | depositDesc/Asc |
| monthly | 월세(월세>0) | minDeposit·maxDeposit·minMonthlyRent·maxMonthlyRent | deposit·monthlyRent Desc/Asc |
| rent | 전세+월세 | 없음 | 없음 |
| all | 전체 | 없음 | 없음 |

공통 정렬: latest, oldest, areaDesc, areaAsc. 기본 dealMode=sale.

---

## 4. 비기능 요구사항

| 분류 | ID | 요구사항 |
| --- | --- | --- |
| 성능 | NFR-P1 | 검색은 DB 우선 조회로 반복 조회 시 빠르게 응답(캐시 시 1초 내) |
| 성능 | NFR-P2 | 미캐시 첫 조회 지연 완화: 응답 우선 + 백그라운드 batch 적재, API numOfRows 1000, batch upsert |
| 보안 | NFR-S1 | 비밀번호 BCrypt 해싱(평문 미저장) |
| 보안 | NFR-S2 | JWT HS256 + HttpOnly·Secure 쿠키, refresh 토큰 블랙리스트(로그아웃) |
| 보안 | NFR-S3 | 운영환경 fail-closed: JWT secret 누락/취약 시 기동 차단 |
| 보안 | NFR-S4 | 회원검색 관리자 권한 분리(일반회원 403) |
| 개인정보 | NFR-S5 | AI 대화 원문·조회결과 미영속, 로깅은 메타데이터(건수·토큰)만 |
| 데이터 | NFR-D1 | api_row_hash 유니크로 중복 적재 방지, import batch로 멱등 |
| 데이터 | NFR-D2 | 공공데이터 출처 보존, mojibake 보정으로 한글 정합성 유지 |
| 사용성 | NFR-U1 | 사전지식 없이 사용 가능, 자연어 AI로 진입장벽 제거 |
| 호환성 | NFR-U2 | 데스크톱·모바일 브라우저 폭(min 320px) 지원 |
| 가용성 | NFR-A1 | GMS 키 부재 시 AI만 503, 나머지 기능 정상 기동(graceful) |
| 가용성 | NFR-A2 | AI 요청 제한: 분당 10회·입력 500자·동시 1요청 |

---

## 5. 화면 명세

| 화면 | 구성 요소 | 비고 |
| --- | --- | --- |
| 메인 검색(좌) | 거래유형, 지역 3단계(시도→시군구→법정동), 아파트명, 거래월 범위, 가격 이중 슬라이더, 정렬, 표시개수 | 법정동은 `/api/regions` 동적 로드 |
| 검색 결과 목록 | 결과 카드(아파트명·주소·금액·면적·층·거래일), 거래유형 배지, 페이지네이션 | Founder pink 강조 |
| 지도(우) | Kakao Map, 결과 마커, 선택 포커싱, 지도 상태 안내 | 페이지 단위 마커 갱신 |
| 회원 패널 | 로그인/회원가입/내 정보(조회·수정·삭제)/비밀번호 찾기/관심지역 | credentials: include |
| 공지사항 | 공지 목록·상세(전체 조회), 작성·수정·삭제(관리자) | F711 |
| AI 챗봇 위젯(우하단) | FAB+패널, 대화 버블, 진행 표시, 드래그 리사이즈 | 단일 /assistant 호출 |

> 사용자 목표(액터별)는 [유스케이스 다이어그램](Use-Case_다이어그램.md)의 5개 서비스(주택정보·계정·개인화AI·공지운영·외부연동) 참조.

---

## 6. API 명세

| Method | Path | 인증 | 설명 | 담당 |
| --- | --- | --- | --- | --- |
| GET | /api/health | - | 헬스체크(DB probe) | 공 |
| GET | /api/houses/search | - | 실거래가 검색(자동임포트 포함) | 민 |
| GET | /api/houses/price-range | - | 조건별 가격 범위 | 민 |
| GET | /api/regions | - | 법정동 목록(catalog 머지) | 민 |
| POST | /api/public-data/apt-trades/import | O | 공공데이터 수동 임포트 | 민 |
| POST | /api/members | - | 회원가입 | 효 |
| POST | /api/auth/login | - | 로그인 | 효 |
| POST | /api/auth/logout | O | 로그아웃 | 효 |
| POST | /api/auth/refresh | - | 토큰 갱신 | 효 |
| POST | /api/auth/password-reset | - | 비밀번호 재설정 | 효 |
| GET | /api/members/me | O | 내 정보 조회 | 효 |
| PUT | /api/members/me | O | 내 정보 수정 | 효 |
| DELETE | /api/members/me | O | 회원 물리 삭제 | 효 |
| GET | /api/members/search | O(관리자) | 회원 검색(관리자 전용) | 효 |
| GET·POST·DELETE | /api/interest-regions | O | 관심지역 목록·등록·삭제(/{id}) | 효 |
| GET·POST·PUT·DELETE | /api/notices | -(조회)/O(관리자) | 공지 목록·상세·작성·수정·삭제 | 효 |
| POST | /api/ai/assistant | O | AI 어시스턴트(질문/조작 분기) | 이 |

> 관심지역·공지 엔드포인트는 코드(`interest/controller/InterestRegionController`, `notice/controller/NoticeController`) 기준 확정.

---

## 7. 데이터 명세 (주요 테이블)

> 기준: `no-home-backend/src/main/resources/schema.sql`. 전체 관계는 [ERD](ER_다이어그램.md) 참조.

| 테이블 | 핵심 컬럼 | 비고 |
| --- | --- | --- |
| regions | region_id(PK), lawd_cd, legal_dong_code, sido, sigungu, umd_nm, lat, lng | 좌표 nullable, uq_regions_lawd_umd |
| houses | house_id(PK), region_id(FK), sgg_cd, umd_nm, jibun, apt_nm, build_year, lat, lng | uq_houses_source_identity |
| house_deals | deal_id(PK), house_id(FK), source_api, lawd_cd, deal_ymd, house_type, deal_type(sale/jeonse/monthly), deal_amount(_manwon), exclu_use_ar, floor, deal_date, api_row_hash, raw_response(JSON) | 전월세: rent_type, deposit(_manwon), monthly_rent(_manwon), contract_term, contract_type, use_rr_right |
| members | member_id(PK), email, password_hash, name, phone | BCrypt, uq_members_email |
| member_refresh_tokens | member_id(PK,FK), token_hash, expires_at | 로그아웃 블랙리스트 |
| notices | notice_id(PK), member_id(FK), title, content, created_at, updated_at | 관리자 작성 공지 |
| interest_regions | interest_region_id(PK), member_id(FK), region_id(FK), created_at | uq_interest_regions_member_region(중복 등록 방지) |
| public_data_import_batches | import_batch_id(PK), source_api, lawd_cd, deal_ymd, house_type, deal_type, status, total/imported/skipped_count, error_message | 멱등 적재 이력, uq_import_batches_request |

관계: `regions → houses → house_deals` / `members → (member_refresh_tokens·notices·interest_regions)` / `regions → interest_regions`.
주요 인덱스: `uq_house_deals_api_row_hash`, `idx_house_deals_lawd_ymd`, `idx_house_deals_deal_mode(deal_type, lawd_cd, deal_ymd)`.

---

## 8. 외부 인터페이스

| 연동 | 용도 | 인증 | 비고 |
| --- | --- | --- | --- |
| 국토부 아파트 매매 실거래가 API | 매매 수집 | serviceKey | RTMSDataSvcAptTrade, XML, resultCode 성공 {"00","000"}, 03=빈결과 |
| 국토부 아파트 전월세 실거래가 API | 전월세 수집 | serviceKey | RTMSDataSvcAptRent, 월세 0=전세/초과=월세 |
| SSAFY GMS (OpenAI 프록시) | LLM 호출 | API key | base-url gms.ssafy.io, gpt-4o-mini |
| Kakao Map | 지도·geocoding | JS key | Web 플랫폼 도메인 등록 필요 |

---

## 부록 A. 요구사항 추적 (담당·근거)
- 이정헌(AI): F-AI1~4 — PR #2,6,16,18,28,29 / 보안 NFR-S3·가용성 — PR #5,7 / 공공데이터 resultCode — PR #20
- 최민식(공공데이터·검색): F701/702/720, F-RENT, F-LIVE — PR #13,23,27,30 / 인프라·폴더구조
- 전효준(회원·지도): F712~716, F-MAP, F-INT, F-ADM, F-PWD, F711 — PR #1,22,26,33

> 본 명세서의 일정·담당 근거는 `wbs-gantt.md`의 git 실측 데이터와 일치한다.
> 데이터·기능·액터 정의는 `docs/diagrams/`(ERD·클래스·유스케이스)와 정합한다.
