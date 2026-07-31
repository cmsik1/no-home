# NoHome Technical Spec

## Repository Layout

As of 2026-06-23, local development uses ordinary folders:

```text
no-home/
  no-home-backend/
  no-home-frontend/
  no-home-artifact/
```

Run the backend on `http://localhost:8080` and the frontend on `http://localhost:5173`. The Vite dev server proxies `/api` requests to the backend.

## Technical Baseline

- Backend: Spring Boot
- Persistence: MyBatis
- Database: MySQL
- Frontend: Vue.js + Vite
- Server: Spring Boot embedded Tomcat
- Auth: session-based login
- Integration: public-data XML APIs imported into MySQL before search

## Public Data Scope

NoHome currently supports Seoul apartment real-estate deal search using two Ministry of Land, Infrastructure and Transport public-data APIs.

### Apartment Trade API

```text
https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade
```

Environment key:

```text
PUBLIC_DATA_SERVICE_KEY
```

Stored deal type:

```text
sale
```

### Apartment Rent API

```text
https://apis.data.go.kr/1613000/RTMSDataSvcAptRent/getRTMSDataSvcAptRent
```

Environment key:

```text
PUBLIC_DATA_APT_RENT_SERVICE_KEY
```

Stored import batch deal type:

```text
rent
```

Rows imported from the rent API are exposed as:

```text
monthlyRent == 0  -> dealType=jeonse
monthlyRent > 0   -> dealType=monthly
```

Common request parameters:

- `serviceKey`
- `LAWD_CD`
- `DEAL_YMD`
- `pageNo`
- `numOfRows`

`LAWD_CD` is the first five digits of the legal-dong code. `DEAL_YMD` is a six-digit contract year/month (`yyyyMM`).

## Data Model

### `regions`

- `region_id`: primary key
- `lawd_cd`: five-digit request code
- `legal_dong_code`: ten-digit legal-dong code, nullable
- `sido`
- `sigungu`
- `umd_nm`
- `lat`, `lng`: nullable

### `houses`

- `house_id`: primary key
- `region_id`: foreign key
- `sgg_cd`
- `umd_nm`
- `jibun`
- `apt_nm`
- `build_year`
- `lat`, `lng`: nullable

### `house_deals`

Common columns:

- `deal_id`: primary key
- `house_id`: foreign key
- `source_api`
- `lawd_cd`
- `deal_ymd`
- `house_type`: currently `apartment`
- `deal_type`: `sale`, `jeonse`, `monthly`
- `deal_year`, `deal_month`, `deal_day`
- `deal_date`
- `deal_amount`
- `deal_amount_manwon`
- `exclu_use_ar`
- `floor`
- `api_row_hash`
- `raw_response`

Trade-specific nullable columns:

- `apt_dong`
- `buyer_gbn`
- `sler_gbn`
- `dealing_gbn`
- `estate_agent_sgg_nm`
- `cdeal_type`
- `cdeal_day`
- `rgst_date`
- `land_leasehold_gbn`

Rent-specific nullable columns:

- `rent_type`
- `deposit`
- `deposit_manwon`
- `monthly_rent`
- `monthly_rent_manwon`
- `contract_term`
- `contract_type`
- `use_rr_right`
- `pre_deposit`
- `pre_deposit_manwon`
- `pre_monthly_rent`
- `pre_monthly_rent_manwon`
- `roadnm`
- `apt_seq`

Indexes:

- `uq_house_deals_api_row_hash`
- `idx_house_deals_lawd_ymd`
- `idx_house_deals_house_date`
- `idx_house_deals_deal_mode (deal_type, lawd_cd, deal_ymd)`

### `public_data_import_batches`

- `source_api`
- `lawd_cd`
- `deal_ymd`
- `house_type`
- `deal_type`
- `status`
- `total_count`
- `imported_count`
- `skipped_count`
- `error_message`
- `requested_at`
- `completed_at`

Unique request key:

```text
source_api, lawd_cd, deal_ymd, house_type, deal_type
```

For rent API imports, `deal_type=rent` is recorded once per `LAWD_CD` and `DEAL_YMD`.

## Search API

### `GET /api/houses/search`

Supported filters:

- `lawdCd`
- `sido`
- `sigungu`
- `umdNm`
- `aptName`
- `dealYmd`
- `startDealYmd`
- `endDealYmd`
- `dealMode`
- `sort`
- `minPrice`
- `maxPrice`
- `minDeposit`
- `maxDeposit`
- `minMonthlyRent`
- `maxMonthlyRent`
- `page`
- `size`
- `autoImport`

`dealMode` values:

| Mode | Meaning | Allowed price filters | Allowed price sorts |
| --- | --- | --- | --- |
| `sale` | trade deals only | `minPrice`, `maxPrice` | `priceDesc`, `priceAsc` |
| `jeonse` | rent API rows with monthly rent 0 | `minDeposit`, `maxDeposit` | `depositDesc`, `depositAsc` |
| `monthly` | rent API rows with monthly rent greater than 0 | `minDeposit`, `maxDeposit`, `minMonthlyRent`, `maxMonthlyRent` | `depositDesc`, `depositAsc`, `monthlyRentDesc`, `monthlyRentAsc` |
| `rent` | jeonse + monthly | none | none |
| `all` | sale + jeonse + monthly | none | none |

All modes support date and exclusive-area sorting:

- `latest`
- `oldest`
- `areaDesc`
- `areaAsc`

Default `dealMode` is `sale` for backward compatibility.

### `GET /api/houses/price-range`

Returns available price bounds for the current search condition. The frontend uses this endpoint to initialize:

- sale price range
- jeonse deposit range
- monthly deposit range
- monthly rent range

`rent` and `all` modes do not expose price filtering in the browser.

## Auto Import Policy

Search uses DB data first. Missing coverage is filled by public-data import when the request has an interpretable Seoul district and an explicit month condition.

Supported month conditions:

- Single month: `dealYmd`
- Inclusive month range: `startDealYmd` through `endDealYmd`

Mode-specific import:

- `sale`: trade API
- `jeonse`: rent API
- `monthly`: rent API
- `rent`: rent API
- `all`: trade API + rent API

Public-data response code `03` means "no data" and is treated as a successful empty response. Key errors, quota errors, timeouts, and provider errors are classified separately.

## Browser Policy

- Search mode options: 매매, 전세, 월세, 전월세, 전체
- `jeonse` exposes deposit filter and deposit sort.
- `monthly` exposes deposit filter, monthly-rent filter, deposit sort, and monthly-rent sort.
- `rent` and `all` disable price filters and price sorts.
- `서울특별시` requires a specific `시군구`; the browser does not offer Seoul-wide auto import.
- AI/agent search is out of scope for the apartment rent expansion.

## Validation

Required checks:

- Backend tests: `.\mvnw.cmd test`
- Frontend tests: `npm.cmd test`
- Frontend build: `npm.cmd run build`
- Manual browser checks for `sale`, `jeonse`, `monthly`, `rent`, and `all`
- Docker rebuild when validating containerized behavior
