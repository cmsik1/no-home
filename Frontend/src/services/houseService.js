import {
  buildHousePriceRangeRequests,
  buildHouseSearchRequests,
} from '../houseSearchParams'
import { SEARCH_ALL_FETCH_SIZE } from '../config/appConfig'
import { fetchWithTimeout, houseRequestTimeoutMs } from './apiClient'

export async function fetchHouseSearch(fields) {
  const params = new URLSearchParams()
  Object.entries(fields).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  const response = await fetchWithTimeout(`/api/houses/search${params.toString() ? `?${params}` : ''}`, {}, houseRequestTimeoutMs(fields))
  const body = await response.json().catch(() => null)
  if (!response.ok || body?.success === false) throw new Error(body?.message || `검색 요청에 실패했습니다 (${response.status})`)
  return body?.data ?? body ?? {}
}

export async function fetchHousePriceRange(fields) {
  const params = new URLSearchParams()
  Object.entries(fields).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  const response = await fetchWithTimeout(`/api/houses/price-range${params.toString() ? `?${params}` : ''}`, {}, houseRequestTimeoutMs(fields))
  const body = await response.json().catch(() => null)
  if (!response.ok || body?.success === false) throw new Error(body?.message || `가격 범위 요청에 실패했습니다 (${response.status})`)
  return body?.data ?? body ?? {}
}

export async function fetchPagedHouseSearchResults(searchFilters, { page, size }) {
  return Promise.all(buildHouseSearchRequests(searchFilters, { page, size }).map(fetchHouseSearch))
}

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
