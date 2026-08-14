const env = import.meta.env || {}

function deploymentFeatureEnabled(configuredValue) {
  if (configuredValue === undefined || String(configuredValue).trim() === '') return !env.PROD
  return String(configuredValue).trim().toLowerCase() === 'true'
}

export const SEARCH_ALL_FETCH_SIZE = 100
export const SEARCH_REQUEST_TIMEOUT_MS = 25000
export const AUTO_IMPORT_REQUEST_TIMEOUT_MS = SEARCH_REQUEST_TIMEOUT_MS
export const REGION_REQUEST_TIMEOUT_MS = 10000
export const MIN_SEARCH_LOADING_MS = 600
export const DEFAULT_DEAL_MONTH = '2026-06'

export const DEFAULT_MAP_CENTER = { lat: 37.566826, lng: 126.9786567 }

export const SORT_OPTIONS = [
  { value: 'latest', label: '최신순' },
  { value: 'oldest', label: '오래된순' },
  { value: 'priceDesc', label: '높은 가격순' },
  { value: 'priceAsc', label: '낮은 가격순' },
  { value: 'areaDesc', label: '전용면적 큰순' },
  { value: 'areaAsc', label: '전용면적 작은순' },
  { value: 'depositDesc', label: '보증금 높은순' },
  { value: 'depositAsc', label: '보증금 낮은순' },
  { value: 'monthlyRentDesc', label: '월세 높은순' },
  { value: 'monthlyRentAsc', label: '월세 낮은순' },
]

export const DEAL_MODE_OPTIONS = [
  { value: 'sale', label: '매매' },
  { value: 'jeonse', label: '전세' },
  { value: 'monthly', label: '월세' },
  { value: 'rent', label: '전월세' },
  { value: 'all', label: '전체' },
]

export const NOTICE_ADMIN_EMAILS = (env.VITE_NOTICE_ADMIN_EMAILS || '')
  .split(',')
  .map((email) => email.trim().toLowerCase())
  .filter(Boolean)

// Production builds mirror the Backend's fail-closed feature policy.
export const PASSWORD_RESET_ENABLED = deploymentFeatureEnabled(env.VITE_PASSWORD_RESET_ENABLED)
export const MEMBER_SEARCH_ENABLED = deploymentFeatureEnabled(env.VITE_MEMBER_SEARCH_ENABLED)

export const KAKAO_MAP_API_KEY = env.VITE_KAKAO_MAP_API_KEY
export const KAKAO_MAP_SDK_ERROR_MESSAGE = 'Kakao Map SDK를 불러오지 못했습니다. JavaScript 키와 Kakao Developers 웹 플랫폼의 사이트 도메인을 확인해 주세요.'

export const SELECTED_MARKER_IMAGE_URL = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="42" height="52" viewBox="0 0 42 52">
  <path d="M21 50C21 50 39 30 39 18C39 8.6 30.9 1 21 1C11.1 1 3 8.6 3 18C3 30 21 50 21 50Z" fill="#f04438" stroke="#ffffff" stroke-width="3"/>
  <circle cx="21" cy="18" r="8" fill="#ffffff"/>
</svg>
`)}`
