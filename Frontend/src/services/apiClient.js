import {
  AUTO_IMPORT_REQUEST_TIMEOUT_MS,
  SEARCH_REQUEST_TIMEOUT_MS,
} from '../config/appConfig.js'

export function houseRequestTimeoutMs(fields = {}) {
  return fields.autoImport === 'true' ? AUTO_IMPORT_REQUEST_TIMEOUT_MS : SEARCH_REQUEST_TIMEOUT_MS
}

/** fetch에 제한 시간을 부여하고 AbortError를 사용자가 이해할 수 있는 검색 오류로 변환한다. */
export async function fetchWithTimeout(url, options = {}, timeoutMs = SEARCH_REQUEST_TIMEOUT_MS) {
  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs)
  try {
    return await fetch(url, { ...options, signal: controller.signal })
  } catch (exception) {
    if (exception?.name === 'AbortError') {
      throw new Error('요청 시간이 초과되었습니다. 조건을 줄이거나 잠시 후 다시 시도해 주세요.')
    }
    throw exception
  } finally {
    window.clearTimeout(timeoutId)
  }
}

function apiErrorMessage(status, message) {
  if (message) return message
  if (status === 401) return '로그인이 필요한 요청입니다.'
  if (status === 403) return '권한이 없습니다.'
  if (status === 404) return '요청한 리소스를 찾을 수 없습니다.'
  return `요청 처리에 실패했습니다. (${status})`
}

/**
 * 쿠키 기반 인증을 포함하는 공통 JSON 요청 경계다. Backend의 ApiResponse 형태와 일반 JSON을
 * 같은 data 값으로 정규화하고 HTTP 상태 및 success=false를 status가 있는 Error로 바꾼다.
 */
export async function requestJson(path, options = {}) {
  const headers = {
    Accept: 'application/json',
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(options.headers || {}),
  }
  const response = await fetch(path, { ...options, headers, credentials: 'include' })
  const body = await response.json().catch(() => null)
  if (!response.ok || body?.success === false) {
    const error = new Error(apiErrorMessage(response.status, body?.message))
    error.status = response.status
    throw error
  }
  return body?.data ?? body ?? {}
}
