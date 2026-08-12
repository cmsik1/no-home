import { InterestRegions } from './components/InterestRegions.jsx'
import { PriceFilter } from './components/PriceFilter.jsx'
import { RegionFields } from './components/RegionFields.jsx'

/** 분리된 검색 상태를 사용자 입력으로 갱신하고 제출 시 첫 페이지 검색을 시작하는 폼이다. */
export function SearchForm({
  filters,
  updateFilter,
  updateFilters,
  handleDealModeChange,
  onSubmit,
  resetSearch,
  seoulDistricts,
  dealModeOptions,
  sortOptions,
  resultDisplayMode,
  setResultDisplayMode,
  visibleCountLabel,
  activeFilterSummary,
  regionError,
  dealMonthError,
  legalDongs,
  legalDongLoading,
  legalDongError,
  selectedLawdCd,
  loadLegalDongs,
  priceFilterVisible,
  priceRange,
  priceRangeAvailable,
  monthlyRentRangeAvailable,
  activeMinPriceKey,
  activeMaxPriceKey,
  priceRangeLoading,
  priceRangeError,
  loadPriceRange,
  member,
  interestRegions,
  canSaveInterestRegion,
  interestRegionLoading,
  interestRegionMessage,
  interestRegionError,
  saveInterestRegion,
  applyInterestRegion,
  deleteInterestRegion,
  searchPanelCollapsed,
  loading,
}) {
  return (
    <form className={`search-panel${searchPanelCollapsed ? ' is-collapsed' : ''}`} noValidate onSubmit={onSubmit}>
      <div className="panel-heading">
        <div>
          <p className="section-kicker">Search</p>
          <h2>주택 검색</h2>
        </div>
        <div className="search-panel-controls">
          <span className="result-count">{visibleCountLabel}</span>
        </div>
      </div>

      <div id="search-panel-body" className="search-panel-body">
        <div className="filter-summary" aria-label="현재 검색 조건">
          {activeFilterSummary.map((part) => <span key={part}>{part}</span>)}
        </div>

        <div className="form-grid">
          <label>
            <span>거래 유형</span>
            <select value={filters.dealMode} onChange={(event) => handleDealModeChange(event.target.value)}>
              {dealModeOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
            </select>
          </label>
          <RegionFields
            filters={filters}
            updateFilter={updateFilter}
            updateFilters={updateFilters}
            seoulDistricts={seoulDistricts}
            legalDongs={legalDongs}
            legalDongLoading={legalDongLoading}
            selectedLawdCd={selectedLawdCd}
            loadLegalDongs={loadLegalDongs}
          />
          <label>
            <span>아파트명</span>
            <input value={filters.aptName} type="search" onChange={(event) => updateFilter('aptName', event.target.value)} />
          </label>
          <label>
            <span>정렬</span>
            <select value={filters.sort} onChange={(event) => updateFilter('sort', event.target.value)}>
              {sortOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
            </select>
          </label>
          <label>
            <span>시작 거래월</span>
            <input value={filters.startDealMonth} type="month" onChange={(event) => updateFilter('startDealMonth', event.target.value)} />
          </label>
          <label>
            <span>종료 거래월</span>
            <input value={filters.endDealMonth} type="month" onChange={(event) => updateFilter('endDealMonth', event.target.value)} />
          </label>
          <label>
            <span>표시 개수</span>
            <select value={resultDisplayMode} onChange={(event) => setResultDisplayMode(event.target.value)}>
              <option value="10">10개</option>
              <option value="20">20개</option>
              <option value="50">50개</option>
              <option value="all">전체</option>
            </select>
          </label>
        </div>

        {priceFilterVisible && (
          <PriceFilter
            filters={filters}
            updateFilter={updateFilter}
            priceRange={priceRange}
            priceRangeAvailable={priceRangeAvailable}
            monthlyRentRangeAvailable={monthlyRentRangeAvailable}
            activeMinPriceKey={activeMinPriceKey}
            activeMaxPriceKey={activeMaxPriceKey}
            priceRangeLoading={priceRangeLoading}
            priceRangeError={priceRangeError}
            loadPriceRange={loadPriceRange}
          />
        )}

        {(regionError || dealMonthError || legalDongError) && (
          <p className="account-message is-error">{regionError || dealMonthError || legalDongError}</p>
        )}

        {member && (
          <InterestRegions
            regions={interestRegions}
            loading={interestRegionLoading}
            message={interestRegionMessage}
            error={interestRegionError}
            canSave={canSaveInterestRegion}
            onSave={saveInterestRegion}
            onApply={applyInterestRegion}
            onDelete={deleteInterestRegion}
          />
        )}

        <div className="actions">
          <button className="primary-button" type="submit" disabled={loading}>
            {loading && <span className="button-spinner" aria-hidden="true"></span>}
            <span>{loading ? '조회 중' : '검색'}</span>
          </button>
          <button className="secondary-button" type="button" onClick={resetSearch}>초기화</button>
        </div>
      </div>
    </form>
  )
}
