<script>
import {
  applyAgentFilters,
  buildHousePriceRangeRequests,
  buildHouseSearchRequests,
  currentDealMonth,
  emptyFilters,
  isSeoul,
  seoulLawdCodes,
  seoulDistricts,
  sortOptionsForDealMode,
} from './houseSearchParams'
import { resolvePaginateTarget, resolveItemTarget } from './chat/agentActions'
import ChatWidget from './components/ChatWidget.vue'

const SEARCH_ALL_FETCH_SIZE = 100

// 검색 폼 초기/리셋 상태. 거래월은 최신월(직전월) 기본값으로 채워, 거래월 미지정 검색(예: AI '강남구 검색')이
// 라이브 조회 없이 0건이 되는 것을 막는다. emptyFilters는 순수 유지하고 여기서만 거래월을 주입한다.
const initialFilters = () => ({
  ...emptyFilters(),
  startDealMonth: currentDealMonth(),
  endDealMonth: currentDealMonth(),
})
const SEARCH_REQUEST_TIMEOUT_MS = 25000
const AUTO_IMPORT_REQUEST_TIMEOUT_MS = SEARCH_REQUEST_TIMEOUT_MS
const REGION_REQUEST_TIMEOUT_MS = 10000
const MIN_SEARCH_LOADING_MS = 600
const DEFAULT_DEAL_MONTH = '2026-06'
const SORT_OPTIONS = [
  { value: 'latest', label: '최신순' },
  { value: 'oldest', label: '오래된 순' },
  { value: 'priceDesc', label: '높은 가격순' },
  { value: 'priceAsc', label: '낮은 가격순' },
  { value: 'areaDesc', label: '전용면적 넓은순' },
  { value: 'areaAsc', label: '전용면적 좁은순' },
  { value: 'depositDesc', label: '보증금 높은순' },
  { value: 'depositAsc', label: '보증금 낮은순' },
  { value: 'monthlyRentDesc', label: '월세 높은순' },
  { value: 'monthlyRentAsc', label: '월세 낮은순' },
]
const DEAL_MODE_OPTIONS = [
  { value: 'sale', label: '매매' },
  { value: 'jeonse', label: '전세' },
  { value: 'monthly', label: '월세' },
  { value: 'rent', label: '전월세' },
  { value: 'all', label: '전체' },
]
const DEFAULT_MAP_CENTER = {
  lat: 37.566826,
  lng: 126.9786567,
}
const SELECTED_MARKER_IMAGE_URL = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="42" height="52" viewBox="0 0 42 52">
  <path d="M21 50C21 50 39 30 39 18C39 8.6 30.9 1 21 1C11.1 1 3 8.6 3 18C3 30 21 50 21 50Z" fill="#f04438" stroke="#ffffff" stroke-width="3"/>
  <circle cx="21" cy="18" r="8" fill="#ffffff"/>
</svg>
`)}`
const KAKAO_MAP_API_KEY = import.meta.env.VITE_KAKAO_MAP_API_KEY
const NOTICE_ADMIN_EMAILS = (import.meta.env.VITE_NOTICE_ADMIN_EMAILS || 'admin@nohome.local,admin@example.com')
  .split(',')
  .map((email) => email.trim().toLowerCase())
  .filter(Boolean)
const KAKAO_MAP_SDK_ERROR_MESSAGE = 'Kakao Map SDK 로드에 실패했습니다. JavaScript 키인지 확인하고 Kakao Developers Web 플랫폼 사이트 도메인에 현재 주소를 등록해 주세요.'
let kakaoMapsSdkPromise = null

const fieldText = (value, fallback = '-') => {
  if (value === null || value === undefined || value === '') {
    return fallback
  }

  return repairMojibake(String(value))
}

const windows1252Bytes = {
  0x20AC: 0x80,
  0x201A: 0x82,
  0x0192: 0x83,
  0x201E: 0x84,
  0x2026: 0x85,
  0x2020: 0x86,
  0x2021: 0x87,
  0x02C6: 0x88,
  0x2030: 0x89,
  0x0160: 0x8A,
  0x2039: 0x8B,
  0x0152: 0x8C,
  0x017D: 0x8E,
  0x2018: 0x91,
  0x2019: 0x92,
  0x201C: 0x93,
  0x201D: 0x94,
  0x2022: 0x95,
  0x2013: 0x96,
  0x2014: 0x97,
  0x02DC: 0x98,
  0x2122: 0x99,
  0x0161: 0x9A,
  0x203A: 0x9B,
  0x0153: 0x9C,
  0x017E: 0x9E,
  0x0178: 0x9F,
}

const repairMojibake = (value) => {
  if (!looksLikeMojibake(value) || /[가-힣]/.test(value) || typeof TextDecoder === 'undefined') {
    return value
  }

  const bytes = []

  for (const char of value) {
    const code = char.charCodeAt(0)

    if (code <= 0xff) {
      bytes.push(code)
    } else if (windows1252Bytes[code]) {
      bytes.push(windows1252Bytes[code])
    } else {
      return value
    }
  }

  try {
    return new TextDecoder('utf-8', { fatal: true }).decode(new Uint8Array(bytes))
  } catch {
    return value
  }
}

const looksLikeMojibake = (value) => {
  for (const char of value) {
    const code = char.charCodeAt(0)
    if ((code >= 0x80 && code <= 0xff) || windows1252Bytes[code]) {
      return true
    }
  }

  return false
}

