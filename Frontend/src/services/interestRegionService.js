import { requestJson } from './apiClient'

/** 관심지역 API 응답을 화면에서 바로 반복 가능한 배열로 정규화한다. */
export async function fetchInterestRegions() {
  const regions = await requestJson('/api/interest-regions')
  return Array.isArray(regions) ? regions : []
}

export function createInterestRegion(payload) {
  return requestJson('/api/interest-regions', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function removeInterestRegion(interestRegionId) {
  return requestJson(`/api/interest-regions/${interestRegionId}`, { method: 'DELETE' })
}
