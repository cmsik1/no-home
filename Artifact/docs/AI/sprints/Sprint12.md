# Sprint12 - Legal Dong Dropdown Stabilization

## Summary

- `/api/regions?lawdCd=...` no longer depends only on imported trade rows.
- The backend now merges existing DB region rows with a Seoul legal-dong catalog for all 25 Seoul districts.
- Mojibake region names are repaired before the API response is returned.
- The frontend keeps an additional mojibake repair guard for region labels.

## Behavior

- A selected Seoul district shows known legal dongs even when the district has not been searched/imported before.
- Invalid administrative-only dong names are not added to the dropdown.
- Search still sends the selected `umdNm` to the existing search API.
- If a valid legal dong has no deal rows for the selected conditions, the normal empty result state is shown.

## Verification

- `Backend`: `.\mvnw.cmd test` passed, 43 tests.
- `Frontend`: `npm run test:auto-import` passed, 3 tests.
- `Frontend`: `npm run build` passed.
