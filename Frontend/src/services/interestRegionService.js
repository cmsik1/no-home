import { requestJson } from './apiClient'

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
