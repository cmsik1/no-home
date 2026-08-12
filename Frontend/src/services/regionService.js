import { REGION_REQUEST_TIMEOUT_MS } from '../config/appConfig'
import { fetchWithTimeout } from './apiClient'
import { fieldText } from '../utils/houseDisplay'

/**
 * 행정구역 API 결과의 깨진 표시 문자열을 보정하고 같은 label을 제거한 정렬 선택지로 변환한다.
 * value에는 서버가 이해하는 원문 동 이름을 유지해 후속 검색 계약을 보존한다.
 */
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
