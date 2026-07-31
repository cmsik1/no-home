# Sprint 11: Founder Pink Search UI and Legal-Dong Select

## Manager Contract

- Goal: Improve the frontend search experience after the folder split.
- Scope:
  - Restyle the left-side house deal result cards so the 10-item page is compact, readable, and not visually broken.
  - Use Founder pink `#FBEAEB` as a soft accent for selected/hover/result status UI only.
  - Remove the floating map information card from the Kakao map.
  - Move map status into the left result area so it does not cover the map.
  - Replace direct `umdNm` text input with a DB-backed legal-dong select.
  - Populate legal-dong options from `GET /api/regions?lawdCd={code}` after a Seoul district is selected.
- Out of scope:
  - Backend API contract changes.
  - Administrative-dong data or legal/administrative dong mapping.
  - Adding dong names that do not exist in the DB/API response.
  - A broad frontend redesign outside the search/result/map surface.
- Completion criteria:
  - `Frontend/package.json` tests still pass.
  - `Frontend` production build passes.
  - The dong field is a select with a default "전체 동" option and DB-backed options.
  - The map no longer has a floating information card over it.
  - Result cards use `result-card` / `item-meta-grid` CSS and render cleanly.

## Generator Instructions

- Keep implementation scoped to `Frontend/src/App.vue`, `Frontend/src/style.css`, and focused frontend tests if needed.
- Reuse existing `/api/regions` and existing `seoulLawdCodes`; do not add new backend routes.
- Use existing `fieldText` repair behavior for displayed region names.

## Generator Result

- Changed the `umdNm` field from text input to a legal-dong select populated from `/api/regions?lawdCd={code}`.
- Added duplicate/encoding-safe legal-dong option normalization so the select shows readable DB-backed dong labels.
- Restyled result cards with matching `result-card` and `item-meta-grid` CSS.
- Applied Founder pink `#FBEAEB` to result count, map status, selected card, and hover accents.
- Removed the map floating information card and moved map status into the left result panel.
- Verification:
  - `Frontend`: `npm run test:auto-import` passed, 3 tests.
  - `Frontend`: `npm run build` passed.
  - `Backend`: `.\mvnw.cmd test` passed, 42 tests.
  - `http://127.0.0.1:5173` returned HTTP 200.
  - `http://127.0.0.1:5173/api/regions?lawdCd=11590` returned legal-dong data through the Vite proxy.
  - Chrome headless screenshot saved to `target/sprint11-frontend.png`; map overlay card is no longer present.

## Reviewer Checklist

- Confirm no `.map-copy` overlay remains in the map panel.
- Confirm selected/hover card UI uses `#FBEAEB`.
- Confirm dong options are loaded from `/api/regions`.
- Confirm only existing DB legal-dong values can be selected.
