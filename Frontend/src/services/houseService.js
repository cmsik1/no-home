import {
  buildHousePriceRangeRequests,
  buildHouseSearchRequests,
} from '../houseSearchParams.js'
import { SEARCH_ALL_FETCH_SIZE } from '../config/appConfig.js'
import { fetchWithTimeout, houseRequestTimeoutMs } from './apiClient.js'

function buildQueryString(fields) {
  const params = new URLSearchParams()
  Object.entries(fields).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  return params.toString() ? `?${params}` : ''
}

async function fetchHousePayload(path, fields, failureLabel) {
  const response = await fetchWithTimeout(`${path}${buildQueryString(fields)}`, {}, houseRequestTimeoutMs(fields))
  const body = await response.json().catch(() => null)
  if (!response.ok || body?.success === false) throw new Error(body?.message || `${failureLabel} (${response.status})`)
  return body?.data ?? body ?? {}
}

/** 직렬화된 검색 필드를 query string으로 보내고 Backend 응답의 data를 검색 payload로 꺼낸다. */
export function fetchHouseSearch(fields) {
  return fetchHousePayload('/api/houses/search', fields, '검색 요청에 실패했습니다')
}

export function fetchHousePriceRange(fields) {
  return fetchHousePayload('/api/houses/price-range', fields, '가격 범위 요청에 실패했습니다')
}

export async function fetchPagedHouseSearchResults(searchFilters, { page, size }) {
  return Promise.all(buildHouseSearchRequests(searchFilters, { page, size }).map(fetchHouseSearch))
}

/**
 * 첫 페이지에서 전체 건수를 확인한 뒤 나머지 페이지를 병렬 조회한다. 최초 요청만 자동 수집을
 * 허용하고 후속 페이지는 DB 조회만 수행해 같은 공공데이터를 반복 적재하지 않는다.
 */
export async function fetchAllHouseSearchResults(searchFilters) {
  const firstRequests = buildHouseSearchRequests(searchFilters, { page: 1, size: SEARCH_ALL_FETCH_SIZE })
  const firstResults = await Promise.all(firstRequests.map(fetchHouseSearch))
  const total = firstResults.reduce((sum, payload) => sum + (Number.isFinite(payload.totalCount) ? payload.totalCount : (payload.items?.length || 0)), 0)
  const pageCount = Math.max(Math.ceil(total / SEARCH_ALL_FETCH_SIZE), 1)
  if (pageCount <= 1) return firstResults

  const restRequests = []
  for (let page = 2; page <= pageCount; page += 1) {
    buildHouseSearchRequests(searchFilters, { page, size: SEARCH_ALL_FETCH_SIZE })
      .forEach((fields) => restRequests.push({ ...fields, autoImport: 'false' }))
  }
  return [...firstResults, ...(await Promise.all(restRequests.map(fetchHouseSearch)))]
}

export async function fetchPriceRangeResults(filters) {
  return Promise.all(buildHousePriceRangeRequests(filters).map(fetchHousePriceRange))
}
