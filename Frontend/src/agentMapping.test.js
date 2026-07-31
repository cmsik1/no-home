import test from 'node:test'
import assert from 'node:assert/strict'

import {
  applyAgentFilters,
  buildHouseSearchRequests,
  capabilities,
  currentDealMonth,
  emptyFilters,
  filterSchema,
} from './houseSearchParams.js'

test('currentDealMonth returns the previous month as YYYY-MM', () => {
  assert.equal(currentDealMonth(new Date(2026, 5, 24)), '2026-05') // 6월 -> 직전 5월
  assert.equal(currentDealMonth(new Date(2026, 0, 15)), '2025-12') // 1월 -> 전년 12월
})

test('capabilities() returns exactly the declared filter keys', () => {
  assert.deepEqual(capabilities(), [
    'sido',
    'sigungu',
    'umdNm',
    'aptName',
    'startDealMonth',
    'endDealMonth',
    'dealMode',
    'sort',
    'minPrice',
    'maxPrice',
    'minDeposit',
    'maxDeposit',
    'minMonthlyRent',
    'maxMonthlyRent',
  ])
  // capabilities는 filterSchema 키와 항상 동일(단일 출처).
  assert.deepEqual(capabilities(), Object.keys(filterSchema))
})

test('every search-form filter key is exposed as an AI capability (no drift)', () => {
  // 검색 폼이 실제 쓰는 필터(emptyFilters)는 모두 filterSchema에 있어야 AI가 인식한다.
  // 전월세(dealMode/deposit/monthlyRent)처럼 폼에만 추가되고 schema 갱신이 누락되면 여기서 잡힌다.
  const schemaKeys = new Set(Object.keys(filterSchema))
  const missing = Object.keys(emptyFilters()).filter((key) => !schemaKeys.has(key))
  assert.deepEqual(missing, [], `filterSchema에 누락된 폼 필터 키: ${missing.join(', ')}`)
})

test('applyAgentFilters applies recognized keys and reports them', () => {
  const filters = emptyFilters()
  const { applied, ignored } = applyAgentFilters(filters, {
    sigungu: '강남구',
    startDealMonth: '2024-05',
  })

  assert.equal(filters.sigungu, '강남구')
  assert.equal(filters.startDealMonth, '2024-05')
  assert.deepEqual(applied, { sigungu: '강남구', startDealMonth: '2024-05' })
  assert.deepEqual(ignored, [])
})

test('applyAgentFilters ignores unknown keys without mutating them', () => {
  const filters = emptyFilters()
  const { applied, ignored } = applyAgentFilters(filters, {
    sigungu: '마포구',
    bedrooms: '3',
    foo: 'bar',
  })

  assert.equal(filters.sigungu, '마포구')
  assert.equal('bedrooms' in filters, false)
  assert.equal('foo' in filters, false)
  assert.deepEqual(applied, { sigungu: '마포구' })
  assert.deepEqual(ignored, ['bedrooms', 'foo'])
})

test('applied filters feed the existing search request builder (round-trip)', () => {
  const filters = emptyFilters()
  applyAgentFilters(filters, {
    sido: '서울특별시',
    sigungu: '강남구',
    startDealMonth: '2024-05',
    endDealMonth: '2024-05',
  })

  const [request] = buildHouseSearchRequests(filters, { page: 1 })

  assert.equal(request.lawdCd, '11680') // 강남구
  assert.equal(request.dealYmd, '202405')
  assert.equal(request.autoImport, 'true')
})

test('applyAgentFilters applies the Phase 2 filter keys (sort/umdNm/min/maxPrice)', () => {
  const filters = emptyFilters()
  const { applied, ignored } = applyAgentFilters(filters, {
    sigungu: '강남구',
    umdNm: '역삼동',
    sort: 'priceDesc',
    minPrice: '50000',
    maxPrice: '90000',
  })

  assert.equal(filters.umdNm, '역삼동')
  assert.equal(filters.sort, 'priceDesc')
  assert.equal(filters.minPrice, '50000')
  assert.equal(filters.maxPrice, '90000')
  assert.deepEqual(applied, {
    sigungu: '강남구',
    umdNm: '역삼동',
    sort: 'priceDesc',
    minPrice: '50000',
    maxPrice: '90000',
  })
  assert.deepEqual(ignored, [])
})

test('Phase 2 filters round-trip into the search request builder', () => {
  const filters = emptyFilters()
  applyAgentFilters(filters, {
    sido: '서울특별시',
    sigungu: '강남구',
    umdNm: '역삼동',
    sort: 'priceDesc',
    minPrice: '50000',
    maxPrice: '90000',
  })

  const [request] = buildHouseSearchRequests(filters, { page: 1 })

  assert.equal(request.umdNm, '역삼동')
  assert.equal(request.sort, 'priceDesc') // 지역이 있으므로 정렬이 전송됨
  assert.equal(request.minPrice, '50000')
  assert.equal(request.maxPrice, '90000')
})

test('sort is dropped from the request when no region is set', () => {
  const filters = emptyFilters()
  applyAgentFilters(filters, { sort: 'priceDesc' })

  const [request] = buildHouseSearchRequests(filters, { page: 1 })

  assert.equal(request.sort, '') // 지역 미설정 → 정렬 미전송(백엔드 무시)
})
