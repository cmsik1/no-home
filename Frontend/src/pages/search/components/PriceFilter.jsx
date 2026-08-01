import { displayManwon } from '../../../utils/houseDisplay'

export function PriceFilter({
  filters,
  updateFilter,
  priceRange,
  priceRangeAvailable,
  monthlyRentRangeAvailable,
  activeMinPriceKey,
  activeMaxPriceKey,
  priceRangeLoading,
  priceRangeError,
  loadPriceRange,
}) {
  return (
    <div className="price-filter">
      <div className="price-filter-header">
        <strong>{filters.dealMode === 'sale' ? '가격 구간' : filters.dealMode === 'monthly' ? '보증금 / 월세 구간' : '보증금 구간'}</strong>
        <button className="secondary-button compact-button" type="button" disabled={priceRangeLoading} onClick={loadPriceRange}>
          {priceRangeLoading ? '조회 중' : '범위 불러오기'}
        </button>
      </div>
      <div className="form-grid">
        <label><span>최소</span><input value={filters[activeMinPriceKey]} type="number" placeholder={priceRangeAvailable ? priceRange.min : ''} onChange={(event) => updateFilter(activeMinPriceKey, event.target.value)} /></label>
        <label><span>최대</span><input value={filters[activeMaxPriceKey]} type="number" placeholder={priceRangeAvailable ? priceRange.max : ''} onChange={(event) => updateFilter(activeMaxPriceKey, event.target.value)} /></label>
        {filters.dealMode === 'monthly' && (
          <>
            <label><span>최소 월세</span><input value={filters.minMonthlyRent} type="number" placeholder={monthlyRentRangeAvailable ? priceRange.monthlyMin : ''} onChange={(event) => updateFilter('minMonthlyRent', event.target.value)} /></label>
            <label><span>최대 월세</span><input value={filters.maxMonthlyRent} type="number" placeholder={monthlyRentRangeAvailable ? priceRange.monthlyMax : ''} onChange={(event) => updateFilter('maxMonthlyRent', event.target.value)} /></label>
          </>
        )}
      </div>
      <p className={`inline-help${priceRangeError ? ' is-error' : ''}`}>{priceRangeError || (priceRangeAvailable ? `${displayManwon(priceRange.min)} ~ ${displayManwon(priceRange.max)}` : '현재 조건 기준 가격 범위를 불러올 수 있습니다.')}</p>
    </div>
  )
}
