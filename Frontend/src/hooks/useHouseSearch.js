import { useCallback, useMemo, useRef, useState } from 'react'
import { currentDealMonth, emptyFilters, isSeoul, seoulLawdCodes, sortOptionsForDealMode } from '../houseSearchParams'
import { DEFAULT_DEAL_MONTH } from '../config/appConfig'
import { fetchAllHouseSearchResults, fetchPagedHouseSearchResults } from '../services/houseService'
import { keepSearchLoadingVisible, waitForPaint } from '../utils/asyncUi'
import { createHouseSearchViewModel } from '../utils/houseSearchViewModel'
import { useLegalDongs } from './useLegalDongs'
import { usePriceRange } from './usePriceRange'

const initialFilters = () => ({ ...emptyFilters(), startDealMonth: currentDealMonth(), endDealMonth: currentDealMonth() })

export function useHouseSearch({ clearMapMarkers, refreshMapMarkers }) {
  const [filters, setFilters] = useState(initialFilters)
  const [items, setItems] = useState([])
  const [totalCount, setTotalCount] = useState(null)
  const [searchPage, setSearchPage] = useState(1)
  const [resultDisplayMode, setResultDisplayMode] = useState('10')
  const [selectedItem, setSelectedItem] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [hasSearched, setHasSearched] = useState(false)
  const [searchPanelCollapsed, setSearchPanelCollapsed] = useState(false)
  const [regionError, setRegionError] = useState('')
  const [dealMonthError, setDealMonthError] = useState('')
  const searchRequestIdRef = useRef(0)

  const selectedLawdCd = isSeoul(filters.sido) && filters.sigungu ? seoulLawdCodes[filters.sigungu] || '' : ''
  const legalDongState = useLegalDongs(selectedLawdCd)
  const updateFilter = useCallback((key, value) => setFilters((prev) => ({ ...prev, [key]: value })), [])
  const updateFilters = useCallback((patch) => setFilters((prev) => ({ ...prev, ...patch })), [])
  const invalidRegionSelection = isSeoul(filters.sido) && !filters.sigungu
  const invalidDealMonthRange = Boolean(filters.startDealMonth && filters.endDealMonth && filters.startDealMonth > filters.endDealMonth)

  const validateRegionForSearch = useCallback(() => {
    if (!invalidRegionSelection) {
      setRegionError('')
      return true
    }
    setRegionError('서울을 선택한 경우 시군구를 먼저 선택해 주세요.')
    return false
  }, [invalidRegionSelection])

  const priceRangeState = usePriceRange({ filters, updateFilters, validateRegionForSearch })
  const { priceRange } = priceRangeState
  const viewModel = useMemo(() => createHouseSearchViewModel({
    filters, priceRange, resultDisplayMode, totalCount, items, searchPage, loading, hasSearched,
  }), [filters, priceRange, resultDisplayMode, totalCount, items, searchPage, loading, hasSearched])
  const { currentPageSize } = viewModel

  const cancelSearch = useCallback(() => {
    searchRequestIdRef.current += 1
    setLoading(false)
  }, [])

  const handleDealModeChange = (dealMode) => {
    const allowed = sortOptionsForDealMode(dealMode)
    setFilters((prev) => ({
      ...prev, dealMode, sort: allowed.includes(prev.sort) ? prev.sort : 'latest',
      minPrice: '', maxPrice: '', minDeposit: '', maxDeposit: '', minMonthlyRent: '', maxMonthlyRent: '',
    }))
    priceRangeState.resetPriceRange()
  }

  const normalizeDealMonthRangeForSearch = () => {
    if (!invalidDealMonthRange) {
      setDealMonthError('')
      return filters
    }
    const normalized = { ...filters, startDealMonth: DEFAULT_DEAL_MONTH, endDealMonth: DEFAULT_DEAL_MONTH }
    setFilters(normalized)
    setDealMonthError('유효하지 않은 거래월 범위를 기본값으로 다시 조회합니다.')
    return normalized
  }

  const searchHouses = async (page = 1, overrideFilters = null) => {
    if (!validateRegionForSearch()) return
    const searchFiltersBase = overrideFilters || normalizeDealMonthRangeForSearch()
    const startedAt = Date.now()
    const requestId = searchRequestIdRef.current + 1
    searchRequestIdRef.current = requestId
    setLoading(true)
    setError('')
    setHasSearched(true)
    setSelectedItem(null)
    setItems([])
    setTotalCount(null)
    setSearchPage(page)
    clearMapMarkers()
    await waitForPaint()

    try {
      const results = resultDisplayMode === 'all'
        ? await fetchAllHouseSearchResults(searchFiltersBase)
        : await fetchPagedHouseSearchResults(searchFiltersBase, { page, size: currentPageSize })
      if (requestId !== searchRequestIdRef.current) return
      await keepSearchLoadingVisible(startedAt)
      if (requestId !== searchRequestIdRef.current) return
      const nextItems = results.flatMap((payload) => Array.isArray(payload.items) ? payload.items : [])
      setItems(nextItems)
      setTotalCount(resultDisplayMode === 'all'
        ? results.reduce((max, payload) => Number.isFinite(payload.totalCount) ? Math.max(max, payload.totalCount) : max, nextItems.length)
        : results.reduce((sum, payload) => sum + (Number.isFinite(payload.totalCount) ? payload.totalCount : (payload.items?.length || 0)), 0))
      priceRangeState.setPriceRangeFromResults(results, { sourceFilters: searchFiltersBase })
      setSearchPanelCollapsed(nextItems.length > 0)
      setLoading(false)
      window.requestAnimationFrame(() => refreshMapMarkers(nextItems))
    } catch (exception) {
      if (requestId !== searchRequestIdRef.current) return
      await keepSearchLoadingVisible(startedAt)
      setItems([])
      setTotalCount(0)
      setError(exception instanceof Error ? exception.message : '검색 중 오류가 발생했습니다.')
      clearMapMarkers()
    } finally {
      if (requestId === searchRequestIdRef.current) setLoading(false)
    }
  }

  const resetSearch = () => {
    searchRequestIdRef.current += 1
    setFilters(initialFilters())
    legalDongState.resetLegalDongs()
    setItems([])
    setTotalCount(null)
    priceRangeState.resetPriceRange()
    setRegionError('')
    setDealMonthError('')
    setSearchPage(1)
    setSearchPanelCollapsed(false)
    setSelectedItem(null)
    setLoading(false)
    setError('')
    setHasSearched(false)
    clearMapMarkers()
  }

  return {
    filters, setFilters, updateFilter, updateFilters, items, totalCount, searchPage, resultDisplayMode,
    setResultDisplayMode, selectedItem, setSelectedItem, loading, error, hasSearched, searchPanelCollapsed,
    setSearchPanelCollapsed, priceRange, priceRangeLoading: priceRangeState.priceRangeLoading,
    priceRangeError: priceRangeState.priceRangeError, regionError, dealMonthError,
    legalDongs: legalDongState.legalDongs, legalDongLoading: legalDongState.legalDongLoading,
    legalDongError: legalDongState.legalDongError, selectedLawdCd, ...viewModel,
    loadLegalDongs: legalDongState.loadLegalDongs, handleDealModeChange, searchHouses,
    loadPriceRange: priceRangeState.loadPriceRange, resetSearch, cancelSearch,
  }
}
