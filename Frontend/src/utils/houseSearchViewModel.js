import { DEAL_MODE_OPTIONS, SORT_OPTIONS } from '../config/appConfig.js'
import { sortOptionsForDealMode } from '../houseSearchParams.js'

/** 원시 검색 상태를 페이지 수, 버튼 활성화 여부, 필터 요약 등 표시 전용 값으로 변환한다. */
export function createHouseSearchViewModel({ filters, priceRange, resultDisplayMode, totalCount, items, searchPage, loading, hasSearched }) {
  const currentPageSize = Number.isFinite(Number(resultDisplayMode)) && Number(resultDisplayMode) > 0 ? Number(resultDisplayMode) : 10
  const totalPages = resultDisplayMode === 'all' ? 1 : Math.max(Math.ceil((totalCount || 0) / currentPageSize), 1)
  const activeFilterSummary = []
  const dealModeLabel = DEAL_MODE_OPTIONS.find((option) => option.value === filters.dealMode)?.label
  if (dealModeLabel) activeFilterSummary.push(dealModeLabel)
  activeFilterSummary.push([filters.sido, filters.sigungu, filters.umdNm].filter(Boolean).join(' ') || '지역 미선택')
  if (filters.aptName) activeFilterSummary.push(`아파트 ${filters.aptName}`)
  if (filters.startDealMonth && filters.endDealMonth) activeFilterSummary.push(filters.startDealMonth === filters.endDealMonth ? filters.startDealMonth : `${filters.startDealMonth}~${filters.endDealMonth}`)
  const sortLabel = SORT_OPTIONS.find((option) => option.value === filters.sort)?.label
  if (sortLabel) activeFilterSummary.push(sortLabel)
  return {
    currentPageSize, totalPages,
    activeMinPriceKey: filters.dealMode === 'sale' ? 'minPrice' : 'minDeposit',
    activeMaxPriceKey: filters.dealMode === 'sale' ? 'maxPrice' : 'maxDeposit',
    priceFilterVisible: ['sale', 'jeonse', 'monthly'].includes(filters.dealMode),
    priceRangeAvailable: Number.isFinite(priceRange.min) && Number.isFinite(priceRange.max) && priceRange.min <= priceRange.max,
    monthlyRentRangeAvailable: filters.dealMode === 'monthly' && Number.isFinite(priceRange.monthlyMin) && Number.isFinite(priceRange.monthlyMax) && priceRange.monthlyMin <= priceRange.monthlyMax,
    activeSortOptions: SORT_OPTIONS.filter((option) => sortOptionsForDealMode(filters.dealMode).includes(option.value)),
    canGoPreviousPage: resultDisplayMode !== 'all' && hasSearched && searchPage > 1 && !loading,
    canGoNextPage: resultDisplayMode !== 'all' && hasSearched && searchPage < totalPages && !loading,
    activeFilterSummary,
    visibleCountLabel: loading ? '조회 중' : !hasSearched ? '검색 전' : `${(totalCount ?? items.length).toLocaleString()}건`,
    pageSummary: !hasSearched ? '검색 전' : resultDisplayMode === 'all' ? '전체 보기' : `${searchPage.toLocaleString()} / ${totalPages.toLocaleString()} 페이지`,
    resultMetaLabel: loading
      ? resultDisplayMode === 'all' ? '조회 중 · 전체 수집' : `조회 중 · 페이지당 ${currentPageSize}개`
      : resultDisplayMode === 'all' ? `${items.length.toLocaleString()} / ${(totalCount ?? 0).toLocaleString()}건 표시` : `${(totalCount ?? 0).toLocaleString()}건 · 페이지당 ${currentPageSize}개`,
  }
}
