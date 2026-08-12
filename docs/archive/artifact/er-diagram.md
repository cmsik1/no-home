# NoHome ERD

현재 프로젝트 기준의 ERD를 Mermaid Markdown으로 정리한다.
한 그림에 모든 테이블을 담으면 렌더링 크기가 작아지므로, 기능 영역별로 분리했다.

## 기준 소스

- DB: `no-home-backend/src/main/resources/schema.sql`

## 1. 실거래가 ERD

![house erd](docs/diagrams/assets/house-erd.svg)

<details>
<summary>Mermaid source</summary>

```mermaid
erDiagram
  REGIONS ||--o{ HOUSES : contains
  HOUSES ||--o{ HOUSE_DEALS : has

  REGIONS {
    BIGINT region_id PK
    VARCHAR lawd_cd "idx_regions_lawd_cd, uq_regions_lawd_umd"
    VARCHAR legal_dong_code
    VARCHAR sido
    VARCHAR sigungu
    VARCHAR umd_nm "uq_regions_lawd_umd"
    DECIMAL lat
    DECIMAL lng
    DATETIME created_at
    DATETIME updated_at
  }

  HOUSES {
    BIGINT house_id PK
    BIGINT region_id FK "idx_houses_region_id"
    VARCHAR sgg_cd "uq_houses_source_identity"
    VARCHAR umd_nm "uq_houses_source_identity"
    VARCHAR jibun "uq_houses_source_identity"
    VARCHAR apt_nm "idx_houses_apt_nm, uq_houses_source_identity"
    INT build_year "uq_houses_source_identity"
    DECIMAL lat
    DECIMAL lng
    DATETIME created_at
    DATETIME updated_at
  }

  HOUSE_DEALS {
    BIGINT deal_id PK
    BIGINT house_id FK "idx_house_deals_house_date"
    VARCHAR source_api
    VARCHAR lawd_cd "idx_house_deals_lawd_ymd"
    CHAR deal_ymd "idx_house_deals_lawd_ymd"
    VARCHAR house_type
    VARCHAR deal_type "idx_house_deals_deal_mode"
    INT deal_year
    INT deal_month
    INT deal_day
    DATE deal_date "idx_house_deals_house_date"
    VARCHAR deal_amount
    INT deal_amount_manwon
    VARCHAR rent_type
    VARCHAR deposit
    INT deposit_manwon
    VARCHAR monthly_rent
    INT monthly_rent_manwon
    DECIMAL exclu_use_ar
    INT floor
    VARCHAR contract_term
    VARCHAR contract_type
    VARCHAR use_rr_right
    CHAR api_row_hash "uq_house_deals_api_row_hash"
    JSON raw_response
    DATETIME created_at
    DATETIME updated_at
  }
```

</details>

읽는 법:

- `regions -> houses -> house_deals`가 실거래가 검색의 핵심 관계다.
- `house_deals`는 매매와 전월세 컬럼을 함께 가진다.

생략 기준:

- `house_deals`의 공공데이터 원문 보조 컬럼 일부는 확대도를 위해 생략했다.

## 2. 회원 기능 ERD

![member erd](docs/diagrams/assets/member-erd.svg)

<details>
<summary>Mermaid source</summary>

```mermaid
erDiagram
  MEMBERS ||--o| MEMBER_REFRESH_TOKENS : owns
  MEMBERS ||--o{ NOTICES : writes
  MEMBERS ||--o{ INTEREST_REGIONS : bookmarks
  REGIONS ||--o{ INTEREST_REGIONS : referenced_by

  MEMBERS {
    BIGINT member_id PK
    VARCHAR email "uq_members_email"
    VARCHAR password_hash
    VARCHAR name
    VARCHAR phone
    DATETIME created_at
    DATETIME updated_at
  }

  MEMBER_REFRESH_TOKENS {
    BIGINT member_id PK,FK
    CHAR token_hash "uq_member_refresh_tokens_hash"
    DATETIME expires_at
    DATETIME created_at
    DATETIME updated_at
  }

  NOTICES {
    BIGINT notice_id PK
    BIGINT member_id FK "idx_notices_member_id"
    VARCHAR title
    TEXT content
    DATETIME created_at "idx_notices_created_at"
    DATETIME updated_at
  }

  INTEREST_REGIONS {
    BIGINT interest_region_id PK
    BIGINT member_id FK "idx_interest_regions_member_id, uq_interest_regions_member_region"
    BIGINT region_id FK "idx_interest_regions_region_id, uq_interest_regions_member_region"
    DATETIME created_at
  }

  REGIONS {
    BIGINT region_id PK
    VARCHAR lawd_cd
    VARCHAR sigungu
    VARCHAR umd_nm
  }
```

</details>

읽는 법:

- 회원은 refresh token, 공지, 관심지역의 기준 엔티티다.
- `interest_regions`는 회원과 지역의 중복 등록을 `uq_interest_regions_member_region`으로 막는다.

생략 기준:

- 관심지역 설명에 필요한 `REGIONS` 컬럼만 축약 표시했다.

## 3. 공공데이터 적재 추적 ERD

![publicdata import erd](docs/diagrams/assets/publicdata-import-erd.svg)

<details>
<summary>Mermaid source</summary>

```mermaid
erDiagram
  PUBLIC_DATA_IMPORT_BATCHES {
    BIGINT import_batch_id PK
    VARCHAR source_api "uq_import_batches_request"
    VARCHAR lawd_cd "idx_import_batches_lawd_ymd, uq_import_batches_request"
    CHAR deal_ymd "idx_import_batches_lawd_ymd, uq_import_batches_request"
    VARCHAR house_type "uq_import_batches_request"
    VARCHAR deal_type "uq_import_batches_request"
    VARCHAR status
    INT total_count
    INT imported_count
    INT skipped_count
    TEXT error_message
    DATETIME requested_at
    DATETIME completed_at
  }
```

</details>

읽는 법:

- 이 테이블은 직접 FK가 없다.
- `source_api`, `lawd_cd`, `deal_ymd`, `house_type`, `deal_type` 조합으로 공공데이터 적재와 캐시 상태를 추적한다.

생략 기준:

- 이 그림은 독립 추적 테이블만 보여주므로 다른 ERD와 관계선을 연결하지 않았다.
