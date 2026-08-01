import { REGION_REQUEST_TIMEOUT_MS } from '../config/appConfig'
import { fetchWithTimeout } from './apiClient'
import { fieldText } from '../utils/houseDisplay'

export async function fetchLegalDongs(lawdCd) {
  if (!lawdCd) return []
  const response = await fetchWithTimeout(`/api/regions?lawdCd=${encodeURIComponent(lawdCd)}`, {}, REGION_REQUEST_TIMEOUT_MS)
  const body = await response.json().catch(() => null)
  if (!response.ok || body?.success === false) {
    throw new Error(body?.message || `읍면동 요청에 실패했습니다 (${response.status})`)
  }

  const dongsByLabel = new Map()
  ;(Array.isArray(body?.data) ? body.data : []).forEach((region) => {
    const rawValue = typeof region?.umdNm === 'string' ? region.umdNm.trim() : ''
    const label = fieldText(rawValue, '').trim()
    if (rawValue && label) dongsByLabel.set(label, { label, value: rawValue })
  })
  return Array.from(dongsByLabel.values()).sort((a, b) => a.label.localeCompare(b.label, 'ko'))
}