const loadKakaoMapsSdk = () => {
  if (!KAKAO_MAP_API_KEY) {
    return Promise.reject(new Error('VITE_KAKAO_MAP_API_KEY가 설정되지 않았습니다.'))
  }

  if (typeof window === 'undefined') {
    return Promise.reject(new Error('브라우저 환경에서만 지도를 사용할 수 있습니다.'))
  }

  if (window.kakao?.maps?.services) {
    return Promise.resolve(window.kakao)
  }

  if (kakaoMapsSdkPromise) {
    return kakaoMapsSdkPromise
  }

  kakaoMapsSdkPromise = new Promise((resolve, reject) => {
    const existingScript = document.querySelector('script[data-kakao-map-sdk="true"]')
    const finishLoad = () => {
      if (!window.kakao?.maps) {
        reject(new Error(KAKAO_MAP_SDK_ERROR_MESSAGE))
        return
      }

      window.kakao.maps.load(() => resolve(window.kakao))
    }

    if (existingScript) {
      existingScript.addEventListener('load', finishLoad, { once: true })
      existingScript.addEventListener('error', () => reject(new Error(KAKAO_MAP_SDK_ERROR_MESSAGE)), { once: true })
      return
    }

    const script = document.createElement('script')
    script.dataset.kakaoMapSdk = 'true'
    script.async = true
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${encodeURIComponent(KAKAO_MAP_API_KEY)}&libraries=services&autoload=false`
    script.addEventListener('load', finishLoad, { once: true })
    script.addEventListener('error', () => reject(new Error(KAKAO_MAP_SDK_ERROR_MESSAGE)), { once: true })
    document.head.appendChild(script)
  })

  return kakaoMapsSdkPromise
}

const fetchWithTimeout = async (url, options = {}, timeoutMs = SEARCH_REQUEST_TIMEOUT_MS) => {
  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs)

  try {
    return await fetch(url, {
      ...options,
      signal: controller.signal,
    })
  } catch (exception) {
    if (exception?.name === 'AbortError') {
      throw new Error('요청 시간이 초과되었습니다. 백엔드 로그 또는 공공데이터 API 응답 상태를 확인해 주세요.')
    }
    throw exception
  } finally {
    window.clearTimeout(timeoutId)
  }
}

const houseRequestTimeoutMs = (fields = {}) => {
  return fields.autoImport === 'true' ? AUTO_IMPORT_REQUEST_TIMEOUT_MS : SEARCH_REQUEST_TIMEOUT_MS
}

const wait = (ms) => new Promise((resolve) => window.setTimeout(resolve, ms))

const waitForPaint = () => new Promise((resolve) => {
  window.requestAnimationFrame(() => {
    window.requestAnimationFrame(resolve)
  })
})

const keepSearchLoadingVisible = async (startedAt) => {
  const remainingMs = MIN_SEARCH_LOADING_MS - (Date.now() - startedAt)
  if (remainingMs > 0) {
    await wait(remainingMs)
  }
}

export default {
  components: {
    ChatWidget,
  },
  data() {
    return {
      filters: initialFilters(),
      items: [],
      totalCount: null,
      searchPage: 1,
      resultDisplayMode: '10',
      selectedItem: null,
      loading: false,
      searchRequestId: 0,
      searchPanelCollapsed: false,
      error: '',
      hasSearched: false,
      seoulDistricts,
      sortOptions: SORT_OPTIONS,
      dealModeOptions: DEAL_MODE_OPTIONS,
      priceRangeMin: null,
      priceRangeMax: null,
      monthlyRentRangeMin: null,
      monthlyRentRangeMax: null,
      priceRangeLoading: false,
      priceRangeError: '',
      regionError: '',
      dealMonthError: '',
      lastStackedThumb: 'max',
      lastMonthlyRentStackedThumb: 'max',
      legalDongs: [],
      legalDongLoading: false,
      legalDongRequestId: 0,
      legalDongError: '',
      member: null,
      // 메인 뷰 분기(search/account/member-search/notice). 기본은 검색 화면.
      // (92cf333 머지 충돌 해결에서 누락됐던 초기화 복원 — issue #18)
      activePage: 'search',
      accountPanelOpen: false,
      accountMode: 'login',
      memberLoading: false,
      memberMessage: '',
      memberError: '',
      loginForm: {
        email: '',
        password: '',
      },
      signupForm: {
        email: '',
        password: '',
        name: '',
        phone: '',
      },
      passwordResetForm: {
        email: '',
        name: '',
        phone: '',
        newPassword: '',
      },
      profileForm: {
        name: '',
        phone: '',
      },
      profileEditing: false,
      memberSearchKeyword: '',
      memberSearchResults: [],
      interestRegions: [],
      interestRegionLoading: false,
      interestRegionMessage: '',
      interestRegionError: '',
      deleteConfirm: '',
      notices: [],
      noticeLoading: false,
      noticeMessage: '',
      noticeError: '',
      noticeEditingId: null,
      noticeForm: {
        title: '',
        content: '',
      },
      kakao: null,
      map: null,
      defaultMarkerImage: null,
      selectedMarkerImage: null,
      markerDisplayCount: 0,
      mapLoading: false,
      mapStatus: '지도 API 키를 확인하고 있습니다.',
      mapError: '',
      mapReady: false,
      agentResult: null,
      agentSeq: 0,
    }
  },
  mounted() {
    this.mapMarkers = []
    this.mapMarkerItems = []
    this.geocodedMapItems = []
    this.loadCurrentMember()
    this.prepareMap()
  },
  watch: {
    // 탭 전환(공지/회원/로그인)으로 검색 main이 v-if로 파괴되면 mapCanvas div도 사라진다.
    // 검색 화면 복귀 시 새 div에 지도를 재생성하고 마커를 복원한다(빈 지도 방지).
    activePage(value) {
      if (value === 'search' && this.mapReady) {
        this.$nextTick(() => {
          this.ensureMap(true)
          this.refreshMapMarkers()
        })
      }
    },
  },
  computed: {
    accountSummary() {
      if (!this.member) {
        return '로그인이 필요합니다.'
      }

      return `${fieldText(this.member.name, this.member.email)} · ${this.member.email}`
    },
    isNoticeAdmin() {
      return Boolean(this.member?.email && NOTICE_ADMIN_EMAILS.includes(String(this.member.email).trim().toLowerCase()))
    },
    visibleCountLabel() {
      if (this.loading) {
        return '조회 중'
      }

      if (!this.hasSearched) {
        return '검색 전'
      }

      if (this.totalCount !== null && this.totalCount !== undefined) {
        return `${this.totalCount.toLocaleString()}건`
      }

      return `${this.items.length.toLocaleString()}건`
    },
    mapSummary() {
      if (!this.selectedItem) {
        return '목록에서 주택을 선택하면 이 영역에 선택 요약만 표시됩니다.'
      }

      return `${this.displayAptName(this.selectedItem)} · ${this.displayAddress(this.selectedItem)}`
    },
    totalPages() {
      if (this.resultDisplayMode === 'all') {
        return 1
      }

      if (!this.totalCount) {
        return 1
      }

      return Math.max(Math.ceil(this.totalCount / this.currentPageSize), 1)
    },
    currentPageSize() {
      const size = Number(this.resultDisplayMode)
      return Number.isFinite(size) && size > 0 ? size : 10
    },
    canGoPreviousPage() {
      return this.resultDisplayMode !== 'all' && this.hasSearched && this.searchPage > 1 && !this.loading
    },
    canGoNextPage() {
      return this.resultDisplayMode !== 'all' && this.hasSearched && this.searchPage < this.totalPages && !this.loading
    },
    pageSummary() {
      if (!this.hasSearched) {
        return '검색 전'
      }

      if (this.resultDisplayMode === 'all') {
        return '전체 표시'
      }

      return `${this.searchPage.toLocaleString()} / ${this.totalPages.toLocaleString()} 페이지`
    },
    resultMetaLabel() {
      if (this.loading) {
        return this.resultDisplayMode === 'all'
          ? '조회 중 · 전체 데이터'
          : `조회 중 · 페이지당 ${this.currentPageSize}개`
      }

      const count = this.totalCount ?? 0
      if (this.resultDisplayMode === 'all') {
        return `${this.items.length.toLocaleString()} / ${count.toLocaleString()}건 표시`
      }
      return `${count.toLocaleString()}건 · 페이지당 ${this.currentPageSize}개`
    },
    markerCountLabel() {
      if (!this.hasSearched) {
        return '검색 전'
      }

      return `${this.markerDisplayCount.toLocaleString()} / ${this.items.length.toLocaleString()}개 표시`
    },
    selectedLawdCd() {
      if (!isSeoul(this.filters.sido) || !this.filters.sigungu) {
        return ''
      }

      return seoulLawdCodes[this.filters.sigungu] || ''
    },
    legalDongDisabled() {
      return !this.selectedLawdCd || this.legalDongLoading || this.legalDongs.length === 0
    },
    canSaveInterestRegion() {
      return Boolean(this.member && this.selectedLawdCd && this.filters.umdNm && !this.interestRegionLoading)
    },
    selectedInterestRegionLabel() {
      const parts = [this.filters.sido, this.filters.sigungu, this.filters.umdNm].filter(Boolean)
      return parts.length ? parts.join(' ') : '관심지역'
    },
    sortDisabled() {
      return !this.filters.sido
    },
    activeSortOptions() {
      const allowed = sortOptionsForDealMode(this.filters.dealMode)
      return this.sortOptions.filter((option) => allowed.includes(option.value))
    },
    priceFilterVisible() {
      return ['sale', 'jeonse', 'monthly'].includes(this.filters.dealMode)
    },
    priceFilterTitle() {
      if (this.filters.dealMode === 'jeonse') {
        return '보증금 구간'
      }
      if (this.filters.dealMode === 'monthly') {
        return '보증금/월세 구간'
      }
      return '가격 구간'
    },
    activeMinPriceKey() {
      return this.filters.dealMode === 'sale' ? 'minPrice' : 'minDeposit'
    },
    activeMaxPriceKey() {
      return this.filters.dealMode === 'sale' ? 'maxPrice' : 'maxDeposit'
    },
    priceRangeAvailable() {
      return Number.isFinite(this.priceRangeMin)
        && Number.isFinite(this.priceRangeMax)
        && this.priceRangeMin <= this.priceRangeMax
    },
    monthlyRentRangeAvailable() {
      return this.filters.dealMode === 'monthly'
        && Number.isFinite(this.monthlyRentRangeMin)
        && Number.isFinite(this.monthlyRentRangeMax)
        && this.monthlyRentRangeMin <= this.monthlyRentRangeMax
    },
    priceSliderStep() {
      if (!this.priceRangeAvailable) {
        return 100
      }

      const span = this.priceRangeMax - this.priceRangeMin
      if (span <= 10000) {
        return 100
      }
      if (span <= 50000) {
        return 500
      }
      return 1000
    },
    priceMinPercent() {
      return this.priceThumbPercent(this.filters[this.activeMinPriceKey])
    },
    priceMaxPercent() {
      return this.priceThumbPercent(this.filters[this.activeMaxPriceKey])
    },
    monthlyRentMinPercent() {
      return this.monthlyRentThumbPercent(this.filters.minMonthlyRent)
    },
    monthlyRentMaxPercent() {
      return this.monthlyRentThumbPercent(this.filters.maxMonthlyRent)
    },
    priceRangeSummary() {
      if (this.priceRangeLoading) {
        return '가격 범위를 불러오는 중입니다.'
      }

      if (this.priceRangeError) {
        return this.priceRangeError
      }

      if (!this.priceRangeAvailable) {
        return '전체 범위 선택으로 먼저 가격 범위를 지정할 수 있습니다.'
      }

      return `${this.displayManwon(this.priceRangeMin)} ~ ${this.displayManwon(this.priceRangeMax)}`
    },
    monthlyRentRangeSummary() {
      if (this.priceRangeLoading) {
        return '월세 범위를 불러오는 중입니다.'
      }

      if (!this.monthlyRentRangeAvailable) {
        return '전체 범위 선택으로 먼저 월세 범위를 지정할 수 있습니다.'
      }

      return `${this.displayManwon(this.monthlyRentRangeMin)} ~ ${this.displayManwon(this.monthlyRentRangeMax)}`
    },
    priceRangeLoadDisabled() {
      return this.loading || this.priceRangeLoading || this.invalidRegionSelection || !this.hasPriceRangeCondition()
    },
    invalidPriceRange() {
      if (!this.priceRangeAvailable) {
        return false
      }

      const min = Number(this.filters[this.activeMinPriceKey])
      const max = Number(this.filters[this.activeMaxPriceKey])
      return !Number.isFinite(min)
        || !Number.isFinite(max)
        || min < this.priceRangeMin
        || max > this.priceRangeMax
        || min >= max
    },
    invalidMonthlyRentRange() {
      if (!this.monthlyRentRangeAvailable) {
        return false
      }

      const min = Number(this.filters.minMonthlyRent)
      const max = Number(this.filters.maxMonthlyRent)
      return !Number.isFinite(min)
        || !Number.isFinite(max)
        || min < this.monthlyRentRangeMin
        || max > this.monthlyRentRangeMax
        || min >= max
    },
    invalidDealMonthRange() {
      return Boolean(
        this.filters.startDealMonth
          && this.filters.endDealMonth
          && this.filters.startDealMonth > this.filters.endDealMonth
      )
    },
    invalidRegionSelection() {
      return isSeoul(this.filters.sido) && !this.filters.sigungu
    },
    mapStatusLabel() {
      if (this.mapError) {
        return this.mapError
      }

      if (this.mapLoading) {
        return '지도 처리 중'
      }

      return this.hasSearched
        ? `${this.markerCountLabel} · ${this.mapStatus}`
        : this.mapStatus
    },
    activeFilterSummary() {
      const parts = []
      const dealModeLabel = this.dealModeOptions.find((option) => option.value === this.filters.dealMode)?.label
      if (dealModeLabel) {
        parts.push(dealModeLabel)
      }

      const region = [
        this.filters.sido,
        this.filters.sigungu,
        this.filters.umdNm,
      ].filter(Boolean).join(' ')
      parts.push(region || '지역 미선택')

      if (this.filters.aptName) {
        parts.push(`아파트 ${this.filters.aptName}`)
      }

      const dealMonth = this.describeFilterDealMonth()
      if (dealMonth) {
        parts.push(dealMonth)
      }

      const sortLabel = this.sortOptions.find((option) => option.value === this.filters.sort)?.label
      if (sortLabel) {
        parts.push(sortLabel)
      }

      parts.push(...this.describeFilterPriceParts())
      return parts
    },
  },
  methods: {
    isSeoul,
    fieldText,
    toggleSearchPanel() {
      this.searchPanelCollapsed = !this.searchPanelCollapsed
    },
    describeFilterDealMonth() {
      const start = this.filters.startDealMonth
      const end = this.filters.endDealMonth
      if (start && end) {
        return start === end ? start : `${start}~${end}`
      }
      return start || end || ''
    },
    describeFilterPriceParts() {
      if (!this.priceFilterVisible) {
        return []
      }

      const parts = []
      const minPrice = this.filters[this.activeMinPriceKey]
      const maxPrice = this.filters[this.activeMaxPriceKey]
      if (minPrice !== '' || maxPrice !== '') {
        const label = this.filters.dealMode === 'sale' ? '실거래가' : '보증금'
        parts.push(`${label} ${this.describeFilterRange(minPrice, maxPrice)}`)
      }

      if (this.filters.dealMode === 'monthly'
          && (this.filters.minMonthlyRent !== '' || this.filters.maxMonthlyRent !== '')) {
        parts.push(`월세 ${this.describeFilterRange(this.filters.minMonthlyRent, this.filters.maxMonthlyRent)}`)
      }

      return parts
    },
    describeFilterRange(min, max) {
      const hasMin = min !== '' && min !== null && min !== undefined
      const hasMax = max !== '' && max !== null && max !== undefined
      if (hasMin && hasMax) {
        return `${this.displayManwon(min)}~${this.displayManwon(max)}`
      }
      if (hasMin) {
        return `${this.displayManwon(min)} 이상`
      }
      if (hasMax) {
        return `${this.displayManwon(max)} 이하`
      }
      return '전체'
    },
    handleDealModeChange() {
      const allowed = sortOptionsForDealMode(this.filters.dealMode)
      if (!allowed.includes(this.filters.sort)) {
        this.filters.sort = 'latest'
      }
      this.priceRangeMin = null
      this.priceRangeMax = null
      this.monthlyRentRangeMin = null
      this.monthlyRentRangeMax = null
      this.priceRangeError = ''
      this.filters.minPrice = ''
      this.filters.maxPrice = ''
      this.filters.minDeposit = ''
      this.filters.maxDeposit = ''
      this.filters.minMonthlyRent = ''
      this.filters.maxMonthlyRent = ''
    },
    priceThumbPercent(value) {
      if (!this.priceRangeAvailable || this.priceRangeMax === this.priceRangeMin) {
        return 0
      }

      const numeric = Number(value)
      if (!Number.isFinite(numeric)) {
        return 0
      }

      const clamped = Math.min(Math.max(numeric, this.priceRangeMin), this.priceRangeMax)
      return ((clamped - this.priceRangeMin) / (this.priceRangeMax - this.priceRangeMin)) * 100
    },
    monthlyRentThumbPercent(value) {
      if (!this.monthlyRentRangeAvailable || this.monthlyRentRangeMax === this.monthlyRentRangeMin) {
        return 0
      }

      const numeric = Number(value)
      if (!Number.isFinite(numeric)) {
        return 0
      }

      const clamped = Math.min(Math.max(numeric, this.monthlyRentRangeMin), this.monthlyRentRangeMax)
      return ((clamped - this.monthlyRentRangeMin) / (this.monthlyRentRangeMax - this.monthlyRentRangeMin)) * 100
    },
    setPriceRangeFromResults(results, { resetSelection = false } = {}) {
      const minField = this.filters.dealMode === 'sale' ? 'minDealAmountManwon' : 'minDepositManwon'
      const maxField = this.filters.dealMode === 'sale' ? 'maxDealAmountManwon' : 'maxDepositManwon'
      const filterMinKey = this.activeMinPriceKey
      const filterMaxKey = this.activeMaxPriceKey
      const mins = results
        .map((payload) => Number(payload?.[minField]))
        .filter(Number.isFinite)
      const maxes = results
        .map((payload) => Number(payload?.[maxField]))
        .filter(Number.isFinite)

      if (!mins.length || !maxes.length) {
        return false
      }

      this.priceRangeMin = Math.min(...mins)
      this.priceRangeMax = Math.max(...maxes)
      if (resetSelection || this.filters[filterMinKey] === '' || !Number.isFinite(Number(this.filters[filterMinKey]))) {
        this.filters[filterMinKey] = this.priceRangeMin
      }
      if (resetSelection || this.filters[filterMaxKey] === '' || !Number.isFinite(Number(this.filters[filterMaxKey]))) {
        this.filters[filterMaxKey] = this.priceRangeMax
      }
      if (!this.invalidPriceRange) {
        this.filters[filterMinKey] = this.clampPrice(this.filters[filterMinKey], this.priceRangeMin, Number(this.filters[filterMaxKey]))
        this.filters[filterMaxKey] = this.clampPrice(this.filters[filterMaxKey], Number(this.filters[filterMinKey]), this.priceRangeMax)
      }

      if (this.filters.dealMode === 'monthly') {
        const monthlyMins = results
          .map((payload) => Number(payload?.minMonthlyRentManwon))
          .filter(Number.isFinite)
        const monthlyMaxes = results
          .map((payload) => Number(payload?.maxMonthlyRentManwon))
          .filter(Number.isFinite)
        if (!monthlyMins.length || !monthlyMaxes.length) {
          this.monthlyRentRangeMin = null
          this.monthlyRentRangeMax = null
          return false
        }
        this.monthlyRentRangeMin = Math.min(...monthlyMins)
        this.monthlyRentRangeMax = Math.max(...monthlyMaxes)
        if (resetSelection || this.filters.minMonthlyRent === '' || !Number.isFinite(Number(this.filters.minMonthlyRent))) {
          this.filters.minMonthlyRent = this.monthlyRentRangeMin
        }
        if (resetSelection || this.filters.maxMonthlyRent === '' || !Number.isFinite(Number(this.filters.maxMonthlyRent))) {
          this.filters.maxMonthlyRent = this.monthlyRentRangeMax
        }
        if (!this.invalidMonthlyRentRange) {
          this.filters.minMonthlyRent = this.clampPrice(this.filters.minMonthlyRent, this.monthlyRentRangeMin, Number(this.filters.maxMonthlyRent))
          this.filters.maxMonthlyRent = this.clampPrice(this.filters.maxMonthlyRent, Number(this.filters.minMonthlyRent), this.monthlyRentRangeMax)
        }
      } else {
        this.monthlyRentRangeMin = null
        this.monthlyRentRangeMax = null
      }
      this.priceRangeError = ''
      return true
    },
    hasPriceRangeCondition() {
      return Boolean(
        this.filters.lawdCd
          || this.filters.sido
          || this.filters.sigungu
          || this.filters.umdNm
          || this.filters.aptName
          || this.filters.startDealMonth
          || this.filters.endDealMonth
      )
    },
    clampPrice(value, min, max) {
      const numeric = Number(value)
      const fallback = Number.isFinite(min) ? min : 0
      const lower = Number.isFinite(min) ? min : fallback
      const upper = Number.isFinite(max) ? max : lower
      if (!Number.isFinite(numeric)) {
        return lower
      }
      return Math.min(Math.max(Math.trunc(numeric), lower), upper)
    },
    handleMinPriceInput(event) {
      if (!this.priceRangeAvailable) {
        return
      }
      this.filters[this.activeMinPriceKey] = event?.target?.value ?? ''
      this.priceRangeError = ''
    },
    handleMaxPriceInput(event) {
      if (!this.priceRangeAvailable) {
        return
      }
      this.filters[this.activeMaxPriceKey] = event?.target?.value ?? ''
      this.priceRangeError = ''
    },
    handleMinPriceThumbInput(event) {
      if (!this.priceRangeAvailable) {
        return
      }
      this.filters[this.activeMinPriceKey] = this.clampPrice(event?.target?.value, this.priceRangeMin, Number(this.filters[this.activeMaxPriceKey]))
      if (Number(this.filters[this.activeMinPriceKey]) === Number(this.filters[this.activeMaxPriceKey])) {
        this.lastStackedThumb = 'min'
      }
    },
    handleMaxPriceThumbInput(event) {
      if (!this.priceRangeAvailable) {
        return
      }
      this.filters[this.activeMaxPriceKey] = this.clampPrice(event?.target?.value, Number(this.filters[this.activeMinPriceKey]), this.priceRangeMax)
      if (Number(this.filters[this.activeMinPriceKey]) === Number(this.filters[this.activeMaxPriceKey])) {
        this.lastStackedThumb = 'max'
      }
    },
    handlePriceThumbPointerDown(thumb) {
      if (Number(this.filters[this.activeMinPriceKey]) === Number(this.filters[this.activeMaxPriceKey])) {
        this.lastStackedThumb = thumb
      }
    },
    handlePriceThumbInput(thumb, event) {
      if (thumb === 'min') {
        this.handleMinPriceThumbInput(event)
      } else {
        this.handleMaxPriceThumbInput(event)
      }
    },
    handleMonthlyRentThumbPointerDown(thumb) {
      if (Number(this.filters.minMonthlyRent) === Number(this.filters.maxMonthlyRent)) {
        this.lastMonthlyRentStackedThumb = thumb
      }
    },
    handleMonthlyRentThumbInput(thumb, event) {
      if (!this.monthlyRentRangeAvailable) {
        return
      }

      if (thumb === 'min') {
        this.filters.minMonthlyRent = this.clampPrice(event?.target?.value, this.monthlyRentRangeMin, Number(this.filters.maxMonthlyRent))
        if (Number(this.filters.minMonthlyRent) === Number(this.filters.maxMonthlyRent)) {
          this.lastMonthlyRentStackedThumb = 'min'
        }
        return
      }

      this.filters.maxMonthlyRent = this.clampPrice(event?.target?.value, Number(this.filters.minMonthlyRent), this.monthlyRentRangeMax)
      if (Number(this.filters.minMonthlyRent) === Number(this.filters.maxMonthlyRent)) {
        this.lastMonthlyRentStackedThumb = 'max'
      }
    },
    resetPriceRange() {
      if (!this.priceRangeAvailable) {
        return
      }

      this.filters[this.activeMinPriceKey] = this.priceRangeMin
      this.filters[this.activeMaxPriceKey] = this.priceRangeMax
      if (this.monthlyRentRangeAvailable) {
        this.filters.minMonthlyRent = this.monthlyRentRangeMin
        this.filters.maxMonthlyRent = this.monthlyRentRangeMax
        this.lastMonthlyRentStackedThumb = 'max'
      }
      this.lastStackedThumb = 'max'
      if (this.hasSearched) {
        this.searchHouses(1)
      }
    },
    async fetchAllHouseSearchResults(searchFilters) {
      const firstRequests = buildHouseSearchRequests(searchFilters, {
        page: 1,
        size: SEARCH_ALL_FETCH_SIZE,
      })
      const firstResults = await Promise.all(firstRequests.map((fields) => this.fetchHouseSearch(fields)))
      const totalCount = firstResults.reduce((sum, payload) => {
        if (Number.isFinite(payload.totalCount)) {
          return sum + payload.totalCount
        }
        return sum + (Array.isArray(payload.items) ? payload.items.length : 0)
      }, 0)
      const totalPages = Math.max(Math.ceil(totalCount / SEARCH_ALL_FETCH_SIZE), 1)

      if (totalPages <= 1) {
        return firstResults
      }

      const restRequests = []
      for (let page = 2; page <= totalPages; page += 1) {
        buildHouseSearchRequests(searchFilters, {
          page,
          size: SEARCH_ALL_FETCH_SIZE,
        }).forEach((fields) => {
          restRequests.push({
            ...fields,
            autoImport: 'false',
          })
        })
      }

      const restResults = await Promise.all(restRequests.map((fields) => this.fetchHouseSearch(fields)))
      return [...firstResults, ...restResults]
    },
    async loadPriceRange() {
      if (!this.validateRegionForSearch()) {
        return
      }
      if (this.priceRangeLoadDisabled) {
        return
      }

      this.priceRangeLoading = true
      this.priceRangeError = ''

      try {
        const requests = buildHousePriceRangeRequests(this.filters)
        const results = await Promise.all(requests.map((fields) => this.fetchHousePriceRange(fields)))
        if (!this.setPriceRangeFromResults(results, { resetSelection: true })) {
          this.priceRangeError = '조건에 맞는 가격 범위가 없습니다.'
        }
      } catch (exception) {
        this.priceRangeError = exception instanceof Error
          ? exception.message
          : '가격 범위를 불러오지 못했습니다.'
      } finally {
        this.priceRangeLoading = false
      }
    },
    async searchFullPriceRange() {
      if (this.loading || this.priceRangeLoading) {
        return
      }
      if (!this.validateRegionForSearch()) {
        return
      }

      this.normalizeDealMonthRangeForSearch()
      if (!this.priceRangeAvailable) {
        await this.loadPriceRange()
      }

      if (this.priceRangeAvailable) {
        this.filters[this.activeMinPriceKey] = this.priceRangeMin
        this.filters[this.activeMaxPriceKey] = this.priceRangeMax
        this.lastStackedThumb = 'max'
      }
      if (this.monthlyRentRangeAvailable) {
        this.filters.minMonthlyRent = this.monthlyRentRangeMin
        this.filters.maxMonthlyRent = this.monthlyRentRangeMax
        this.lastMonthlyRentStackedThumb = 'max'
      }

      return this.searchHouses(1)
    },
    normalizeDealMonthRangeForSearch() {
      if (!this.invalidDealMonthRange) {
        this.dealMonthError = ''
        return
      }

      this.filters.startDealMonth = DEFAULT_DEAL_MONTH
      this.filters.endDealMonth = DEFAULT_DEAL_MONTH
      this.dealMonthError = '유효하지 않은 거래월 범위 입니다'
    },
    validateRegionForSearch() {
      if (!this.invalidRegionSelection) {
        this.regionError = ''
        return true
      }

      this.regionError = '서울특별시를 선택한 경우 시군구를 선택해 주세요.'
      return false
    },
    async requestMemberApi(path, options = {}) {
      const headers = {
        Accept: 'application/json',
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(options.headers || {}),
      }
      const response = await fetch(path, {
        ...options,
        headers,
        credentials: 'include',
      })
      const body = await response.json().catch(() => null)

      if (!response.ok || body?.success === false) {
        const error = new Error(this.apiErrorMessage(response.status, body?.message))
        error.status = response.status
        throw error
      }

      return body?.data ?? body ?? {}
    },
    apiErrorMessage(status, message) {
      if (message) {
        return message
      }
      if (status === 401) {
        return '로그인 후 이용할 수 있습니다.'
      }
      if (status === 403) {
        return '권한이 없습니다.'
      }
      if (status === 404) {
        return '요청한 정보를 찾을 수 없습니다.'
      }
      return `요청 처리에 실패했습니다. (${status})`
    },
    async requestNoticeApi(path, options = {}) {
      return this.requestMemberApi(path, options)
    },
    async requestInterestRegionApi(path, options = {}) {
      return this.requestMemberApi(path, options)
    },
    async loadInterestRegions({ silent = false } = {}) {
      if (!this.member) {
        this.interestRegions = []
        this.interestRegionLoading = false
        return
      }

      this.interestRegionLoading = true
      if (!silent) {
        this.interestRegionError = ''
        this.interestRegionMessage = ''
      }

      try {
        const regions = await this.requestInterestRegionApi('/api/interest-regions')
        this.interestRegions = Array.isArray(regions) ? regions : []
      } catch (exception) {
        this.interestRegions = []
        if (!silent) {
          this.interestRegionError = exception instanceof Error
            ? exception.message
            : '관심지역을 불러오지 못했습니다.'
        }
      } finally {
        this.interestRegionLoading = false
      }
    },
    async saveInterestRegion() {
      if (!this.member) {
        this.interestRegionError = '로그인 후 관심지역을 저장할 수 있습니다.'
        return
      }
      if (!this.selectedLawdCd || !this.filters.umdNm) {
        this.interestRegionError = '시군구와 읍면동을 선택해 주세요.'
        return
      }

      this.interestRegionLoading = true
      this.interestRegionError = ''
      this.interestRegionMessage = ''

      try {
        await this.requestInterestRegionApi('/api/interest-regions', {
          method: 'POST',
          body: JSON.stringify({
            lawdCd: this.selectedLawdCd,
            sido: this.filters.sido,
            sigungu: this.filters.sigungu,
            umdNm: this.filters.umdNm,
          }),
        })
        this.interestRegionMessage = `${this.selectedInterestRegionLabel}을 저장했습니다.`
        await this.loadInterestRegions({ silent: true })
      } catch (exception) {
        this.interestRegionError = exception instanceof Error
          ? exception.message
          : '관심지역 저장에 실패했습니다.'
      } finally {
        this.interestRegionLoading = false
      }
    },
    applyInterestRegion(region) {
      if (!region) {
        return
      }

      this.filters.sido = region.sido || ''
      this.filters.sigungu = region.sigungu || ''
      this.filters.umdNm = region.umdNm || ''
      this.searchRequestId += 1
      this.loading = false
      this.legalDongs = []
      this.legalDongError = ''
      this.loadLegalDongs(region.lawdCd || this.selectedLawdCd)
      this.interestRegionMessage = `${this.displayInterestRegion(region)}을 검색 조건에 적용했습니다.`
      this.interestRegionError = ''
    },
    async deleteInterestRegion(region) {
      if (!region?.interestRegionId) {
        return
      }

      this.interestRegionLoading = true
      this.interestRegionError = ''
      this.interestRegionMessage = ''

      try {
        await this.requestInterestRegionApi(`/api/interest-regions/${region.interestRegionId}`, {
          method: 'DELETE',
        })
        this.interestRegionMessage = `${this.displayInterestRegion(region)}을 삭제했습니다.`
        await this.loadInterestRegions({ silent: true })
      } catch (exception) {
        this.interestRegionError = exception instanceof Error
          ? exception.message
          : '관심지역 삭제에 실패했습니다.'
      } finally {
        this.interestRegionLoading = false
      }
    },
    displayInterestRegion(region) {
      return [region?.sido, region?.sigungu, region?.umdNm]
        .filter(Boolean)
        .map((value) => fieldText(value))
        .join(' ')
    },
    async loadNotices({ silent = false } = {}) {
      this.noticeLoading = true
      if (!silent) {
        this.noticeError = ''
      }

      try {
        const notices = await this.requestNoticeApi('/api/notices?limit=10')
        this.notices = Array.isArray(notices) ? notices : []
      } catch (exception) {
        this.notices = []
        if (!silent) {
          this.noticeError = exception instanceof Error
            ? exception.message
            : '공지사항을 불러오지 못했습니다.'
        }
      } finally {
        this.noticeLoading = false
      }
    },
    resetNoticeForm() {
      this.noticeEditingId = null
      this.noticeForm = {
        title: '',
        content: '',
      }
    },
    editNotice(notice) {
      this.noticeEditingId = notice.noticeId
      this.noticeForm = {
        title: notice.title || '',
        content: notice.content || '',
      }
      this.noticeMessage = ''
      this.noticeError = ''
    },
    async saveNotice() {
      if (!this.isNoticeAdmin) {
        this.noticeError = '관리자만 공지사항을 작성할 수 있습니다.'
        return
      }

      this.noticeLoading = true
      this.noticeError = ''
      this.noticeMessage = ''

      try {
        const path = this.noticeEditingId
          ? `/api/notices/${this.noticeEditingId}`
          : '/api/notices'
        await this.requestNoticeApi(path, {
          method: this.noticeEditingId ? 'PUT' : 'POST',
          body: JSON.stringify(this.noticeForm),
        })
        this.noticeMessage = this.noticeEditingId ? '공지사항이 수정되었습니다.' : '공지사항이 등록되었습니다.'
        this.resetNoticeForm()
        await this.loadNotices({ silent: false })
      } catch (exception) {
        this.noticeError = exception instanceof Error
          ? exception.message
          : '공지사항 저장에 실패했습니다.'
      } finally {
        this.noticeLoading = false
      }
    },
    async deleteNotice(notice) {
      if (!notice?.noticeId) {
        return
      }

      this.noticeLoading = true
      this.noticeError = ''
      this.noticeMessage = ''

      try {
        await this.requestNoticeApi(`/api/notices/${notice.noticeId}`, {
          method: 'DELETE',
        })
        if (this.noticeEditingId === notice.noticeId) {
          this.resetNoticeForm()
        }
        this.noticeMessage = '공지사항이 삭제되었습니다.'
        await this.loadNotices({ silent: false })
      } catch (exception) {
        this.noticeError = exception instanceof Error
          ? exception.message
          : '공지사항 삭제에 실패했습니다.'
      } finally {
        this.noticeLoading = false
      }
    },
    displayNoticeDate(notice) {
      const value = notice?.updatedAt || notice?.createdAt
      if (!value) {
        return '-'
      }

      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return fieldText(value)
      }

      return new Intl.DateTimeFormat('ko-KR', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      }).format(date)
    },
    async loadCurrentMember({ silent = true } = {}) {
      this.memberLoading = true
      if (!silent) {
        this.memberError = ''
        this.memberMessage = ''
      }

      try {
        const member = await this.requestMemberApi('/api/members/me')
        this.setCurrentMember(member)
        await this.loadInterestRegions({ silent: true })
      } catch (exception) {
        if (exception?.status === 401) {
          this.setCurrentMember(null)
          if (!silent) {
            this.memberMessage = '로그인 후 내 정보를 확인할 수 있습니다.'
          }
          return
        }

        if (!silent) {
          this.memberError = exception instanceof Error
            ? exception.message
            : '회원 정보를 불러오지 못했습니다.'
        }
      } finally {
        this.memberLoading = false
      }
    },
    setCurrentMember(member) {
      this.member = member
      this.profileForm = {
        name: member?.name || '',
        phone: member?.phone || '',
      }
      if (!member || !this.isNoticeAdmin) {
        this.clearMemberSearch()
      }
      if (!member) {
        this.interestRegions = []
        this.interestRegionMessage = ''
        this.interestRegionError = ''
        if (this.activePage === 'member-search') {
          this.activePage = 'search'
        }
      } else if (this.activePage === 'member-search' && !this.isNoticeAdmin) {
        this.activePage = 'search'
      }
    },
    openSearchPage() {
      this.activePage = 'search'
      this.noticeError = ''
      this.noticeMessage = ''
    },
    openNoticePage() {
      this.activePage = 'notice'
      this.noticeError = ''
      this.noticeMessage = ''
      this.loadNotices({ silent: true })
    },
    openMemberSearchPage() {
      if (!this.isNoticeAdmin) {
        this.memberError = '관리자만 회원 검색을 사용할 수 있습니다.'
        return
      }
      this.activePage = 'member-search'
      this.memberError = ''
      this.memberMessage = ''
    },
    openAccountPanel(mode = this.member ? 'profile' : 'login') {
      this.accountMode = mode
      this.activePage = 'account'
      this.accountPanelOpen = false
      this.profileEditing = false
      this.memberMessage = ''
      this.memberError = ''
      if (mode === 'profile') {
        this.loadCurrentMember({ silent: false })
      }
    },
    closeAccountPanel() {
      this.activePage = 'search'
      this.memberMessage = ''
      this.memberError = ''
      this.deleteConfirm = ''
      this.profileEditing = false
      this.clearMemberSearch()
    },
    switchAccountMode(mode) {
      this.accountMode = mode
      this.memberMessage = ''
      this.memberError = ''
      this.deleteConfirm = ''
      this.profileEditing = false
      this.clearMemberSearch()
      if (mode === 'profile') {
        this.loadCurrentMember({ silent: false })
      }
    },
    async signupMember() {
      this.memberLoading = true
      this.memberError = ''
      this.memberMessage = ''

      try {
        const member = await this.requestMemberApi('/api/members', {
          method: 'POST',
          body: JSON.stringify(this.signupForm),
        })
        this.loginForm.email = member.email || this.signupForm.email
        this.loginForm.password = ''
        this.signupForm = {
          email: '',
          password: '',
          name: '',
          phone: '',
        }
        this.accountMode = 'login'
        this.memberMessage = '회원가입이 완료되었습니다. 방금 만든 계정으로 로그인해 주세요.'
      } catch (exception) {
        this.memberError = exception instanceof Error
          ? exception.message
          : '회원가입에 실패했습니다.'
      } finally {
        this.memberLoading = false
      }
    },
    async loginMember() {
      this.memberLoading = true
      this.memberError = ''
      this.memberMessage = ''

      try {
        const member = await this.requestMemberApi('/api/auth/login', {
          method: 'POST',
          body: JSON.stringify(this.loginForm),
        })
        this.setCurrentMember(member)
        await this.loadInterestRegions({ silent: true })
        this.loginForm.password = ''
        this.accountMode = 'profile'
        this.memberMessage = '로그인되었습니다.'
        if (this.activePage === 'notice') {
          await this.loadNotices({ silent: true })
        }
      } catch (exception) {
        this.memberError = exception instanceof Error
          ? exception.message
          : '로그인에 실패했습니다.'
      } finally {
        this.memberLoading = false
      }
    },
    async resetPassword() {
      this.memberLoading = true
      this.memberError = ''
      this.memberMessage = ''

      try {
        await this.requestMemberApi('/api/auth/password-reset', {
          method: 'POST',
          body: JSON.stringify(this.passwordResetForm),
        })
        this.loginForm.email = this.passwordResetForm.email
        this.loginForm.password = ''
        this.passwordResetForm = {
          email: '',
          name: '',
          phone: '',
          newPassword: '',
        }
        this.accountMode = 'login'
        this.memberMessage = '비밀번호가 변경되었습니다. 새 비밀번호로 로그인해 주세요.'
      } catch (exception) {
        this.memberError = exception instanceof Error
          ? exception.message
          : '비밀번호 변경에 실패했습니다.'
      } finally {
        this.memberLoading = false
      }
    },
    async logoutMember() {
      this.memberLoading = true
      this.memberError = ''
      this.memberMessage = ''

      try {
        await this.requestMemberApi('/api/auth/logout', {
          method: 'POST',
        })
        this.setCurrentMember(null)
        this.resetNoticeForm()
        this.clearMemberSearch()
        this.profileEditing = false
        if (this.activePage === 'notice') {
          await this.loadNotices({ silent: true })
        }
        this.accountMode = 'login'
        this.memberMessage = '로그아웃되었습니다.'
      } catch (exception) {
        this.setCurrentMember(null)
        this.resetNoticeForm()
        this.clearMemberSearch()
        this.profileEditing = false
        if (this.activePage === 'notice') {
          await this.loadNotices({ silent: true })
        }
        this.accountMode = 'login'
        this.memberMessage = '로그인 상태를 정리했습니다.'
      } finally {
        this.memberLoading = false
      }
    },
    async updateMember() {
      this.memberLoading = true
      this.memberError = ''
      this.memberMessage = ''

      try {
        const member = await this.requestMemberApi('/api/members/me', {
          method: 'PUT',
          body: JSON.stringify(this.profileForm),
        })
        this.setCurrentMember(member)
        this.profileEditing = false
        this.memberMessage = '내 정보가 수정되었습니다.'
      } catch (exception) {
        this.memberError = exception instanceof Error
          ? exception.message
          : '내 정보 수정에 실패했습니다.'
      } finally {
        this.memberLoading = false
      }
    },
    startProfileEdit() {
      this.profileForm = {
        name: this.member?.name || '',
        phone: this.member?.phone || '',
      }
      this.memberMessage = ''
      this.memberError = ''
      this.profileEditing = true
    },
    cancelProfileEdit() {
      this.profileForm = {
        name: this.member?.name || '',
        phone: this.member?.phone || '',
      }
      this.profileEditing = false
      this.memberError = ''
    },
    async searchMembers() {
      if (!this.isNoticeAdmin) {
        this.memberError = '관리자만 회원 검색을 사용할 수 있습니다.'
        return
      }

      const keyword = this.memberSearchKeyword.trim()
      if (!keyword) {
        this.memberError = '검색어를 입력해 주세요.'
        return
      }

      this.memberLoading = true
      this.memberError = ''
      this.memberMessage = ''

      try {
        const params = new URLSearchParams({ keyword })
        const members = await this.requestMemberApi(`/api/members/search?${params.toString()}`)
        this.memberSearchResults = Array.isArray(members) ? members : []
        this.memberMessage = `${this.memberSearchResults.length.toLocaleString()}명의 회원을 찾았습니다.`
      } catch (exception) {
        this.memberError = exception instanceof Error
          ? exception.message
          : '회원 검색에 실패했습니다.'
      } finally {
        this.memberLoading = false
      }
    },
    clearMemberSearch() {
      this.memberSearchKeyword = ''
      this.memberSearchResults = []
    },
    async deleteMember() {
      if (this.deleteConfirm !== '삭제') {
        this.memberError = '회원 삭제를 진행하려면 확인을 위해 삭제를 입력해 주세요.'
        return
      }

      this.memberLoading = true
      this.memberError = ''
      this.memberMessage = ''

      try {
        await this.requestMemberApi('/api/members/me', {
          method: 'DELETE',
        })
        this.setCurrentMember(null)
        this.deleteConfirm = ''
        this.accountMode = 'login'
        this.memberMessage = '회원 정보가 삭제되었습니다.'
      } catch (exception) {
        this.memberError = exception instanceof Error
          ? exception.message
          : '회원 삭제에 실패했습니다.'
      } finally {
        this.memberLoading = false
      }
    },
    async searchHouses(page = 1) {
      if (!this.validateRegionForSearch()) {
        return
      }
      this.loading = false
      this.normalizeDealMonthRangeForSearch()
      const startedAt = Date.now()
      const requestId = this.searchRequestId + 1
      this.searchRequestId = requestId
      this.loading = true
      this.error = ''
      this.hasSearched = true
      this.selectedItem = null
      this.items = []
      this.totalCount = null
      this.searchPage = page
      this.clearMapMarkers()
      await this.$nextTick()
      await waitForPaint()

      try {
        const shouldIgnorePriceRange = this.invalidPriceRange || this.invalidMonthlyRentRange
        const searchFilters = shouldIgnorePriceRange
          ? {
              ...this.filters,
              minPrice: '',
              maxPrice: '',
              minDeposit: '',
              maxDeposit: '',
              minMonthlyRent: '',
              maxMonthlyRent: '',
            }
          : this.filters
        if (shouldIgnorePriceRange) {
          this.priceRangeError = '유효하지 않은 가격 범위 입니다'
        }
        const results = this.resultDisplayMode === 'all'
          ? await this.fetchAllHouseSearchResults(searchFilters)
          : await Promise.all(buildHouseSearchRequests(searchFilters, {
              page: this.searchPage,
              size: this.currentPageSize,
            }).map((fields) => this.fetchHouseSearch(fields)))
        if (requestId !== this.searchRequestId) {
          return
        }
        await keepSearchLoadingVisible(startedAt)
        if (requestId !== this.searchRequestId) {
          return
        }

        this.items = results.flatMap((payload) => Array.isArray(payload.items) ? payload.items : [])
        this.totalCount = this.resultDisplayMode === 'all'
          ? results.reduce((max, payload) => {
              if (Number.isFinite(payload.totalCount)) {
                return Math.max(max, payload.totalCount)
              }
              return max
            }, this.items.length)
          : results.reduce((sum, payload) => {
              if (Number.isFinite(payload.totalCount)) {
                return sum + payload.totalCount
              }

              return sum + (Array.isArray(payload.items) ? payload.items.length : 0)
            }, 0)
        this.setPriceRangeFromResults(results)
        if (shouldIgnorePriceRange) {
          this.priceRangeError = '유효하지 않은 가격 범위 입니다'
        }
        this.searchPanelCollapsed = this.items.length > 0
        this.loading = false
        this.$nextTick(() => {
          this.refreshMapMarkers()
        })
      } catch (exception) {
        if (requestId !== this.searchRequestId) {
          return
        }
        await keepSearchLoadingVisible(startedAt)
        if (requestId !== this.searchRequestId) {
          return
        }
        this.items = []
        this.totalCount = 0
        this.error = exception instanceof Error
          ? exception.message
          : '검색 중 오류가 발생했습니다.'
        this.clearMapMarkers()
      } finally {
        if (requestId === this.searchRequestId) {
          this.loading = false
        }
      }
    },
    async fetchHouseSearch(fields) {
      const params = new URLSearchParams()

      Object.entries(fields).forEach(([key, value]) => {
        if (value) {
          params.set(key, value)
        }
      })

      const query = params.toString()
      const response = await fetchWithTimeout(
        `/api/houses/search${query ? `?${query}` : ''}`,
        {},
        houseRequestTimeoutMs(fields)
      )
      const body = await response.json().catch(() => null)

      if (!response.ok || body?.success === false) {
        throw new Error(body?.message || `검색 요청 실패 (${response.status})`)
      }

      return body?.data ?? body ?? {}
    },
    async fetchHousePriceRange(fields) {
      const params = new URLSearchParams()

      Object.entries(fields).forEach(([key, value]) => {
        if (value) {
          params.set(key, value)
        }
      })

      const query = params.toString()
      const response = await fetchWithTimeout(
        `/api/houses/price-range${query ? `?${query}` : ''}`,
        {},
        houseRequestTimeoutMs(fields)
      )
      const body = await response.json().catch(() => null)

      if (!response.ok || body?.success === false) {
        throw new Error(body?.message || `가격 범위 요청 실패 (${response.status})`)
      }

      return body?.data ?? body ?? {}
    },
    handleSidoChange(event) {
      this.filters.sido = event?.target?.value ?? this.filters.sido
      this.regionError = ''
      this.searchRequestId += 1
      this.loading = false
      this.filters.umdNm = ''
      if (!this.filters.sido) {
        this.filters.sort = 'latest'
      }
      this.legalDongs = []
      this.legalDongError = ''
      if (!isSeoul(this.filters.sido)) {
        this.filters.sigungu = ''
      }
      this.loadLegalDongs(this.selectedLawdCd)
    },
    handleSigunguChange(event) {
      this.filters.sigungu = event?.target?.value ?? this.filters.sigungu
      this.regionError = ''
      this.searchRequestId += 1
      this.loading = false
      this.filters.umdNm = ''
      this.legalDongs = []
      this.loadLegalDongs()
    },
    // 에이전트(실행) 모드: ChatWidget이 받은 구조화 명령을 페이지 동작으로 실행한다.
    // 인식하는 필터 키만 적용하고, 적용 결과 요약(권위)을 위젯으로 돌려준다(서버 무상태).
    async handleAgentCommand(command) {
      try {
        if (!command || !command.action) {
          this.reportAgent('무엇을 도와드릴까요? 예: "강남구 2024년 5월 검색해줘"')
          return
        }
        switch (command.action) {
          case 'clarify':
            this.reportAgent(command.clarify || command.summary || '조금 더 자세히 알려주세요.')
            return
          case 'reset':
            this.resetSearch()
            this.reportAgent('검색 조건을 초기화했어요.')
            return
          case 'search':
          case 'setFilters': {
            const { applied, ignored } = applyAgentFilters(this.filters, command.filters || {})
            this.normalizeAgentFilters(applied)
            if (command.action === 'search') {
              await this.searchHouses(1)
            } else {
              // setFilters는 검색을 실행하지 않아 패널 자동 접힘(searchHouses) 로직을 타지 않는다.
              // 직전 검색으로 접혀 있으면 바뀐 조건이 가려지므로, 조건을 보여주도록 패널을 펼친다.
              this.searchPanelCollapsed = false
            }
            this.reportAgent(this.buildAgentSummary(applied, ignored, command.action))
            return
          }
          case 'paginate': {
            const decision = resolvePaginateTarget(command, {
              hasSearched: this.hasSearched,
              displayMode: this.resultDisplayMode,
              currentPage: this.searchPage,
              totalPages: this.totalPages,
            })
            if (!decision.ok) {
              this.reportAgent(decision.message)
              return
            }
            await this.searchHouses(decision.targetPage)
            this.reportAgent(`${decision.targetPage}페이지로 이동했어요.`)
            return
          }
          case 'mapFocus':
          case 'selectItem': {
            const itemCount = Array.isArray(this.items) ? this.items.length : 0
            const decision = resolveItemTarget(command.itemIndex, itemCount)
            if (!decision.ok) {
              this.reportAgent(decision.message)
              return
            }
            const item = this.items[decision.index]
            if (command.action === 'selectItem') {
              this.selectItem(item)
              this.reportAgent(`${command.itemIndex}번째 매물(${this.displayAptName(item)})을 선택했어요.`)
              return
            }
            // mapFocus: 선택 없이 지도만 이동. 지도 미준비 시 graceful 안내.
            if (!this.map || !this.kakao) {
              this.reportAgent('지도가 준비되면 다시 시도해 주세요.')
              return
            }
            this.focusMapItem(item)
            this.reportAgent(`지도를 ${command.itemIndex}번째 매물로 옮겼어요.`)
            return
          }
          default:
            this.reportAgent('아직 지원하지 않는 동작이에요. 검색·조건설정·초기화만 도와드릴 수 있어요.')
        }
      } catch (exception) {
        this.reportAgent('요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.')
      }
    },
    // 앱은 서울 전용이고 필터 간 의존성이 있어, 에이전트가 채운 필터에 동일한 side-effect를 반영한다.
    normalizeAgentFilters(applied) {
      // 자치구가 지정되면 시도를 서울로 보정해야 lawdCd 해석/정렬이 동작한다.
      if ('sigungu' in applied && this.filters.sigungu && !isSeoul(this.filters.sido)) {
        this.filters.sido = '서울특별시'
      }
      // 시도/시군구가 바뀌면 읍면동 목록을 갱신한다(동을 직접 지정하지 않았으면 초기화).
      if ('sido' in applied || 'sigungu' in applied) {
        if (!('umdNm' in applied)) {
          this.filters.umdNm = ''
        }
        this.searchRequestId += 1
        this.loadLegalDongs(this.selectedLawdCd)
      }
      // 시도가 비면 정렬 기본값 복원(handleSidoChange와 동일 규칙).
      if (!this.filters.sido) {
        this.filters.sort = 'latest'
      }
    },
    buildAgentSummary(applied, ignored, action) {
      const parts = []
      if (applied.sigungu) {
        parts.push(applied.sigungu)
      } else if (applied.sido) {
        parts.push(applied.sido)
      }
      if (applied.umdNm) {
        parts.push(applied.umdNm)
      }
      if (applied.aptName) {
        parts.push(applied.aptName)
      }
      const month = this.describeAgentMonth(applied)
      if (month) {
        parts.push(month)
      }
      const price = this.describeAgentPrice(applied)
      if (price) {
        parts.push(price)
      }
      // 정렬은 normalize 이후의 '실효값'을 본다(지역이 없으면 latest로 다운그레이드되므로 거짓 주장 방지).
      const sortLabel = this.describeAgentSort(applied)
      if (sortLabel) {
        parts.push(sortLabel)
      }
      const condition = parts.length ? parts.join('·') : '입력하신 조건'
      let summary = action === 'search'
        ? `${condition}로 검색했어요.`
        : `${condition}로 검색 조건을 설정했어요.`
      if (ignored && ignored.length) {
        summary += ` (${ignored.join(', ')} 항목은 지원하지 않아 반영하지 못했어요.)`
      }
      return summary
    },
    describeAgentMonth(applied) {
      const start = applied.startDealMonth
      const end = applied.endDealMonth
      if (start && end) {
        return start === end ? start : `${start}~${end}`
      }
      return start || end || ''
    },
    describeAgentPrice(applied) {
      const hasMin = applied.minPrice !== undefined && applied.minPrice !== ''
      const hasMax = applied.maxPrice !== undefined && applied.maxPrice !== ''
      if (hasMin && hasMax) {
        return `${this.displayManwon(applied.minPrice)} ~ ${this.displayManwon(applied.maxPrice)}`
      }
      if (hasMin) {
        return `${this.displayManwon(applied.minPrice)} 이상`
      }
      if (hasMax) {
        return `${this.displayManwon(applied.maxPrice)} 이하`
      }
      return ''
    },
    describeAgentSort(applied) {
      // 에이전트가 정렬을 요청했고(applied.sort), 지역이 있어 실제 적용된 경우에만 표기한다.
      if (!('sort' in applied) || !this.filters.sido) {
        return ''
      }
      const effective = this.filters.sort
      if (!effective || effective === 'latest') {
        return ''
      }
      return SORT_OPTIONS.find((option) => option.value === effective)?.label || ''
    },
    reportAgent(text) {
      this.agentSeq += 1
      this.agentResult = { text, seq: this.agentSeq }
    },
    async loadLegalDongs(lawdCd = this.selectedLawdCd) {
      const requestId = this.legalDongRequestId + 1
      this.legalDongRequestId = requestId
      this.legalDongError = ''
      this.legalDongs = []

      if (!lawdCd) {
        this.legalDongLoading = false
        return
      }

      this.legalDongLoading = true
      try {
        const response = await fetchWithTimeout(
          `/api/regions?lawdCd=${encodeURIComponent(lawdCd)}`,
          {},
          REGION_REQUEST_TIMEOUT_MS
        )
        const body = await response.json().catch(() => null)
        if (requestId !== this.legalDongRequestId || lawdCd !== this.selectedLawdCd) {
          return
        }

        if (!response.ok || body?.success === false) {
          throw new Error(body?.message || `동 목록 요청 실패 (${response.status})`)
        }

        this.legalDongs = this.normalizeLegalDongs(Array.isArray(body?.data) ? body.data : [])
      } catch (exception) {
        if (requestId !== this.legalDongRequestId || lawdCd !== this.selectedLawdCd) {
          return
        }
        this.legalDongs = []
        this.legalDongError = exception instanceof Error
          ? exception.message
          : '동 목록을 불러오지 못했습니다.'
      } finally {
        if (requestId === this.legalDongRequestId) {
          this.legalDongLoading = false
        }
      }
    },
    normalizeLegalDongs(regions) {
      const dongsByLabel = new Map()

      regions.forEach((region) => {
        const rawValue = typeof region?.umdNm === 'string' ? region.umdNm.trim() : ''
        const label = fieldText(rawValue, '').trim()
        if (!rawValue || !label) {
          return
        }

        const previous = dongsByLabel.get(label)
        if (!previous || previous.value !== previous.label && rawValue === label) {
          dongsByLabel.set(label, { label, value: rawValue })
        }
      })

      return Array.from(dongsByLabel.values())
        .sort((first, second) => first.label.localeCompare(second.label, 'ko'))
    },
    resetSearch() {
      this.searchRequestId += 1
      this.legalDongRequestId += 1
      this.filters = initialFilters()
      this.legalDongs = []
      this.legalDongLoading = false
      this.legalDongError = ''
      this.items = []
      this.totalCount = null
      this.priceRangeMin = null
      this.priceRangeMax = null
      this.monthlyRentRangeMin = null
      this.monthlyRentRangeMax = null
      this.priceRangeLoading = false
      this.priceRangeError = ''
      this.regionError = ''
      this.dealMonthError = ''
      this.lastStackedThumb = 'max'
      this.lastMonthlyRentStackedThumb = 'max'
      this.searchPage = 1
      this.searchPanelCollapsed = false
      this.selectedItem = null
      this.loading = false
      this.error = ''
      this.hasSearched = false
      this.clearMapMarkers()
    },
    selectItem(item) {
      this.selectedItem = item
      this.$nextTick(() => {
        this.updateSelectedMarker()
        this.focusMapItem(item)
      })
    },
    backToList() {
      this.selectedItem = null
      this.updateSelectedMarker()
    },
    searchFirstPage(event) {
      event?.preventDefault?.()
      return this.searchHouses(1)
    },
    goPreviousPage() {
      if (this.canGoPreviousPage) {
        return this.searchHouses(this.searchPage - 1)
      }
    },
    goNextPage() {
      if (this.canGoNextPage) {
        return this.searchHouses(this.searchPage + 1)
      }
    },
    displayAptName(item) {
      return fieldText(item?.aptNm, '아파트명 없음')
    },
    displayAddress(item) {
      if (item?.roadnm) {
        return fieldText(item.roadnm)
      }
      const dong = fieldText(item?.umdNm, '')
      const jibun = fieldText(item?.jibun, '')
      const address = [dong, jibun].filter(Boolean).join(' ')
      return address || '-'
    },
    displayDealAmount(item) {
      if (item?.dealType === 'jeonse' || item?.dealType === 'monthly') {
        const deposit = this.displayManwon(item?.depositManwon ?? String(item?.deposit ?? '').replace(/,/g, ''))
        if (item?.dealType === 'monthly') {
          return `보증금 ${deposit} / 월세 ${this.displayManwon(item?.monthlyRentManwon ?? item?.monthlyRent)}`
        }
        return `보증금 ${deposit}`
      }
      const amount = Number(item?.dealAmountManwon ?? String(item?.dealAmount ?? '').replace(/,/g, ''))
      return this.displayKoreanPrice(amount)
    },
    displayDealType(item) {
      if (item?.dealType === 'jeonse') {
        return '전세'
      }
      if (item?.dealType === 'monthly') {
        return '월세'
      }
      return '매매'
    },
    displayManwon(value) {
      const numeric = Number(value)
      if (!Number.isFinite(numeric)) {
        return '-'
      }
      return this.displayKoreanPrice(numeric)
    },
    displayKoreanPrice(value) {
      const numeric = Number(value)
      if (!Number.isFinite(numeric)) {
        return '-'
      }

      const manwon = Math.trunc(numeric)
      if (manwon < 10000) {
        return `${manwon.toLocaleString()}만`
      }

      const eok = Math.floor(manwon / 10000)
      const remainder = manwon % 10000
      const parts = []

      parts.push(`${eok.toLocaleString()}억`)
      if (remainder > 0) {
        parts.push(`${remainder.toLocaleString()}만`)
      }

      return parts.join(' ')
    },
    displayArea(item) {
      return item?.excluUseAr ? `${item.excluUseAr}㎡` : '-'
    },
    displayFloor(item) {
      return item?.floor || item?.floor === 0 ? `${item.floor}층` : '-'
    },
    displayDealDate(item) {
      if (item?.dealDate) {
        return item.dealDate
      }

      if (item?.dealYmd && item.dealYmd.length === 6) {
        return `${item.dealYmd.slice(0, 4)}-${item.dealYmd.slice(4, 6)}`
      }

      return '-'
    },
    displayBuildYear(item) {
      return item?.buildYear ? `${item.buildYear}년식` : '-'
    },
    displayRegion(item) {
      const sido = fieldText(item?.sido, '')
      const sigungu = fieldText(item?.sigungu, '')
      return [sido, sigungu].filter(Boolean).join(' ') || '-'
    },
    displayCoordinateState(item) {
      if (item?.lat && item?.lng) {
        return `${item.lat}, ${item.lng}`
      }

      return '주소 기반 검색'
    },
    itemKey(item) {
      return item?.resultKey ?? item?.apiRowHash ?? item?.dealId ?? `${item?.houseId ?? 'house'}-${item?.dealDate ?? 'date'}-${item?.floor ?? 'floor'}`
    },
    mapAddress(item) {
      if (item?.roadnm) {
        return [
          fieldText(item?.sido, ''),
          fieldText(item?.sigungu, ''),
          fieldText(item?.roadnm, ''),
        ].filter(Boolean).join(' ')
      }
      return [
        fieldText(item?.sido, ''),
        fieldText(item?.sigungu, ''),
        fieldText(item?.umdNm, ''),
        fieldText(item?.jibun, ''),
      ].filter(Boolean).join(' ')
    },
    async prepareMap() {
      this.mapLoading = true
      this.mapError = ''

      try {
        const kakao = await loadKakaoMapsSdk()
        this.kakao = kakao
        this.mapReady = true
        this.mapStatus = '검색 결과가 지도에 표시됩니다.'
        this.$nextTick(() => {
          this.ensureMap()
          this.refreshMapMarkers()
        })
      } catch (exception) {
        this.mapReady = false
        this.mapError = exception instanceof Error
          ? exception.message
          : 'Kakao Map을 불러오지 못했습니다.'
        this.mapStatus = '지도 설정이 필요합니다.'
      } finally {
        this.mapLoading = false
      }
    },
    ensureMap(force = false) {
      if (!this.kakao || !this.$refs.mapCanvas) {
        return
      }

      if (force || !this.map || !this.$refs.mapCanvas.children.length) {
        this.$refs.mapCanvas.innerHTML = ''
        this.map = null
      }

      if (this.map) {
        return
      }

      const center = new this.kakao.maps.LatLng(DEFAULT_MAP_CENTER.lat, DEFAULT_MAP_CENTER.lng)
      this.map = new this.kakao.maps.Map(this.$refs.mapCanvas, {
        center,
        level: 6,
      })
      this.relayoutMap()
    },
    relayoutMap() {
      if (this.map && typeof this.map.relayout === 'function') {
        this.map.relayout()
      }
    },
    clearMapMarkers() {
      this.mapMarkers.forEach((marker) => marker.setMap(null))
      this.mapMarkers = []
      this.mapMarkerItems = []
      this.geocodedMapItems = []
      this.markerDisplayCount = 0
      if (this.hasSearched && this.mapReady) {
        this.mapStatus = '현재 페이지에서 표시할 마커가 없습니다.'
      }
    },
    async refreshMapMarkers() {
      this.clearMapMarkers()

      if (!this.items.length) {
        return
      }

      if (!this.mapReady || !this.kakao) {
        if (!KAKAO_MAP_API_KEY) {
          this.mapStatus = 'VITE_KAKAO_MAP_API_KEY 설정 후 지도를 사용할 수 있습니다.'
        }
        return
      }

      this.mapLoading = true
      this.mapError = ''
      this.mapStatus = '현재 페이지 주소를 좌표로 변환하고 있습니다.'

      try {
        const geocoder = new this.kakao.maps.services.Geocoder()
        const geocodedItems = await Promise.all(this.items.map((item) => this.geocodeItem(geocoder, item)))
        const successfulItems = geocodedItems.filter(Boolean)

        this.geocodedMapItems = successfulItems
        this.markerDisplayCount = successfulItems.length
        this.mapStatus = successfulItems.length
          ? `현재 페이지 ${successfulItems.length}개 주소를 지도에 표시했습니다.`
          : '현재 페이지 주소를 지도에서 찾지 못했습니다.'
        this.mapLoading = false
        await this.$nextTick()
        this.renderMapMarkers(successfulItems)
      } catch (exception) {
        this.mapError = exception instanceof Error
          ? exception.message
          : '지도 마커 표시 중 오류가 발생했습니다.'
        this.mapLoading = false
      }
    },
    renderMapMarkers(successfulItems = this.geocodedMapItems) {
      if (!this.mapReady || !this.kakao || !this.$refs.mapCanvas) {
        return
      }

      this.mapMarkers.forEach((marker) => marker.setMap(null))
      this.mapMarkers = []
      this.mapMarkerItems = []
      this.ensureMap(true)
      if (!this.map) {
        return
      }

      if (!this.selectedMarkerImage) {
        this.selectedMarkerImage = new this.kakao.maps.MarkerImage(
          SELECTED_MARKER_IMAGE_URL,
          new this.kakao.maps.Size(42, 52),
          { offset: new this.kakao.maps.Point(21, 52) }
        )
      }

      const bounds = new this.kakao.maps.LatLngBounds()
      successfulItems.forEach(({ item, position }) => {
        const marker = new this.kakao.maps.Marker({
          map: this.map,
          position,
        })
        if (!this.defaultMarkerImage && typeof marker.getImage === 'function') {
          this.defaultMarkerImage = marker.getImage()
        }
        this.kakao.maps.event.addListener(marker, 'click', () => {
          this.selectItem(item)
        })
        bounds.extend(position)
        this.mapMarkers.push(marker)
        this.mapMarkerItems.push(item)
      })
      this.updateSelectedMarker()

      if (this.mapMarkers.length > 1) {
        this.relayoutMap()
        this.map.setBounds(bounds)
      } else if (this.mapMarkers.length === 1) {
        this.relayoutMap()
        this.map.setCenter(this.mapMarkers[0].getPosition())
        this.map.setLevel(4)
      } else {
        this.relayoutMap()
      }
    },
    updateSelectedMarker() {
      if (!this.kakao || !this.selectedMarkerImage || !this.mapMarkers.length) {
        return
      }

      const selectedKey = this.selectedItem ? this.itemKey(this.selectedItem) : ''
      this.mapMarkers.forEach((marker, index) => {
        const isSelected = selectedKey && this.itemKey(this.mapMarkerItems[index]) === selectedKey
        marker.setImage(isSelected ? this.selectedMarkerImage : this.defaultMarkerImage)
        marker.setZIndex(isSelected ? 10 : 1)
      })
    },
    geocodeItem(geocoder, item) {
      const address = this.mapAddress(item)
      if (!address) {
        return Promise.resolve(null)
      }

      return new Promise((resolve) => {
        geocoder.addressSearch(address, (result, status) => {
          if (status !== this.kakao.maps.services.Status.OK || !result?.[0]) {
            resolve(null)
            return
          }

          resolve({
            item,
            position: new this.kakao.maps.LatLng(Number(result[0].y), Number(result[0].x)),
          })
        })
      })
    },
    focusMapItem(item) {
      if (!this.map || !this.kakao) {
        return
      }

      const index = this.mapMarkerItems.findIndex((markerItem) => this.itemKey(markerItem) === this.itemKey(item))
      if (index < 0) {
        return
      }

      const marker = this.mapMarkers[index]
      this.map.setCenter(marker.getPosition())
      this.map.setLevel(Math.min(this.map.getLevel(), 4))
    },
  },
}
</script>

<template>
  <div class="app-shell">
    <header class="top-bar">
      <div class="brand">
        <button class="brand-mark" type="button" aria-label="검색 화면" @click="openSearchPage">
          <img src="/nohome-logo.png" alt="" />
        </button>
        <div>
          <p class="app-kicker">SSAFY Home</p>
          <h1>NoHome 실거래가 검색</h1>
        </div>
      </div>
      <nav class="top-nav" aria-label="주요 화면">
        <button class="nav-tab icon-tab" type="button" :class="{ 'is-active': activePage === 'notice' }" aria-label="공지사항" title="공지사항" @click="openNoticePage">📢</button>
        <button v-if="isNoticeAdmin" class="nav-tab" type="button" :class="{ 'is-active': activePage === 'member-search' }" @click="openMemberSearchPage">회원 검색</button>
      </nav>
      <div class="account-actions" aria-label="회원 메뉴">
        <span class="account-summary">{{ accountSummary }}</span>
        <template v-if="member">
          <button class="account-button" type="button" @click="openAccountPanel('profile')">내 정보</button>
          <button class="secondary-button compact-button" type="button" :disabled="memberLoading" @click="logoutMember">로그아웃</button>
        </template>
        <template v-else>
          <button class="account-button" type="button" @click="openAccountPanel('login')">로그인</button>
        </template>
      </div>
    </header>

    <main v-if="activePage === 'search'" class="workspace" aria-label="주택 실거래가 검색 화면">
      <section class="left-panel" aria-label="검색과 결과">
        <form class="search-panel" :class="{ 'is-collapsed': searchPanelCollapsed }" novalidate @submit.prevent="searchFirstPage">
          <div class="panel-heading">
            <div>
              <p class="section-kicker">Search</p>
              <h2>주택 검색</h2>
            </div>
            <div class="search-panel-controls">
              <span class="result-count">{{ visibleCountLabel }}</span>
            </div>
          </div>

          <div class="filter-summary" aria-label="현재 검색 조건 요약">
            <span v-for="part in activeFilterSummary" :key="part">{{ part }}</span>
          </div>

            <div id="search-panel-body" class="search-panel-body">
              <div class="deal-mode-tabs" role="radiogroup" aria-label="거래 유형">
                <button
                  v-for="option in dealModeOptions"
                  :key="option.value"
                  type="button"
                  class="deal-mode-button"
                  :class="{ 'is-active': filters.dealMode === option.value }"
                  :aria-pressed="filters.dealMode === option.value ? 'true' : 'false'"
                  @click="filters.dealMode = option.value; handleDealModeChange()"
                >
                  {{ option.label }}
                </button>
              </div>

              <div class="field-grid">
                <label><span>시도</span><select v-model="filters.sido" name="sido" @change="handleSidoChange"><option value="">전체/선택 안 함</option><option value="서울특별시">서울특별시</option></select></label>
                <label><span>시군구</span><select v-model="filters.sigungu" name="sigungu" :disabled="!isSeoul(filters.sido)" @change="handleSigunguChange"><option value="" disabled>시군구 선택</option><option v-for="district in seoulDistricts" :key="district" :value="district">{{ district }}</option></select></label>
                <label><span>읍면동</span><select :key="selectedLawdCd || 'no-lawd'" v-model="filters.umdNm" name="umdNm" :disabled="legalDongDisabled"><option value="">{{ legalDongLoading ? '동 목록 불러오는 중' : '전체 동' }}</option><option v-for="dong in legalDongs" :key="dong.value" :value="dong.value">{{ dong.label }}</option></select></label>
                <label><span>아파트명</span><input v-model.trim="filters.aptName" name="aptName" type="text" autocomplete="off" placeholder="래미안" /></label>
                <label><span>시작월</span><input v-model="filters.startDealMonth" name="startDealMonth" type="month" /></label>
                <label><span>종료월</span><input v-model="filters.endDealMonth" name="endDealMonth" type="month" /></label>
                <label><span>정렬</span><select v-model="filters.sort" name="sort" :disabled="sortDisabled"><option v-for="option in activeSortOptions" :key="option.value" :value="option.value">{{ option.label }}</option></select></label>
                <label><span>표시 방식</span><select v-model="resultDisplayMode" name="resultDisplayMode"><option value="5">5개씩 보기</option><option value="10">10개씩 보기</option><option value="20">20개씩 보기</option><option value="all">전체 보기</option></select></label>
              </div>
              <div v-if="priceFilterVisible" class="price-filter" :class="{ 'is-disabled': !priceRangeAvailable }">
                <template v-if="filters.dealMode === 'monthly'">
                  <div class="price-filter-header">
                    <span>월세 구간</span>
                    <strong>{{ monthlyRentRangeSummary }}</strong>
                  </div>
                  <div class="price-input-grid">
                    <label><span>최소 월세</span><input v-model="filters.minMonthlyRent" type="number" inputmode="numeric" min="0" step="10" :disabled="!monthlyRentRangeAvailable" /></label>
                    <label><span>최대 월세</span><input v-model="filters.maxMonthlyRent" type="number" inputmode="numeric" min="0" step="10" :disabled="!monthlyRentRangeAvailable" /></label>
                  </div>
                  <div class="range-control" :style="{ '--range-min': `${monthlyRentMinPercent}%`, '--range-max': `${monthlyRentMaxPercent}%` }">
                    <div class="range-track" aria-hidden="true"></div>
                    <input class="range-thumb" :class="{ 'is-stacked': lastMonthlyRentStackedThumb === 'min' }" :value="filters.minMonthlyRent" type="range" :min="monthlyRentRangeMin ?? 0" :max="monthlyRentRangeMax ?? 0" step="10" :disabled="!monthlyRentRangeAvailable" aria-label="최소 월세" @pointerdown="handleMonthlyRentThumbPointerDown('min')" @input="handleMonthlyRentThumbInput('min', $event)" />
                    <input class="range-thumb" :class="{ 'is-stacked': lastMonthlyRentStackedThumb === 'max' }" :value="filters.maxMonthlyRent" type="range" :min="monthlyRentRangeMin ?? 0" :max="monthlyRentRangeMax ?? 0" step="10" :disabled="!monthlyRentRangeAvailable" aria-label="최대 월세" @pointerdown="handleMonthlyRentThumbPointerDown('max')" @input="handleMonthlyRentThumbInput('max', $event)" />
                  </div>
                  <div class="price-filter-header sub-filter-header">
                    <span>보증금 구간</span>
                    <strong>{{ priceRangeSummary }}</strong>
                  </div>
                  <div class="price-input-grid">
                    <label><span>최소 보증금</span><input :value="filters[activeMinPriceKey]" type="number" inputmode="numeric" min="0" step="100" :disabled="!priceRangeAvailable" @input="handleMinPriceInput" /></label>
                    <label><span>최대 보증금</span><input :value="filters[activeMaxPriceKey]" type="number" inputmode="numeric" min="0" step="100" :disabled="!priceRangeAvailable" @input="handleMaxPriceInput" /></label>
                  </div>
                  <div class="range-control" :style="{ '--range-min': `${priceMinPercent}%`, '--range-max': `${priceMaxPercent}%` }">
                    <div class="range-track" aria-hidden="true"></div>
                    <input class="range-thumb" :class="{ 'is-stacked': lastStackedThumb === 'min' }" :value="filters[activeMinPriceKey]" type="range" :min="priceRangeMin ?? 0" :max="priceRangeMax ?? 0" :step="priceSliderStep" :disabled="!priceRangeAvailable" aria-label="최소 보증금" @pointerdown="handlePriceThumbPointerDown('min')" @input="handlePriceThumbInput('min', $event)" />
                    <input class="range-thumb" :class="{ 'is-stacked': lastStackedThumb === 'max' }" :value="filters[activeMaxPriceKey]" type="range" :min="priceRangeMin ?? 0" :max="priceRangeMax ?? 0" :step="priceSliderStep" :disabled="!priceRangeAvailable" aria-label="최대 보증금" @pointerdown="handlePriceThumbPointerDown('max')" @input="handlePriceThumbInput('max', $event)" />
                  </div>
                </template>
                <template v-else>
                  <div class="price-filter-header">
                    <span>{{ priceFilterTitle }}</span>
                    <strong>{{ priceRangeSummary }}</strong>
                  </div>
                  <div class="price-input-grid">
                    <label><span>{{ filters.dealMode === 'sale' ? '최소 실거래가' : '최소 보증금' }}</span><input :value="filters[activeMinPriceKey]" type="number" inputmode="numeric" min="0" step="100" :disabled="!priceRangeAvailable" @input="handleMinPriceInput" /></label>
                    <label><span>{{ filters.dealMode === 'sale' ? '최대 실거래가' : '최대 보증금' }}</span><input :value="filters[activeMaxPriceKey]" type="number" inputmode="numeric" min="0" step="100" :disabled="!priceRangeAvailable" @input="handleMaxPriceInput" /></label>
                  </div>
                  <div class="range-control" :style="{ '--range-min': `${priceMinPercent}%`, '--range-max': `${priceMaxPercent}%` }">
                    <div class="range-track" aria-hidden="true"></div>
                    <input class="range-thumb" :class="{ 'is-stacked': lastStackedThumb === 'min' }" :value="filters[activeMinPriceKey]" type="range" :min="priceRangeMin ?? 0" :max="priceRangeMax ?? 0" :step="priceSliderStep" :disabled="!priceRangeAvailable" aria-label="최소 가격" @pointerdown="handlePriceThumbPointerDown('min')" @input="handlePriceThumbInput('min', $event)" />
                    <input class="range-thumb" :class="{ 'is-stacked': lastStackedThumb === 'max' }" :value="filters[activeMaxPriceKey]" type="range" :min="priceRangeMin ?? 0" :max="priceRangeMax ?? 0" :step="priceSliderStep" :disabled="!priceRangeAvailable" aria-label="최대 가격" @pointerdown="handlePriceThumbPointerDown('max')" @input="handlePriceThumbInput('max', $event)" />
                  </div>
                </template>
                <div class="price-filter-actions">
                  <button class="secondary-button compact-button" type="button" :disabled="priceRangeLoadDisabled" @click="searchFullPriceRange">{{ priceRangeLoading ? '조회 중' : '전체 조회' }}</button>
                  <button class="primary-button compact-button price-filter-search-action" type="button" :disabled="loading || invalidPriceRange || invalidMonthlyRentRange" @click="searchFirstPage">검색</button>
                </div>
              </div>
              <p v-if="regionError" class="inline-error">{{ regionError }}</p>
              <p v-if="legalDongError" class="inline-error">{{ legalDongError }}</p>
              <p v-if="dealMonthError" class="inline-error">{{ dealMonthError }}</p>

              <div class="actions">
                <button class="primary-button" :class="{ 'is-loading': loading }" type="button" :aria-busy="loading ? 'true' : 'false'" @click="searchFirstPage">
                  <span v-if="loading" class="button-spinner" aria-hidden="true"></span>
                  <span>{{ loading ? '조회중' : '검색' }}</span>
                </button>
                <button class="secondary-button" type="button" @click="resetSearch">초기화</button>
              </div>
            </div>
        </form>

        <div class="list-panel" aria-live="polite">
          <button
            v-if="hasSearched"
            class="list-filter-toggle"
            :class="{ 'is-collapsed': searchPanelCollapsed }"
            type="button"
            :aria-expanded="searchPanelCollapsed ? 'false' : 'true'"
            aria-controls="search-panel-body"
            :aria-label="searchPanelCollapsed ? '검색 조건 펼치기' : '검색 조건 접기'"
            :title="searchPanelCollapsed ? '검색 조건 펼치기' : '검색 조건 접기'"
            @click="toggleSearchPanel"
          >
            <span class="filter-handle-icon" aria-hidden="true">
              <span></span>
              <span></span>
              <span></span>
            </span>
          </button>
          <div v-if="hasSearched" class="pagination-bar">
            <div><strong>{{ pageSummary }}</strong><span>{{ resultMetaLabel }}</span></div>
            <div class="pagination-actions">
              <button class="secondary-button compact-button" type="button" :disabled="!canGoPreviousPage" @click="goPreviousPage">이전</button>
              <button class="secondary-button compact-button" type="button" :disabled="!canGoNextPage" @click="goNextPage">다음</button>
            </div>
          </div>
          <div v-if="hasSearched || mapReady || mapError" class="map-status-strip" :class="{ 'is-error': mapError }">
            <span>지도</span>
            <strong>{{ mapStatusLabel }}</strong>
          </div>

          <div v-if="loading && items.length === 0" class="state-box loading-state"><strong>검색 중입니다.</strong><span>조건에 맞는 실거래가 목록을 불러오고 있습니다.</span><span class="loading-dots" aria-hidden="true"><i></i><i></i><i></i></span></div>
          <div v-else-if="error" class="state-box danger-state"><strong>검색에 실패했습니다.</strong><span>{{ error }}</span></div>
          <div v-else-if="hasSearched && items.length === 0" class="state-box"><strong>검색 결과가 없습니다.</strong><span>지역명, 아파트명, 거래월 조건을 조정해 다시 검색해 주세요.</span></div>
          <div v-else-if="!hasSearched" class="state-box"><strong>검색 조건을 입력해 주세요.</strong><span>백엔드가 실행 중이 아니어도 화면 구조는 유지됩니다.</span></div>

          <ul v-else class="result-list">
            <li v-for="item in items" :key="itemKey(item)">
              <button type="button" class="result-card" :class="{ 'is-selected': selectedItem && itemKey(selectedItem) === itemKey(item) }" @click="selectItem(item)">
                <span class="item-card-header">
                  <span class="item-title"><span class="deal-type-badge">{{ displayDealType(item) }}</span>{{ displayAptName(item) }}</span>
                  <strong class="item-price">{{ displayDealAmount(item) }}</strong>
                </span>
                <span class="item-address">{{ displayRegion(item) }} {{ displayAddress(item) }}</span>
                <span class="item-date">{{ displayDealDate(item) }}</span>
                <span class="item-meta-grid">
                  <span>{{ displayArea(item) }}</span>
                  <span>{{ displayFloor(item) }}</span>
                  <span>{{ displayBuildYear(item) }}</span>
                  <span>{{ fieldText(item?.umdNm) }}</span>
                </span>
              </button>
            </li>
          </ul>
        </div>
      </section>

      <aside class="map-panel" aria-label="지도 영역">
        <div class="map-surface">
          <div ref="mapCanvas" v-once class="map-canvas" aria-label="Kakao 지도"></div>
          <div v-if="!mapReady" class="map-grid" aria-hidden="true"></div>
          <div v-if="!mapReady || mapError" class="map-overlay-state" :class="{ 'is-error': mapError }">
            <strong>{{ mapError ? '지도를 불러오지 못했습니다.' : '지도 준비 중입니다.' }}</strong>
            <span>{{ mapStatusLabel }}</span>
          </div>
          <div v-if="selectedItem" class="map-detail-panel">
            <button class="map-detail-close" type="button" aria-label="상세 닫기" @click="backToList">×</button>
            <strong>{{ displayAptName(selectedItem) }}</strong>
            <span class="map-detail-price">{{ displayDealAmount(selectedItem) }}</span>
            <dl>
              <div><dt>거래일</dt><dd>{{ displayDealDate(selectedItem) }}</dd></div>
              <div><dt>주소</dt><dd>{{ displayRegion(selectedItem) }} {{ displayAddress(selectedItem) }}</dd></div>
              <div><dt>전용면적</dt><dd>{{ displayArea(selectedItem) }}</dd></div>
              <div><dt>층</dt><dd>{{ displayFloor(selectedItem) }}</dd></div>
              <div><dt>건축연도</dt><dd>{{ displayBuildYear(selectedItem) }}</dd></div>
              <template v-if="selectedItem?.dealType === 'jeonse' || selectedItem?.dealType === 'monthly'">
                <div><dt>거래유형</dt><dd>{{ displayDealType(selectedItem) }}</dd></div>
                <div><dt>계약기간</dt><dd>{{ fieldText(selectedItem?.contractTerm) }}</dd></div>
                <div><dt>계약구분</dt><dd>{{ fieldText(selectedItem?.contractType) }}</dd></div>
                <div><dt>갱신권</dt><dd>{{ fieldText(selectedItem?.useRRRight) }}</dd></div>
                <div><dt>종전금액</dt><dd>보증금 {{ displayManwon(selectedItem?.preDepositManwon) }} / 월세 {{ displayManwon(selectedItem?.preMonthlyRentManwon) }}</dd></div>
              </template>
            </dl>
          </div>
        </div>
      </aside>
    </main>

    <main v-else-if="activePage === 'account'" class="notice-page account-page" aria-label="회원 관리 화면">
      <section class="notice-page-inner account-page-inner">
        <div class="notice-page-heading">
          <div>
            <p class="section-kicker">Account</p>
            <h2>회원 관리</h2>
          </div>
          <button class="back-button compact-button" type="button" @click="closeAccountPanel">검색으로</button>
        </div>

        <div v-if="!member" class="account-tabs" role="tablist" aria-label="인증 선택">
          <button class="secondary-button compact-button" type="button" :class="{ 'is-active': accountMode === 'login' }" @click="switchAccountMode('login')">로그인</button>
          <button class="secondary-button compact-button" type="button" :class="{ 'is-active': accountMode === 'signup' }" @click="switchAccountMode('signup')">회원가입</button>
          <button class="secondary-button compact-button" type="button" :class="{ 'is-active': accountMode === 'password-reset' }" @click="switchAccountMode('password-reset')">비밀번호 찾기</button>
        </div>

        <p v-if="memberMessage" class="account-message">{{ memberMessage }}</p>
        <p v-if="memberError" class="account-message is-error">{{ memberError }}</p>

        <form v-if="accountMode === 'login' && !member" class="account-form" @submit.prevent="loginMember">
          <label><span>이메일</span><input v-model.trim="loginForm.email" type="email" autocomplete="email" required /></label>
          <label><span>비밀번호</span><input v-model="loginForm.password" type="password" autocomplete="current-password" required /></label>
          <div class="actions">
            <button class="primary-button" type="submit" :disabled="memberLoading">로그인</button>
            <button class="secondary-button" type="button" @click="switchAccountMode('signup')">회원가입으로</button>
          </div>
        </form>

        <form v-else-if="accountMode === 'password-reset' && !member" class="account-form" @submit.prevent="resetPassword">
          <label><span>이메일</span><input v-model.trim="passwordResetForm.email" type="email" autocomplete="email" required /></label>
          <label><span>이름</span><input v-model.trim="passwordResetForm.name" type="text" autocomplete="name" required /></label>
          <label><span>전화번호</span><input v-model.trim="passwordResetForm.phone" type="tel" autocomplete="tel" /></label>
          <label><span>새 비밀번호</span><input v-model="passwordResetForm.newPassword" type="password" autocomplete="new-password" required /></label>
          <div class="actions">
            <button class="primary-button" type="submit" :disabled="memberLoading">비밀번호 변경</button>
            <button class="secondary-button" type="button" @click="switchAccountMode('login')">로그인으로</button>
          </div>
        </form>

        <form v-else-if="accountMode === 'signup' && !member" class="account-form" @submit.prevent="signupMember">
          <label><span>이메일</span><input v-model.trim="signupForm.email" type="email" autocomplete="email" required /></label>
          <label><span>비밀번호</span><input v-model="signupForm.password" type="password" autocomplete="new-password" required /></label>
          <label><span>이름</span><input v-model.trim="signupForm.name" type="text" autocomplete="name" required /></label>
          <label><span>전화번호</span><input v-model.trim="signupForm.phone" type="tel" autocomplete="tel" /></label>
          <div class="actions">
            <button class="primary-button" type="submit" :disabled="memberLoading">회원가입</button>
            <button class="secondary-button" type="button" @click="switchAccountMode('login')">로그인으로</button>
          </div>
        </form>

        <div v-else-if="member" class="profile-layout">
          <dl class="detail-list">
            <div><dt>이메일</dt><dd>{{ member.email }}</dd></div>
            <div><dt>이름</dt><dd>{{ member.name }}</dd></div>
            <div><dt>전화번호</dt><dd>{{ member.phone || '-' }}</dd></div>
          </dl>
          <div v-if="!profileEditing" class="actions">
            <button class="primary-button" type="button" :disabled="memberLoading" @click="startProfileEdit">수정</button>
            <button class="secondary-button" type="button" :disabled="memberLoading" @click="logoutMember">로그아웃</button>
          </div>
          <form v-else class="account-form" @submit.prevent="updateMember">
            <label><span>이름</span><input v-model.trim="profileForm.name" type="text" required /></label>
            <label><span>전화번호</span><input v-model.trim="profileForm.phone" type="tel" /></label>
            <div class="actions">
              <button class="primary-button" type="submit" :disabled="memberLoading">저장</button>
              <button class="secondary-button" type="button" :disabled="memberLoading" @click="cancelProfileEdit">취소</button>
            </div>
          </form>
          <div class="danger-zone">
            <strong>회원 탈퇴</strong>
            <p>삭제하면 현재 계정이 완전히 삭제되고 세션이 종료됩니다.</p>
            <label><span>확인 문구</span><input v-model="deleteConfirm" type="text" placeholder="삭제" /></label>
            <button class="danger-button" type="button" :disabled="memberLoading" @click="deleteMember">회원 삭제</button>
          </div>
        </div>
      </section>
    </main>

    <main v-else-if="activePage === 'member-search' && isNoticeAdmin" class="notice-page account-page" aria-label="회원 검색 화면">
      <section class="notice-page-inner account-page-inner">
        <div class="notice-page-heading">
          <div>
            <p class="section-kicker">Admin</p>
            <h2>회원 검색</h2>
          </div>
          <button class="back-button compact-button" type="button" @click="openSearchPage">검색으로</button>
        </div>

        <p v-if="memberMessage" class="account-message">{{ memberMessage }}</p>
        <p v-if="memberError" class="account-message is-error">{{ memberError }}</p>

        <form class="account-form" @submit.prevent="searchMembers">
          <label><span>검색어</span><input v-model.trim="memberSearchKeyword" type="search" placeholder="이메일, 이름, 전화번호" required /></label>
          <div class="actions">
            <button class="primary-button" type="submit" :disabled="memberLoading">검색</button>
            <button class="secondary-button" type="button" :disabled="memberLoading" @click="clearMemberSearch">초기화</button>
          </div>
        </form>

        <dl v-if="memberSearchResults.length" class="detail-list member-search-results">
          <div v-for="searchedMember in memberSearchResults" :key="searchedMember.memberId">
            <dt>{{ searchedMember.name }}</dt>
            <dd>{{ searchedMember.email }} · {{ searchedMember.phone || '-' }}</dd>
          </div>
        </dl>
      </section>
    </main>

    <main v-else class="notice-page" aria-label="공지사항 화면">
      <section class="notice-page-inner">
        <div class="notice-page-heading">
          <div>
            <p class="section-kicker">Notice</p>
            <h2>공지사항</h2>
          </div>
          <span class="result-count">{{ notices.length.toLocaleString() }}건</span>
        </div>

        <p v-if="noticeMessage" class="account-message">{{ noticeMessage }}</p>
        <p v-if="noticeError" class="account-message is-error">{{ noticeError }}</p>

        <form v-if="isNoticeAdmin" class="notice-form" @submit.prevent="saveNotice">
          <label><span>제목</span><input v-model.trim="noticeForm.title" type="text" maxlength="200" required /></label>
          <label><span>내용</span><textarea v-model.trim="noticeForm.content" rows="5" required></textarea></label>
          <div class="actions">
            <button class="primary-button" type="submit" :disabled="noticeLoading">{{ noticeEditingId ? '수정' : '등록' }}</button>
            <button class="secondary-button" type="button" :disabled="noticeLoading" @click="resetNoticeForm">취소</button>
          </div>
        </form>

        <div v-if="noticeLoading && notices.length === 0" class="state-box loading-state">
          <strong>공지사항을 불러오는 중입니다.</strong>
        </div>
        <div v-else-if="notices.length === 0" class="state-box">
          <strong>등록된 공지사항이 없습니다.</strong>
        </div>
        <ul v-else class="notice-list">
          <li v-for="notice in notices" :key="notice.noticeId">
            <article class="notice-item">
              <div class="notice-item-header">
                <strong>{{ notice.title }}</strong>
                <span>{{ displayNoticeDate(notice) }}</span>
              </div>
              <p>{{ notice.content }}</p>
              <div v-if="isNoticeAdmin && notice.editable" class="notice-actions">
                <button class="secondary-button compact-button" type="button" :disabled="noticeLoading" @click="editNotice(notice)">수정</button>
                <button class="danger-button compact-button" type="button" :disabled="noticeLoading" @click="deleteNotice(notice)">삭제</button>
              </div>
            </article>
          </li>
        </ul>
      </section>
    </main>

    <ChatWidget
      v-if="activePage === 'search'"
      :logged-in="!!member"
      :current-filters="filters"
      :current-page="searchPage"
      :total-pages="totalPages"
      :agent-result="agentResult"
      @agent-command="handleAgentCommand"
    />
  </div>
</template>
